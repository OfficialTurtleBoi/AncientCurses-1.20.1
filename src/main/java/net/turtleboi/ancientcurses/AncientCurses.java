package net.turtleboi.ancientcurses;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.command.ClearCurseCommand;
import net.turtleboi.ancientcurses.config.AncientCursesConfig;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.enchantment.ModEnchantments;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.init.ModAttributes;
import net.turtleboi.ancientcurses.item.ModCreativeModeTabs;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.screen.ModMenuTypes;
import net.turtleboi.ancientcurses.sound.ModSounds;
import net.turtleboi.ancientcurses.util.ModItemProperties;
import net.turtleboi.ancientcurses.world.structures.ModStructures;
import org.slf4j.Logger;

@Mod(AncientCurses.MOD_ID)
public class AncientCurses {
    public static final String MOD_ID = "ancientcurses";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AncientCurses() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModParticleTypes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModEntities.register(modEventBus);

        ModEffects.register(modEventBus);
        ModSounds.register(modEventBus);

        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        ModStructures.register(modEventBus);

        ModAttributes.REGISTRY.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AncientCursesConfig.SPEC, "ancientcurses-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {

        });

        ModItemProperties.addCustomItemProperties();
        CurseRegistry.initialize();
        ModNetworking.register();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        ClearCurseCommand.register(event.getServer().getCommands().getDispatcher());
    }
}
