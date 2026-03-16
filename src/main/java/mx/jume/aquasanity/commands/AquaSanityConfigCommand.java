package mx.jume.aquasanity.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import mx.jume.aquasanity.ui.pages.AquaSanityConfigPage;

public class AquaSanityConfigCommand extends AbstractPlayerCommand {

    public AquaSanityConfigCommand() {
        super("aquasanityconfig", "Open the AquaSanity configuration menu", true);
    }

    @Override
    protected void execute(
            @NonNullDecl CommandContext context,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl Ref<EntityStore> ref,
            @NonNullDecl PlayerRef playerRef,
            @NonNullDecl World world) {

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        AquaSanityConfigPage page = new AquaSanityConfigPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
