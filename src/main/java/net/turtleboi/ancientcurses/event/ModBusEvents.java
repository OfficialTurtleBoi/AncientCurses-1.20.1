package net.turtleboi.ancientcurses.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.entities.AncientWraithEntity;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ANCIENT_WRAITH.get(),
                AncientWraithEntity.createAttributes().build());
    }
}
