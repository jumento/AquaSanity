package mx.jume.aquasanity.ui.hud;

import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommandType;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import mx.jume.aquasanity.AquaSanity;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Internal HUD container that supports stacking multiple CustomUIHuds.
 * Adapted from InternalMultipleHUD in Aqua-Thirst-hunger.
 */
public class InternalMultipleHUD extends CustomUIHud {

    private static Method BUILD_METHOD;
    private static Field COMMANDS_FIELD;

    static {
        try {
            BUILD_METHOD = CustomUIHud.class.getDeclaredMethod("build", UICommandBuilder.class);
            BUILD_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            BUILD_METHOD = null;
            AquaSanity.LOGGER.at(Level.SEVERE).log("Could not find method 'build' in CustomUIHud");
        }

        try {
            COMMANDS_FIELD = UICommandBuilder.class.getDeclaredField("commands");
            COMMANDS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            COMMANDS_FIELD = null;
            AquaSanity.LOGGER.at(Level.SEVERE).log("Could not find field 'commands' in UICommandBuilder");
        }
    }

    private static final Map<PlayerRef, InternalMultipleHUD> playerContainerMap = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<PlayerRef, String> externalParentMap = Collections.synchronizedMap(new WeakHashMap<>());

    public static InternalMultipleHUD getContainer(PlayerRef playerRef) {
        return playerContainerMap.get(playerRef);
    }

    public static void registerExternalParent(PlayerRef playerRef, String parentSelector) {
        externalParentMap.put(playerRef, parentSelector);
    }

    public static String getExternalParent(PlayerRef playerRef) {
        return externalParentMap.get(playerRef);
    }

    public static void removePlayer(PlayerRef playerRef) {
        playerContainerMap.remove(playerRef);
        externalParentMap.remove(playerRef);
    }

    private final ConcurrentHashMap<String, String> normalizedIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CustomUIHud> customHuds = new ConcurrentHashMap<>();

    public InternalMultipleHUD(@NonNullDecl PlayerRef playerRef) {
        super(playerRef);
        playerContainerMap.put(playerRef, this);
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("HUD/SanityMultipleHUD.ui");
    }

    @Override
    public void show() {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        this.build(commandBuilder);
        for (String identifier : customHuds.keySet()) {
            String normalizedId = normalizedIds.get(identifier);
            CustomUIHud hud = customHuds.get(identifier);
            if (normalizedId != null && hud != null) {
                buildHud(commandBuilder, normalizedId, hud, false);
            }
        }
        this.update(true, commandBuilder);
    }

    public void appendAllCommands(UICommandBuilder commandBuilder) {
        this.build(commandBuilder);
        for (String identifier : customHuds.keySet()) {
            String normalizedId = normalizedIds.get(identifier);
            CustomUIHud hud = customHuds.get(identifier);
            if (normalizedId != null && hud != null) {
                buildHud(commandBuilder, normalizedId, hud, false);
            }
        }
    }

    public void add(@NonNullDecl String identifier, @NonNullDecl CustomUIHud hud) {
        UICommandBuilder commandBuilder = new UICommandBuilder();

        String normalizedId = normalizedIds.computeIfAbsent(identifier, i -> i.replaceAll("[^a-zA-Z0-9]", ""));
        CustomUIHud existingHud = customHuds.get(identifier);
        if (existingHud != hud) {
            customHuds.put(identifier, hud);
        }

        buildHud(commandBuilder, normalizedId, hud, existingHud != null);
        update(false, commandBuilder);
    }

    public void remove(@NonNullDecl String identifier) {
        String normalizedId = normalizedIds.get(identifier);
        if (normalizedId == null)
            return;

        normalizedIds.remove(identifier);
        customHuds.remove(identifier);

        UICommandBuilder commandBuilder = new UICommandBuilder();
        commandBuilder.remove("#AquaSanityMultipleHUD #" + normalizedId);
        update(false, commandBuilder);
    }

    @Nullable
    public CustomUIHud get(@NonNullDecl String identifier) {
        return customHuds.get(identifier);
    }

    public static boolean updatePlayerChild(
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl String childIdentifier,
            @NonNullDecl UICommandBuilder partialCommands) {
        InternalMultipleHUD container = playerContainerMap.get(playerRef);
        if (container == null)
            return false;
        container.updateChild(childIdentifier, partialCommands);
        return true;
    }

    public void updateChild(@NonNullDecl String identifier, @NonNullDecl UICommandBuilder partialCommands) {
        String normalizedId = normalizedIds.get(identifier);
        if (normalizedId == null || COMMANDS_FIELD == null)
            return;
        try {
            @SuppressWarnings("unchecked")
            List<CustomUICommand> srcCmds = (List<CustomUICommand>) COMMANDS_FIELD.get(partialCommands);
            if (srcCmds == null || srcCmds.isEmpty())
                return;

            PrefixedUICommandBuilder prefixed = new PrefixedUICommandBuilder("#AquaSanityMultipleHUD", normalizedId);
            @SuppressWarnings("unchecked")
            List<CustomUICommand> prefixedInternal = (List<CustomUICommand>) COMMANDS_FIELD.get(prefixed);
            if (prefixedInternal != null) {
                prefixedInternal.addAll(srcCmds);
            }

            UICommandBuilder outBuilder = new UICommandBuilder();
            prefixed.appendCommandsTo(outBuilder);

            this.update(false, outBuilder);
        } catch (IllegalAccessException e) {
            AquaSanity.LOGGER.at(Level.SEVERE).log("Error in updateChild: " + e.getMessage());
        }
    }

