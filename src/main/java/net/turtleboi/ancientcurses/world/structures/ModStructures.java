package net.turtleboi.ancientcurses.world.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, AncientCurses.MOD_ID);

    public static final RegistryObject<StructureType<CurseAltarStructure>> CURSED_ALTAR =
            STRUCTURE_TYPES.register("cursedaltar", () -> typeCodec(CurseAltarStructure.CODEC));

    private static <S extends net.minecraft.world.level.levelgen.structure.Structure> StructureType<S> typeCodec(Codec<S> codec) {
        return () -> codec;
    }

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
    }
}

