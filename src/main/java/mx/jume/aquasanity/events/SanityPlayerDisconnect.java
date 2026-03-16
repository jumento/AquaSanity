package mx.jume.aquasanity.events;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import mx.jume.aquasanity.ui.SanityHud;
import mx.jume.aquasanity.ui.hud.InternalMultipleHUD;

public class SanityPlayerDisconnect {
    public static void handle(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef == null)
            return;
        SanityHud.removeHud(playerRef);
        InternalMultipleHUD.removePlayer(playerRef);
    }
}
