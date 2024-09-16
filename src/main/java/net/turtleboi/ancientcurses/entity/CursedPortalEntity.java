package net.turtleboi.ancientcurses.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class CursedPortalEntity extends Entity {
    private static final EntityDataAccessor<Integer> TEXTURE_INDEX = SynchedEntityData.defineId(CursedPortalEntity.class, EntityDataSerializers.INT);
    private int textureTickCounter = 0;
    protected int age;
    public CursedPortalEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TEXTURE_INDEX, 0);
    }

    @Override
    public void tick() {
        super.tick();
        age = this.tickCount;
        if (!this.level().isClientSide) {
            textureTickCounter++;
            int ticksPerFrame = 2;
            if (textureTickCounter >= ticksPerFrame) {
                int newIndex = (this.getTextureIndex() + 1) % getMaxTextureIndex();
                this.setTextureIndex(newIndex);
                textureTickCounter = 0;
            }
        }
    }

    public int getTextureIndex() {
        return this.entityData.get(TEXTURE_INDEX);
    }

    public void setTextureIndex(int index) {
        this.entityData.set(TEXTURE_INDEX, index);
    }

    public int getMaxTextureIndex() {
        return 13;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
