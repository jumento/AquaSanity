
package mx.jume.aquasanity.events;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.*;
import com.hypixel.hytale.server.core.event.events.ecs.*;
import com.hypixel.hytale.server.core.event.events.entity.*;
import com.hypixel.hytale.event.IEvent;

public class DiagnosticListener {
    public static void register(EventRegistry registry) {
        
        try {
            registry.registerGlobal(EventPriority.FIRST, PlayerInteractEvent.class, DiagnosticListener::onInteract);
            registry.registerGlobal(EventPriority.FIRST, PlayerReadyEvent.class, DiagnosticListener::onReady);
        } catch (Exception e) {
        }
    }

    private static void onInteract(PlayerInteractEvent e) {
    }

    private static void onReady(PlayerReadyEvent e) {
    }
}
