package mx.jume.aquasanity;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;
import mx.jume.aquasanity.commands.AquaSanityCommand;
import mx.jume.aquasanity.commands.AquaSanityConfigCommand;
import mx.jume.aquasanity.components.SanityComponent;
import mx.jume.aquasanity.config.ConfigManager;
import mx.jume.aquasanity.events.SanityFeedingListener;
import mx.jume.aquasanity.events.SanityFarmingListener;
import mx.jume.aquasanity.events.SanityPlayerDisconnect;
import mx.jume.aquasanity.events.SanityPlayerReady;
import mx.jume.aquasanity.systems.SanitySystem;
import mx.jume.aquasanity.systems.SanityRespawnSystem;
import mx.jume.aquasanity.systems.SanityKillSystem;

import java.util.logging.Level;

public class AquaSanity extends JavaPlugin {
    private static AquaSanity instance;
    private ConfigManager configManager;
    private ComponentType<EntityStore, SanityComponent> sanityComponentType;
    public static HytaleLogger LOGGER;
    public static boolean aquaThirstPresent = false;

    static {
    }

    public AquaSanity(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static AquaSanity getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        LOGGER = this.getLogger();
        this.configManager = new ConfigManager(this.getDataDirectory().getParent().resolve("AquaSanity"));
        this.configManager.load();
        this.sanityComponentType = this.getEntityStoreRegistry().registerComponent(SanityComponent.class,
                "aquasanity:sanity", SanityComponent.CODEC);
        this.getEntityStoreRegistry().registerSystem(new SanitySystem());
        this.getEntityStoreRegistry().registerSystem(new SanityRespawnSystem());
        this.getEntityStoreRegistry().registerSystem(new SanityKillSystem());

        try {
            Class.forName("mx.jume.aquahunger.ui.hud.InternalMultipleHUD");
            aquaThirstPresent = true;
        } catch (ClassNotFoundException ignored) {
        }

        PacketAdapters.registerOutbound(new mx.jume.aquasanity.events.HudConflictWatcher());

        com.hypixel.hytale.server.core.io.adapter.PacketAdapters
                .registerOutbound(new mx.jume.aquasanity.events.GameModePacketWatcher());
    }

    @Override
    protected void start() {
        try {
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, SanityPlayerReady::handle);
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("Failed to register PlayerReadyEvent: " + e.getMessage());
        }

        try {
            this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, SanityPlayerDisconnect::handle);
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("Failed to register PlayerDisconnectEvent: " + e.getMessage());
        }

        try {
            this.getEventRegistry().registerGlobal(com.hypixel.hytale.event.EventPriority.FIRST,
                    LivingEntityInventoryChangeEvent.class, SanityFeedingListener::handle);
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).log("Failed to register SanityFeedingListener: " + e.getMessage());
        }

        this.getCommandRegistry().registerCommand(new AquaSanityCommand());
        this.getCommandRegistry().registerCommand(new AquaSanityConfigCommand());
        this.getEventRegistry().registerGlobal(
                com.hypixel.hytale.event.EventPriority.FIRST,
                LivingEntityInventoryChangeEvent.class,
                SanityFarmingListener::handle);

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ComponentType<EntityStore, SanityComponent> getSanityComponentType() {
        return sanityComponentType;
    }

    public static AquaSanity get() {
        return instance;
    }
}
