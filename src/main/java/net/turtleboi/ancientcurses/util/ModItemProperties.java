package net.turtleboi.ancientcurses.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.FathomlessCauldronItem;
import net.turtleboi.ancientcurses.item.items.GoldenFeatherItem;
import net.turtleboi.ancientcurses.item.items.SoulCompassItem;

import java.util.Comparator;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ModItemProperties {
    public static void addCustomItemProperties() {
            ItemProperties.register(ModItems.FATHOMLESS_CAULDRON.get(), new ResourceLocation(AncientCurses.MOD_ID, "has_contents"),
                    (itemStack, clientLevel, livingEntity, i) ->
                            FathomlessCauldronItem.hasContentsProperty(itemStack));

            ItemProperties.register(ModItems.GOLDEN_FEATHER.get(), new  ResourceLocation(AncientCurses.MOD_ID, "broken"),
                    (itemStack, clientLevel, livingEntity, i) ->
                            GoldenFeatherItem.canDash(itemStack) ? 0.0F : 1.0F);

            ItemProperties.register(ModItems.SOUL_COMPASS.get(), new ResourceLocation("angle"),
                    ModItemProperties::getSoulCompassAngle);
    }

    private static float getSoulCompassAngle(ItemStack stack, ClientLevel level, LivingEntity holder, int seed) {
        if (level == null || holder == null) {
            return 0.0F;
        }

        Optional<EntityType<?>> trackedType = SoulCompassItem.getTrackedEntityType(stack);
        if (trackedType.isEmpty()) {
            return getErraticAngle(level, seed);
        }

        AABB searchArea = holder.getBoundingBox().inflate(128.0D);
        Optional<LivingEntity> nearestTarget = level.getEntitiesOfClass(LivingEntity.class, searchArea,
                        entity -> entity != holder
                                && entity.isAlive()
                                && entity.getType() == trackedType.get())
                .stream()
                .min(Comparator.comparingDouble(holder::distanceToSqr));

        if (nearestTarget.isEmpty()) {
            return getErraticAngle(level, seed);
        }

        double xOffset = nearestTarget.get().getX() - holder.getX();
        double zOffset = nearestTarget.get().getZ() - holder.getZ();
        double targetAngle = Math.atan2(zOffset, xOffset) / (Math.PI * 2.0D);
        double holderYaw = Mth.positiveModulo(holder.getVisualRotationYInDegrees() / 360.0D, 1.0D);
        double compassAngle = 0.5D - (holderYaw - 0.25D - targetAngle);
        return (float) Mth.positiveModulo(compassAngle + 0.5D, 1.0D);
    }

    private static float getErraticAngle(ClientLevel level, int seed) {
        return (float) Mth.positiveModulo(((level.getGameTime() + seed) % 20) / 20.0D, 1.0D);
    }
}
