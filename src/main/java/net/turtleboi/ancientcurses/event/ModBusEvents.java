package net.turtleboi.ancientcurses.event;

import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.particle.custom.CursedFlameParticle;
import net.turtleboi.ancientcurses.particle.custom.CursedParticle;
import net.turtleboi.ancientcurses.particle.custom.HealParticle;
import net.turtleboi.ancientcurses.particle.custom.SleepParticle;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ModParticleTypes.HEAL_PARTICLE.get(),
                HealParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CURSED_PARTICLE.get(),
                CursedParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SLEEP_PARTICLE.get(),
                SleepParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                CursedFlameParticle.Provider::new);
    }
}
