package net.turtleboi.ancientcurses.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.client.ModRenderTypes;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.particle.ModParticles;
import net.turtleboi.ancientcurses.particle.custom.HealParticles;
import net.turtleboi.ancientcurses.util.ItemValueMap;
import org.joml.Matrix4f;

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
                renderPinkOverlay(event.getGuiGraphics());
            }

            if (player.hasEffect(ModEffects.CURSE_OF_ENDING.get())) {
                renderPurpleOverlay(event.getGuiGraphics());
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

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        if (PlayerClientData.isVoid()) {
            renderLightBeams(player, poseStack, bufferSource, event.getPartialTick());
        }
        bufferSource.endBatch();
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

    private static void renderPinkOverlay(GuiGraphics guiGraphics) {
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

    private static void renderPurpleOverlay(GuiGraphics guiGraphics) {
        //System.out.println(Component.literal(String.valueOf(PlayerClientData.isVoid()))); //debug code
        if (PlayerClientData.isVoid()) {
            //System.out.println(Component.literal("Purple screen!")); //debug code
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1F, 1F, 1F);
            Minecraft minecraft = Minecraft.getInstance();
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();
            guiGraphics.fill(0, 0, screenWidth, screenHeight, FastColor.ARGB32.color(4, 54, 1, 63));
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    public static void renderLightBeams(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        long gameTime = player.level().getGameTime();
        long voidStartTime = PlayerClientData.getVoidStartTime();
        float elapsedTicks = (gameTime - voidStartTime) + partialTicks;
        float beamLife = elapsedTicks / 200.0F;
        beamLife = beamLife % 1.0F;
        float beamIntensity = beamLife; // Keep intensity constant or adjust as needed
        RandomSource randomsource = RandomSource.create(432L);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(ModRenderTypes.getPlayerBeam());

        poseStack.pushPose();
        poseStack.translate(0.0F, player.getBbHeight() / 2.0F, 0.0F);

        // Adjust the number of beams and their properties
        int beamCount = 20; // Number of beams to render
        for (int i = 0; i < beamCount; ++i) {
            // Modify rotations and scales based on beamLife
            float rotation = randomsource.nextFloat() * 360.0F + (beamLife / 4) * 360.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

            float beamLength = randomsource.nextFloat() * 2.0F + 1.5F;
            float beamWidth = randomsource.nextFloat() * 0.5F + 0.3F;

            Matrix4f matrix4f = poseStack.last().pose();
            int alpha = Mth.clamp((int) (255 * beamLife), 0, 255);


            // Render the beam segments
            vertex01(vertexconsumer, matrix4f, alpha);
            vertex2(vertexconsumer, matrix4f, beamLength, beamWidth);
            vertex3(vertexconsumer, matrix4f, beamLength, beamWidth);
            vertex01(vertexconsumer, matrix4f, alpha);
            vertex3(vertexconsumer, matrix4f, beamLength, beamWidth);
            vertex4(vertexconsumer, matrix4f, beamLength, beamWidth);
            vertex01(vertexconsumer, matrix4f, alpha);
            vertex4(vertexconsumer, matrix4f, beamLength, beamWidth);
            vertex2(vertexconsumer, matrix4f, beamLength, beamWidth);
        }

        // Restore the matrix state
        poseStack.popPose();
    }

    private static void vertex01(VertexConsumer pConsumer, Matrix4f pMatrix, int pAlpha) {
        pConsumer.vertex(pMatrix, 0.0F, 0.0F, 0.0F).color(255, 255, 255, pAlpha).endVertex();
    }

    private static void vertex2(VertexConsumer pConsumer, Matrix4f pMatrix, float pX, float pZ) {
        pConsumer.vertex(pMatrix, -1 * (float)(Math.sqrt(3.0D) / 2.0D) * pZ, pX, -0.5F * pZ).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex3(VertexConsumer pConsumer, Matrix4f pMatrix, float pX, float pZ) {
        pConsumer.vertex(pMatrix, (float)(Math.sqrt(3.0D) / 2.0D) * pZ, pX, -0.5F * pZ).color(255, 0, 255, 0).endVertex();
    }

    private static void vertex4(VertexConsumer pConsumer, Matrix4f pMatrix, float pX, float pZ) {
        pConsumer.vertex(pMatrix, 0.0F, pX, pZ).color(255, 0, 255, 0).endVertex();
    }
}
