package net.turtleboi.ancientcurses.event;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.turtleboi.ancientcurses.entity.client.CursedPortalModel;
import net.turtleboi.ancientcurses.entity.client.renderer.CursedPortalRenderer;
import net.turtleboi.ancientcurses.item.ModItems;
import net.turtleboi.ancientcurses.particle.ModParticleTypes;
import net.turtleboi.ancientcurses.particle.custom.CursedFlameParticle;
import net.turtleboi.ancientcurses.particle.custom.CursedParticle;
import net.turtleboi.ancientcurses.particle.custom.HealParticle;
import net.turtleboi.ancientcurses.particle.custom.SleepParticle;
import net.turtleboi.ancientcurses.screen.LapidaristTableContainerScreen;
import net.turtleboi.ancientcurses.screen.ModMenuTypes;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientBusEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.CURSED_PORTAL.get(), CursedPortalRenderer::new);
        MenuScreens.register(ModMenuTypes.LAPIDARIST_MENU.get(), LapidaristTableContainerScreen::new);

        event.enqueueWork(() -> ItemProperties.register(ModItems.GOLDEN_AMULET.get(), new ResourceLocation(AncientCurses.MOD_ID, "main_gem"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {
                    CompoundTag tag = stack.getTag();
                    if (tag != null && tag.contains("MainGem")) {
                        ItemStack mainGemStack = ItemStack.of(tag.getCompound("MainGem"));
                        if (!mainGemStack.isEmpty()) {
                            if (mainGemStack.getItem() == ModItems.PERFECT_AMETHYST.get() || mainGemStack.getItem() == ModItems.POLISHED_AMETHYST.get()) return 1.0F;
                            if (mainGemStack.getItem() == ModItems.PERFECT_DIAMOND.get() || mainGemStack.getItem() == ModItems.POLISHED_DIAMOND.get()) return 2.0F;
                            if (mainGemStack.getItem() == ModItems.PERFECT_EMERALD.get() || mainGemStack.getItem() == ModItems.POLISHED_EMERALD.get()) return 3.0F;
                            if (mainGemStack.getItem() == ModItems.PERFECT_RUBY.get() || mainGemStack.getItem() == ModItems.POLISHED_RUBY.get()) return 4.0F;
                            if (mainGemStack.getItem() == ModItems.PERFECT_SAPPHIRE.get() || mainGemStack.getItem() == ModItems.POLISHED_SAPPHIRE.get()) return 5.0F;
                            if (mainGemStack.getItem() == ModItems.PERFECT_TOPAZ.get() || mainGemStack.getItem() == ModItems.POLISHED_TOPAZ.get()) return 6.0F;
                            if (mainGemStack.getItem() == ModItems.ANCIENT_ALEXANDRITE.get()) return 7.0F;
                            if (mainGemStack.getItem() == ModItems.PERFECT_AMETHYST.get()) return 8.0F;
                            if (mainGemStack.getItem() == ModItems.ANCIENT_CHRYSOBERYL.get()) return 9.0F;
                        }
                    }
                    return 0.0F;
                }));
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
