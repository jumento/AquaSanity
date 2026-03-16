package mx.jume.aquasanity.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.components.SanityComponent;
import mx.jume.aquasanity.ui.SanityHud;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class SetSanityCommand extends AbstractPlayerCommand {
    public static final String requiredPermission = "aquasanity.sanity.set.self";

    private final RequiredArg<Float> sanityLevel = this.withRequiredArg("sanityLevel",
            "A value between 0 and 100", ArgTypes.FLOAT);

    public SetSanityCommand() {
        super("set", "Set own sanity level", false);
        this.requirePermission(requiredPermission);
        this.addUsageVariant(new SetSanityOtherCommand());
    }

    private static void applySanityLevel(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef targetPlayerRef,
            float newLevel) {
        if (newLevel < 0 || newLevel > SanityComponent.maxSanityLevel) {
            context.sendMessage(Message.empty().insert(
                    "Sanity level must be between 0 and " + SanityComponent.maxSanityLevel + "."));
            return;
        }

        SanityComponent sanity = store.getComponent(ref, SanityComponent.getComponentType());
        if (sanity == null) {
            context.sendMessage(Message.empty().insert("Player does not have a SanityComponent."));
            return;
        }
        sanity.setSanityLevel(newLevel);
        float threshold = AquaSanity.get().getConfigManager().getSanityConfig().getInsanity();
        boolean isInsane = newLevel <= threshold;
        
        SanityHud.updatePlayerSanityLevel(targetPlayerRef, newLevel, sanity.hasAgro(), sanity.isInDarkness(),
                (sanity.getDamageVisualTimer() > 0), (sanity.getGainVisualTimer() > 0),
                (sanity.getStaminaVisualTimer() > 0), isInsane);

        context.sendMessage(Message.empty().insert("Sanity level set to " + newLevel
                + " for player " + targetPlayerRef.getUsername() + "."));
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world) {
        float level = this.sanityLevel.get(context);
        applySanityLevel(context, store, ref, playerRef, level);
    }

    // ---- Sub-command: set <player> <value> ----

    public static class SetSanityOtherCommand extends CommandBase {
        public static final String requiredPermission = "aquasanity.sanity.set.other";

        private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player",
                "The target player", ArgTypes.PLAYER_REF);
        private final RequiredArg<Float> sanityLevel = this.withRequiredArg("sanityLevel",
                "A value between 0 and 100", ArgTypes.FLOAT);

        public SetSanityOtherCommand() {
            super("Set another player's sanity level");
            this.requirePermission(requiredPermission);
        }

        @Override
        protected void executeSync(@NonNullDecl CommandContext context) {
            PlayerRef targetPlayerRef = this.playerArg.get(context);
            float newLevel = this.sanityLevel.get(context);
            Ref<EntityStore> ref = targetPlayerRef.getReference();
            if (ref == null || !ref.isValid()) {
                context.sendMessage(Message.raw("Player not found or not in the world."));
                return;
            }
            Store<EntityStore> store = ref.getStore();
            store.getExternalData().getWorld().execute(() -> {
                applySanityLevel(context, store, ref, targetPlayerRef, newLevel);
            });
        }
    }
}
