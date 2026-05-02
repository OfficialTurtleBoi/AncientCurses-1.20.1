package net.turtleboi.ancientcurses.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.RegistryObject;
import net.turtleboi.ancientcurses.AncientCurses;
import net.turtleboi.ancientcurses.client.PlayerClientData;
import net.turtleboi.ancientcurses.client.rites.ClientRiteState;
import net.turtleboi.ancientcurses.client.rites.NoRiteState;
import net.turtleboi.ancientcurses.sound.ModSounds;

import java.util.List;

public final class RiteMusicController {
    private static final long HANDOFF_ON_SOUND_END = -1L;
    private static final long CROSSFADE_MS = 1_250L;
    private static final long CURSE_YOU_INTRO_HANDOFF_MS = 20_600L;
    private static final long CURSE_YOU_VERSE_1_HANDOFF_MS = 21_700L;
    private static final long CURSE_YOU_VERSE_2_HANDOFF_MS = 21_800L;
    private static final long CURSE_YOU_CHORUS_1_HANDOFF_MS = 40_500L;
    private static final long CURSE_YOU_BRIDGE_HANDOFF_MS = 22_200L;
    private static final long CURSE_YOU_CHORUS_2_HANDOFF_MS = 40_500L;
    private static final long CURSE_YOU_OUTRO_HANDOFF_MS = HANDOFF_ON_SOUND_END;
    private static final long CURSE_YOU_OUTRO_MAX_PLAY_MS = 5_000L;

    private static final SectionTrack INTRO_SECTION =
            new SectionTrack("intro", ModSounds.CURSE_YOU_INTRO, CURSE_YOU_INTRO_HANDOFF_MS, CROSSFADE_MS, HANDOFF_ON_SOUND_END);
    private static final SectionTrack VERSE_1_SECTION =
            new SectionTrack("verse1", ModSounds.CURSE_YOU_VERSE_1, CURSE_YOU_VERSE_1_HANDOFF_MS, CROSSFADE_MS, HANDOFF_ON_SOUND_END);
    private static final SectionTrack CHORUS_1_SECTION =
            new SectionTrack("chorus1", ModSounds.CURSE_YOU_CHORUS_1, CURSE_YOU_CHORUS_1_HANDOFF_MS, 0L, HANDOFF_ON_SOUND_END);
    private static final SectionTrack VERSE_2_SECTION =
            new SectionTrack("verse2", ModSounds.CURSE_YOU_VERSE_2, CURSE_YOU_VERSE_2_HANDOFF_MS, CROSSFADE_MS, HANDOFF_ON_SOUND_END);
    private static final SectionTrack BRIDGE_SECTION =
            new SectionTrack("bridge", ModSounds.CURSE_YOU_BRIDGE, CURSE_YOU_BRIDGE_HANDOFF_MS, 0L, HANDOFF_ON_SOUND_END);
    private static final SectionTrack CHORUS_2_SECTION =
            new SectionTrack("chorus2", ModSounds.CURSE_YOU_CHORUS_2, CURSE_YOU_CHORUS_2_HANDOFF_MS, 0L, HANDOFF_ON_SOUND_END);
    private static final SectionTrack OUTRO_SECTION =
            new SectionTrack("outro", ModSounds.CURSE_YOU_END, CURSE_YOU_OUTRO_HANDOFF_MS, 0L, CURSE_YOU_OUTRO_MAX_PLAY_MS);

    private static RiteMusicSoundInstance currentSound;
    private static SectionTrack currentTrack;
    private static String currentRiteId = "";
    private static String previousRiteId = "";
    private static boolean previousRiteComplete;
    private static boolean introPlayed;
    private static boolean outroQueued;
    private static boolean outroStarted;
    private static boolean musicOwnershipActive;
    private static boolean lastKnownSoundActive;
    private static boolean currentTrackHandoffTriggered;
    private static int lastCompletedDegrees;
    private static int lastActiveDegreeIndex = -1;
    private static float lastProgressSnapshot;
    private static double currentTrackStartedAtMs;
    private static LoopPhase nextLoopPhase = LoopPhase.FIRST_VERSE;

