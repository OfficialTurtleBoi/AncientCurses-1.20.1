package net.turtleboi.ancientcurses.item.items;

import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.AABB;
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
    private static final String AIRBORNE_TAG     = "GoldenFeatherAirborne";
    private static final String HEAT_TAG         = "GoldenFeatherHeat";
    private static final String OVERHEAT_TAG     = "GoldenFeatherOverheated";
    private static final String LANDING_HEAT_TAG = "GoldenFeatherLandingHeat";

    private static final ResourceLocation LAUNCH_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_launch");
    private static final ResourceLocation SEISMIC_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_seismic");
    private static final ResourceLocation ZEPHYR_RUSH_SPELL_ID =
            new ResourceLocation(AncientCurses.MOD_ID, "golden_feather_zephyr_rush");

    private static final float BASE_SHOCKWAVE_RADIUS      = 5.0F;
    private static final float SHOCKWAVE_RADIUS_PER_LEVEL = 0.75F;
    private static final float BASE_SHOCKWAVE_POWER       = 2.0F;
    private static final float SHOCKWAVE_POWER_PER_LEVEL  = 1.0F;

    // Heat mechanics
    // Base rate fills heat in ~45 ticks (≈2.25 s); each Soaring level slows buildup
    private static final float HEAT_RATE_BASE                        = 0.022f;
    private static final float HEAT_RATE_REDUCTION_PER_SOARING       = 0.003f;
    // Passive decay while idle — full cool from max in ~27 s
    private static final float HEAT_DECAY_RATE                       = 0.006f;
    // Boost: Soaring and heat both scale the launch velocity multiplicatively
    private static final float HEAT_BOOST_MULTIPLIER                 = 0.6f;
    // Overheat cooldown base 6 s; each Soaring level trims 1 s
    private static final int   OVERHEAT_COOLDOWN_BASE                = 120;
    private static final int   OVERHEAT_COOLDOWN_REDUCTION_PER_SOARING = 20;
    // Seismic shockwave ignites targets when boost heat was above this threshold
    private static final float SEISMIC_IGNITE_HEAT_THRESHOLD         = 0.6f;
    private static final int   SEISMIC_IGNITE_SECONDS                = 4;

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
        if (!(pLivingEntity instanceof Player pPlayer)) return;

        CompoundTag tag = pStack.getOrCreateTag();
        int soaringLevel = pStack.getEnchantmentLevel(ModEnchantments.SOARING.get());

        // Build heat — Soaring slows the rate, making overheat harder to trigger
        float heat = tag.getFloat(HEAT_TAG);
        float heatRate = Math.max(0.01f, HEAT_RATE_BASE - soaringLevel * HEAT_RATE_REDUCTION_PER_SOARING);
        heat = Math.min(1.0f, heat + heatRate);
        tag.putFloat(HEAT_TAG, heat);

        if (heat >= 1.0f) {
            tag.putBoolean(OVERHEAT_TAG, true);
            pPlayer.stopUsingItem(); // triggers releaseUsing with overheat flag set
            return;
        }

        // Particle count scales with heat (hotter = more particles)
        int particleCount = 3 + (int) (heat * 6);
        for (int j = 0; j < particleCount; j++) {
            pLevel.addParticle(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                    pPlayer.getX() + (pPlayer.getRandom().nextDouble() - 0.5),
                    pPlayer.getY() + (pPlayer.getRandom().nextDouble() - 0.5),
                    pPlayer.getZ() + (pPlayer.getRandom().nextDouble() - 0.5),
                    0.0, 0.1, 0.0);
        }

        if (pPlayer instanceof ServerPlayer serverPlayer) {
            castLaunchSpell(serverPlayer, pStack, heat);
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        super.releaseUsing(pStack, pLevel, pLivingEntity, pTimeCharged);
        if (!(pLivingEntity instanceof Player pPlayer)) return;

        CompoundTag tag = pStack.getOrCreateTag();
        boolean overheated = tag.getBoolean(OVERHEAT_TAG);
        tag.remove(OVERHEAT_TAG);
        float heat = tag.getFloat(HEAT_TAG);

        int soaringLevel = pStack.getEnchantmentLevel(ModEnchantments.SOARING.get());
        int tailwindLevel = pStack.getEnchantmentLevel(ModEnchantments.TAILWIND.get());

        pStack.hurtAndBreak(1, pPlayer, (p) -> p.broadcastBreakEvent(Objects.requireNonNull(pStack.getEquipmentSlot())));
        pPlayer.awardStat(Stats.ITEM_USED.get(this));

        if (overheated) {
            // Soaring reduces the overheat penalty — reward skilled heat management
            int cooldown = Math.max(40, OVERHEAT_COOLDOWN_BASE - soaringLevel * OVERHEAT_COOLDOWN_REDUCTION_PER_SOARING);
            pPlayer.getCooldowns().addCooldown(this, cooldown);
        } else {
            pPlayer.getCooldowns().addCooldown(this, 75 - 15 * tailwindLevel);
        }

        if (!pPlayer.onGround()) {
            tag.putBoolean(AIRBORNE_TAG, true);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                armLandingSpells(serverPlayer, pStack, heat);
            }
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return 72000; // Heat overheat or player release are the only stops
    }

    public static boolean canDash(ItemStack pGoldenFeatherStack) {
        return pGoldenFeatherStack.getDamageValue() < pGoldenFeatherStack.getMaxDamage() - 1;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity instanceof Player pPlayer) {
            CompoundTag tag = pStack.getOrCreateTag();

            // Decay heat while not actively using this feather
            boolean isBeingUsed = pPlayer.isUsingItem() && pPlayer.getUseItem().getItem() == this;
            if (!isBeingUsed) {
                float heat = tag.getFloat(HEAT_TAG);
                if (heat > 0f) {
                    tag.putFloat(HEAT_TAG, Math.max(0f, heat - HEAT_DECAY_RATE));
                }
            }

            boolean airborne = tag.getBoolean(AIRBORNE_TAG);
            if (airborne) {
                int particleCount = 2 + (int) (tag.getFloat(HEAT_TAG) * 3);
                for (int j = 0; j < particleCount; j++) {
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

                    // Seismic ignite — if boost heat was above threshold, set nearby targets on fire
                    int seismicLevel = pStack.getEnchantmentLevel(ModEnchantments.SEISMIC.get());
                    float landingHeat = tag.getFloat(LANDING_HEAT_TAG);
                    if (!pLevel.isClientSide() && seismicLevel > 0 && landingHeat >= SEISMIC_IGNITE_HEAT_THRESHOLD) {
                        float radius = BASE_SHOCKWAVE_RADIUS + (seismicLevel - 1) * SHOCKWAVE_RADIUS_PER_LEVEL;
                        AABB box = pPlayer.getBoundingBox().inflate(radius);
                        List<LivingEntity> targets = pLevel.getEntitiesOfClass(LivingEntity.class, box,
                                e -> e != pPlayer && !e.fireImmune());
                        for (LivingEntity target : targets) {
                            target.setSecondsOnFire(SEISMIC_IGNITE_SECONDS);
                        }
                    }

                    tag.remove(LANDING_HEAT_TAG);
                    tag.putBoolean(AIRBORNE_TAG, false);
                }
            } else if (pPlayer instanceof ServerPlayer serverPlayer) {
                disarmLandingSpells(serverPlayer);
            }
        }
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    private static void armLandingSpells(ServerPlayer serverPlayer, ItemStack itemStack, float heat) {
        int seismicLevel    = itemStack.getEnchantmentLevel(ModEnchantments.SEISMIC.get());
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

        // Store heat at boost time so inventoryTick can check ignite threshold on landing
        itemStack.getOrCreateTag().putFloat(LANDING_HEAT_TAG, heat);
    }

    private static void disarmLandingSpells(ServerPlayer serverPlayer) {
        ToggleSpellManager.disarm(serverPlayer.getUUID(), SEISMIC_SPELL_ID);
        ToggleSpellManager.disarm(serverPlayer.getUUID(), ZEPHYR_RUSH_SPELL_ID);
    }

    private static void castLaunchSpell(ServerPlayer serverPlayer, ItemStack itemStack, float heat) {
        int soaringLevel = itemStack.getEnchantmentLevel(ModEnchantments.SOARING.get());
        Vec3 lookDirection = serverPlayer.getViewVector(1);
        double soaringModifier = 1 + (soaringLevel * 0.42);
        double heatBoost = 1.0 + (heat * HEAT_BOOST_MULTIPLIER);
        double totalModifier = soaringModifier * heatBoost;
        Vec3 dashVelocity = new Vec3(
                (lookDirection.x() * 2) * totalModifier,
                (lookDirection.y() * 0.5 * totalModifier) + 0.4,
                (lookDirection.z() * 1.8) * totalModifier
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
