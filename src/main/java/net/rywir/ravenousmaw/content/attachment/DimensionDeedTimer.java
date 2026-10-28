package net.rywir.ravenousmaw.content.attachment;

/**
 * Attached to a player: ticks left before the dimension they stand in grants its assimilation.
 * Not serialized on purpose; it is rebuilt from the player's dimension (see RavenousDimensionDeedEvent).
 */
public class DimensionDeedTimer {
    public static final int NOT_RUNNING = -1;

    private int ticksLeft = NOT_RUNNING;

    public boolean isRunning() {
        return ticksLeft != NOT_RUNNING;
    }

    public void start(int ticks) {
        this.ticksLeft = ticks;
    }

    public void cancel() {
        this.ticksLeft = NOT_RUNNING;
    }

    /** @return true on the tick the countdown reaches zero (the timer then stops) */
    public boolean tick() {
        if (ticksLeft == NOT_RUNNING) return false;

        if (--ticksLeft > 0) return false;

        cancel();
        return true;
    }
}
