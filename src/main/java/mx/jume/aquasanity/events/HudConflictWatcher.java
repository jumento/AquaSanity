package mx.jume.aquasanity.events;

import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interface_.CustomHud;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class HudConflictWatcher implements PlayerPacketWatcher {
    public HudConflictWatcher() {
        mx.jume.aquasanity.AquaSanity.LOGGER.at(java.util.logging.Level.INFO).log("[AquaSanity-HudWatcher] Registered");
    }

    // Detectores de origen de paquete (solo para logging y skip)
    private static final String OUR_ROOT_FILE    = "SanityMultipleHUD.ui";
    private static final String AQUA_THIRST_FILE = "HHMMultipleHUD.ui";

    @Override
    public void accept(PlayerRef playerRef, Packet packet) {
        if (!(packet instanceof CustomHud customHud))
            return;

        // Solo nos interesan los clear=true (reconstrucción completa del DOM)
        if (!customHud.clear)
            return;

        CustomUICommand[] cmds = customHud.commands;

        // Verificar si el paquete es nuestra propia inicialización
        if (cmds != null) {
            for (CustomUICommand cmd : cmds) {
                if (cmd == null || cmd.type != CustomUICommandType.Append || cmd.text == null)
                    continue;
                if (cmd.text.contains(OUR_ROOT_FILE)) {
                    // Es nuestro propio paquete de inicialización — ignorar
                    return;
                }
            }
        }

        // Detectar origen para logging
        boolean isAquaThirst = false;
        if (cmds != null) {
            for (CustomUICommand cmd : cmds) {
                if (cmd != null && cmd.type == CustomUICommandType.Append
                        && cmd.text != null && cmd.text.contains(AQUA_THIRST_FILE)) {
                    isAquaThirst = true;
                    break;
                }
            }
        }

        // NUNCA inyectar directamente en un clear=true ajeno.
        // Inyectar aquí combina nuestros comandos con los de Aqua-Thirst / EndgameAndQoL
        // en el mismo paquete, causando conflictos de IDs (#AquaSanityCreativeOverlay u otros)
        // que crashean al cliente con "Failed to apply CustomUI HUD commands".
        //
        // Estrategia: marcar para re-inyección diferida. El siguiente tick de SanitySystem
        // enviará Sanity.ui como paquete clear=false separado, libre de conflictos.
        mx.jume.aquasanity.ui.SanityHud.markForReinjection(playerRef);
        mx.jume.aquasanity.AquaSanity.LOGGER.at(java.util.logging.Level.INFO).log(
                "[AquaSanity-HudWatcher] External clear=true -> deferred reinjection"
                + " origin=" + (isAquaThirst ? "AquaThirst" : "unknown")
                + " cmds=" + (cmds != null ? cmds.length : 0));
    }
}
