package net.turtleboi.ancientcurses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CursedNodeEntity extends Entity{
    private static final EntityDataAccessor<Integer> TEXTURE_INDEX = SynchedEntityData.defineId(CursedNodeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PROGRESS = SynchedEntityData.defineId(CursedNodeEntity.class, EntityDataSerializers.INT);

    public CursedNodeEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.time = this.random.nextInt(100000);
        this.setGlowingTag(true);
    }

    private int nodeLifetime = 620;
    private BlockPos altarPos;
    private Object owner;
    private int textureTickCounter = 0;
    protected int age;
    public int time;



    @Override
    protected void defineSynchedData() {
        this.entityData.define(TEXTURE_INDEX, 0);
        this.entityData.define(PROGRESS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        age = this.tickCount;
        ++time;

        if (this.level().isClientSide) {
            if (age % 20 == 0) {
                //spawnNodeParticles();
            }
        }

        if (!this.level().isClientSide) {
            textureTickCounter++;
            int ticksPerFrame = 2;
            if (textureTickCounter >= ticksPerFrame) {
                int newIndex = (this.getTextureIndex() + 1) % getMaxTextureIndex();
                this.setTextureIndex(newIndex);
                textureTickCounter = 0;
            }

            if (age >= nodeLifetime){
                this.discard();
            }

            if (getOwner() == null){
                this.discard();
            }
        }
    }

    public void setOwner(Object pOwner) {
        if (pOwner instanceof Entity || pOwner instanceof BlockEntity) {
            this.owner = pOwner;
        }
    }

    public Object getOwner() {
        return this.owner;
    }

    public int getTextureIndex() {
        return this.entityData.get(TEXTURE_INDEX);
    }

    public void setTextureIndex(int index) {
        this.entityData.set(TEXTURE_INDEX, index);
    }

    public int getMaxTextureIndex() {
        return 63;
    }

    public void setAltarPos(BlockPos pos) {
        this.altarPos = pos;
    }

    public int getProgress() {
        return this.entityData.get(PROGRESS);
    }

    public void setProgress(int prog) {
        this.entityData.set(PROGRESS, prog);
    }

    public void setNodeLifetime(int nodeLifetime) {
        this.nodeLifetime = nodeLifetime;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }
}
