package net.turtleboi.ancientcurses.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class RiteMusicSoundInstance extends AbstractTickableSoundInstance {
    private static final float MAX_VOLUME = 1.0F;

    private long fadeOutStartedAtTick = -1L;
    private long fadeOutDurationTicks;

    public RiteMusicSoundInstance(SoundEvent soundEvent) {
        super(soundEvent, SoundSource.MUSIC, RandomSource.create());
        this.looping = false;
        this.delay = 0;
        this.volume = MAX_VOLUME;
        this.pitch = 1.0F;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    public void beginFadeOut(long durationMillis) {
        if (this.fadeOutStartedAtTick >= 0L) {
            return;
        }

        this.fadeOutDurationTicks = millisToTicks(durationMillis);
        if (this.fadeOutDurationTicks <= 0L) {
            stop();
            return;
        }

        this.fadeOutStartedAtTick = getCurrentGameTick();
    }

    @Override
    public void tick() {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.isRemoved()) {
            stop();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        updateVolume();
    }

    private void updateVolume() {
        long nowTick = getCurrentGameTick();

        float fadeOutFactor = 1.0F;
        if (fadeOutStartedAtTick >= 0L) {
            float elapsed = (float) (nowTick - fadeOutStartedAtTick) / (float) fadeOutDurationTicks;
            fadeOutFactor = Math.max(0.0F, 1.0F - elapsed);
            if (fadeOutFactor <= 0.0F) {
                stop();
                return;
            }
        }

        this.volume = MAX_VOLUME * fadeOutFactor;
    }

    private long getCurrentGameTick() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null ? minecraft.level.getGameTime() : 0L;
    }

    private long millisToTicks(long durationMillis) {
        return Math.max(0L, Math.round(durationMillis / 50.0D));
    }
}
