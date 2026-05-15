package net.turtleboi.ancientcurses.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeyMappings {
    public static final String CATEGORY = "key.categories.ancientcurses";
    public static final KeyMapping ARTIFACT_ABILITY = new KeyMapping(
            "key.ancientcurses.artifact_ability",
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    private ModKeyMappings() {
    }
}
