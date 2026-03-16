package mx.jume.aquasanity.ui.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.config.AquaSanityConfig;
import mx.jume.aquasanity.config.AquaSanityConfig.HudPosition;
import mx.jume.aquasanity.ui.SanityHud;

import javax.annotation.Nonnull;

public class AquaSanityConfigPage extends InteractiveCustomUIPage<AquaSanityConfigPage.FormData> {

    public static class FormData {
        public String action;
        public String position;
        public float insanity;
        public boolean enableDamage;
        public float lightThreshold;
        public float damageSanityLoss;
        public float sanityLossInterval;
        public float tamingSanityRestore;
        public float farmingSanityRestore;
        public float killSanityRestore;
        public float passiveKillSanityLoss;
        public float eventRecoveryChance;
        public float maxEventPause;

        public static final BuilderCodec<FormData> CODEC = BuilderCodec.builder(FormData.class, FormData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (obj, val) -> obj.action = val, obj -> obj.action)
                .add()
                .append(new KeyedCodec<>("@Position", Codec.STRING), (obj, val) -> obj.position = val,
                        obj -> obj.position)
                .add()
                .append(new KeyedCodec<>("@Insanity", Codec.FLOAT),
                        (obj, val) -> obj.insanity = (val != null ? val : 20.0f),
                        obj -> obj.insanity)
                .add()
                .append(new KeyedCodec<>("@EnableDamage", Codec.BOOLEAN),
                        (obj, val) -> obj.enableDamage = (val != null ? val : false),
                        obj -> obj.enableDamage)
                .add()
                .append(new KeyedCodec<>("@LightThreshold", Codec.FLOAT),
                        (obj, val) -> obj.lightThreshold = (val != null ? val : 5.0f),
                        obj -> obj.lightThreshold)
                .add()
                .append(new KeyedCodec<>("@DamageSanityLoss", Codec.FLOAT),
                        (obj, val) -> obj.damageSanityLoss = (val != null ? val : 2.0f),
                        obj -> obj.damageSanityLoss)
                .add()
                .append(new KeyedCodec<>("@SanityLossInterval", Codec.FLOAT),
                        (obj, val) -> obj.sanityLossInterval = (val != null ? val : 5.0f),
                        obj -> obj.sanityLossInterval)
                .add()
                .append(new KeyedCodec<>("@TamingSanityRestore", Codec.FLOAT),
                        (obj, val) -> obj.tamingSanityRestore = (val != null ? val : 10.0f),
                        obj -> obj.tamingSanityRestore)
                .add()
                .append(new KeyedCodec<>("@FarmingSanityRestore", Codec.FLOAT),
                        (obj, val) -> obj.farmingSanityRestore = (val != null ? val : 2.0f),
                        obj -> obj.farmingSanityRestore)
                .add()
                .append(new KeyedCodec<>("@KillSanityRestore", Codec.FLOAT),
                        (obj, val) -> obj.killSanityRestore = (val != null ? val : 10.0f),
                        obj -> obj.killSanityRestore)
                .add()
                .append(new KeyedCodec<>("@PassiveKillSanityLoss", Codec.FLOAT),
                        (obj, val) -> obj.passiveKillSanityLoss = (val != null ? val : 3.0f),
                        obj -> obj.passiveKillSanityLoss)
                .add()
                .append(new KeyedCodec<>("@EventRecoveryChance", Codec.FLOAT),
                        (obj, val) -> obj.eventRecoveryChance = (val != null ? val : 50.0f),
                        obj -> obj.eventRecoveryChance)
                .add()
                .append(new KeyedCodec<>("@MaxEventPause", Codec.FLOAT),
                        (obj, val) -> obj.maxEventPause = (val != null ? val : 60.0f),
                        obj -> obj.maxEventPause)
                .add()
                .build();
    }

    private final PlayerRef configPlayerRef;

