package studio.nathanfcs.vanguard.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import studio.nathanfcs.vanguard.VanguardPlugin;
import studio.nathanfcs.vanguard.data.StaffProfile;

import java.util.*;
import java.util.stream.Collectors;

public final class VanguardCommand implements CommandExecutor, TabCompleter {

    private final VanguardPlugin plugin;
    private final Map<String, PendingReset> pendingResets = new HashMap<>();
    private static final long RESET_CONFIRM_WINDOW_MS = 10_000L;

    public VanguardCommand(VanguardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanguard.staff")) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            sendStats(sender, args);
            return true;
        }

        if (args[0].equalsIgnoreCase("top")) {
            sendTop(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            handleReset(sender, args);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        plugin.msg(sender, "");
        plugin.msg(sender, " &6&lVanguard &8— &7Staff Activity Tracker");
        plugin.msg(sender, "");
        plugin.msg(sender, " &8▸ &f/vanguard stats &e[player] &8— &7View performance data");
        plugin.msg(sender, " &8▸ &f/vanguard top &8— &7Staff leaderboard");
        plugin.msg(sender, " &8▸ &f/afk &8— &7Toggle tracking pause");

        if (sender.hasPermission("vanguard.admin")) {
            plugin.msg(sender, " &8▸ &f/vanguard reset &e<player> &8— &7Clear one profile");
            plugin.msg(sender, " &8▸ &f/vanguard reset &eall &8— &7Clear all tracked profiles");
        }

        plugin.msg(sender, "");
        plugin.msg(sender, " &8Developed by &6NathanFCS Studio &8(&72026&8)");
        plugin.msg(sender, " &8v" + plugin.getDescription().getVersion() + " &8| &7github.com/NTHFCS");
        plugin.msg(sender, "");
    }

    private void sendStats(CommandSender sender, String[] args) {
        OfflinePlayer target;

        if (args.length >= 2) {
            target = Bukkit.getOfflinePlayer(args[1]);
        } else {
            if (!(sender instanceof Player player)) {
                plugin.msg(sender, "&8[&6Vanguard&8]&r &cSpecify a player name.");
                return;
            }
            target = player;
        }

        StaffProfile profile = plugin.getProfiles().get(target.getUniqueId());
        if (profile == null && !target.isOnline()) {
            plugin.msg(sender, "&8[&6Vanguard&8]&r &cNo data found for &f" + target.getName() + "&c.");
            return;
        }

        String name = profile != null ? profile.getName() : target.getName();
        long total = plugin.getSessionManager().getEffectiveTotalSeconds(target.getUniqueId());
        long current = plugin.getSessionManager().getCurrentSessionSeconds(target.getUniqueId());
        int actions = profile != null ? profile.getModerationActions() : 0;
        String status = plugin.getSessionManager().getStatus(target.getUniqueId(), target.isOnline());

        plugin.msg(sender, "");
        plugin.msg(sender, " &6&lVanguard &8— &f" + name);
        plugin.msg(sender, "");
        plugin.msg(sender, " &8▸ &7Status: " + status);
        plugin.msg(sender, " &8▸ &7Active Time: &f" + plugin.formatDuration(total));
        plugin.msg(sender, " &8▸ &7Session: &f" + plugin.formatDuration(current));
        plugin.msg(sender, " &8▸ &7Actions: &f" + actions);
        plugin.msg(sender, "");
    }

    private void sendTop(CommandSender sender) {
        List<StaffProfile> sorted = plugin.getProfiles().values().stream()
                .sorted((a, b) -> Long.compare(
                        plugin.getSessionManager().getEffectiveTotalSeconds(b.getUuid()),
                        plugin.getSessionManager().getEffectiveTotalSeconds(a.getUuid())
                ))
                .limit(5)
                .collect(Collectors.toList());

        plugin.msg(sender, "");
        plugin.msg(sender, " &6&lVanguard &8— &7Leaderboard");
        plugin.msg(sender, "");

        if (sorted.isEmpty()) {
            plugin.msg(sender, " &7No tracked data yet.");
        } else {
            int index = 1;
            for (StaffProfile profile : sorted) {
                long total = plugin.getSessionManager().getEffectiveTotalSeconds(profile.getUuid());
                plugin.msg(sender, " &8#" + index + " &f" + profile.getName()
                        + " &8— &a" + plugin.formatDuration(total)
                        + " &8| &7" + profile.getModerationActions() + " actions");
                index++;
            }
        }

        plugin.msg(sender, "");
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("vanguard.admin")) return;

        if (args.length < 2) {
            plugin.msg(sender, "&8[&6Vanguard&8]&r &7Usage: &f/vanguard reset <player|all>");
            return;
        }

        String actorKey = getActorKey(sender);
        purgeExpired(actorKey);

        if (args[1].equalsIgnoreCase("all")) {
            String resetKey = "all";

            if (isConfirmed(actorKey, resetKey)) {
                executeResetAll(sender);
                pendingResets.remove(actorKey);
                return;
            }

            pendingResets.put(actorKey, new PendingReset(resetKey, System.currentTimeMillis() + RESET_CONFIRM_WINDOW_MS));
            plugin.msg(sender, "");
            plugin.msg(sender, " &6&lVanguard Reset");
            plugin.msg(sender, " &8&m                                    ");
            plugin.msg(sender, " &7You are about to clear &fall tracked staff data&7.");
            plugin.msg(sender, " &7This includes active time, current session, and moderation actions.");
            plugin.msg(sender, " &7To confirm, repeat &f/vg reset all &7within &f10 seconds&7.");
            plugin.msg(sender, " &8&m                                    ");
            plugin.msg(sender, "");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        StaffProfile profile = plugin.getProfiles().get(uuid);
        if (profile == null && !target.isOnline()) {
            plugin.msg(sender, "&8[&6Vanguard&8]&r &cNo tracked data found for &f" + args[1] + "&c.");
            return;
        }

        String resetKey = "player:" + uuid;
        String displayName = target.getName() != null ? target.getName() : args[1];

        if (isConfirmed(actorKey, resetKey)) {
            executeResetPlayer(sender, target, displayName);
            pendingResets.remove(actorKey);
            return;
        }

        pendingResets.put(actorKey, new PendingReset(resetKey, System.currentTimeMillis() + RESET_CONFIRM_WINDOW_MS));
        plugin.msg(sender, "");
        plugin.msg(sender, " &6&lVanguard Reset");
        plugin.msg(sender, " &8&m                                    ");
        plugin.msg(sender, " &7You are about to clear all tracked data for &f" + displayName + "&7.");
        plugin.msg(sender, " &7This includes active time, current session, and moderation actions.");
        plugin.msg(sender, " &7To confirm, repeat &f/vg reset " + displayName + " &7within &f10 seconds&7.");
        plugin.msg(sender, " &8&m                                    ");
        plugin.msg(sender, "");
    }

    private void executeResetPlayer(CommandSender sender, OfflinePlayer target, String displayName) {
        StaffProfile profile = plugin.getOrCreateProfile(target.getUniqueId(), displayName);
        profile.setName(displayName);
        profile.setTotalSeconds(0L);
        profile.setModerationActions(0);

        plugin.getSessionManager().clearSession(target.getUniqueId());

        if (target.isOnline() && target.getPlayer() != null && target.getPlayer().hasPermission("vanguard.staff")) {
            plugin.getSessionManager().startTracking(target.getPlayer());
        }

        plugin.saveProfiles();

        plugin.msg(sender, "");
        plugin.msg(sender, " &6&lVanguard Reset");
        plugin.msg(sender, " &8&m                                    ");
        plugin.msg(sender, " &7Tracked data for &f" + displayName + " &7has been cleared.");
        plugin.msg(sender, " &8&m                                    ");
        plugin.msg(sender, "");
    }

    private void executeResetAll(CommandSender sender) {
        for (StaffProfile profile : plugin.getProfiles().values()) {
            profile.setTotalSeconds(0L);
            profile.setModerationActions(0);
        }

        plugin.getSessionManager().clearAllSessions();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("vanguard.staff")) {
                plugin.getSessionManager().startTracking(online);
            }
        }

