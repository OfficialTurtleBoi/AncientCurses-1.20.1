package net.turtleboi.ancientcurses.item.items;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.event.ModClientEvents;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.items.BeaconInfoPacketS2C;
import net.turtleboi.turtlecore.util.TargetingUtils;

import java.util.List;

public class FirstBeaconItem extends ArtifactItem {
    public static double range = 64.0F;
    public static int chargeRate = 80;

    public FirstBeaconItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pItemStack = pPlayer.getItemInHand(pUsedHand);

        pPlayer.startUsingItem(pUsedHand);
        if (pLevel.isClientSide()) {
            PlayerClientData.startFirstBeaconUse(getUseDuration(pItemStack));
        }

        return InteractionResultHolder.consume(pItemStack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pItemStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pItemStack, pRemainingUseDuration);
        if (!(pLivingEntity instanceof Player pPlayer)) return;

        Vec3 lookVec;
        if (pLevel.isClientSide()) {
            lookVec = getClientLookVector(pPlayer);
        } else {
            lookVec = pPlayer.getLookAngle();
        }
        Vec3 startVec = pPlayer.getEyePosition(1.0f);
        int ticksElapsed = getUseDuration(pItemStack) - pRemainingUseDuration;
        float chargeProgress = Math.min(1.0f, (float) ticksElapsed / chargeRate);
        Vec3 endVec = startVec.add(lookVec.scale(range));

        HitResult result = TargetingUtils.rayTraceClosest(pPlayer, startVec, endVec);
        double hitDistance = startVec.distanceTo(result.getLocation());

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(new BeaconInfoPacketS2C(
                    getUseDuration(pItemStack), pRemainingUseDuration, hitDistance, true
            ), serverPlayer);
        }

        if (chargeProgress > 0.35f) {
            if (pLevel.isClientSide()) {
                pLevel.addParticle(ParticleTypes.CLOUD,
                        result.getLocation().x, result.getLocation().y, result.getLocation().z,
                        0, 0.1, 0);
                return;
            }

            int slownessAmp = (int) ((chargeProgress - 0.35f) / 0.325f);
            pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, slownessAmp, false, false, false));

            if (ticksElapsed % 5 == 0) {
                List<EntityHitResult> hitResults = TargetingUtils.rayTraceEntities(pPlayer, startVec, endVec);
                if (!hitResults.isEmpty()) {
                    float baseDamage = 2.5f * chargeProgress;
                    for (int i = 0; i < hitResults.size(); i++) {
                        EntityHitResult hit = hitResults.get(i);
                        if (hit.getEntity() instanceof LivingEntity targetEntity) {
                            float damageMultiplier = Math.max(0f, 1.0f - (i * 0.05f));
                            targetEntity.hurt(pPlayer.level().damageSources().magic(), baseDamage * damageMultiplier);
                        }
                    }
                }
                pItemStack.hurtAndBreak(1, pPlayer, (p_41300_) -> {
                    if (pPlayer instanceof ServerPlayer serverPlayer) {
                        ModNetworking.sendToPlayer(new BeaconInfoPacketS2C(
                                getUseDuration(pItemStack), 40, 0, false), serverPlayer);
                    }
                    p_41300_.broadcastBreakEvent(pLivingEntity.getUsedItemHand());
                });
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pItemStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pItemStack, pLevel, pLivingEntity, pTimeCharged);
        if (pLivingEntity instanceof Player pPlayer) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                ModNetworking.sendToPlayer(new BeaconInfoPacketS2C(
                        getUseDuration(pItemStack),
                        40,
                        0,
                        false
                ), serverPlayer);
            }

            pPlayer.getCooldowns().addCooldown(this, 41);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public int getUseDuration(ItemStack pItemStack) {
        return pItemStack.getMaxDamage();
    }

    private static Vec3 getClientLookVector(Player player) {
        float limitedYaw = ModClientEvents.getBeaconLimitedYaw();
        float limitedPitch = ModClientEvents.getBeaconLimitedPitch();
        if (Float.isNaN(limitedYaw)) {
            return player.getLookAngle();
        }

        float pitchRadians = limitedPitch * (float)(Math.PI / 180.0);
        float yawRadians = -limitedYaw * (float)(Math.PI / 180.0);
        float cosPitch = Mth.cos(pitchRadians);
        return new Vec3(
                Mth.sin(yawRadians) * cosPitch,
                -Mth.sin(pitchRadians),
                Mth.cos(yawRadians) * cosPitch
        );
    }
}
