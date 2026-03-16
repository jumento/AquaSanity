
package mx.jume.aquasanity.events;

import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.math.vector.Vector3d;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.components.SanityComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class SanityFeedingListener {
    
    public static void handle(LivingEntityInventoryChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID playerUuid = player.getUuid();

        Transaction transaction = event.getTransaction();
        if (transaction == null || !transaction.succeeded()) return;

        // Filtro de Drop/Move: Si la transacción es de tipo movimiento, no es alimentación.
        String transInfo = transaction.toString();
        if (transInfo.contains("MoveTransaction")) {
            return;
        }

        ItemStack itemBefore = null;
        ItemStack itemAfter = null;
        
        if (transaction instanceof SlotTransaction) {
            SlotTransaction st = (SlotTransaction) transaction;
            itemBefore = st.getSlotBefore();
            itemAfter = st.getSlotAfter();
        } else {
            return;
        }

        if (itemBefore == null || itemBefore.getItemId() == null) return;

        // FILTRO CRÍTICO: Evitar que el daño de armadura restaure cordura.
        // El daño de armadura dispara un evento de inventario donde el ID es el mismo
        // y la CANTIDAD es la misma (1), pero cambia la durabilidad.
        // El feeding SIEMPRE consume el ítem (cantidad disminuye).
        if (itemAfter != null) {
            if (itemAfter.getItemId().equals(itemBefore.getItemId()) && 
                itemAfter.getQuantity() >= itemBefore.getQuantity()) {
                return; // Solo cambió durabilidad o metadatos, no se consumió nada.
            }
        }

        World world = player.getWorld();
        Store<EntityStore> store = player.getReference().getStore();
        
        // El ítem ha desaparecido y NO ha sido un Drop. 
        // Solo falta confirmar que tenemos un animal cerca que pueda habérselo comido.
        world.execute(() -> {
            try {
                Vector3d pPos = getEntityPosition(player);
                boolean npcFound = false;

                // Acceder a las entidades a través del EntityStore como respaldo seguro
                Field entitiesField = com.hypixel.hytale.server.core.universe.world.storage.EntityStore.class.getDeclaredField("entitiesByUuid");
                entitiesField.setAccessible(true);
                Map<UUID, ?> entityRefs = (Map<UUID, ?>) entitiesField.get(world.getEntityStore());

                // Buscamos cualquier NPC en un radio de 5 metros
                for (UUID uuid : entityRefs.keySet()) {
                    if (uuid.equals(playerUuid)) continue;
                    Entity entity = world.getEntity(uuid);
                    if (entity == null || !entity.getClass().getName().contains("NPCEntity")) continue;

                    Vector3d nPos = getEntityPosition(entity);
                    double dx = pPos.getX() - nPos.getX();
                    double dy = pPos.getY() - nPos.getY();
                    double dz = pPos.getZ() - nPos.getZ();
                    
                    if ((dx*dx + dy*dy + dz*dz) < 25.0) { // 5 metros
                        npcFound = true;
                        break;
                    }
                }

                if (npcFound) {
                    applySanityRestore(player, store);
                }
            } catch (Exception e) {}
        });
    }

    private static Vector3d getEntityPosition(Object entity) {
        try {
            Method getTC = entity.getClass().getMethod("getTransformComponent");
            Object tc = getTC.invoke(entity);
            Method getPos = tc.getClass().getMethod("getPosition");
            return (Vector3d) getPos.invoke(tc);
        } catch (Exception e) {
            return new Vector3d(0, 0, 0);
        }
    }

    private static void applySanityRestore(Player player, Store<EntityStore> store) {
        try {
            SanityComponent sanity = store.getComponent(player.getReference(), SanityComponent.getComponentType());
            if (sanity != null) {
                float amount = 10.0f;
                if (AquaSanity.get() != null) {
                    amount = AquaSanity.get().getConfigManager().getSanityConfig().getTamingSanityRestore();
                }
                float oldVal = sanity.getSanityLevel();
                sanity.setSanityLevel(Math.min(SanityComponent.maxSanityLevel, oldVal + amount));
                sanity.setGainVisualTimer(1.5f); // Activa el flash dorado
            }
        } catch (Exception e) {}
    }
}
