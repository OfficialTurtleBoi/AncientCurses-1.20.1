package net.turtleboi.ancientcurses.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;

public class RiteCompletedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("curses")
                .then(Commands.literal("ritescompleted")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(RiteCompletedCommand::sendCompletedTrials))));
    }

    private static int sendCompletedTrials(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        int ritesCompleted = target.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA)
                .map(PlayerRiteDataCapability::getPlayerRitesCompleted)
                .orElse(0);

        source.sendSuccess(() -> Component.literal("Player has completed: " + ritesCompleted + " rites"), true);
        return 1;
    }
}
