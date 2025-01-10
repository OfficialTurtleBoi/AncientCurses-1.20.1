package net.turtleboi.ancientcurses.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, AncientCurses.MOD_ID);

    public static final RegistryObject<SimpleParticleType> HEAL_PARTICLE =
            PARTICLES_TYPES.register("heal_particles", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType>  CURSED_PARTICLE =
            PARTICLES_TYPES.register("cursed_particles", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SLEEP_PARTICLE =
            PARTICLES_TYPES.register("sleep_particles", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> CURSED_FLAME_PARTICLE =
            PARTICLES_TYPES.register("cursed_flame_particles", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GOLDEN_FEATHER_PARTICLE =
            PARTICLES_TYPES.register("golden_feather_particle", () -> new SimpleParticleType(true));
    public static void register(IEventBus eventBus) {
        PARTICLES_TYPES.register(eventBus);
    }
}

