package net.turtleboi.ancientcurses.trials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.CameraShakeS2C;
import net.turtleboi.ancientcurses.network.packets.SyncTrialDataS2C;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
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

    public FetchTrial(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        this.playerUUID = player.getUUID();
        this.altar = altar;
        this.effect = effect;
        this.completed = false;
        this.requiredItem = selectRandomItem();
        ItemStack stack = new ItemStack(requiredItem);
        Component itemNameComponent = stack.getDisplayName();
        this.itemName = itemNameComponent.getString();
        this.collectedCount = 0;
        this.requiredCount = calculateRequiredCount(amplifier);

        PlayerTrialData.setCurseEffect(player, effect);
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
        int effectiveAmplifier = Math.max(amplifier, 1);
        Random random = new Random();
        int base = random.nextInt(9) + 8;
        return base * (effectiveAmplifier * 2);
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
        return PlayerTrialData.fetchTrial;
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
        return completed || collectedCount >= requiredCount;
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
            float progressPercentage = Math.min((float) collectedCount / requiredCount, 1.0f);
            ModNetworking.sendToPlayer(
                    new SyncTrialDataS2C(
                            PlayerTrialData.fetchTrial,
                            "",
                            0,
                            0,
                            0,
                            0,
                            itemName,
                            collectedCount,
                            requiredCount),
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
                        PlayerTrialData.fetchTrial,
                        "",
                        0,
                        0,
                        0,
                        0,
                        itemName,
                        requiredCount,
                        requiredCount),
                (ServerPlayer) player
        );
        player.removeEffect(this.effect);

        PlayerTrialData.clearCurseEffect(player);

        ModNetworking.sendToPlayer(new CameraShakeS2C(0.125F, 1000), (ServerPlayer) player);
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

        PlayerTrialData.setTrialCompleted(player, altar.getBlockPos());
        this.completed = true;
        altar.setPlayerTrialCompleted(player);
    }

    public void incrementFetchCount() {
        collectedCount++;
    }

    public Item getRequiredItem() {
        return requiredItem;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public int getCollectedCount() {
        return collectedCount;
    }
}
