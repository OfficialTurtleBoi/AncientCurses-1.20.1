package net.turtleboi.ancientcurses.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteDataCapability;
import net.turtleboi.ancientcurses.capabilities.rites.PlayerRiteProvider;
import net.turtleboi.ancientcurses.effect.ModEffects;
import net.turtleboi.ancientcurses.network.ModNetworking;
import net.turtleboi.ancientcurses.network.packets.rites.SyncRiteDataS2C;
import net.turtleboi.ancientcurses.rite.Rite;
import net.turtleboi.ancientcurses.rite.util.RiteLocator;

public class ClearCurseCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("curses")
                .then(Commands.literal("clearcurses")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ClearCurseCommand::clearCurses))));
    }

    private static int clearCurses(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        var activeAltar = RiteLocator.findActiveAltar(target);
        if (activeAltar != null) {
            Rite activeRite = activeAltar.getPlayerRite(target.getUUID());
            if (activeRite != null) {
                activeRite.onPlayerDeath(target);
            }
            if (activeRite == null || !activeRite.hasPendingAltarWork()) {
                activeAltar.removePlayerFromRite(target);
            }
        }
        target.getCapability(PlayerRiteProvider.PLAYER_RITE_DATA).ifPresent(PlayerRiteDataCapability::clearPlayerCurse);
        ModNetworking.sendToPlayer(SyncRiteDataS2C.none(), target);

        for (MobEffectInstance effectInstance : target.getActiveEffects()) {
            MobEffect effect = effectInstance.getEffect();
            if (ModEffects.isCurseEffect(effect)) {
                target.removeEffect(effect);
            }
        }

        String name = target.getGameProfile().getName();
        source.sendSuccess(() -> Component.literal("Curses cleared for player: " + name), true);
        return 1;
    }
}
