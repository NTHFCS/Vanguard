package studio.nathanfcs.vanguard.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import studio.nathanfcs.vanguard.VanguardPlugin;

public final class AfkCommand implements CommandExecutor {

    private final VanguardPlugin plugin;

    public AfkCommand(VanguardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!player.hasPermission("vanguard.staff")) return true;

        if (plugin.getSessionManager().isAfk(player.getUniqueId())) {
            plugin.getSessionManager().resumeTracking(player);
            plugin.msg(player, "&8[&6Vanguard&8]&r &aTracking resumed.");
        } else {
            plugin.getSessionManager().pauseTracking(player);
            plugin.msg(player, "&8[&6Vanguard&8]&r &eTracking paused.");
        }

        return true;
    }
}