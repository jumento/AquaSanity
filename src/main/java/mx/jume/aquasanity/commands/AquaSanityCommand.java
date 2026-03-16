package mx.jume.aquasanity.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AquaSanityCommand extends AbstractPlayerCommand {
    public static final String requiredPermission = "aquasanity.command.base";

    static final String helpBase = """
            AquaSanity commands

            /aquasanity - Show this help message.""";
    static final String helpSetSelf = "/aquasanity set <sanityLevel> - Set your own sanity level (0-100).";
    static final String helpSetOther = "/aquasanity set <player> <sanityLevel> - Set another player's sanity level (0-100).";

    public AquaSanityCommand() {
        super("aquasanity", "Sanity Command", false);
        this.requirePermission(requiredPermission);
        this.addSubCommand(new SetSanityCommand());
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world) {

        Message message = Message.empty();
        message.insert(helpBase);

        if (context.sender().hasPermission(SetSanityCommand.requiredPermission)) {
            message.insert("\n").insert(helpSetSelf);
        }
        if (context.sender().hasPermission(SetSanityCommand.SetSanityOtherCommand.requiredPermission)) {
            message.insert("\n").insert(helpSetOther);
        }
        playerRef.sendMessage(message);
    }
}
