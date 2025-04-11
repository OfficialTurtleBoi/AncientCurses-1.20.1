package net.turtleboi.ancientcurses.rites;

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
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.turtlecore.network.CoreNetworking;
import net.turtleboi.turtlecore.network.packet.util.CameraShakeS2C;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FamineRite implements Rite {
    private UUID playerUUID;
    private int collectedCount;
    private CursedAltarBlockEntity altar;
    private MobEffect effect;

    private boolean completed;

    private int currentDegree = 0;
    public boolean completedFirstDegree;
    public boolean completedSecondDegree;
    public boolean completedThirdDegree;

    private final List<Item> degreeItems = new ArrayList<>();
    private final List<Integer> degreeCounts = new ArrayList<>();
    private final List<Integer> degreeCollected = new ArrayList<>();
    private final List<String> degreeItemNames = new ArrayList<>();

    public FamineRite(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
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

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setCurseEffect(effect);
            riteData.setActiveRite(this);
            this.collectedCount = riteData.getFetchItems();
        });
    }

    public FamineRite(CursedAltarBlockEntity altar) {
        this.altar = altar;
        this.completed = false;
    }

    private Item selectRandomItem() {
        List<? extends String> configuredItems = AncientCursesConfig.FETCH_TRIAL_ITEMS.get();

        List<Item> validItems = configuredItems.stream()
                .map(ResourceLocation::new)
                .map(ForgeRegistries.ITEMS::getValue)
                .filter(Objects::nonNull)
                .toList();

        if (validItems.isEmpty()) {
            System.err.println("Fetch Trial: No valid items found in config.");
            return Items.AIR;
        }

        return validItems.get(ThreadLocalRandom.current().nextInt(validItems.size()));
    }

    private int calculateRequiredCount(int amplifier) {
        Random random = new Random();
        int base = random.nextInt(5) + random.nextInt(5);
        return base * ((amplifier * amplifier) * 2);
    }

    public boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null && !completed;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        tag.putUUID("PlayerUUID", playerUUID);
        tag.putInt("CurrentDegree", currentDegree);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("CompletedFirst", completedFirstDegree);
        tag.putBoolean("CompletedSecond", completedSecondDegree);
        tag.putBoolean("CompletedThird", completedThirdDegree);

        for (int i = 0; i < degreeItems.size(); i++) {
            tag.putString("DegreeItem_" + i, ForgeRegistries.ITEMS.getKey(degreeItems.get(i)).toString());
            tag.putInt("DegreeCount_" + i, degreeCounts.get(i));
            tag.putInt("DegreeCollected_" + i, degreeCollected.get(i));
            tag.putString("DegreeItemName_" + i, degreeItemNames.get(i));
        }

        if (effect != null) {
            tag.putString("CurseEffect", ForgeRegistries.MOB_EFFECTS.getKey(effect).toString());
        }
    }


    @Override
    public void loadFromNBT(CompoundTag tag) {
        this.playerUUID = tag.getUUID("PlayerUUID");
        this.currentDegree = tag.getInt("CurrentDegree");
        this.completed = tag.getBoolean("Completed");
        this.completedFirstDegree = tag.getBoolean("CompletedFirst");
        this.completedSecondDegree = tag.getBoolean("CompletedSecond");
        this.completedThirdDegree = tag.getBoolean("CompletedThird");

        degreeItems.clear();
        degreeCounts.clear();
        degreeCollected.clear();
        degreeItemNames.clear();

        for (int i = 0; i < 3; i++) {
            ResourceLocation itemId = new ResourceLocation(tag.getString("DegreeItem_" + i));
            degreeItems.add(ForgeRegistries.ITEMS.getValue(itemId));
            degreeCounts.add(tag.getInt("DegreeCount_" + i));
            degreeCollected.add(tag.getInt("DegreeCollected_" + i));
            degreeItemNames.add(tag.getString("DegreeItemName_" + i));
        }

        if (tag.contains("CurseEffect")) {
            ResourceLocation effectId = new ResourceLocation(tag.getString("CurseEffect"));
            effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
        }
    }


    @Override
    public String getType() {
        return Rite.famineRite;
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
    public boolean isRiteCompleted(Player player) {
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

            float progressPercentage = Math.min((float) collectedCount / required, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncRiteDataS2C(
                            Rite.famineRite,
                            isRiteCompleted(player),
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
            //         Component.literal("Fetch Rite Progress: " + collectedCount + "/" + requiredCount)
            //                 .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    @Override
    public void concludeRite(Player player) {
        // player.displayClientMessage(Component.literal("You have completed the Fetch Rite! Collect your reward.").withStyle(ChatFormatting.GREEN), true);
        ModNetworking.sendToPlayer(
                new SyncRiteDataS2C(
                        Rite.famineRite,
                        isRiteCompleted(player),
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
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearCurseEffect);

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

        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData ->
                riteData.setRiteCompleted(altar.getBlockPos()));
        this.completed = true;
        altar.setPlayerRiteCompleted(player);
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
            concludeRite(player);
        } else {
            trackProgress(player);
        }
    }

    public void incrementFetchCount(Player player, int itemCount) {
        int collected = degreeCollected.get(currentDegree);
        collected += itemCount;
        degreeCollected.set(currentDegree, collected);

        int finalCollected = collected;
        player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(riteData -> {
            riteData.setFetchItems(finalCollected);
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
