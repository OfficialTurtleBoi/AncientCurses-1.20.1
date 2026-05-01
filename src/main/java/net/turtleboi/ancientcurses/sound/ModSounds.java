package net.turtleboi.ancientcurses.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AncientCurses.MOD_ID);

    public static final RegistryObject<SoundEvent> DEPRECOPHOBIA = registerSoundEvents("deprecophobia");
    public static final RegistryObject<SoundEvent> CURSE_YOU_INTRO = registerSoundEvents("curseyou_intro");
    public static final RegistryObject<SoundEvent> CURSE_YOU_VERSE_1 = registerSoundEvents("curseyou_verse1");
    public static final RegistryObject<SoundEvent> CURSE_YOU_VERSE_2 = registerSoundEvents("curseyou_verse2");
    public static final RegistryObject<SoundEvent> CURSE_YOU_CHORUS_1 = registerSoundEvents("curseyou_chorus1");
    public static final RegistryObject<SoundEvent> CURSE_YOU_BRIDGE = registerSoundEvents("curseyou_bridge");
    public static final RegistryObject<SoundEvent> CURSE_YOU_CHORUS_2 = registerSoundEvents("curseyou_chorus2");
    public static final RegistryObject<SoundEvent> CURSE_YOU_END = registerSoundEvents("curseyou_end");
    public static final RegistryObject<SoundEvent> GEM_PLACE = registerSoundEvents("gem_place");

    private static RegistryObject<SoundEvent> registerSoundEvents(String name){
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AncientCurses.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
