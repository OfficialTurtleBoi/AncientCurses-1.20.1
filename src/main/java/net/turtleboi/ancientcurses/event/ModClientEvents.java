package net.turtleboi.ancientcurses.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.turtleboi.ancientcurses.client.TrialEventBar;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.PortalOverlayPacketC2S;
import net.turtleboi.ancientcurses.util.ItemValueMap;
import org.joml.Matrix4f;

import java.util.Random;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        Minecraft minecraft = Minecraft.getInstance();
        if (player != null){
            if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
                if (player.hasEffect(ModEffects.CURSE_OF_GLUTTONY.get())) {
                    event.setCanceled(true);
                    renderCustomHungerBar(event.getGuiGraphics(), player);
                }
            }

            if (PlayerClientData.hasTrial()) {
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                TrialEventBar.render(event.getGuiGraphics(), (screenWidth - 192) / 2, 11, minecraft);
            }

            if (event.getOverlay() == VanillaGuiOverlay.BOSS_EVENT_PROGRESS.type()) {
                if (PlayerClientData.hasTrial()) {
                    event.setCanceled(true);
                }
            }

            if (player.hasEffect(ModEffects.CURSE_OF_OBESSSION.get())) {
                renderPinkOverlay(event.getGuiGraphics());
            }

            if (player.hasEffect(ModEffects.CURSE_OF_ENDING.get())) {
                renderPurpleOverlay(event.getGuiGraphics());
            }

            if (PlayerClientData.getPortalOverlayAlpha() > 0){
                ModNetworking.sendToServer(new PortalOverlayPacketC2S(PlayerClientData.getPortalOverlayAlpha()));
                renderCursedPortalOverlay(minecraft);
            }
        }
    }

    @SubscribeEvent
    public static void onCustomizeBossEventProgress(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (PlayerClientData.hasTrial()) {
            int originalY = event.getY();
            //event.setY(originalY + 9 + 5);
            event.setIncrement(event.getIncrement() + 9 + 5);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Level level = event.getEntity() != null ? event.getEntity().level() : null;

        if (level != null) {
            int itemValue = ItemValueMap.getItemValue(itemStack, level);
            int itemStackValue = itemValue * itemStack.getCount();
            //event.getToolTip().add(Component.literal("Item Value: " + itemStackValue));
        }

        if (level != null && itemStack.hasTag()) {
            CompoundTag itemTag = itemStack.getTag();

            if (itemTag != null && itemTag.getBoolean("Socketable")) {
                if (itemTag != null) {
                    if (itemTag.contains("MainGem")) {
                        ItemStack mainGemStack = ItemStack.of(itemTag.getCompound("MainGem"));
                        event.getToolTip().add(Component.literal("Main Gem: " + mainGemStack.getHoverName().getString()).withStyle(ChatFormatting.GOLD));
                    } else {
                        event.getToolTip().add(Component.literal("Main Gem: None").withStyle(ChatFormatting.GRAY));
                    }

                    if (itemTag.contains("MinorGems")) {
                        ListTag minorGems = itemTag.getList("MinorGems", CompoundTag.TAG_COMPOUND);
                        if (!minorGems.isEmpty()) {
                            event.getToolTip().add(Component.literal("Minor Gems:").withStyle(ChatFormatting.GREEN));
                            for (int i = 0; i < minorGems.size(); i++) {
                                ItemStack minorGemStack = ItemStack.of(minorGems.getCompound(i));
                                event.getToolTip().add(Component.literal("  - " + minorGemStack.getHoverName().getString()).withStyle(ChatFormatting.YELLOW));
                            }
                        } else {
                            event.getToolTip().add(Component.literal("Minor Gems: None").withStyle(ChatFormatting.GRAY));
                        }
                    } else {
                        event.getToolTip().add(Component.literal("Minor Gems: None").withStyle(ChatFormatting.GRAY));
                    }
                } else {
                    event.getToolTip().add(Component.literal("Main Gem: None").withStyle(ChatFormatting.GRAY));
                    event.getToolTip().add(Component.literal("Minor Gems: None").withStyle(ChatFormatting.GRAY));
                }
            }
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

    public static void renderLightBeams(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float pPartialTicks) {
        int totalLifetime = PlayerClientData.getTotalVoidTime();
        int currentLifetime = PlayerClientData.getVoidTimer();

        if (totalLifetime <= 0 || currentLifetime <= 0 || currentLifetime > totalLifetime) {
            //System.out.println("Invalid lifetime values, skipping rendering."); //debug code
            return;
        }

        float beamLife = (float) (totalLifetime - currentLifetime) / totalLifetime;
        float beamIntensity = beamLife * 2;

        RandomSource randomsource = RandomSource.create(432L);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(ModRenderTypes.getPlayerBeam());

        poseStack.pushPose();
        poseStack.translate(0.0F, player.getBbHeight() / 2.0F, 0.0F);

        float beamSpawnRate = 10.0F;
        int beamCount = Math.min(20, (int) ((totalLifetime - currentLifetime) / beamSpawnRate));

        long gameTime = player.level().getGameTime();

        for (int i = 0; i < beamCount; ++i) {
            float randomRotationX = randomsource.nextFloat() * 360.0F;
            float randomRotationY = randomsource.nextFloat() * 360.0F;
            float randomRotationZ = randomsource.nextFloat() * 360.0F;
            float timeRotationFactor = (gameTime + pPartialTicks) * 1.5F;

            poseStack.mulPose(Axis.XP.rotationDegrees(randomRotationX + timeRotationFactor));
            poseStack.mulPose(Axis.YP.rotationDegrees(randomRotationY + timeRotationFactor));
            poseStack.mulPose(Axis.ZP.rotationDegrees(randomRotationZ + timeRotationFactor));

            float baseBeamLength = 0.1F;
            float maxBeamLength = 5.0F;

            float baseBeamWidth = 0.1F;
            float maxBeamWidth = 2.0F;

            float beamLength = baseBeamLength + (maxBeamLength - baseBeamLength) * beamLife + randomsource.nextFloat() * 0.5F;
            float beamWidth = baseBeamWidth + (maxBeamWidth - baseBeamWidth) * beamLife + randomsource.nextFloat() * 0.2F;

            Matrix4f matrix4f = poseStack.last().pose();
            int alpha = Mth.clamp((int) (64 + (191 * beamIntensity)), 0, 255);
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

    public static void renderCursedPortalOverlay(Minecraft minecraft) {
        // Get the current alpha from player data
        float alpha = PlayerClientData.getPortalOverlayAlpha();

        if (alpha <= 0) {
            return; // No need to render if alpha is zero or less
        }

        // Setup the pose stack for rendering
        PoseStack poseStack = new PoseStack();

        // Get the screen dimensions
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc(); // Use default blending
        RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, alpha); // Red color with alpha

        // Get the texture (this can be your custom texture or the vanilla nether portal texture)
        ResourceLocation portalTexture = new ResourceLocation("minecraft", "textures/block/nether_portal.png");
        RenderSystem.setShaderTexture(0, portalTexture);

        long time = minecraft.level.getGameTime();
        int totalFrames = 32; // The Nether portal has 16 frames in its animation
        int frame = (int) (time % totalFrames); // Loop over the frames based on the game time

        // Each frame is stacked vertically in the texture, so calculate the UV coordinates for the current frame
        float frameHeight = 1.0f / totalFrames; // Height of each frame in the texture
        float vMin = frame * frameHeight; // Start of the current frame (v)
        float vMax = vMin + frameHeight; // End of the current frame (v)

        // Start rendering the quad with the selected frame
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // Render the full-screen quad with the current frame of the portal texture
        buffer.vertex(0.0D, screenHeight, -90.0D).uv(0.0F, vMax).endVertex();
        buffer.vertex(screenWidth, screenHeight, -90.0D).uv(1.0F, vMax).endVertex();
        buffer.vertex(screenWidth, 0.0D, -90.0D).uv(1.0F, vMin).endVertex();
        buffer.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, vMin).endVertex();

        Tesselator.getInstance().end();

        // Disable blending after rendering
        RenderSystem.disableBlend();
    }



}
