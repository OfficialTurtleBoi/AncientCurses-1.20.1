package net.turtleboi.ancientcurses.event;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.effect.effects.CurseOfLust;
import net.turtleboi.ancientcurses.particle.ModParticles;
import net.turtleboi.ancientcurses.particle.custom.HealParticles;
import net.turtleboi.ancientcurses.util.ItemValueMap;

import java.util.Random;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player != null){
            if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
                if (player.hasEffect(ModEffects.CURSE_OF_GLUTTONY.get())) {
                    event.setCanceled(true);
                    renderCustomHungerBar(event.getGuiGraphics(), player);
                }
            }

            if (player.hasEffect(ModEffects.CURSE_OF_LUST.get())) {
                renderPinkOverlay(event.getGuiGraphics(), player);
            }
        }
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(ModParticles.HEAL_PARTICLES.get(),
                HealParticles.Provider::new);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Level level = event.getEntity() != null ? event.getEntity().level() : null;

        if (level != null) {
            int itemValue = ItemValueMap.getItemValue(itemStack, level);
            int itemStackValue = itemValue * itemStack.getCount();
            event.getToolTip().add(Component.literal("Item Value: " + itemStackValue));
        }
    }

    private static final ResourceLocation HUNGER_ICONS = new ResourceLocation(AncientCurses.MOD_ID, "textures/gui/hunger_icons.png");

    private static void renderCustomHungerBar(GuiGraphics guiGraphics, Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int xStart = screenWidth / 2 + 91;
        int yStart = screenHeight - 39;

        boolean isGluttonous = player.hasEffect(ModEffects.CURSE_OF_GLUTTONY.get());
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            int x = xStart - i * 8 - 9;
            int y = yStart;
            if (isGluttonous && random.nextFloat() < 0.1F) {
                x += random.nextInt(3) - 1;
                y += random.nextInt(3) - 1;
            }
            guiGraphics.blit(HUNGER_ICONS, x, y, 18, 0, 9, 9, 27, 9);
            if (i * 2 + 1 < foodLevel) {
                guiGraphics.blit(HUNGER_ICONS, x, y, 0, 0, 9, 9,27, 9);
            } else if (i * 2 + 1 == foodLevel) {
                guiGraphics.blit(HUNGER_ICONS, x, y, 9, 0, 9, 9,27, 9);
            }
        }
    }

    private static void renderPinkOverlay(GuiGraphics guiGraphics, Player player) {
        //System.out.println(Component.literal(String.valueOf(PlayerClientData.isLusted()))); //debug code
        if (PlayerClientData.isLusted()) {
            //System.out.println(Component.literal("Pink screen!")); //debug code
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1F, 1F, 1F);
            Minecraft minecraft = Minecraft.getInstance();
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            guiGraphics.fill(0, 0, screenWidth, screenHeight, FastColor.ARGB32.color(8, 255, 20, 147));
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}
