package net.turtleboi.ancientcurses.trials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialDataCapability;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.trials.SyncTrialDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FetchTrial implements Trial {
    private UUID playerUUID;
    private Item requiredItem;
    private String itemName;
    private int requiredCount;
    private int collectedCount;
    private CursedAltarBlockEntity altar;
    private MobEffect effect;
    public static final String fetchCount = "FetchCount";
    public static final String fetchRequirement = "FetchRequirement";
    public static final String fetchItem = "FetchItem";

    private boolean completed;

    private int currentDegree = 0;
    public boolean completedFirstDegree;
    public boolean completedSecondDegree;
    public boolean completedThirdDegree;

    private final List<Item> degreeItems = new ArrayList<>();
    private final List<Integer> degreeCounts = new ArrayList<>();
    private final List<Integer> degreeCollected = new ArrayList<>();
    private final List<String> degreeItemNames = new ArrayList<>();

    public FetchTrial(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.completed = false;
        this.collectedCount = 0;

        for (int i = 0; i < 3; i++) {
            Item item = selectRandomItem();
            degreeItems.add(item);
            degreeCounts.add(calculateRequiredCount(amplifier + 1));
            degreeCollected.add(0);
            ItemStack stack = new ItemStack(item);
            Component itemNameComponent = stack.getDisplayName();
            degreeItemNames.add(itemNameComponent.getString());
        }

        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            trialData.setCurseEffect(effect);
            trialData.setActiveTrial(this);
            this.collectedCount = trialData.getFetchItems();
        });
    }

    public FetchTrial(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
    }

    private Item selectRandomItem() {
        Item[] possibleItems = new Item[]{
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "iron_ingot")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "gold_ingot")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "copper_ingot")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "raw_iron")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "raw_gold")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "raw_copper")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "lapis_lazuli")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "redstone")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "rotten_flesh")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "bone")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "ender_pearl")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "gunpowder")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "spider_eye")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "glowstone_dust")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "sugar")),
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", "string"))
        };

        Item[] filteredItems = Arrays.stream(possibleItems)
                .filter(Objects::nonNull)
                .toArray(Item[]::new);

        if (filteredItems.length == 0) {
            System.err.println("Fetch Trial: No valid items found in possibleItems list.");
            return Items.AIR;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(filteredItems.length);
        return filteredItems[randomIndex];
    }

    private int calculateRequiredCount(int amplifier) {
        Random random = new Random();
        int base = random.nextInt(9) + 8;
        return base * ((amplifier * amplifier) * 2);
    }

    public boolean isTrialActive() {
        return altar.getPlayerTrial(playerUUID) != null && !completed;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putString(fetchItem, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(requiredItem)).toString());
        tag.putInt(fetchCount, collectedCount);
        tag.putInt(fetchRequirement, requiredCount);
        tag.putBoolean("Completed", completed);
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        String itemName = tag.getString(fetchItem);
        this.requiredItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        this.collectedCount = tag.getInt(fetchCount);
        this.requiredCount = tag.getInt(fetchRequirement);
        this.completed = tag.getBoolean("Completed");
    }

    @Override
    public String getType() {
        return Trial.fetchTrial;
    }

    @Override
    public void setAltar(CursedAltarBlockEntity altar) {
        this.altar = altar;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public MobEffect getEffect() {
        return this.effect;
    }

    public Player getPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }

    @Override
    public boolean isTrialCompleted(Player player) {
        return completed || completedThirdDegree;
    }

    @Override
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public void onEntityKilled(Player player, Entity entity) {

    }

    @Override
    public void onPlayerTick(Player player) {

    }

    @Override
    public void trackProgress(Player player) {
        if (player != null) {
            String currentItemName = getCurrentItemName();
            int collected = getCollectedCount();
            int required = getRequiredCount();

            float progressPercentage = Math.min((float) collectedCount / requiredCount, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            Trial.fetchTrial,
                            isTrialCompleted(player),
                            "",
                            0,
                            0,
                            0,
                            0,
                            0,
                            currentItemName,
                            collected,
                            required),
                    (ServerPlayer) player
            );
            // player.displayClientMessage(
            //         Component.literal("Fetch Trial Progress: " + collectedCount + "/" + requiredCount)
            //                 .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @Override
    public void concludeTrial(Player player) {
        // player.displayClientMessage(Component.literal("You have completed the Fetch Trial! Collect your reward.").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncTrialDataS2C(
                        Trial.fetchTrial,
                        isTrialCompleted(player),
                        "",
                        0,
                        0,
                        0,
                        0,
                        0,
                        "",
                        0,
                        0),
                (ServerPlayer) player
        );
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(PlayerTrialDataCapability::clearCurseEffect);

        List<MobEffect> cursesToRemove = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            if (CurseRegistry.getCurses().contains(effect)) {
                cursesToRemove.add(effect);
            }
        }

        for (MobEffect effect : cursesToRemove) {
            player.removeEffect(effect);
        }

        CoreNetworking.sendToNear((new CameraShakeS2C(0.125F, 1000)), player);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD.get(),
                    SoundSource.AMBIENT,
                    1.00f,
                    0.25f
            );
        }

        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData ->
                trialData.setTrialCompleted(altar.getBlockPos()));
        this.completed = true;
        altar.setPlayerTrialCompleted(player);
    }

    public void advanceDegree(Player player) {
        if (currentDegree < 3) {
            currentDegree++;
        }

        if (currentDegree == 1) {
            completedFirstDegree = true;
        } else if (currentDegree == 2) {
            completedSecondDegree = true;
        } else if (currentDegree == 3) {
            completedThirdDegree = true;
        }

        if (completedThirdDegree) {
            concludeTrial(player);
        } else {
            trackProgress(player);
        }
    }

    public void incrementFetchCount(Player player, int itemCount) {
        int collected = degreeCollected.get(currentDegree);
        collected += itemCount;
        degreeCollected.set(currentDegree, collected);

        int finalCollected = collected;
        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(trialData -> {
            trialData.setFetchItems(finalCollected);
        });
    }

    public Item getRequiredItem() {
        return degreeItems.get(currentDegree);
    }

    public int getRequiredCount() {
        return degreeCounts.get(currentDegree);
    }

    public int getCollectedCount() {
        return degreeCollected.get(currentDegree);
    }

    public String getCurrentItemName() {
        return degreeItemNames.get(currentDegree);
    }
}
