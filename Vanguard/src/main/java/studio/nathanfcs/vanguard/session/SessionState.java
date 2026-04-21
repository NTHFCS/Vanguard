package studio.nathanfcs.vanguard.session;

public final class SessionState {

    private long startedAtEpoch;
    private boolean afk;

    public SessionState(long startedAtEpoch, boolean afk) {
        this.startedAtEpoch = startedAtEpoch;
        this.afk = afk;
    }

    public long getStartedAtEpoch() {
        return startedAtEpoch;
    }

    public void setStartedAtEpoch(long startedAtEpoch) {
        this.startedAtEpoch = startedAtEpoch;
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
    }
}