    private static void buildHud(
            @Nonnull UICommandBuilder uiCommandBuilder,
            @NonNullDecl String normalizedId,
            @Nonnull CustomUIHud hud,
            boolean hudExists) {
        buildHudForParent(uiCommandBuilder, "#AquaSanityMultipleHUD", normalizedId, hud, hudExists);
    }

    private static void buildHudForParent(
            @Nonnull UICommandBuilder uiCommandBuilder,
            @NonNullDecl String parentSelector,
            @NonNullDecl String normalizedId,
            @Nonnull CustomUIHud hud,
            boolean hudExists) {
        try {
            if (BUILD_METHOD == null || COMMANDS_FIELD == null)
                return;

            PrefixedUICommandBuilder singleHudBuilder = new PrefixedUICommandBuilder(parentSelector, normalizedId);
            if (hudExists) {
                singleHudBuilder.addCustomCommand(CustomUICommandType.Clear, singleHudBuilder.getPrefix(), null);
            } else {
                singleHudBuilder.addCustomCommand(CustomUICommandType.AppendInline, parentSelector,
                        "Group #" + normalizedId + " {}");
            }

            BUILD_METHOD.invoke(hud, singleHudBuilder);
            singleHudBuilder.appendCommandsTo(uiCommandBuilder);

        } catch (IllegalAccessException | InvocationTargetException e) {
            AquaSanity.LOGGER.at(Level.SEVERE).log("Error building inner HUD: " + e.getMessage());
        }
    }

    /**
     * Builds a child HUD under an external parent root (e.g. Aqua-Thirst's #MultipleHUD).
     * Does NOT append SanityMultipleHUD.ui — injects directly as a child of parentSelector.
     */
    public static void buildChildForExternalParent(
            @Nonnull UICommandBuilder outBuilder,
            @NonNullDecl String parentSelector,
            @NonNullDecl String childId,
            @Nonnull CustomUIHud childHud) {
        buildHudForParent(outBuilder, parentSelector, childId, childHud, false);
    }

    /**
     * Sends a prefixed update for a child HUD living inside an external parent
     * (e.g. #MultipleHUD #mxjumeaquasanityhudsanity) by re-prefixing the given commands
     * and sending via hudForSending.update(false, ...).
     */
    public static boolean updatePlayerChildWithExternalParent(
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl String childIdentifier,
            @NonNullDecl UICommandBuilder partialCommands,
            @NonNullDecl CustomUIHud hudForSending) {
        String externalParent = externalParentMap.get(playerRef);
        if (externalParent == null || COMMANDS_FIELD == null)
            return false;

        String normalizedId = childIdentifier.replaceAll("[^a-zA-Z0-9]", "");
        String prefix = externalParent + " #" + normalizedId;

        try {
            @SuppressWarnings("unchecked")
            List<CustomUICommand> srcCmds = (List<CustomUICommand>) COMMANDS_FIELD.get(partialCommands);
            if (srcCmds == null || srcCmds.isEmpty())
                return true;

            // Prefijamos los selectores in-place (igual que PrefixedUICommandBuilder.prefixCommands)
            for (CustomUICommand cmd : srcCmds) {
                if (cmd == null)
                    continue;
                if (cmd.selector == null) {
                    cmd.selector = prefix;
                } else {
                    cmd.selector = prefix + " " + cmd.selector;
                }
            }

            hudForSending.update(false, partialCommands);
            return true;
        } catch (IllegalAccessException e) {
            AquaSanity.LOGGER.at(Level.SEVERE).log("Error in updatePlayerChildWithExternalParent: " + e.getMessage());
            return false;
        }
    }

    private static class PrefixedUICommandBuilder extends UICommandBuilder {
        private final List<CustomUICommand> wrappedCommands = new ArrayList<>();
        private final String prefix;

        public PrefixedUICommandBuilder(@NonNullDecl String parent, @NonNullDecl String id) {
            this.prefix = parent + " #" + id;
        }

        public String getPrefix() {
            return this.prefix;
        }

        @SuppressWarnings("unchecked")
        private void prefixCommands() throws IllegalAccessException {
            if (COMMANDS_FIELD == null)
                return;
            final List<CustomUICommand> commands = (List<CustomUICommand>) COMMANDS_FIELD.get(this);

            if (commands != null) {
                for (CustomUICommand command : commands) {
                    if (command != null) {
                        if (command.selector == null) {
                            command.selector = this.prefix;
                        } else {
                            command.selector = this.prefix + " " + command.selector;
                        }
                        wrappedCommands.add(command);
                    }
                }
                commands.clear();
            }
        }

        @Override
        @Nonnull
        public CustomUICommand[] getCommands() {
            try {
                this.prefixCommands();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            CustomUICommand[] commands = wrappedCommands.toArray(new CustomUICommand[0]);
            wrappedCommands.clear();
            return commands;
        }

        void appendCommandsTo(@Nonnull UICommandBuilder builder) throws IllegalAccessException {
            this.prefixCommands();
            if (COMMANDS_FIELD == null)
                return;
            @SuppressWarnings("unchecked")
            final List<CustomUICommand> commands = (List<CustomUICommand>) COMMANDS_FIELD.get(builder);
            if (commands != null) {
                commands.addAll(this.wrappedCommands);
            }
            this.wrappedCommands.clear();
        }

        void addCustomCommand(@Nonnull CustomUICommandType type, @Nullable String selector,
                @Nullable String document) {
            this.wrappedCommands.add(new CustomUICommand(type, selector, null, document));
        }
    }
}