    private RiteMusicController() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            reset();
            return;
        }

        ClientRiteState riteState = PlayerClientData.getActiveRiteState();
        boolean hasRite = !(riteState instanceof NoRiteState);
        boolean shouldOwnMusic = hasRite || outroQueued || outroStarted || isCurrentSoundActive();

        logSoundLifecycle();

        if (shouldOwnMusic && !musicOwnershipActive) {
            suppressVanillaMusic(minecraft);
            // AncientCurses.LOGGER.info("[RiteMusic] Took over music for riteId={}", hasRite ? riteState.getRiteId() : currentRiteId);
            musicOwnershipActive = true;
        } else if (!shouldOwnMusic) {
            musicOwnershipActive = false;
        }

        if (!hasRite) {
            handleNoActiveRite();
        } else {
            handleActiveRite(riteState);
        }

        previousRiteId = hasRite ? riteState.getRiteId() : "";
        previousRiteComplete = hasRite && riteState.isComplete();
    }

    public static void renderTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.isPaused()) {
            return;
        }

        ClientRiteState riteState = PlayerClientData.getActiveRiteState();
        if (hasReachedTrackStopPoint()) {
            // AncientCurses.LOGGER.info("[RiteMusic] Stop point reached for track={} at {:.2f}ms, ending track",
            //         currentTrack != null ? currentTrack.label() : "unknown",
            //         getElapsedTrackMs());
            stopCurrentSound();
            return;
        }

        if (hasReachedTrackHandoff()) {
            // AncientCurses.LOGGER.info("[RiteMusic] Handoff reached for track={} at {:.2f}ms, crossfading now",
            //         currentTrack != null ? currentTrack.label() : "unknown",
            //         getElapsedTrackMs());
            currentTrackHandoffTriggered = true;
            crossfadeToNextSection(riteState);
        }
    }

    public static void reset() {
        stopCurrentSound();
        currentTrack = null;
        currentRiteId = "";
        previousRiteId = "";
        previousRiteComplete = false;
        introPlayed = false;
        outroQueued = false;
        outroStarted = false;
        musicOwnershipActive = false;
        lastKnownSoundActive = false;
        currentTrackHandoffTriggered = false;
        lastCompletedDegrees = 0;
        lastActiveDegreeIndex = -1;
        lastProgressSnapshot = 0.0F;
        currentTrackStartedAtMs = 0.0D;
        nextLoopPhase = LoopPhase.FIRST_VERSE;
    }

    private static void handleNoActiveRite() {
        boolean turningInCompletedRite = previousRiteComplete && previousRiteId != null && !previousRiteId.isBlank();
        if (turningInCompletedRite) {
            currentRiteId = previousRiteId;
            // AncientCurses.LOGGER.info("[RiteMusic] Completed rite turned in, cutting current track and playing outro now for riteId={}", currentRiteId);
            stopCurrentSound();
            playSection(SongSection.OUTRO);
            outroQueued = false;
            outroStarted = true;
            return;
        } else if (!outroStarted) {
            reset();
            return;
        }

        if (!isCurrentSoundActive()) {
            if (outroQueued && !outroStarted) {
                playSection(SongSection.OUTRO);
                outroQueued = false;
                outroStarted = true;
                return;
            }

            if (outroStarted) {
                reset();
            }
        }
    }

    private static void handleActiveRite(ClientRiteState riteState) {
        String riteId = riteState.getRiteId();
        if (!riteId.equals(currentRiteId)) {
            startNewRite(riteState);
        }

        if (!isCurrentSoundActive()) {
            SongSection nextSection = chooseNextSection(riteState);
            playSection(nextSection);
        }

        lastCompletedDegrees = Math.max(0, riteState.getCompletedDegrees());
        lastActiveDegreeIndex = riteState.getActiveDegreeIndex();
        lastProgressSnapshot = Mth.clamp(riteState.getProgress(), 0.0F, 1.0F);
    }

    private static void startNewRite(ClientRiteState riteState) {
        stopCurrentSound();
        currentTrack = null;
        currentRiteId = riteState.getRiteId();
        introPlayed = false;
        outroQueued = false;
        outroStarted = false;
        lastCompletedDegrees = Math.max(0, riteState.getCompletedDegrees());
        lastActiveDegreeIndex = riteState.getActiveDegreeIndex();
        lastProgressSnapshot = Mth.clamp(riteState.getProgress(), 0.0F, 1.0F);
        nextLoopPhase = LoopPhase.FIRST_VERSE;
        // AncientCurses.LOGGER.info("[RiteMusic] Starting music controller for riteId={}, progress={}, completedDegrees={}, activeDegreeIndex={}",
        //         currentRiteId, lastProgressSnapshot, lastCompletedDegrees, lastActiveDegreeIndex);
    }

    private static SongSection chooseNextSection(ClientRiteState riteState) {
        if (!introPlayed) {
            introPlayed = true;
            return SongSection.INTRO;
        }

        if (outroQueued) {
            outroQueued = false;
            outroStarted = true;
            // AncientCurses.LOGGER.info("[RiteMusic] Playing queued outro for riteId={}", currentRiteId);
            return SongSection.OUTRO;
        }

        // AncientCurses.LOGGER.info("[RiteMusic] Choosing next section from fixed sequence for riteId={}, progress={} -> {}, completedDegrees={} -> {}, activeDegreeIndex={} -> {}",
        //         currentRiteId,
        //         lastProgressSnapshot,
        //         Mth.clamp(riteState.getProgress(), 0.0F, 1.0F),
        //         lastCompletedDegrees,
        //         riteState.getCompletedDegrees(),
        //         lastActiveDegreeIndex,
        //         riteState.getActiveDegreeIndex());
        return SongSection.LOOP;
    }

    private static void playSection(SongSection section) {
        playTrack(resolveTrack(section));
    }

    private static SectionTrack resolveTrack(SongSection section) {
        return switch (section) {
            case INTRO -> INTRO_SECTION;
            case LOOP -> nextLoopTrack();
            case OUTRO -> OUTRO_SECTION;
            case NONE -> null;
        };
    }

    private static void playTrack(SectionTrack track) {
        if (track == null) {
            return;
        }

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        currentTrack = track;
        currentTrackStartedAtMs = getCurrentMusicTimeMs();
        currentTrackHandoffTriggered = false;
        currentSound = new RiteMusicSoundInstance(track.sound().get());
        // AncientCurses.LOGGER.info("[RiteMusic] Playing track={} sound={} riteId={} handoffMs={} crossfadeMs={}",
        //         track.label(),
        //         track.sound().getId(),
        //         currentRiteId,
        //         track.handoffMs(),
        //         track.crossfadeMs());
        soundManager.play(currentSound);
    }

    private static void crossfadeToNextSection(ClientRiteState riteState) {
        if (currentTrack == OUTRO_SECTION) {
            return;
        }

        SectionTrack outgoingTrack = currentTrack;
        RiteMusicSoundInstance outgoingSound = currentSound;
        SectionTrack incomingTrack = resolveTrack(chooseNextSection(riteState));
        if (incomingTrack == null) {
            return;
        }

        // AncientCurses.LOGGER.info("[RiteMusic] Crossfading from track={} to track={} over {}ms",
        //         outgoingTrack != null ? outgoingTrack.label() : "unknown",
        //         incomingTrack.label(),
        //         outgoingTrack != null ? outgoingTrack.crossfadeMs() : 0L);

        if (outgoingSound != null && outgoingTrack != null) {
            outgoingSound.beginFadeOut(outgoingTrack.crossfadeMs());
        }

        playTrack(incomingTrack);
    }

    private static SectionTrack nextLoopTrack() {
        SectionTrack selectedTrack = switch (nextLoopPhase) {
            case FIRST_VERSE -> VERSE_1_SECTION;
            case CHORUS_1 -> CHORUS_1_SECTION;
            case VERSE_2 -> VERSE_2_SECTION;
            case BRIDGE_AFTER_VERSE_2, BRIDGE_AFTER_CHORUS_2 -> BRIDGE_SECTION;
            case CHORUS_2 -> CHORUS_2_SECTION;
        };

        nextLoopPhase = switch (nextLoopPhase) {
            case FIRST_VERSE -> LoopPhase.CHORUS_1;
            case CHORUS_1 -> LoopPhase.VERSE_2;
            case VERSE_2 -> LoopPhase.BRIDGE_AFTER_VERSE_2;
            case BRIDGE_AFTER_VERSE_2 -> LoopPhase.CHORUS_2;
            case CHORUS_2 -> LoopPhase.BRIDGE_AFTER_CHORUS_2;
            case BRIDGE_AFTER_CHORUS_2 -> LoopPhase.VERSE_2;
        };

        return selectedTrack;
    }

    private static boolean isCurrentSoundActive() {
        return currentSound != null && !currentSound.isStopped();
    }

    private static void stopCurrentSound() {
        if (currentSound != null) {
            // AncientCurses.LOGGER.info("[RiteMusic] Stopping current section track={} riteId={}",
            //         currentTrack != null ? currentTrack.label() : "unknown",
            //         currentRiteId);
            Minecraft.getInstance().getSoundManager().stop(currentSound);
            currentSound = null;
        }
    }

    private static void suppressVanillaMusic(Minecraft minecraft) {
        minecraft.getMusicManager().stopPlaying();
    }

    public static boolean shouldBlockVanillaMusic() {
        return musicOwnershipActive || outroQueued || outroStarted || currentSound != null;
    }

    public static boolean isRiteMusic(ResourceLocation soundId) {
        return soundId != null
                && AncientCurses.MOD_ID.equals(soundId.getNamespace())
                && soundId.getPath().startsWith("curseyou_");
    }

    private static void logSoundLifecycle() {
        boolean active = isCurrentSoundActive();
        if (active != lastKnownSoundActive) {
            // AncientCurses.LOGGER.info("[RiteMusic] Sound active changed: active={}, riteId={}, track={}",
            //         active,
            //         currentRiteId,
            //         currentTrack != null ? currentTrack.label() : "none");
            lastKnownSoundActive = active;
        }
    }

    private static boolean hasReachedTrackHandoff() {
        if (currentSound == null || currentTrack == null || currentTrackHandoffTriggered) {
            return false;
        }

        if (currentTrack.handoffMs() <= HANDOFF_ON_SOUND_END) {
            return false;
        }

        return getElapsedTrackMs() >= currentTrack.handoffMs();
    }

    private static boolean hasReachedTrackStopPoint() {
        if (currentSound == null || currentTrack == null) {
            return false;
        }

        if (currentTrack.maxPlayMs() <= HANDOFF_ON_SOUND_END) {
            return false;
        }

        return getElapsedTrackMs() >= currentTrack.maxPlayMs();
    }

    private static double getCurrentMusicTimeMs() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0.0D;
        }

        double partialTick = minecraft.isPaused() ? 0.0D : minecraft.getFrameTime();
        return (minecraft.level.getGameTime() + partialTick) * 50.0D;
    }

    private static double getElapsedTrackMs() {
        return Math.max(0.0D, getCurrentMusicTimeMs() - currentTrackStartedAtMs);
    }

    private record SectionTrack(String label, RegistryObject<SoundEvent> sound, long handoffMs, long crossfadeMs, long maxPlayMs) {
    }

    private enum LoopPhase {
        FIRST_VERSE,
        CHORUS_1,
        VERSE_2,
        BRIDGE_AFTER_VERSE_2,
        CHORUS_2,
        BRIDGE_AFTER_CHORUS_2
    }

    private enum SongSection {
        NONE,
        INTRO,
        LOOP,
        OUTRO
    }
}
