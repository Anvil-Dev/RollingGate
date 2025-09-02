package dev.anvilcraft.rg.tools;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.anvilcraft.rg.RollingGate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ModCommands {
    public static @NotNull LiteralArgumentBuilder<CommandSourceStack> root(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, String string) {
        String prefix = "";
        if (dispatcher.getRoot().getChild(string) != null) prefix = "rg";
        return Commands.literal(prefix + string);
    }

    public static @NotNull LiteralArgumentBuilder<CommandSourceStack> root(@NotNull CommandDispatcher<CommandSourceStack> dispatcher, String string, String prefix) {
        String _prefix = "";
        if (dispatcher.getRoot().getChild(string) != null) _prefix = prefix;
        return Commands.literal(_prefix + string);
    }

    public static void notifyPlayersCommandsChanged(MinecraftServer server) {
        if (server == null) return;
        server.schedule(new TickTask(server.getTickCount(), () ->
        {
            try {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    server.getCommands().sendCommands(player);
                }
            } catch (NullPointerException e) {
                RollingGate.LOGGER.warn("Exception while refreshing commands, please report this to RollingGate", e);
            }
        }));
    }
}
