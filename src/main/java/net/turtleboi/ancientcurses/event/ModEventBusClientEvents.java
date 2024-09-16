package net.turtleboi.ancientcurses.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.ModBlocks;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.block.entity.renderer.CursedAltarRenderer;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.client.CursedPortalModel;
import net.turtleboi.ancientcurses.entity.client.renderer.CursedPortalRenderer;
import net.turtleboi.ancientcurses.particle.ModParticles;
import net.turtleboi.ancientcurses.particle.custom.HealParticles;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CURSED_ALTAR.get(), RenderType.cutout());
        EntityRenderers.register(ModEntities.CURSED_PORTAL.get(), CursedPortalRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(CursedPortalModel.CURSED_PORTAL_LAYER, CursedPortalModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.HEAL_PARTICLES.get(), HealParticles.Provider::new);
    }
}
