package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.turtlecore.capabilities.party.PlayerPartyProvider;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class RuinationBrandItem extends ArtifactItem {
    public static final int MAX_STACKS = 5;
    public static final int BRAND_DURATION_TICKS = 160;
    public static final float BONUS_DAMAGE_PER_STACK = 0.75F;
    public static final float DETONATION_DAMAGE_PER_STACK = 1.5F;
    public static final float DETONATION_BASE_DAMAGE = 2.0F;
    public static final float DETONATION_HEAL_PER_STACK = 1.0F;
    public static final double DETONATION_RADIUS = 4.5D;
    public static final double HEAL_RADIUS = 8.0D;
    private static final String OWNER_TAG = "RuinationBrandOwner";

    public RuinationBrandItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ancientcurses.ruination_brand.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    public static void onOwnerDamagedTarget(ServerPlayer player, LivingEntity target) {
        if (!isEquipped(player) || !target.isAlive()) {
            return;
        }

        int stacks = getStacksForOwner(target, player);
        setBrand(target, player, Math.min(MAX_STACKS, stacks + 1));

        if (getStacksForOwner(target, player) >= MAX_STACKS) {
            detonateBrand(player, target, MAX_STACKS, false);
        }
    }

    public static float getBonusDamage(ServerPlayer player, LivingEntity target) {
        if (!isEquipped(player)) {
            return 0.0F;
        }
        return getStacksForOwner(target, player) * BONUS_DAMAGE_PER_STACK;
    }

    public static void onOwnerKilledTarget(ServerPlayer player, LivingEntity target) {
        int stacks = getStacksForOwner(target, player);
        if (stacks > 0) {
            detonateBrand(player, target, stacks, true);
        }
    }

    public static void tickBrand(LivingEntity entity) {
        if (entity.hasEffect(ModEffects.RUINATION_MARK.get())) {
            return;
        }
        entity.getPersistentData().remove(OWNER_TAG);
    }

    public static boolean isEquipped(Player player) {
        return !getEquippedStack(player).isEmpty();
    }

    private static ItemStack getEquippedStack(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.getItem() instanceof RuinationBrandItem) {
            return offhand;
        }

        if (!ModList.get().isLoaded("curios")) {
            return ItemStack.EMPTY;
        }

        AtomicReference<ItemStack> equipped = new AtomicReference<>(ItemStack.EMPTY);
        CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory ->
                curiosInventory.getStacksHandler("charm").ifPresent(handler -> {
                    for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                        ItemStack stack = handler.getStacks().getStackInSlot(i);
                        if (stack.getItem() instanceof RuinationBrandItem) {
                            equipped.set(stack);
                            return;
                        }
                    }
                }));
        return equipped.get();
    }

    private static int getStacksForOwner(LivingEntity target, ServerPlayer owner) {
        MobEffectInstance effect = target.getEffect(ModEffects.RUINATION_MARK.get());
        if (effect == null) {
            return 0;
        }

        CompoundTag data = target.getPersistentData();
        if (!data.hasUUID(OWNER_TAG) || !data.getUUID(OWNER_TAG).equals(owner.getUUID())) {
            return 0;
        }

        return effect.getAmplifier() + 1;
    }

    private static void setBrand(LivingEntity target, ServerPlayer owner, int stacks) {
        target.getPersistentData().putUUID(OWNER_TAG, owner.getUUID());
        target.addEffect(new MobEffectInstance(ModEffects.RUINATION_MARK.get(),
                BRAND_DURATION_TICKS, Math.max(0, stacks - 1), false, false, false));
    }

    private static void clearBrand(LivingEntity target) {
        target.removeEffect(ModEffects.RUINATION_MARK.get());
        target.getPersistentData().remove(OWNER_TAG);
    }

    private static void detonateBrand(ServerPlayer owner, LivingEntity target, int stacks, boolean fromDeath) {
        if (!(owner.level() instanceof ServerLevel level)) {
            return;
        }

        clearBrand(target);

        float detonationDamage = DETONATION_BASE_DAMAGE + (stacks * DETONATION_DAMAGE_PER_STACK);
        AABB area = target.getBoundingBox().inflate(DETONATION_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && entity != target && entity != owner)) {
            if (!isFriendly(owner, nearby)) {
                nearby.hurt(owner.damageSources().magic(), detonationDamage);
            }
        }

        healAllies(owner, stacks);
        level.playSound(null, target.getX(), target.getY(), target.getZ(),
                fromDeath ? SoundEvents.WARDEN_ATTACK_IMPACT : SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 0.9F, fromDeath ? 1.1F : 0.85F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(),
                12 + stacks * 4, 0.35D, 0.5D, 0.35D, 0.01D);
    }

    private static void healAllies(ServerPlayer owner, int stacks) {
        float healAmount = stacks * DETONATION_HEAL_PER_STACK;
        for (Player ally : getNearbyAllies(owner)) {
            ally.heal(healAmount);
            ally.level().playSound(null, ally.getX(), ally.getY(), ally.getZ(),
                    SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.45F, 1.5F);
        }
    }

    private static Set<Player> getNearbyAllies(ServerPlayer owner) {
        Set<Player> allies = new HashSet<>();
        allies.add(owner);
        AABB area = owner.getBoundingBox().inflate(HEAL_RADIUS);

        owner.getCapability(PlayerPartyProvider.PLAYER_PARTY).ifPresent(party -> {
            for (UUID memberUUID : party.getMemberUUIDs()) {
                ServerPlayer partyMember = owner.server.getPlayerList().getPlayer(memberUUID);
                if (partyMember != null && partyMember.level() == owner.level() && area.contains(partyMember.position())) {
                    allies.add(partyMember);
                }
            }
        });

        return allies;
    }

    private static boolean isFriendly(ServerPlayer owner, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.getUUID().equals(owner.getUUID())) {
                return true;
            }

            AtomicReference<Boolean> friendly = new AtomicReference<>(false);
            owner.getCapability(PlayerPartyProvider.PLAYER_PARTY).ifPresent(party -> {
                if (party.getMemberUUIDs().contains(player.getUUID())) {
                    friendly.set(true);
                }
            });
            return friendly.get();
        }

        return false;
    }
}
