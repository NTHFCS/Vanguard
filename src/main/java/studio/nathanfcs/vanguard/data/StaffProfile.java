package studio.nathanfcs.vanguard.data;

import java.util.UUID;

public final class StaffProfile {

    private final UUID uuid;
    private String name;
    private long totalSeconds;
    private int moderationActions;

    public StaffProfile(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(long totalSeconds) {
        this.totalSeconds = totalSeconds;
    }

    public void addSeconds(long seconds) {
        if (seconds > 0) {
            this.totalSeconds += seconds;
        }
    }

    public int getModerationActions() {
        return moderationActions;
    }

    public void setModerationActions(int moderationActions) {
        this.moderationActions = moderationActions;
    }

    public void addModerationAction() {
        this.moderationActions++;
    }
}