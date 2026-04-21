package studio.nathanfcs.vanguard.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import studio.nathanfcs.vanguard.VanguardPlugin;

public final class JoinQuitListener implements Listener {

    private final VanguardPlugin plugin;

    public JoinQuitListener(VanguardPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().hasPermission("vanguard.staff")) {
            plugin.getSessionManager().startTracking(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().hasPermission("vanguard.staff")) {
            plugin.getSessionManager().stopTracking(event.getPlayer());
        }
    }
}