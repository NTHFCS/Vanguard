package studio.nathanfcs.vanguard.data;

import org.bukkit.configuration.file.YamlConfiguration;
import studio.nathanfcs.vanguard.VanguardPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class DataStore {

    private final VanguardPlugin plugin;
    private final File file;

    public DataStore(VanguardPlugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void loadInto(Map<UUID, StaffProfile> profiles) {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.isConfigurationSection("profiles")) {
            return;
        }

        for (String key : yaml.getConfigurationSection("profiles").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String base = "profiles." + key + ".";
                String name = yaml.getString(base + "name", "Unknown");
                long totalSeconds = yaml.getLong(base + "total-seconds", 0L);
                int actions = yaml.getInt(base + "moderation-actions", 0);

                StaffProfile profile = new StaffProfile(uuid, name);
                profile.setTotalSeconds(totalSeconds);
                profile.setModerationActions(actions);

                profiles.put(uuid, profile);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveAll(Collection<StaffProfile> profiles) {
        YamlConfiguration yaml = new YamlConfiguration();

        for (StaffProfile profile : profiles) {
            String base = "profiles." + profile.getUuid() + ".";
            yaml.set(base + "name", profile.getName());
            yaml.set(base + "total-seconds", profile.getTotalSeconds());
            yaml.set(base + "moderation-actions", profile.getModerationActions());
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data.yml: " + e.getMessage());
        }
    }
}