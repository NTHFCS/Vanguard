package studio.nathanfcs.vanguard;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import studio.nathanfcs.vanguard.command.AfkCommand;
import studio.nathanfcs.vanguard.command.VanguardCommand;
import studio.nathanfcs.vanguard.data.DataStore;
import studio.nathanfcs.vanguard.data.StaffProfile;
import studio.nathanfcs.vanguard.listener.ActivityListener;
import studio.nathanfcs.vanguard.listener.JoinQuitListener;
import studio.nathanfcs.vanguard.session.SessionManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class VanguardPlugin extends JavaPlugin {

    private final Map<UUID, StaffProfile> profiles = new ConcurrentHashMap<>();
    private DataStore dataStore;
    private SessionManager sessionManager;

    private static final Set<String> MODERATION_ROOTS = Set.of(
            "ban", "tempban", "kick", "mute", "tempmute", "warn", "unmute", "freeze", "jail"
    );

    @Override
    public void onEnable() {
        this.dataStore = new DataStore(this);
        this.dataStore.loadInto(profiles);
        this.sessionManager = new SessionManager(this);

        Objects.requireNonNull(getCommand("vanguard")).setExecutor(new VanguardCommand(this));
        Objects.requireNonNull(getCommand("vanguard")).setTabCompleter(new VanguardCommand(this));
        Objects.requireNonNull(getCommand("afk")).setExecutor(new AfkCommand(this));

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new ActivityListener(this), this);

        for (Player player : getServer().getOnlinePlayers()) {
            if (player.hasPermission("vanguard.staff")) {
                sessionManager.startTracking(player);
            }
        }

        getLogger().info("Vanguard v" + getDescription().getVersion() + " — Staff tracking initialized.");
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            if (player.hasPermission("vanguard.staff")) {
                sessionManager.stopTracking(player);
            }
        }
        dataStore.saveAll(profiles.values());
        getLogger().info("Vanguard v" + getDescription().getVersion() + " — Data saved. Shutting down.");
    }

    public StaffProfile getOrCreateProfile(UUID uuid, String name) {
        return profiles.computeIfAbsent(uuid, ignored -> new StaffProfile(uuid, name));
    }

    public Map<UUID, StaffProfile> getProfiles() {
        return profiles;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public boolean isModerationCommand(String root) {
        return MODERATION_ROOTS.contains(root);
    }

    public void saveProfiles() {
        dataStore.saveAll(profiles.values());
    }

    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void msg(CommandSender sender, String text) {
        sender.sendMessage(color(text));
    }

    public String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) return "0s";

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        List<String> parts = new ArrayList<>();
        if (days > 0) parts.add(days + "d");
        if (hours > 0) parts.add(hours + "h");
        if (minutes > 0) parts.add(minutes + "m");
        if (seconds > 0 && parts.size() < 3) parts.add(seconds + "s");

        return String.join(" ", parts);
    }
}