package net.turtleboi.ancientcurses.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AncientCurses.MOD_ID);

    public static final RegistryObject<EntityType<CursedPortalEntity>> CURSED_PORTAL =
            ENTITY_TYPES.register("cursed_portal" , () -> EntityType.Builder.of(CursedPortalEntity::new, MobCategory.MISC)
                    .sized(0.25F, 2.625F)
                    .build("cursed_portal"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