        plugin.saveProfiles();

        plugin.msg(sender, "");
        plugin.msg(sender, " &6&lVanguard Reset");
        plugin.msg(sender, " &8&m                                    ");
        plugin.msg(sender, " &7All tracked staff data has been cleared.");
        plugin.msg(sender, " &8&m                                    ");
        plugin.msg(sender, "");
    }

    private boolean isConfirmed(String actorKey, String resetKey) {
        PendingReset pending = pendingResets.get(actorKey);
        return pending != null
                && pending.resetKey.equals(resetKey)
                && pending.expiresAt >= System.currentTimeMillis();
    }

    private void purgeExpired(String actorKey) {
        PendingReset pending = pendingResets.get(actorKey);
        if (pending != null && pending.expiresAt < System.currentTimeMillis()) {
            pendingResets.remove(actorKey);
        }
    }

    private String getActorKey(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.getUniqueId().toString();
        }
        return "CONSOLE";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("vanguard.staff")) return Collections.emptyList();

        if (args.length == 1) {
            List<String> base = new ArrayList<>(List.of("help", "stats", "top"));
            if (sender.hasPermission("vanguard.admin")) {
                base.add("reset");
            }
            return base.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("vanguard.admin")) {
            List<String> options = new ArrayList<>();
            options.add("all");
            Bukkit.getOnlinePlayers().forEach(p -> options.add(p.getName()));

            return options.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .distinct()
                    .toList();
        }

        return Collections.emptyList();
    }

    private static final class PendingReset {
        private final String resetKey;
        private final long expiresAt;

        private PendingReset(String resetKey, long expiresAt) {
            this.resetKey = resetKey;
            this.expiresAt = expiresAt;
        }
    }
}