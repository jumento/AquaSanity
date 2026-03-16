package mx.jume.aquasanity.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.player.SetGameMode;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.components.SanityComponent;
import mx.jume.aquasanity.ui.SanityHud;

public class GameModePacketWatcher implements PlayerPacketWatcher {
    @Override
    public void accept(PlayerRef playerRef, Packet packet) {
        if (!(packet instanceof SetGameMode setGameMode))
            return;

        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null)
            return;
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null)
            return;

        world.execute(() -> {
            GameMode gameMode = setGameMode.gameMode;
            
            // Update HUD
            SanityHud.updatePlayerGameMode(playerRef, gameMode);
            
            // If creative, restore sanity to 100
            if (gameMode == GameMode.Creative) {
                SanityComponent sanity = store.getComponent(ref, SanityComponent.getComponentType());
                if (sanity != null) {
                    sanity.setSanityLevel(SanityComponent.maxSanityLevel);
                }
            }
        });
    }
}
