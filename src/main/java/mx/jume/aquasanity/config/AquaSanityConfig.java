package mx.jume.aquasanity.config;

import com.google.gson.annotations.SerializedName;

public class AquaSanityConfig {

    /** Incrementar cada vez que se cambie la estructura o defaults del config. */
    public static final int CURRENT_VERSION = 1;

    public enum HudPosition {
        LEFT, CENTER, RIGHT
    }

    @SerializedName("ConfigVersion")
    private int configVersion = CURRENT_VERSION;

    @SerializedName("Insanity")
    private float insanity = 40.0f;

    @SerializedName("HudPosition")
    private HudPosition hudPosition = HudPosition.RIGHT;

    @SerializedName("EnableDamage")
    private boolean enableDamage = true;

    @SerializedName("LightThreshold")
    private float lightThreshold = 6.0f;

    @SerializedName("DamageSanityLoss")
    private float damageSanityLoss = 3.0f;

    @SerializedName("SanityLossInterval")
    private float sanityLossInterval = 5.0f;

    @SerializedName("TamingSanityRestore")
    private float tamingSanityRestore = 5.0f;

    @SerializedName("FarmingSanityRestore")
    private float farmingSanityRestore = 2.0f;

    @SerializedName("KillSanityRestore")
    private float killSanityRestore = 2.0f;

    @SerializedName("PassiveKillSanityLoss")
    private float passiveKillSanityLoss = 3.0f;

    @SerializedName("EventRecoveryChance")
    private float eventRecoveryChance = 50.0f;

    @SerializedName("MaxEventPause")
    private float maxEventPause = 60.0f;

    public float getKillSanityRestore() {
        return killSanityRestore;
    }

    public void setKillSanityRestore(float v) {
        this.killSanityRestore = Math.max(0, Math.min(100, v));
    }

    public float getPassiveKillSanityLoss() {
        return passiveKillSanityLoss;
    }

    public void setPassiveKillSanityLoss(float v) {
        this.passiveKillSanityLoss = Math.max(0, Math.min(100, v));
    }

    public float getEventRecoveryChance() {
        return eventRecoveryChance;
    }

    public void setEventRecoveryChance(float v) {
        this.eventRecoveryChance = Math.max(0, Math.min(100, v));
    }

    public float getMaxEventPause() {
        return maxEventPause;
    }

    public void setMaxEventPause(float v) {
        this.maxEventPause = Math.max(1, v);
    }

    public float getFarmingSanityRestore() {
        return farmingSanityRestore;
    }

    public void setFarmingSanityRestore(float v) {
        this.farmingSanityRestore = Math.max(0, Math.min(100, v));
    }

    public AquaSanityConfig() {
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public float getInsanity() {
        return insanity;
    }

    public void setInsanity(float insanity) {
        this.insanity = insanity;
    }

    public HudPosition getHudPosition() {
        return hudPosition;
    }

    public void setHudPosition(HudPosition hudPosition) {
        this.hudPosition = hudPosition;
    }

    public boolean isEnableDamage() {
        return enableDamage;
    }

    public void setEnableDamage(boolean enableDamage) {
        this.enableDamage = enableDamage;
    }

    public float getLightThreshold() {
        return lightThreshold;
    }

    public void setLightThreshold(float lightThreshold) {
        this.lightThreshold = lightThreshold;
    }

    public float getDamageSanityLoss() {
        return damageSanityLoss;
    }

    public void setDamageSanityLoss(float damageSanityLoss) {
        this.damageSanityLoss = damageSanityLoss;
    }

    public float getSanityLossInterval() {
        return sanityLossInterval;
    }

    public void setSanityLossInterval(float sanityLossInterval) {
        this.sanityLossInterval = sanityLossInterval;
    }

    public float getTamingSanityRestore() {
        return tamingSanityRestore;
    }

    public void setTamingSanityRestore(float tamingSanityRestore) {
        this.tamingSanityRestore = tamingSanityRestore;
    }
}
