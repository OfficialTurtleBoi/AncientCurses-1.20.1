package net.turtleboi.ancientcurses.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.entities.AncientWraithEntity;
import net.turtleboi.ancientcurses.entity.entities.PlagueIdolEntity;
import net.turtleboi.ancientcurses.entity.entities.VoodooSoulEntity;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ANCIENT_WRAITH.get(),
                AncientWraithEntity.createAttributes().build());
        event.put(ModEntities.PLAGUE_IDOL.get(),
                PlagueIdolEntity.createAttributes().build());
        event.put(ModEntities.VOODOO_SOUL.get(),
                VoodooSoulEntity.createAttributes().build());
    }
}
