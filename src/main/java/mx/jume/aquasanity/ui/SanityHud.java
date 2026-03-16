package mx.jume.aquasanity.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.components.SanityComponent;
import mx.jume.aquasanity.config.AquaSanityConfig.HudPosition;
import mx.jume.aquasanity.ui.hud.InternalMultipleHUD;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class SanityHud extends CustomUIHud {
    private static final Map<PlayerRef, SanityHud> hudMap = Collections.synchronizedMap(new WeakHashMap<>());
    public static final String hudIdentifier = "mx.jume.aquasanity.hud.sanity";

    private float sanityLevel;
    private HudPosition hudPosition;
    private boolean hasAgro;
    private boolean isInDarkness;
    private boolean isTakingDamage;
    private boolean isGainingSanity;
    private boolean isStaminaDepleted;
    private boolean isInsane;
    private com.hypixel.hytale.protocol.GameMode gameMode = com.hypixel.hytale.protocol.GameMode.Adventure;

    // Cuando un clear=true externo borra el DOM, el siguiente update de SanitySystem
    // enviará Append + Sets como clear=false para re-establecer los elementos en root.
    private volatile boolean needsReinjection = true;

    public SanityHud(@NonNullDecl PlayerRef playerRef, com.hypixel.hytale.protocol.GameMode gameMode,
            float sanityLevel) {
        super(playerRef);
        this.gameMode = gameMode;
        this.sanityLevel = sanityLevel;
        this.hudPosition = AquaSanity.get().getConfigManager().getSanityConfig().getHudPosition();
        hudMap.put(playerRef, this);
    }

    @Override
    protected void build(@NonNullDecl UICommandBuilder uiCommandBuilder) {
        // Path is relative to Common/UI/Custom/
        uiCommandBuilder.append("Sanity/HUD/Sanity.ui");

        updateHudPosition(uiCommandBuilder, this.hudPosition);
        updateGameMode(uiCommandBuilder, this.gameMode);
        updateSanityLevel(uiCommandBuilder, this.sanityLevel, this.hasAgro, this.isInDarkness, this.isTakingDamage,
                this.isGainingSanity, this.isStaminaDepleted, this.isInsane);
    }

    private void updateHudPosition(UICommandBuilder uiCommandBuilder, HudPosition hudPosition) {
        this.hudPosition = hudPosition;
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(90));
        anchor.setHeight(Value.of(90));

        // Default bottom Y offset
        anchor.setBottom(Value.of(15));

        switch (hudPosition) {
            case LEFT:
                // Ancla desde la izquierda de la caja virtual central (ancho 702)
                anchor.setLeft(Value.of(-180));
                anchor.setBottom(Value.of(95)); // Y para LEFT
                break;
            case CENTER:
                // Centrado matemático perfecto dentro de la caja de 702: (702 - 90) / 2 = 306
                // Aquí mueves la posición vertical (Y) cuando está centrado.
                // Auméntalo para subirlo (ej: 40), redúcelo para bajarlo.
                anchor.setLeft(Value.of(306));
                anchor.setBottom(Value.of(125)); // <-- MUEVE ESTE VALOR (eje Y)
                break;
            case RIGHT:
                // Ancla de forma inversa, desde la derecha de la caja virtual central.
                anchor.setRight(Value.of(-85));
                anchor.setBottom(Value.of(25)); // Y para RIGHT
                break;
        }

        uiCommandBuilder.setObject("#AquaSanityContainer.Anchor", anchor);
    }

    private void updateSanityLevel(UICommandBuilder uiCommandBuilder, float sanityLevel) {
        this.updateSanityLevel(uiCommandBuilder, sanityLevel, this.hasAgro, this.isInDarkness, this.isTakingDamage,
                this.isGainingSanity, this.isStaminaDepleted, this.isInsane);
    }

    private void updateSanityLevel(UICommandBuilder uiCommandBuilder, float sanityLevel, boolean hasAgro,
            boolean isInDarkness, boolean isTakingDamage, boolean isGainingSanity, boolean isStaminaDepleted,
            boolean isInsane) {
        this.sanityLevel = sanityLevel;
        this.hasAgro = hasAgro;
        this.isInDarkness = isInDarkness;
        this.isTakingDamage = isTakingDamage;
        this.isGainingSanity = isGainingSanity;
        this.isStaminaDepleted = isStaminaDepleted;
        this.isInsane = isInsane;

        // Si el DOM fue borrado por un clear=true externo (Aqua-Thirst rebuild / bossbar),
        // re-establecemos Sanity.ui vía Append en un paquete clear=false separado.
        // Esto evita inyectar en el clear=true de Aqua-Thirst (que conflictuaría con EndgameAndQoL).
        if (this.needsReinjection) {
            uiCommandBuilder.append("Sanity/HUD/Sanity.ui");
            updateHudPosition(uiCommandBuilder, this.hudPosition);
            updateGameMode(uiCommandBuilder, this.gameMode);
            this.needsReinjection = false;
            AquaSanity.LOGGER.at(java.util.logging.Level.INFO).log("[AquaSanity] Reinjecting Sanity.ui via clear=false Append");
        }

        float barValue = Math.min(sanityLevel, 100.0f) / 100.0f;
        uiCommandBuilder.set("#AquaSanityBar.Value", barValue);
        uiCommandBuilder.set("#AquaSanityBarEffect.Value", barValue);

        // --- Color Logic ---
        // Priority: Damage > Agro > Gaining > Insane (Black) > Blue (Darkness/Tired) >
        // Normal
        if (isTakingDamage) {
            uiCommandBuilder.set("#AquaSanityBar.Color", "#8B0000"); // Dark Red (Damage)
        } else if (isGainingSanity) {
            uiCommandBuilder.set("#AquaSanityBar.Color", "#FFF110"); // Amarillo (Recovery)
        } else if (hasAgro) {
            uiCommandBuilder.set("#AquaSanityBar.Color", "#FF0000"); // Danger Red (Agro)
        } else if (isInsane) {
            uiCommandBuilder.set("#AquaSanityBar.Color", "#0EDC00"); // BLACK (Insanity)
        } else if (isStaminaDepleted || isInDarkness) {
            uiCommandBuilder.set("#AquaSanityBar.Color", "#00BFFF"); // Blue (Darkness/Tired)
        } else {
            uiCommandBuilder.set("#AquaSanityBar.Color", "#9932CC"); // Dark orchid (Purple/Normal)
        }
    }

    protected void updateGameMode(UICommandBuilder uiCommandBuilder, com.hypixel.hytale.protocol.GameMode gameMode) {
        this.gameMode = gameMode;
        // Simplemente mostramos u ocultamos la capa de creativo que está por encima
        uiCommandBuilder.set("#AquaSanityCreativeOverlay.Visible", gameMode == com.hypixel.hytale.protocol.GameMode.Creative);
    }

    public void appendAllCommands(UICommandBuilder uiCommandBuilder) {
        this.build(uiCommandBuilder);
    }

    // ---- Static helpers ----

    public static boolean hasHud(@NonNullDecl PlayerRef playerRef) {
        return hudMap.containsKey(playerRef);
    }

    public static SanityHud getHud(@NonNullDecl PlayerRef playerRef) {
        return hudMap.get(playerRef);
    }

    /**
     * Marca el HUD para re-inyección en el siguiente update de SanitySystem.
     * Se llama cuando un clear=true externo borra el DOM (ej: rebuild de Aqua-Thirst / bossbar).
     */
    public static void removeHud(@NonNullDecl PlayerRef playerRef) {
        hudMap.remove(playerRef);
    }

    public static void markForReinjection(@NonNullDecl PlayerRef playerRef) {
        SanityHud hud = hudMap.get(playerRef);
        if (hud != null) {
            hud.needsReinjection = true;
        }
    }

    public static void updatePlayerSanityLevel(@NonNullDecl PlayerRef playerRef, float sanityLevel, boolean hasAgro,
            boolean isInDarkness, boolean isTakingDamage, boolean isGainingSanity, boolean isStaminaDepleted,
            boolean isInsane) {
        SanityHud hud = hudMap.get(playerRef);
        if (hud == null)
            return;

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        hud.updateSanityLevel(uiCommandBuilder, sanityLevel, hasAgro, isInDarkness, isTakingDamage, isGainingSanity,
                isStaminaDepleted, isInsane);
        if (!InternalMultipleHUD.updatePlayerChild(playerRef, hudIdentifier, uiCommandBuilder)) {
            if (!InternalMultipleHUD.updatePlayerChildWithExternalParent(playerRef, hudIdentifier, uiCommandBuilder, hud)) {
                hud.update(false, uiCommandBuilder);
            }
        }
    }

    public static void updatePlayerHudPosition(@NonNullDecl PlayerRef playerRef, HudPosition position) {
        SanityHud hud = hudMap.get(playerRef);
        if (hud == null)
            return;
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        hud.updateHudPosition(uiCommandBuilder, position);
        if (!InternalMultipleHUD.updatePlayerChild(playerRef, hudIdentifier, uiCommandBuilder)) {
            if (!InternalMultipleHUD.updatePlayerChildWithExternalParent(playerRef, hudIdentifier, uiCommandBuilder, hud)) {
                hud.update(false, uiCommandBuilder);
            }
        }
    }

    public static void updatePlayerGameMode(@NonNullDecl PlayerRef playerRef,
            com.hypixel.hytale.protocol.GameMode gameMode) {
        SanityHud hud = hudMap.get(playerRef);
        if (hud == null)
            return;
        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        hud.updateGameMode(uiCommandBuilder, gameMode);
        if (!InternalMultipleHUD.updatePlayerChild(playerRef, hudIdentifier, uiCommandBuilder)) {
            if (!InternalMultipleHUD.updatePlayerChildWithExternalParent(playerRef, hudIdentifier, uiCommandBuilder, hud)) {
                hud.update(false, uiCommandBuilder);
            }
        }
    }

    public static void createPlayerHud(
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl Player player) {

        SanityComponent sanity = store.ensureAndGetComponent(ref, SanityComponent.getComponentType());
        SanityHud hud = new SanityHud(playerRef, player.getGameMode(), sanity.getSanityLevel());

        CustomUIHud current = player.getHudManager().getCustomHud();

        if (current instanceof InternalMultipleHUD internalMultipleHUD) {
            // Nuestro propio InternalMultipleHUD: agregar directamente
            internalMultipleHUD.add(hudIdentifier, hud);
        } else if (current != null && (current.getClass().getName().contains("InternalMultipleHUD")
                || current.getClass().getName().contains("MultipleHUD"))) {
            // Contenedor externo (ej: Aqua-Thirst): NO agregar via reflexión.
            // Si se agrega, Aqua-Thirst envía child updates con selectores prefijados
            // (#MultipleHUD #id #AquaSanityBar) que no existen en el DOM (root-level) → crash.
            // El needsReinjection flag maneja la inyección inicial vía clear=false en el primer tick.
            AquaSanity.LOGGER.at(java.util.logging.Level.INFO).log("[AquaSanity] External container detected ("
                    + current.getClass().getSimpleName() + "), using deferred clear=false reinjection");
        } else {
            // Sin contenedor externo: crear nuestro propio InternalMultipleHUD.
            // Con Aqua-Thirst presente (nuevo adaptador): su InternalHUDAdapter detectará
            // nuestro contenedor via reflexión y se añadirá a él en lugar de crear el suyo.
            // Sin Aqua-Thirst: el contenedor es nuestro propio manager de hijos.
            InternalMultipleHUD wrapper = new InternalMultipleHUD(playerRef);
            player.getHudManager().setCustomHud(playerRef, wrapper);
            if (current != null) {
                wrapper.add("BaseHUD", current);
            }
            wrapper.add(hudIdentifier, hud);
        }
    }
}
