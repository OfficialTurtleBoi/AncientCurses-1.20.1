package net.turtleboi.ancientcurses.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialDataCapability;
import net.turtleboi.ancientcurses.capabilities.trials.PlayerTrialProvider;
import net.turtleboi.ancientcurses.effect.CurseRegistry;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.trials.SyncTrialDataS2C;

public class ClearCurseCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("curses")
                .then(Commands.literal("clearcurses")
                        .then(Commands.argument("playerName", StringArgumentType.string())
                                .executes(ClearCurseCommand::clearCurses))));
    }

    private static int clearCurses(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "playerName");
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null) {
            source.sendFailure(Component.literal("Target not found: " + playerName));
            return 0;
        }

        player.getCapability(PlayerTrialProvider.PLAYER_TRIAL_DATA).ifPresent(PlayerTrialDataCapability::clearPlayerCurse);
        ModNetworking.sendToPlayer(
                new SyncTrialDataS2C(
                        "None",
                        false,
                        "",
                        0,
                        0,
                        0,
                        0,
                        0,
                        "",
                        0,
                        0),
                player
        );

        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            if (CurseRegistry.getCurses().contains(effect)) {
                player.removeEffect(effect);
            }
        }

        source.sendSuccess(() -> Component.literal("Curses cleared for player: " + playerName), true);
        return 1;
    }
}
