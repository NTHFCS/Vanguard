package studio.nathanfcs.vanguard.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import studio.nathanfcs.vanguard.VanguardPlugin;

public final class ActivityListener implements Listener {

    private final VanguardPlugin plugin;

    public ActivityListener(VanguardPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("vanguard.staff")) continue;
                if (plugin.getSessionManager().isAfk(player.getUniqueId())) {
                    player.sendActionBar(
                            Component.text("⏸ Tracking Paused", TextColor.color(0xC2A56D))
                                    .append(Component.text("  —  Movement or /afk will resume tracking", NamedTextColor.GRAY))
                    );
                }
            }
        }, 20L, 40L);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission("vanguard.staff")) return;
        if (!plugin.getSessionManager().isAfk(event.getPlayer().getUniqueId())) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getSessionManager().resumeTracking(event.getPlayer());
            plugin.msg(event.getPlayer(), "&8[&6Vanguard&8]&r &aTracking resumed.");
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.getPlayer().hasPermission("vanguard.staff")) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (!plugin.getSessionManager().isAfk(event.getPlayer().getUniqueId())) return;

        plugin.getSessionManager().resumeTracking(event.getPlayer());
        plugin.msg(event.getPlayer(), "&8[&6Vanguard&8]&r &aTracking resumed.");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().hasPermission("vanguard.staff")) return;

        String raw = event.getMessage().toLowerCase();
        String withoutSlash = raw.startsWith("/") ? raw.substring(1) : raw;
        String root = withoutSlash.split(" ")[0];

        if (root.contains(":")) {
            root = root.substring(root.indexOf(':') + 1);
        }

        if (plugin.getSessionManager().isAfk(event.getPlayer().getUniqueId()) && !root.equals("afk")) {
            plugin.getSessionManager().resumeTracking(event.getPlayer());
            plugin.msg(event.getPlayer(), "&8[&6Vanguard&8]&r &aTracking resumed.");
        }

        String remainder = "";
        int firstSpace = withoutSlash.indexOf(' ');
        if (firstSpace >= 0 && firstSpace + 1 < withoutSlash.length()) {
            remainder = withoutSlash.substring(firstSpace + 1).trim();
        }

        if (plugin.isModerationCommand(root) && !remainder.isBlank()) {
            plugin.getSessionManager().recordModerationAction(event.getPlayer());
        }
    }
}