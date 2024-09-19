package net.turtleboi.ancientcurses.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.effect.effects.*;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AncientCurses.MOD_ID);

    public static final RegistryObject<MobEffect> CURSE_OF_SLOTH = MOB_EFFECTS.register("curse_of_sloth",
            () -> new CurseOfSlothEffect(MobEffectCategory.HARMFUL, 5592405));

    public static final RegistryObject<MobEffect> CURSE_OF_FRAILTY = MOB_EFFECTS.register("curse_of_frailty",
            () -> new CurseOfFrailtyEffect(MobEffectCategory.HARMFUL, 16776960));

    public static final RegistryObject<MobEffect> CURSE_OF_GLUTTONY = MOB_EFFECTS.register("curse_of_gluttony",
            () -> new CurseOfGluttonyEffect(MobEffectCategory.HARMFUL, 15164707));

    public static final RegistryObject<MobEffect> CURSE_OF_AVARICE = MOB_EFFECTS.register("curse_of_avarice",
            () -> new CurseOfAvariceEffect(MobEffectCategory.HARMFUL, 16766720));

    public static final RegistryObject<MobEffect> CURSE_OF_NATURE = MOB_EFFECTS.register("curse_of_nature",
            () -> new CurseOfNatureEffect(MobEffectCategory.HARMFUL, 5635925));

    public static final RegistryObject<MobEffect> CURSE_OF_ENVY = MOB_EFFECTS.register("curse_of_envy",
            () -> new CurseOfEnvyEffect(MobEffectCategory.HARMFUL, 65280));

    public static final RegistryObject<MobEffect> CURSE_OF_WRATH = MOB_EFFECTS.register("curse_of_wrath",
            () -> new CurseOfWrathEffect(MobEffectCategory.HARMFUL, 14423100));

    public static final RegistryObject<MobEffect> CURSE_OF_SHADOWS = MOB_EFFECTS.register("curse_of_shadows",
            () -> new CurseOfShadowsEffect(MobEffectCategory.HARMFUL, 0));

    public static final RegistryObject<MobEffect> CURSE_OF_PRIDE = MOB_EFFECTS.register("curse_of_pride",
            () -> new CurseOfPrideEffect(MobEffectCategory.HARMFUL, 8388736));

    public static final RegistryObject<MobEffect> CURSE_OF_ENDING = MOB_EFFECTS.register("curse_of_ending",
            () -> new CurseOfEndingEffect(MobEffectCategory.HARMFUL, 11141290));

    public static final RegistryObject<MobEffect> CURSE_OF_PESTILENCE = MOB_EFFECTS.register("curse_of_pestilence",
            () -> new CurseOfPestilenceEffect(MobEffectCategory.HARMFUL, 8421376));

    public static final RegistryObject<MobEffect> CURSE_OF_OBESSSION = MOB_EFFECTS.register("curse_of_obsession",
            () -> new CurseOfObessionEffect(MobEffectCategory.HARMFUL, 16711900));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
