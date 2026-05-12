package net.turtleboi.ancientcurses.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.CameraType;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.client.ModRenderTypes;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.client.RiteEventBar;
import net.turtleboi.ancientcurses.client.VoodooSoulClientData;
import net.turtleboi.ancientcurses.client.renderer.DowsingRodRenderer;
import net.turtleboi.ancientcurses.client.renderer.FirstBeaconEffectRenderer;
import net.turtleboi.ancientcurses.client.sound.RiteMusicController;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.item.items.DowsingRod;
import net.turtleboi.ancientcurses.item.items.FirstBeaconItem;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.PortalOverlayPacketC2S;
import net.turtleboi.ancientcurses.util.ItemValueMap;
import net.turtleboi.ancientcurses.util.ModItemProperties;
import net.turtleboi.turtlecore.client.data.ScreenEffectsData;
import net.turtleboi.turtlecore.client.util.TintingVertexConsumer;
import org.joml.Matrix4f;

import java.util.Random;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    private static final float VOODOO_SOUL_RED = 0.15F;
    private static final float VOODOO_SOUL_GREEN = 1.0F;
    private static final float VOODOO_SOUL_BLUE = 0.9F;
    private static final float VOODOO_SOUL_ALPHA = 0.55F;
    private static boolean renderingVoodooSoul;

    private static float beaconSavedRightArmXRot;
    private static float beaconSavedRightArmYRot;
    private static float beaconSavedLeftArmXRot;
    private static float beaconSavedLeftArmYRot;
    private static boolean beaconArmPoseActive;

    private static float beaconLimitedYaw = Float.NaN;
    private static float beaconLimitedPitch = Float.NaN;
    private static long beaconFrameNanos = 0L;

    public static float getBeaconLimitedYaw() {
        return beaconLimitedYaw;
    }

    public static float getBeaconLimitedPitch() {
        return beaconLimitedPitch;
    }

    public static boolean isBeaconArmPoseActive() {
        return beaconArmPoseActive;
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (Float.isNaN(beaconLimitedYaw)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            event.setYaw(beaconLimitedYaw);
            event.setPitch(beaconLimitedPitch);
        }
    }

@SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        if (PlayerClientData.getDowsingRodUsed() && player != null && !(player.getMainHandItem().getItem() instanceof DowsingRod)) {
            PlayerClientData.setDowsingRodUsed(false);
            PlayerClientData.setDowsingRodUsedTime(-1);
        }

        if (PlayerClientData.getItemUsed() && player != null
                && !(player.getMainHandItem().getItem() instanceof FirstBeaconItem)
                && !(player.getOffhandItem().getItem() instanceof FirstBeaconItem)) {
            PlayerClientData.setItemUsed(false);
            PlayerClientData.setItemRemainingUseTime(0);
        }

        if (PlayerClientData.getItemUsed() && PlayerClientData.getItemRemainingUseTime() > 0) {
            PlayerClientData.setItemRemainingUseTime(PlayerClientData.getItemRemainingUseTime() - 1);
        }

        if (!PlayerClientData.getItemUsed() && PlayerClientData.getItemRemainingUseTime() <= 100) {
            if (PlayerClientData.getItemRemainingUseTime() > 0) {
                PlayerClientData.setItemRemainingUseTime(PlayerClientData.getItemRemainingUseTime() - 1);
            }
        }

        RiteMusicController.tick();
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            beaconLimitedYaw = Float.NaN;
            beaconLimitedPitch = Float.NaN;
            beaconFrameNanos = 0L;
            return;
        }

        boolean holdingBeacon = player.getMainHandItem().getItem() instanceof FirstBeaconItem
                || player.getOffhandItem().getItem() instanceof FirstBeaconItem;

        if (!holdingBeacon || !player.isUsingItem()) {
            beaconLimitedYaw = Float.NaN;
            beaconLimitedPitch = Float.NaN;
            beaconFrameNanos = 0L;
            return;
        }

        int remainingUseTime = PlayerClientData.getItemRemainingUseTime();
        int maxDuration = PlayerClientData.getItemMaxDurationTicks();
        if (maxDuration <= 0 || remainingUseTime <= 0) {
            beaconLimitedYaw = Float.NaN;
            beaconLimitedPitch = Float.NaN;
            beaconFrameNanos = 0L;
            return;
        }

        float ticksElapsed = maxDuration - remainingUseTime;
        float progress = Math.min(1.0f, ticksElapsed / FirstBeaconItem.chargeRate);
        if (progress <= 0.35f) {
            beaconLimitedYaw = player.getYRot();
            beaconLimitedPitch = player.getXRot();
            beaconFrameNanos = 0L;
            return;
        }

        long now = System.nanoTime();
        float elapsedTicks = beaconFrameNanos > 0L
                ? Mth.clamp((now - beaconFrameNanos) / 50_000_000f, 0.001f, 2f)
                : (1f / 20f);
        beaconFrameNanos = now;

        float beamProgressFactor = (progress - 0.35f) / 0.65f;
        float maxTurnDegrees = Mth.lerp(beamProgressFactor, 8.0f, 1.5f);
        float maxDegreesPerFrame = maxTurnDegrees * elapsedTicks;

        if (Float.isNaN(beaconLimitedYaw)) {
            beaconLimitedYaw = player.getYRot();
            beaconLimitedPitch = player.getXRot();
        }

        float yawDelta = Mth.wrapDegrees(player.getYRot() - beaconLimitedYaw);
        float pitchDelta = player.getXRot() - beaconLimitedPitch;

        beaconLimitedYaw += Mth.clamp(yawDelta, -maxDegreesPerFrame, maxDegreesPerFrame);
        beaconLimitedPitch += Mth.clamp(pitchDelta, -maxDegreesPerFrame, maxDegreesPerFrame);
        beaconLimitedPitch = Mth.clamp(beaconLimitedPitch, -90f, 90f);

        player.setYRot(beaconLimitedYaw);
        player.setXRot(beaconLimitedPitch);
        player.yRotO = beaconLimitedYaw;
        player.xRotO = beaconLimitedPitch;
    }

@SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        Minecraft minecraft = Minecraft.getInstance();
        if (player != null){
            if (PlayerClientData.getPortalOverlayAlpha() > 0){
                ModNetworking.sendToServer(new PortalOverlayPacketC2S(PlayerClientData.getPortalOverlayAlpha()));
                renderCursedPortalOverlay(minecraft);
            }

            if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
                if (!player.getAbilities().instabuild && player.hasEffect(ModEffects.CURSE_OF_GLUTTONY.get())) {
                    event.setCanceled(true);
                    renderCustomHungerBar(event.getGuiGraphics(), player);
                }
            }

            if (PlayerClientData.hasRite()) {
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                RiteEventBar.render(event.getGuiGraphics(), (screenWidth - 192) / 2, 11, minecraft);
            }

            if (event.getOverlay() == VanillaGuiOverlay.BOSS_EVENT_PROGRESS.type()) {
                if (PlayerClientData.hasRite()) {
                    event.setCanceled(true);
                }
            }

            if (player.hasEffect(ModEffects.CURSE_OF_OBESSSION.get())) {
                renderPinkOverlay(event.getGuiGraphics());
            }

            if (player.hasEffect(ModEffects.CURSE_OF_ENDING.get())) {
                renderPurpleOverlay(event.getGuiGraphics());
            }
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!RiteMusicController.shouldBlockVanillaMusic() || event.getSound() == null) {
            return;
        }

        if (event.getSound().getSource() != SoundSource.MUSIC) {
            return;
        }

        ResourceLocation soundId = event.getSound().getLocation();
        if (RiteMusicController.isRiteMusic(soundId)) {
            return;
        }

        event.setSound(null);
    }



    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!VoodooSoulClientData.isSoulClone(event.getEntity().getUUID()) || renderingVoodooSoul) {
            return;
        }

        renderVoodooSoul(event);
        event.setCanceled(true);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void renderVoodooSoul(RenderLivingEvent.Pre event) {
        renderingVoodooSoul = true;
        int hurtTime = event.getEntity().hurtTime;
        int deathTime = event.getEntity().deathTime;
        try {
            event.getEntity().hurtTime = 0;
            event.getEntity().deathTime = 0;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            event.getRenderer().render(
                    event.getEntity(),
                    event.getEntity().getYRot(),
                    event.getPartialTick(),
                    event.getPoseStack(),
                    new VoodooSoulBuffer(event.getMultiBufferSource(),
                            event.getRenderer().getTextureLocation(event.getEntity())),
                    event.getPackedLight());
        } finally {
            event.getEntity().hurtTime = hurtTime;
            event.getEntity().deathTime = deathTime;
            RenderSystem.disableBlend();
            renderingVoodooSoul = false;
        }
    }

    private record VoodooSoulBuffer(MultiBufferSource delegate, ResourceLocation texture) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new TintingVertexConsumer(
                    delegate.getBuffer(RenderType.entityTranslucentEmissive(texture)),
                    VOODOO_SOUL_RED,
                    VOODOO_SOUL_GREEN,
                    VOODOO_SOUL_BLUE,
                    VOODOO_SOUL_ALPHA);
        }
    }

    @SubscribeEvent
    public static void onRenderFirstPerson(RenderHandEvent event) {
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        PoseStack poseStack = event.getPoseStack();
        InteractionHand interactionHand = event.getHand();
        ItemStack itemStack = event.getItemStack();

        FirstBeaconEffectRenderer.renderFirstPerson(bufferSource, poseStack, interactionHand, itemStack);

        if (itemStack.getItem() instanceof DowsingRod && PlayerClientData.getDowsingRodUsed()) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) return;

            DowsingRodRenderer.renderFirstPerson(bufferSource, poseStack, interactionHand, itemStack);
            event.setCanceled(true);

            int packedLight = LightTexture.pack(
                    minecraft.level.getBrightness(LightLayer.BLOCK, player.blockPosition()),
                    minecraft.level.getBrightness(LightLayer.SKY, player.blockPosition()));

            if (!(event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource buffer)) return;

            PlayerRenderer playerRenderer = (PlayerRenderer)
                    minecraft.getEntityRenderDispatcher().getRenderer(player);

            float slideProgress = 1f;
            if (PlayerClientData.getDowsingRodUsedTime() > 0) {
                long elapsed = System.currentTimeMillis() - PlayerClientData.getDowsingRodUsedTime();
                slideProgress = Mth.clamp((float)elapsed / 300, 0f, 1f);
                if (slideProgress >= 1f) {
                    PlayerClientData.setDowsingRodUsedTime(-1);
                }
            }

            float yOffset = (1f - slideProgress) * 0.5f;

            poseStack.pushPose();
            poseStack.scale(1.25f, 1.25f, 1.25f);
            poseStack.translate(0, -yOffset - 0.5f, 0f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-75f));
            playerRenderer.renderRightHand(poseStack, buffer, packedLight, player);
            playerRenderer.renderLeftHand(poseStack, buffer, packedLight, player);
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onCustomizeBossEventProgress(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (PlayerClientData.hasRite()) {
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
        }

        if (level != null && itemStack.hasTag()) {
            CompoundTag itemTag = itemStack.getTag();

            if (itemTag != null && itemTag.getBoolean("Socketable")) {
                if (itemTag.contains("MainGem")) {
                    ItemStack mainGemStack = ItemStack.of(itemTag.getCompound("MainGem"));
                    event.getToolTip().add(Component.translatable("item.ancientcurses.amulet.main_gem", mainGemStack.getHoverName().getString())
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    event.getToolTip().add(Component.translatable("item.ancientcurses.amulet.main_gem_none")
                            .withStyle(ChatFormatting.GRAY));
                }

                if (itemTag.contains("MinorGems")) {
                    ListTag minorGems = itemTag.getList("MinorGems", CompoundTag.TAG_COMPOUND);
                    if (!minorGems.isEmpty()) {
                        event.getToolTip().add(Component.translatable("item.ancientcurses.amulet.minor_gems")
                                .withStyle(ChatFormatting.GREEN));
                        for (int i = 0; i < minorGems.size(); i++) {
                            ItemStack minorGemStack = ItemStack.of(minorGems.getCompound(i));
                            event.getToolTip().add(Component.translatable("item.ancientcurses.amulet.minor_gem_entry", minorGemStack.getHoverName().getString())
                                    .withStyle(ChatFormatting.YELLOW));
                        }
                    } else {
                        event.getToolTip().add(Component.translatable("item.ancientcurses.amulet.minor_gems_none")
                                .withStyle(ChatFormatting.GRAY));
                    }
                } else {
                    event.getToolTip().add(Component.translatable("item.ancientcurses.amulet.minor_gems_none")
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }


    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof FirstBeaconItem)) {
            return;
        }
        PlayerModel<?> model = event.getRenderer().getModel();
        beaconSavedRightArmXRot = model.rightArm.xRot;
        beaconSavedRightArmYRot = model.rightArm.yRot;
        beaconSavedLeftArmXRot = model.leftArm.xRot;
        beaconSavedLeftArmYRot = model.leftArm.yRot;
        beaconArmPoseActive = true;
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (beaconArmPoseActive) {
            PlayerModel<?> model = event.getRenderer().getModel();
            model.rightArm.xRot = beaconSavedRightArmXRot;
            model.rightArm.yRot = beaconSavedRightArmYRot;
            model.leftArm.xRot = beaconSavedLeftArmXRot;
            model.leftArm.yRot = beaconSavedLeftArmYRot;
            beaconArmPoseActive = false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        if (PlayerClientData.isSingularity()) {
            renderLightBeams(player, poseStack, bufferSource, event.getPartialTick());
        }
        bufferSource.endBatch();
    }

    @SubscribeEvent
    public static void onRenderTick(RenderLevelStageEvent event) {
        RiteMusicController.renderTick();
        if (ScreenEffectsData.getCameraShakeDuration() > 0) {
            applyShake(event);
        }
    }

    private static final Random random = new Random();
    private static int ticksElapsed = 0;
    private static void applyShake(RenderLevelStageEvent event) {
        if (ScreenEffectsData.getCameraShakeDuration() <= 0) {
            return;
        }

        ticksElapsed++;

        if (ticksElapsed > ScreenEffectsData.getCameraShakeDuration()) {
            ScreenEffectsData.setCameraShakeIntensity(0.0F);
            ScreenEffectsData.setCameraShakeDuration(0);
            ticksElapsed = 0;
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        float currentIntensity = ScreenEffectsData.getCameraShakeIntensity() * (1.0f - ((float) ticksElapsed / ScreenEffectsData.getCameraShakeDuration()));

        double offsetX = (random.nextDouble() - 0.5) * 2 * currentIntensity;
        double offsetY = (random.nextDouble() - 0.5) * 2 * currentIntensity;

        PoseStack poseStack = event.getPoseStack();
        poseStack.translate(offsetX, offsetY, 0.0);
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
        if (PlayerClientData.isObsessed()) {
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
        if (PlayerClientData.isSingularity()) {
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
        int totalLifetime = PlayerClientData.getTotalSingularityType();
        int currentLifetime = PlayerClientData.getSingularityTimer();

        if (totalLifetime <= 0 || currentLifetime <= 0 || currentLifetime > totalLifetime) {
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
        float alpha = PlayerClientData.getPortalOverlayAlpha();

        if (alpha <= 0) {
            return;
        }

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();

        try {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, alpha);

            ResourceLocation portalTexture = new ResourceLocation("minecraft", "textures/block/nether_portal.png");
            RenderSystem.setShaderTexture(0, portalTexture);

            long time = minecraft.level.getGameTime();
            int totalFrames = 32;
            int frame = (int) (time % totalFrames);

            float frameHeight = 1.0f / totalFrames;
            float vMin = frame * frameHeight;
            float vMax = vMin + frameHeight;

            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            buffer.vertex(0.0D, screenHeight, -90.0D).uv(0.0F, vMax).endVertex();
            buffer.vertex(screenWidth, screenHeight, -90.0D).uv(1.0F, vMax).endVertex();
            buffer.vertex(screenWidth, 0.0D, -90.0D).uv(1.0F, vMin).endVertex();
            buffer.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, vMin).endVertex();

            Tesselator.getInstance().end();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }




}