    public AquaSanityConfigPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, FormData.CODEC);
        this.configPlayerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd, @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store) {
        cmd.append("Sanity/Pages/Config.ui");

        AquaSanityConfig config = AquaSanity.get().getConfigManager().getSanityConfig();

        cmd.set("#PosSelector.Value", config.getHudPosition().name());
        cmd.set("#InsanitySlider.Value", (int) config.getInsanity());
        cmd.set("#DamageCheckbox #CheckBox.Value", config.isEnableDamage());
        cmd.set("#LightThresholdSlider.Value", (int) config.getLightThreshold());
        cmd.set("#DamageSanityLossSlider.Value", (int) config.getDamageSanityLoss());
        cmd.set("#SanityLossIntervalInput.Value", (int) config.getSanityLossInterval());
        cmd.set("#TamingSanityRestoreSlider.Value", (int) config.getTamingSanityRestore());
        cmd.set("#FarmingSanityRestoreSlider.Value", (int) config.getFarmingSanityRestore());
        cmd.set("#KillSanityRestoreSlider.Value", (int) config.getKillSanityRestore());
        cmd.set("#PassiveKillSanityLossSlider.Value", (int) config.getPassiveKillSanityLoss());
        cmd.set("#EventRecoveryChanceSlider.Value", (int) config.getEventRecoveryChance());
        cmd.set("#MaxEventPauseInput.Value", (int) config.getMaxEventPause());

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#SaveBtn", createSyncData("Save"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn", createSyncData("Close"));
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseTopBtn", createSyncData("Close"));
    }

    private EventData createSyncData(String action) {
        return new EventData()
                .append("Action", action)
                .append("@Position", "#PosSelector.Value")
                .append("@Insanity", "#InsanitySlider.Value")
                .append("@EnableDamage", "#DamageCheckbox #CheckBox.Value")
                .append("@LightThreshold", "#LightThresholdSlider.Value")
                .append("@DamageSanityLoss", "#DamageSanityLossSlider.Value")
                .append("@SanityLossInterval", "#SanityLossIntervalInput.Value")
                .append("@TamingSanityRestore", "#TamingSanityRestoreSlider.Value")
                .append("@FarmingSanityRestore", "#FarmingSanityRestoreSlider.Value")
                .append("@KillSanityRestore", "#KillSanityRestoreSlider.Value")
                .append("@PassiveKillSanityLoss", "#PassiveKillSanityLossSlider.Value")
                .append("@EventRecoveryChance", "#EventRecoveryChanceSlider.Value")
                .append("@MaxEventPause", "#MaxEventPauseInput.Value");
    }


    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
            @Nonnull FormData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || configPlayerRef == null)
            return;

        if ("Save".equals(data.action)) {
            AquaSanityConfig config = AquaSanity.get().getConfigManager().getSanityConfig();
            try {
                config.setHudPosition(HudPosition.valueOf(data.position));
                config.setInsanity(data.insanity);
                config.setEnableDamage(data.enableDamage);
                config.setLightThreshold(data.lightThreshold);
                config.setDamageSanityLoss(data.damageSanityLoss);
                config.setSanityLossInterval(data.sanityLossInterval);
                config.setTamingSanityRestore(data.tamingSanityRestore);
                config.setFarmingSanityRestore(data.farmingSanityRestore);
                config.setKillSanityRestore(data.killSanityRestore);
                config.setPassiveKillSanityLoss(data.passiveKillSanityLoss);
                config.setEventRecoveryChance(data.eventRecoveryChance);
                config.setMaxEventPause(data.maxEventPause);

                AquaSanity.get().getConfigManager().save();

                // Hot reload HUD position
                SanityHud.updatePlayerHudPosition(configPlayerRef, config.getHudPosition());

                configPlayerRef.sendMessage(
                        com.hypixel.hytale.server.core.Message.empty().insert("AquaSanity configuration saved and applied!").color("#55FF55"));
            } catch (Exception e) {
                configPlayerRef.sendMessage(
                        com.hypixel.hytale.server.core.Message.empty().insert("Error saving configuration.").color("#FF5555"));
                e.printStackTrace();
            }
        }

        player.getPageManager().setPage(ref, store, Page.None);
    }
}
