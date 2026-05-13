package net.turtleboi.ancientcurses.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.entities.AncientWraithEntity;
import net.turtleboi.ancientcurses.entity.entities.CursedNodeEntity;
import net.turtleboi.ancientcurses.entity.entities.CursedPortalEntity;
import net.turtleboi.ancientcurses.entity.entities.PlagueIdolEntity;
import net.turtleboi.ancientcurses.entity.entities.VoodooSoulEntity;
import net.turtleboi.ancientcurses.entity.entities.items.LingeringCauldronCloud;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownCauldronPotion;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownCursedPearl;
import net.turtleboi.ancientcurses.entity.entities.items.ThrownIceSpark;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AncientCurses.MOD_ID);

    public static final RegistryObject<EntityType<AncientWraithEntity>> ANCIENT_WRAITH =
            ENTITY_TYPES.register("ancient_wraith" , () -> EntityType.Builder.of(AncientWraithEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 1.125F)
                    .build("ancient_wraith"));

    public static final RegistryObject<EntityType<CursedPortalEntity>> CURSED_PORTAL =
            ENTITY_TYPES.register("cursed_portal" , () -> EntityType.Builder.of(CursedPortalEntity::new, MobCategory.MISC)
                    .sized(0.25F, 2.625F)
                    .build("cursed_portal"));

    public static final RegistryObject<EntityType<ThrownCauldronPotion>> CAULDRON_POTION =
            ENTITY_TYPES.register("cauldron_potion", () -> EntityType.Builder.<ThrownCauldronPotion>of(ThrownCauldronPotion::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("cauldron_potion"));

    public static final RegistryObject<EntityType<ThrownCursedPearl>> CURSED_PEARL =
            ENTITY_TYPES.register("cursed_pearl" , () -> EntityType.Builder.<ThrownCursedPearl>of(ThrownCursedPearl::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(10)
                    .build("cursed_pearl"));

    public static final RegistryObject<EntityType<ThrownIceSpark>> ICE_SPARK =
            ENTITY_TYPES.register("ice_spark" , () -> EntityType.Builder.<ThrownIceSpark>of(ThrownIceSpark::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("ice_spark"));

    public static final RegistryObject<EntityType<PlagueIdolEntity>> PLAGUE_IDOL =
            ENTITY_TYPES.register("plague_idol" , () -> EntityType.Builder.<PlagueIdolEntity>of(PlagueIdolEntity::new, MobCategory.MISC)
                    .sized(0.625F, 0.375F)
                    .clientTrackingRange(10)
                    .build("plague_idol"));

    public static final RegistryObject<EntityType<VoodooSoulEntity>> VOODOO_SOUL =
            ENTITY_TYPES.register("voodoo_soul" , () -> EntityType.Builder.<VoodooSoulEntity>of(VoodooSoulEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 1.125F)
                    .clientTrackingRange(10)
                    .build("voodoo_soul"));

    public static final RegistryObject<EntityType<LingeringCauldronCloud>> LINGERING_CAULDRON_CLOUD =
            ENTITY_TYPES.register("lingering_cauldron_cloud", () -> EntityType.Builder.<LingeringCauldronCloud>of(LingeringCauldronCloud::new, MobCategory.MISC)
                    .sized(6.0F, 1.0F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .noSummon()
                    .fireImmune()
                    .build("lingering_cauldron_cloud"));

    public static final RegistryObject<EntityType<CursedNodeEntity>> CURSED_NODE =
            ENTITY_TYPES.register("cursed_node" , () -> EntityType.Builder.of(CursedNodeEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .build("cursed_node"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
