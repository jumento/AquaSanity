package mx.jume.aquasanity.events;

import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.components.SanityComponent;

import java.util.Set;

public class SanityFarmingListener {

    private static final Set<String> FARMING_TOOL_PATTERNS = Set.of(
            "Tool_Watering_Can_State_Filled_Water",
            "Tool_Fertilizer",
            "Tool_Fertilizer_Crystal");

    private static boolean isFarmingTool(String itemId) {
        if (itemId == null)
            return false;
        String clean = itemId.startsWith("*") ? itemId.substring(1) : itemId;
        return FARMING_TOOL_PATTERNS.contains(clean);
    }

    public static void handle(LivingEntityInventoryChangeEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        Transaction transaction = event.getTransaction();
        if (transaction == null || !transaction.succeeded())
            return;

        String transInfo = transaction.toString();
        if (transInfo.contains("MoveTransaction") || transInfo.contains("Drop")) {
            return;
        }

        if (!(transaction instanceof SlotTransaction st))
            return;

        ItemStack before = st.getSlotBefore();
        ItemStack after = st.getSlotAfter();

        // 1. Validar que había un ítem en el slot antes del evento
        if (before == null || before.getItemId() == null)
            return;

        // 2. NUEVO FILTRO CRÍTICO: Si "after" es nulo, significa que el ítem salió del
        // slot.
        // Esto pasa cuando lo TIRAS al suelo o lo MUEVES a otra casilla. Lo bloqueamos.
        if (after == null || after.getItemId() == null)
            return;

        String beforeId = before.getItemId();
        String afterId = after.getItemId();

        // Solo herramientas de farming
        if (!isFarmingTool(beforeId))
            return;

        String cleanBefore = beforeId.startsWith("*") ? beforeId.substring(1) : beforeId;
        String cleanAfter = afterId.startsWith("*") ? afterId.substring(1) : afterId;

        // 3. Filtro contra intercambios de inventario:
        // Si el jugador agarra tierra con el cursor y la intercambia por la
        // herramienta,
        // los IDs serán diferentes. Solo dejamos pasar si la herramienta sigue siendo
        // la misma
        // (lo que confirma que solo perdió durabilidad) o si es la regadera cambiando
        // de estado.
        if (!cleanBefore.equals(cleanAfter) && !afterId.contains("Tool_Watering_Can")) {
            return;
        }

        // ¡Acción de farming confirmada! (La herramienta se usó, no se tiró ni se
        // movió)
        Store<EntityStore> store = player.getReference().getStore();
        applySanityRestore(player, store, cleanBefore);
    }

    private static void applySanityRestore(Player player, Store<EntityStore> store, String toolId) {
        try {
            SanityComponent sanity = store.getComponent(
                    player.getReference(), SanityComponent.getComponentType());
            if (sanity == null)
                return;

            float amount = AquaSanity.get().getConfigManager()
                    .getSanityConfig().getFarmingSanityRestore();

            float oldVal = sanity.getSanityLevel();
            sanity.setSanityLevel(Math.min(SanityComponent.maxSanityLevel, oldVal + amount));
            sanity.setGainVisualTimer(1.5f);

        } catch (Exception e) {
        }
    }
}