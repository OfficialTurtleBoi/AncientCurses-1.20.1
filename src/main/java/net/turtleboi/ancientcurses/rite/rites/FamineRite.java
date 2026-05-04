package net.turtleboi.ancientcurses.rite.rites;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.turtleboi.ancientcurses.block.entity.CursedAltarBlockEntity;
import net.turtleboi.ancientcurses.client.rites.FamineClientRiteState;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.rite.AbstractRite;
import net.turtleboi.ancientcurses.rite.ModRites;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FamineRite extends AbstractRite {
    private static final String CURRENT_DEGREE_KEY = "CurrentDegree";
    private static final String AMPLIFIER_KEY = "Amplifier";

    private int amplifier;
    private int currentDegree = 0;

    private final List<Item> degreeItems = new ArrayList<>();
    private final List<Integer> degreeCounts = new ArrayList<>();
    private final List<Integer> degreeCollected = new ArrayList<>();
    private final List<String> degreeItemNames = new ArrayList<>();

    public FamineRite(Player player, MobEffect effect, int amplifier, CursedAltarBlockEntity altar) {
        super(altar);
        this.playerUUID = player.getUUID();
        this.effect = effect;
        this.amplifier = amplifier + 1;
        ensureDegreeDataSize();
    }

    public FamineRite(CursedAltarBlockEntity altar) {
        super(altar);
    }

    private Item selectRandomItem() {
        List<? extends String> configuredItems = AncientCursesConfig.FAMINE_RITE_ITEMS.get();

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
        int base = Math.max(1, random.nextInt(5) + random.nextInt(5));
        return base * (2 << amplifier);
    }

    public boolean isRiteActive() {
        return altar.getPlayerRite(playerUUID) != null && !completed;
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        saveBaseData(tag);
        tag.putInt(CURRENT_DEGREE_KEY, currentDegree);
        tag.putInt(AMPLIFIER_KEY, amplifier);

        for (int i = 0; i < degreeItems.size(); i++) {
            tag.putString("DegreeItem_" + i, ForgeRegistries.ITEMS.getKey(degreeItems.get(i)).toString());
            tag.putInt("DegreeCount_" + i, degreeCounts.get(i));
            tag.putInt("DegreeCollected_" + i, degreeCollected.get(i));
            tag.putString("DegreeItemName_" + i, degreeItemNames.get(i));
        }
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        loadBaseData(tag);
        this.currentDegree = tag.getInt(CURRENT_DEGREE_KEY);
        this.amplifier = tag.getInt(AMPLIFIER_KEY);

        degreeItems.clear();
        degreeCounts.clear();
        degreeCollected.clear();
        degreeItemNames.clear();

        for (int i = 0; i < getMaxDegrees(); i++) {
            ResourceLocation itemId = new ResourceLocation(tag.getString("DegreeItem_" + i));
            degreeItems.add(ForgeRegistries.ITEMS.getValue(itemId));
            degreeCounts.add(tag.getInt("DegreeCount_" + i));
            degreeCollected.add(tag.getInt("DegreeCollected_" + i));
            degreeItemNames.add(tag.getString("DegreeItemName_" + i));
        }
    }

    @Override
    public ResourceLocation getId() {
        return ModRites.FAMINE;
    }

    public Player getPlayer() {
        if (altar.getLevel() instanceof ServerLevel serverLevel) {
            return serverLevel.getPlayerByUUID(playerUUID);
        }
        return null;
    }

    @Override
    public boolean isRiteCompleted(Player player) {
        return completed || getCompletionDegree() >= getMaxDegrees();
    }

    @Override
    public int getCompletionDegree() {
        return Math.min(currentDegree, getMaxDegrees());
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
            syncToClient(player);
        }
    }

    @Override
    public void concludeRite(Player player) {
        finishRite(player, false, 0.125F);
    }

    public void advanceDegree(Player player) {
        if (currentDegree < getMaxDegrees()) {
            currentDegree++;
        }

        if (getCompletionDegree() >= getMaxDegrees()) {
            concludeRite(player);
        } else {
            trackProgress(player);
        }
    }

    public void incrementFetchCount(int itemCount) {
        int collected = degreeCollected.get(getActiveDegreeIndexForProgress());
        collected += itemCount;
        degreeCollected.set(getActiveDegreeIndexForProgress(), collected);
    }

    public Item getRequiredItem() {
        return degreeItems.get(getActiveDegreeIndexForProgress());
    }

    public int getRequiredCount() {
        return degreeCounts.get(getActiveDegreeIndexForProgress());
    }

    public int getCollectedCount() {
        return degreeCollected.get(getActiveDegreeIndexForProgress());
    }

    public String getCurrentItemName() {
        return degreeItemNames.get(getActiveDegreeIndexForProgress());
    }

    @Override
    public boolean onItemToss(Player player, ItemEntity itemEntity) {
        Item tossedItem = itemEntity.getItem().getItem();
        int itemCount = itemEntity.getItem().getCount();
        BlockPos altarPos = altar.getBlockPos();
        BlockPos itemPos = itemEntity.blockPosition();
        BlockPos lowerBound = altarPos.above(1);
        BlockPos upperBound = altarPos.above(3);
        boolean isWithinHeight = itemPos.getY() >= lowerBound.getY() && itemPos.getY() <= upperBound.getY();
        boolean isWithinRadius = altarPos.getCenter().closerThan(itemEntity.position(), 3.0);
        if (!isWithinHeight || !isWithinRadius) {
            return false;
        }

        Item requiredItem = getRequiredItem();
        if (!tossedItem.equals(requiredItem)) {
            return false;
        }

        incrementFetchCount(itemCount);
        itemEntity.discard();
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                    altarPos.getX() + 0.5,
                    altarPos.getY() + 1.0,
                    altarPos.getZ() + 0.5,
                    100,
                    0.2,
                    2.0,
                    0.2,
                    0.01
            );
            serverLevel.playSound(
                    null,
                    altarPos.getX() + 0.5,
                    altarPos.getY() + 1.0,
                    altarPos.getZ() + 0.5,
                    net.minecraft.sounds.SoundEvents.GHAST_SHOOT,
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    1.0f,
                    0.5f
            );
        }
        trackProgress(player);

        if (getCollectedCount() >= getRequiredCount()) {
            advanceDegree(player);
        }
        return true;
    }

    @Override
    protected SyncRiteDataS2C buildSyncPacket(Player player) {
        int completedDegrees = getCompletionDegree();
        int totalDegrees = getDisplayDegreeCount();
        int activeDegreeIndex = isRiteCompleted(player) ? -1 : Math.min(currentDegree, totalDegrees - 1);
        return SyncRiteDataS2C.fromState(new FamineClientRiteState(
                isRiteCompleted(player),
                getCurrentItemName(),
                getCollectedCount(),
                getRequiredCount(),
                totalDegrees,
                completedDegrees,
                activeDegreeIndex
        ));
    }

    @Override
    public void setMaxDegrees(int maxDegrees) {
        super.setMaxDegrees(maxDegrees);
        ensureDegreeDataSize();
    }

    private void ensureDegreeDataSize() {
        while (degreeItems.size() < getMaxDegrees()) {
            Item item = selectRandomItem();
            degreeItems.add(item);
            degreeCounts.add(calculateRequiredCount(this.amplifier));
            degreeCollected.add(0);
            ItemStack stack = new ItemStack(item);
            Component itemNameComponent = stack.getDisplayName();
            degreeItemNames.add(itemNameComponent.getString());
        }

        trimToMaxDegrees(degreeItems);
        trimToMaxDegrees(degreeCounts);
        trimToMaxDegrees(degreeCollected);
        trimToMaxDegrees(degreeItemNames);
    }

    private int getActiveDegreeIndexForProgress() {
        return Math.min(currentDegree, Math.max(0, getMaxDegrees() - 1));
    }

    private <T> void trimToMaxDegrees(List<T> values) {
        while (values.size() > getMaxDegrees()) {
            values.remove(values.size() - 1);
        }
    }
}
