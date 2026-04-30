package net.turtleboi.ancientcurses.item.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.turtleboi.turtlecore.spell.SpellType;
import net.turtleboi.turtlecore.spell.util.SpellManager;
import net.turtleboi.turtlecore.spell.util.ToggleSpellManager;
import net.turtleboi.turtlecore.spell.util.spec.SpellSpec;
import net.turtleboi.turtlecore.spell.util.spec.action.ActionGroupSpec;
import net.turtleboi.turtlecore.spell.util.spec.action.ActionSpec;
import net.turtleboi.turtlecore.spell.util.spec.action.EffectsActionSpec;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GoldenFeatherItem extends Item {
    private static final String AIRBORNE_TAG = "GoldenFeatherAirborne";
    private static final ResourceLocation LAUNCH_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_launch");
    private static final ResourceLocation SEISMIC_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_seismic");
    private static final ResourceLocation ZEPHYR_RUSH_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_zephyr_rush");
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
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    castLaunchSpell(serverPlayer, pStack);
                }

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

                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    armLandingSpells(serverPlayer, pStack);
                }
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
                    if (pPlayer instanceof ServerPlayer serverPlayer) {
                        disarmLandingSpells(serverPlayer);
                    }

                    pStack.getOrCreateTag().putBoolean(AIRBORNE_TAG, false);
                }
            } else if (pPlayer instanceof ServerPlayer serverPlayer) {
                disarmLandingSpells(serverPlayer);
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    private static void armLandingSpells(ServerPlayer serverPlayer, ItemStack itemStack) {
        int seismicLevel = itemStack.getEnchantmentLevel(ModEnchantments.SEISMIC.get());
        int zephyrRushLevel = itemStack.getEnchantmentLevel(ModEnchantments.ZEPHYR_RUSH.get());

        disarmLandingSpells(serverPlayer);

        if (seismicLevel > 0) {
            SpellManager.SpellDefinition seismicDefinition = createSeismicLandingSpell(seismicLevel);
            if (seismicDefinition != null) {
                ToggleSpellManager.arm(serverPlayer, seismicDefinition);
            }
        }

        if (zephyrRushLevel > 0) {
            SpellManager.SpellDefinition zephyrRushDefinition = createZephyrRushLandingSpell(zephyrRushLevel);
            if (zephyrRushDefinition != null) {
                ToggleSpellManager.arm(serverPlayer, zephyrRushDefinition);
            }
        }
    }

    private static void disarmLandingSpells(ServerPlayer serverPlayer) {
        ToggleSpellManager.disarm(serverPlayer.getUUID(), SEISMIC_SPELL_ID);
        ToggleSpellManager.disarm(serverPlayer.getUUID(), ZEPHYR_RUSH_SPELL_ID);
    }

    private static void castLaunchSpell(ServerPlayer serverPlayer, ItemStack itemStack) {
        int soaringLevel = itemStack.getEnchantmentLevel(ModEnchantments.SOARING.get());
        Vec3 lookDirection = serverPlayer.getViewVector(1);
        double dashModifier = 1 + (soaringLevel * 0.42);
        Vec3 dashVelocity = new Vec3(
                (lookDirection.x() * 2) * dashModifier,
                (lookDirection.y() * 0.5 * dashModifier) + 0.4,
                (lookDirection.z() * 1.8) * dashModifier
        );
        Map<String, Float> spellOverrides = new HashMap<>();
        spellOverrides.put("launch.motion_x", (float) dashVelocity.x());
        spellOverrides.put("launch.motion_y", (float) dashVelocity.y());
        spellOverrides.put("launch.motion_z", (float) dashVelocity.z());
        SpellDispatcher.castChained(serverPlayer, LAUNCH_SPELL_ID, spellOverrides, null);
    }

    private static SpellManager.SpellDefinition createSeismicLandingSpell(int seismicLevel) {
        SpellManager.SpellDefinition baseDefinition = SpellManager.getById(SEISMIC_SPELL_ID);
        if (baseDefinition == null || baseDefinition.spell() == null) {
            return null;
        }

        SpellSpec baseSpell = baseDefinition.spell();
        float shockwaveRadius = BASE_SHOCKWAVE_RADIUS + ((seismicLevel - 1) * SHOCKWAVE_RADIUS_PER_LEVEL);
        float shockwavePower = BASE_SHOCKWAVE_POWER + ((seismicLevel - 1) * SHOCKWAVE_POWER_PER_LEVEL);
        List<ActionGroupSpec> actionGroups = replaceFirstGroupRadius(baseSpell.actionGroups(), shockwaveRadius);
        SpellSpec modifiedSpell = new SpellSpec(
                baseSpell.cooldownTicks(),
                baseSpell.spellClasses(),
                baseSpell.iconTexture(),
                baseSpell.description(),
                SpellType.TOGGLE,
                shockwavePower,
                shockwaveRadius,
                baseSpell.angle(),
                baseSpell.speed(),
                baseSpell.durationTicks(),
                baseSpell.pulses(),
                actionGroups,
                baseSpell.particle(),
                baseSpell.colorRgb(),
                baseSpell.origin(),
                baseSpell.target(),
                baseSpell.auraTexture(),
                baseSpell.tintAura(),
                baseSpell.useFloorParticles(),
                baseSpell.maxChannelTicks(),
                baseSpell.toggleCondition(),
                baseSpell.resourceCost(),
                baseSpell.onCastGroups(),
                baseSpell.onEndGroups()
        );
        return new SpellManager.SpellDefinition(baseDefinition.id(), baseDefinition.mask(), modifiedSpell);
    }

    private static SpellManager.SpellDefinition createZephyrRushLandingSpell(int zephyrRushLevel) {
        SpellManager.SpellDefinition baseDefinition = SpellManager.getById(ZEPHYR_RUSH_SPELL_ID);
        if (baseDefinition == null || baseDefinition.spell() == null) {
            return null;
        }

        SpellSpec baseSpell = baseDefinition.spell();
        List<ActionGroupSpec> actionGroups = replaceEffectsDefaults(baseSpell.actionGroups(), 50, zephyrRushLevel - 1);
        SpellSpec modifiedSpell = new SpellSpec(
                baseSpell.cooldownTicks(),
                baseSpell.spellClasses(),
                baseSpell.iconTexture(),
                baseSpell.description(),
                SpellType.TOGGLE,
                baseSpell.power(),
                baseSpell.range(),
                baseSpell.angle(),
                baseSpell.speed(),
                baseSpell.durationTicks(),
                baseSpell.pulses(),
                actionGroups,
                baseSpell.particle(),
                baseSpell.colorRgb(),
                baseSpell.origin(),
                baseSpell.target(),
                baseSpell.auraTexture(),
                baseSpell.tintAura(),
                baseSpell.useFloorParticles(),
                baseSpell.maxChannelTicks(),
                baseSpell.toggleCondition(),
                baseSpell.resourceCost(),
                baseSpell.onCastGroups(),
                baseSpell.onEndGroups()
        );
        return new SpellManager.SpellDefinition(baseDefinition.id(), baseDefinition.mask(), modifiedSpell);
    }

    private static List<ActionGroupSpec> replaceFirstGroupRadius(List<ActionGroupSpec> actionGroups, float radius) {
        if (actionGroups.isEmpty()) {
            return actionGroups;
        }

        List<ActionGroupSpec> updatedGroups = new ArrayList<>(actionGroups);
        ActionGroupSpec firstGroup = actionGroups.get(0);
        updatedGroups.set(
                0,
                new ActionGroupSpec(
                        firstGroup.target(),
                        firstGroup.filter(),
                        firstGroup.includeSelf(),
                        radius,
                        firstGroup.maxTargets(),
                        firstGroup.perTargetCooldownTicks(),
                        firstGroup.actions()
                )
        );
        return List.copyOf(updatedGroups);
    }

    private static List<ActionGroupSpec> replaceEffectsDefaults(
            List<ActionGroupSpec> actionGroups,
            int durationTicks,
            int amplifier
    ) {
        List<ActionGroupSpec> updatedGroups = new ArrayList<>(actionGroups.size());

        for (ActionGroupSpec actionGroup : actionGroups) {
            List<ActionSpec> updatedActions = new ArrayList<>(actionGroup.actions().size());

            for (ActionSpec action : actionGroup.actions()) {
                if (action instanceof EffectsActionSpec effectsAction) {
                    updatedActions.add(
                            new EffectsActionSpec(
                                    effectsAction.chance(),
                                    new EffectsActionSpec.EffectDefaults(
                                            durationTicks,
                                            amplifier,
                                            effectsAction.defaults().ambient(),
                                            effectsAction.defaults().showParticles(),
                                            effectsAction.defaults().showIcon(),
                                            effectsAction.defaults().chance()
                                    ),
                                    effectsAction.effects()
                            )
                    );
                } else {
                    updatedActions.add(action);
                }
            }

            updatedGroups.add(
                    new ActionGroupSpec(
                            actionGroup.target(),
                            actionGroup.filter(),
                            actionGroup.includeSelf(),
                            actionGroup.radius(),
                            actionGroup.maxTargets(),
                            actionGroup.perTargetCooldownTicks(),
                            List.copyOf(updatedActions)
                    )
            );
        }

        return List.copyOf(updatedGroups);
    }
}
