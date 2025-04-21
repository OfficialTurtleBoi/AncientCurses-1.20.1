package net.turtleboi.ancientcurses.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;

public class TrialsCompletedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("curses")
                .then(Commands.literal("trialcompleted")
                        .then(Commands.argument("playerName", StringArgumentType.string())
                                .executes(TrialsCompletedCommand::sendCompletedTrials))));
    }

    private static int sendCompletedTrials(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "playerName");
        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null) {
            source.sendFailure(Component.literal("Target not found: " + playerName));
            return 0;
        }

        int trialsCompleted = player.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(PlayerRiteDataCapability::getPlayerRitesCompleted)
                .orElse(0);

        source.sendSuccess(() -> Component.literal("Player has completed: " + trialsCompleted + " trials"), true);
        return 1;
    }
}
