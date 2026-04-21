package studio.nathanfcs.vanguard.session;

import org.bukkit.entity.Player;
import studio.nathanfcs.vanguard.VanguardPlugin;
import studio.nathanfcs.vanguard.data.StaffProfile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager {

    private final VanguardPlugin plugin;
    private final Map<UUID, SessionState> sessions = new ConcurrentHashMap<>();

    public SessionManager(VanguardPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTracking(Player player) {
        if (!player.hasPermission("vanguard.staff")) return;

        UUID uuid = player.getUniqueId();
        StaffProfile profile = plugin.getOrCreateProfile(uuid, player.getName());
        profile.setName(player.getName());

        sessions.put(uuid, new SessionState(now(), false));
    }

    public void pauseTracking(Player player) {
        if (!player.hasPermission("vanguard.staff")) return;

        UUID uuid = player.getUniqueId();
        StaffProfile profile = plugin.getOrCreateProfile(uuid, player.getName());
        profile.setName(player.getName());

        SessionState state = sessions.get(uuid);
        if (state != null && !state.isAfk()) {
            long elapsed = Math.max(0, now() - state.getStartedAtEpoch());
            profile.addSeconds(elapsed);
        }

        sessions.put(uuid, new SessionState(now(), true));
        plugin.saveProfiles();
    }

    public void resumeTracking(Player player) {
        if (!player.hasPermission("vanguard.staff")) return;

        UUID uuid = player.getUniqueId();
        StaffProfile profile = plugin.getOrCreateProfile(uuid, player.getName());
        profile.setName(player.getName());

        SessionState state = sessions.get(uuid);
        if (state == null || !state.isAfk()) return;

        sessions.put(uuid, new SessionState(now(), false));
    }

    public void stopTracking(Player player) {
        if (!player.hasPermission("vanguard.staff")) return;

        UUID uuid = player.getUniqueId();
        StaffProfile profile = plugin.getOrCreateProfile(uuid, player.getName());
        profile.setName(player.getName());

        SessionState state = sessions.remove(uuid);
        if (state != null && !state.isAfk()) {
            long elapsed = Math.max(0, now() - state.getStartedAtEpoch());
            profile.addSeconds(elapsed);
        }

        plugin.saveProfiles();
    }

    public void clearSession(UUID uuid) {
        sessions.remove(uuid);
    }

    public void clearAllSessions() {
        sessions.clear();
    }

    public boolean isAfk(UUID uuid) {
        SessionState state = sessions.get(uuid);
        return state != null && state.isAfk();
    }

    public long getCurrentSessionSeconds(UUID uuid) {
        SessionState state = sessions.get(uuid);
        if (state == null || state.isAfk()) return 0L;
        return Math.max(0, now() - state.getStartedAtEpoch());
    }

    public long getEffectiveTotalSeconds(UUID uuid) {
        StaffProfile profile = plugin.getProfiles().get(uuid);
        long total = profile != null ? profile.getTotalSeconds() : 0L;
        return total + getCurrentSessionSeconds(uuid);
    }

    public String getStatus(UUID uuid, boolean online) {
        if (!online) return "&cOFFLINE";
        SessionState state = sessions.get(uuid);
        if (state == null) return "&7IDLE";
        if (state.isAfk()) return "&eAFK";
        return "&aACTIVE";
    }

    public void recordModerationAction(Player player) {
        if (!player.hasPermission("vanguard.staff")) return;

        StaffProfile profile = plugin.getOrCreateProfile(player.getUniqueId(), player.getName());
        profile.setName(player.getName());
        profile.addModerationAction();
        plugin.saveProfiles();
    }

    private long now() {
        return Instant.now().getEpochSecond();
    }
}