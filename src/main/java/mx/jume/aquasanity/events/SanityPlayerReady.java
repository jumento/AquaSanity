package mx.jume.aquasanity.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.ui.SanityHud;

public class SanityPlayerReady {
    public static void handle(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        if (player == null)
            return;
        Ref<EntityStore> ref = event.getPlayerRef();
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null)
            return;

        world.execute(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null)
                return;
            if (SanityHud.hasHud(playerRef))
                return;
            SanityHud.createPlayerHud(store, ref, playerRef, player);
        });
    }
}
