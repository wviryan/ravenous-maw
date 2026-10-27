package net.rywir.ravenousmaw.content.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class MawTickData {
    public static final Codec<MawTickData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("ravenous_tick_counter").forGetter(MawTickData::getTickCounter)
        ).apply(instance, MawTickData::new)
    );

    private int tickCounter;

    // Not saved: only used to tell whether this game tick was already counted.
    private long lastGameTime = -1;
    private boolean resetPending;

    public MawTickData() {
        this.tickCounter = 0;
    }

    public MawTickData(int tickCounter) {
        this.tickCounter = tickCounter;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    /** Debug only (/ravenousmaw tick): jumps the counter to {@code ticks} and drops any pending reset. */
    public void setTickCounter(int ticks) {
        this.tickCounter = ticks;
        this.resetPending = false;
    }

    /**
     * Advances the counter once per game tick, however many Maw stacks call it.
     * inventoryTick runs once per stack, so every stack in the same tick must see the same value.
     * A pending reset is applied here, on the next tick, so it never lands in the middle of a tick.
     */
    public void advance(long gameTime) {
        if (gameTime == lastGameTime) return;
        lastGameTime = gameTime;

        if (resetPending) {
            tickCounter = 0;
            resetPending = false;
        }
        tickCounter++;
    }

    /** Restarts the counter on the next {@link #advance(long)}, keeping the current value for this tick. */
    public void resetTicks() {
        resetPending = true;
    }
}
