package net.turtleboi.ancientcurses.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.turtleboi.ancientcurses.trials.PlayerTrialData;

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
        PlayerTrialData.clearPlayerCurse(player);
        source.sendSuccess(() -> Component.literal("Curses cleared for player: " + playerName), true);
        return 1;
    }
}