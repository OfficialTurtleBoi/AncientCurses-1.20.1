package net.turtleboi.ancientcurses.item.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SoulCompassItem extends Item {
    private static final String TRACKED_MOB_TAG = "TrackedMob";

    public SoulCompassItem(Properties properties) {
        super(properties);
    }

    public static void attuneTo(ItemStack stack, LivingEntity killedEntity) {
        ResourceLocation entityTypeId = ForgeRegistries.ENTITY_TYPES.getKey(killedEntity.getType());
        if (entityTypeId == null) {
            return;
        }

        stack.getOrCreateTag().putString(TRACKED_MOB_TAG, entityTypeId.toString());
    }

    public static Optional<EntityType<?>> getTrackedEntityType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TRACKED_MOB_TAG)) {
            return Optional.empty();
        }

        ResourceLocation entityTypeId = ResourceLocation.tryParse(tag.getString(TRACKED_MOB_TAG));
        if (entityTypeId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(ForgeRegistries.ENTITY_TYPES.getValue(entityTypeId));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Optional<EntityType<?>> trackedType = getTrackedEntityType(stack);
        if (trackedType.isPresent()) {
            tooltip.add(Component.translatable("item.ancientcurses.soul_compass.tracking",
                    trackedType.get().getDescription()).withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("item.ancientcurses.soul_compass.unattuned")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
