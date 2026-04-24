package net.turtleboi.ancientcurses.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.block.entity.ModBlockEntities;
import net.turtleboi.ancientcurses.block.entity.renderer.CursedAltarRenderer;
import net.turtleboi.ancientcurses.entity.ModEntities;
import net.turtleboi.ancientcurses.entity.client.model.CursedPortalModel;
import net.turtleboi.ancientcurses.entity.client.renderer.CursedNodeRenderer;
import net.turtleboi.ancientcurses.entity.client.renderer.CursedPortalRenderer;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.item.items.PreciousGemItem;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.particle.custom.CursedFlameParticle;
import net.turtleboi.ancientcurses.particle.custom.CursedParticle;
import net.turtleboi.ancientcurses.particle.custom.GoldenFeatherParticle;
import net.turtleboi.ancientcurses.screen.LapidaristTableContainerScreen;
import net.turtleboi.ancientcurses.screen.ModMenuTypes;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientBusEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.CURSED_PORTAL.get(), CursedPortalRenderer::new);
        EntityRenderers.register(ModEntities.CURSED_NODE.get(), CursedNodeRenderer::new);
        EntityRenderers.register(ModEntities.CURSED_PEARL.get(), ThrownItemRenderer::new);
        MenuScreens.register(ModMenuTypes.LAPIDARIST_MENU.get(), LapidaristTableContainerScreen::new);

        event.enqueueWork(() -> ItemProperties.register(ModItems.GOLDEN_AMULET.get(), new ResourceLocation(AncientCurses.MOD_ID, "main_gem"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("MainGem")) {
                        ItemStack mainGemStack = ItemStack.of(tag.getCompound("MainGem"));
                        if (mainGemStack.getItem() instanceof PreciousGemItem gemItem) {
                            return gemItem.getGemType().getAmuletModelValue();
                        }
                    }
                    return 0.0F;
                }));
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(CursedPortalModel.CURSED_PORTAL_LAYER, CursedPortalModel::createBodyLayer);
        event.registerLayerDefinition(CursedNodeRenderer.CURSED_NODE_LAYER, CursedNodeRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.CURSED_ALTAR_BE.get(), CursedAltarRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ModParticleTypes.CURSED_PARTICLE.get(),
                CursedParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CURSED_FLAME_PARTICLE.get(),
                CursedFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GOLDEN_FEATHER_PARTICLE.get(),
                GoldenFeatherParticle.Provider::new);
    }
}
