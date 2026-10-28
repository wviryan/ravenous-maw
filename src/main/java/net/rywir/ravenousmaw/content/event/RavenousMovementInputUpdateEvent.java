package net.rywir.ravenousmaw.content.event;

import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.rywir.ravenousmaw.registry.RavenousMobEffects;

public class RavenousMovementInputUpdateEvent {
    @SubscribeEvent
    public static void onMovementInputUpdateEvent(MovementInputUpdateEvent event) {
        Player player = event.getEntity();

        if (!(player instanceof LocalPlayer localPlayer)) {
            return;
        }

        boolean infected = localPlayer.hasEffect(RavenousMobEffects.SYMBIOTIC_INFECTION);

        // The move vector is protected in 26.1, so it cannot be negated from the event. The input is
        // swapped for a wrapper that negates it, and swapped back once the infection ends.
        if (infected && !(localPlayer.input instanceof InvertedInput)) {
            localPlayer.input = new InvertedInput(localPlayer.input);
        } else if (!infected && localPlayer.input instanceof InvertedInput inverted) {
            localPlayer.input = inverted.delegate;
        }
    }

    /**
     * Wraps the player's real input and reverses forward/backward and left/right movement.
     */
    private static class InvertedInput extends ClientInput {
        private final ClientInput delegate;

        private InvertedInput(ClientInput delegate) {
            this.delegate = delegate;
            this.keyPresses = delegate.keyPresses;
            this.moveVector = invert(delegate.getMoveVector());
        }

        @Override
        public void tick() {
            delegate.tick();
            this.keyPresses = delegate.keyPresses;
            this.moveVector = invert(delegate.getMoveVector());
        }

        private static Vec2 invert(Vec2 vector) {
            return new Vec2(-vector.x, -vector.y);
        }
    }
}
