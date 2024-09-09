package net.turtleboi.ancientcurses.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.util.ItemValueMap;

import java.util.Random;

@Mod.EventBusSubscriber(modid = AncientCurses.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()) {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.hasEffect(ModEffects.CURSE_OF_GLUTTONY.get())) {
                event.setCanceled(true);
                renderCustomHungerBar(event.getGuiGraphics(), player);
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Level level = event.getEntity() != null ? event.getEntity().level() : null;

        if (level != null) {
            int itemValue = ItemValueMap.getItemValue(itemStack, level);
            event.getToolTip().add(Component.literal("Item Value: " + itemValue));
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
}
