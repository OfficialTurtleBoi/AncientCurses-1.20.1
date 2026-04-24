package net.turtleboi.ancientcurses.entity.entities;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AncientWraithEntity extends Monster {
    public AncientWraithEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        //this.goalSelector.addGoal(1, new EnderMan.EndermanFreezeWhenLookedAt(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        //this.targetSelector.addGoal(1, new EnderMan.EndermanLookForPlayerGoal(this, this::isAngryAt));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Endermite.class, true, false));
        //this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 18.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ATTACK_SPEED, 1.6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D);
    }

    public AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    private float dragPitch;
    public float getDragPitch() {
        return dragPitch;
    }

    public float getPartsPitch() {
        float maxPitchRadians = 30.0f * Mth.DEG_TO_RAD;
        return Mth.clamp(Math.abs(dragPitch) / maxPitchRadians, 0.0f, 1.0f);
    }

    public void updateDragPitch() {
        Vec3 deltaMovement = getDeltaMovement();

        double horizontalSpeed = Math.sqrt(deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z);
        double verticalSpeed = Math.abs(deltaMovement.y);

        if (horizontalSpeed < 1.0E-4 && verticalSpeed < 1.0E-4) {
            dragPitch = Mth.lerp(0.20f, dragPitch, 0.0f);
            return;
        }

        float horizontalDegrees = getHorizontalDegrees(horizontalSpeed, deltaMovement);
        float verticalDegrees = (float) Mth.clamp(-deltaMovement.y * 6.0, -6.0, 6.0);

        float targetDegrees = Mth.clamp(horizontalDegrees + verticalDegrees, -30.0f, 30.0f);
        float targetRadians = targetDegrees * Mth.DEG_TO_RAD;

        dragPitch = Mth.lerp(0.25f, dragPitch, targetRadians);
    }

    private float getHorizontalDegrees(double horizontalSpeed, Vec3 deltaMovement) {
        float yawRadians = getYRot() * Mth.DEG_TO_RAD;
        float forwardX = -Mth.sin(yawRadians);
        float forwardZ = Mth.cos(yawRadians);

        float forwardSpeed = 0.0f;
        if (horizontalSpeed >= 1.0E-6) {
            float moveXNorm = (float)(deltaMovement.x / horizontalSpeed);
            float moveZNorm = (float)(deltaMovement.z / horizontalSpeed);
            float alignment = forwardX * moveXNorm + forwardZ * moveZNorm;
            forwardSpeed = Math.max(0.0f, alignment);
        }

        float speedWeight = (float) Mth.clamp(horizontalSpeed / 0.1, 0.0, 1.0);
        return -(forwardSpeed * speedWeight) * 30.0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            updateDragPitch();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            Vec3 velocity = this.getDeltaMovement();

            if (velocity.y < 0.0) {
                double fallDampen = 0.60;
                double maxDownwardSpeed = -0.08;
                double newY = Math.max(velocity.y * fallDampen, maxDownwardSpeed);
                velocity = new Vec3(velocity.x, newY, velocity.z);
            }

            double horizontalDampen = 0.91;
            velocity = new Vec3(velocity.x * horizontalDampen, velocity.y, velocity.z * horizontalDampen);

            this.setDeltaMovement(velocity);
            this.hasImpulse = true;
            this.hurtMarked = true;
        } else {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates(){
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 120;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }
}
