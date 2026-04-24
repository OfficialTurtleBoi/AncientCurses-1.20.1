package net.turtleboi.ancientcurses.item.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.turtlecore.spell.SpellDispatcher;
import net.turtleboi.turtlecore.spell.util.SpellManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GoldenFeatherItem extends Item {
    private static final String AIRBORNE_TAG = "GoldenFeatherAirborne";
    private static final ResourceLocation SHOCKWAVE_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_shockwave");
    private static final float BASE_SHOCKWAVE_RADIUS = 5.0F;
    private static final float SHOCKWAVE_RADIUS_PER_LEVEL = 0.75F;
    private static final float BASE_SHOCKWAVE_POWER = 2.0F;
    private static final float SHOCKWAVE_POWER_PER_LEVEL = 1.0F;

    public GoldenFeatherItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack pItemStack = pPlayer.getItemInHand(pUsedHand);

        pPlayer.startUsingItem(pUsedHand);
        return InteractionResultHolder.success(pItemStack);
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        super.onUseTick(pLevel, pLivingEntity, pStack, pRemainingUseDuration);
        if (pLivingEntity instanceof Player pPlayer ) {
            if (pRemainingUseDuration > 1) {
                int soaringLevel = pStack.getEnchantmentLevel(ModEnchantments.SOARING.get());

                Vec3 playerLook = pPlayer.getViewVector(1);
                double dashmodifier = 1 + soaringLevel * 0.42;
                Vec3 dashVec = new Vec3((playerLook.x() * 2) * dashmodifier, playerLook.y() * 0.5 * dashmodifier + 0.4, (playerLook.z() * 1.8) * dashmodifier);
                pPlayer.setDeltaMovement(dashVec);

                for (int j = 0; j < 5; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }
            } else {
                releaseUsing(pStack, pLevel, pLivingEntity, getUseDuration(pStack));
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        int tailwindLevel = pStack.getEnchantmentLevel(ModEnchantments.TAILWIND.get());
        if (pLivingEntity instanceof Player pPlayer) {

            pStack.hurtAndBreak(1, pPlayer, (p_41300_) -> {
                p_41300_.broadcastBreakEvent(Objects.requireNonNull(pStack.getEquipmentSlot()));
            });

            int cooldownReduction = 15 * tailwindLevel;
            pPlayer.getCooldowns().addCooldown(this, 75 - cooldownReduction);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));

            if (!pPlayer.onGround()){
                pStack.getOrCreateTag().putBoolean(AIRBORNE_TAG, true);
            }
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return 10;
    }

    public static boolean canDash(ItemStack pGoldenFeatherStack) {
        return pGoldenFeatherStack.getDamageValue() < pGoldenFeatherStack.getMaxDamage() - 1;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if(pEntity instanceof Player pPlayer) {
            int seismicLevel = pStack.getEnchantmentLevel(ModEnchantments.SEISMIC.get());
            int zephyrLevel = pStack.getEnchantmentLevel(ModEnchantments.ZEPHYR_RUSH.get());
            boolean airborne = pStack.getOrCreateTag().getBoolean(AIRBORNE_TAG);

            if (airborne) {
                for (int j = 0; j < 3; j++) {
                    pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                            pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                            pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                            0.0, 0.1, 0.0);
                }

                if (pPlayer.onGround() || pPlayer.isSwimming()) {
                    //AncientCurses.LOGGER.info("Golden Feather landing detected for {} with Seismic level {}", pPlayer.getScoreboardName(), seismicLevel);
                    if(seismicLevel > 0) {
                        if (pPlayer instanceof ServerPlayer serverPlayer) {
                            SpellManager.SpellDefinition shockwave = SpellManager.getById(SHOCKWAVE_SPELL_ID);
                            if (shockwave != null) {
                                Map<String, Object> overrides = new HashMap<>();
                                overrides.put("range_override", BASE_SHOCKWAVE_RADIUS + ((seismicLevel - 1) * SHOCKWAVE_RADIUS_PER_LEVEL));
                                overrides.put("power_override", BASE_SHOCKWAVE_POWER + ((seismicLevel - 1) * SHOCKWAVE_POWER_PER_LEVEL));
                                overrides.put("origin", "caster");
                                //AncientCurses.LOGGER.info("Casting Golden Feather shockwave spell {} for {}", SHOCKWAVE_SPELL_ID, pPlayer.getScoreboardName());
                                SpellDispatcher.castPassive(serverPlayer, shockwave, overrides, serverPlayer);
                            } else {
                                //AncientCurses.LOGGER.warn("Golden Feather shockwave spell {} was not found in SpellManager for {}", SHOCKWAVE_SPELL_ID, pPlayer.getScoreboardName());
                            }
                        }
                    }

                    if (zephyrLevel > 0) {
                        pPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50, zephyrLevel - 1));
                    }
                    pStack.getOrCreateTag().putBoolean(AIRBORNE_TAG, false);
                }
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }
}
