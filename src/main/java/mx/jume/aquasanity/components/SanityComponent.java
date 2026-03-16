package mx.jume.aquasanity.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.AquaSanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;

public class SanityComponent implements Component<EntityStore> {

    public static final BuilderCodec<SanityComponent> CODEC = BuilderCodec
            .builder(SanityComponent.class, SanityComponent::new)
            .append(new KeyedCodec<>("SanityLevel", Codec.FLOAT),
                    ((data, value) -> data.sanityLevel = value),
                    SanityComponent::getSanityLevel)
            .add()
            .append(new KeyedCodec<>("StaminaPenaltyTimer", Codec.FLOAT),
                    ((data, value) -> data.staminaPenaltyTimer = value),
                    SanityComponent::getStaminaPenaltyTimer)
            .add()
            .append(new KeyedCodec<>("TriggeredAtFive", Codec.BOOLEAN),
                    ((data, value) -> data.triggeredAtFive = (value != null ? value : false)),
                    SanityComponent::hasTriggeredAtFive)
            .add()
            .build();

    public static final float maxSanityLevel = 100.0f;
    private float sanityLevel;
    private float elapsedTime = 0.0f;
    private float nextDamageCooldown = 0.0f;
    private Instant lastDamageTimestamp = Instant.MIN;
    private boolean hasAgro = false;
    private boolean isInDarkness = false;
    private float damageVisualTimer = 0.0f;
    private float gainVisualTimer = 0.0f;
    private float staminaVisualTimer = 0.0f;
    private float staminaPenaltyTimer = 0.0f;
    private float safeZoneElapsedTime = 0.0f;
    private float randomEventCooldown = 0.0f;
    private boolean triggeredAtFive = false;

    public SanityComponent() {
        // Default to max sanity (lucid)
        this.sanityLevel = maxSanityLevel;
    }

    public SanityComponent(float sanityLevel) {
        this.sanityLevel = Math.max(0.0f, Math.min(sanityLevel, maxSanityLevel));
    }

    public SanityComponent(SanityComponent other) {
        this.sanityLevel = other.sanityLevel;
        this.elapsedTime = other.elapsedTime;
        this.nextDamageCooldown = other.nextDamageCooldown;
        this.lastDamageTimestamp = other.lastDamageTimestamp;
        this.hasAgro = other.hasAgro;
        this.isInDarkness = other.isInDarkness;
        this.damageVisualTimer = other.damageVisualTimer;
        this.gainVisualTimer = other.gainVisualTimer;
        this.staminaVisualTimer = other.staminaVisualTimer;
        this.staminaPenaltyTimer = other.staminaPenaltyTimer;
        this.safeZoneElapsedTime = other.safeZoneElapsedTime;
        this.triggeredAtFive = other.triggeredAtFive;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new SanityComponent(this);
    }

    public float getSanityLevel() {
        return this.sanityLevel;
    }

    public void setSanityLevel(float sanityLevel) {
        this.sanityLevel = Math.max(0.0f, Math.min(sanityLevel, maxSanityLevel));
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void addElapsedTime(float delta) {
        this.elapsedTime += delta;
    }

    public void resetElapsedTime() {
        this.elapsedTime = 0.0f;
    }

    public float getNextDamageCooldown() {
        return nextDamageCooldown;
    }

    public void setNextDamageCooldown(float nextDamageCooldown) {
        this.nextDamageCooldown = nextDamageCooldown;
    }

    public Instant getLastDamageTimestamp() {
        return lastDamageTimestamp;
    }

    public void setLastDamageTimestamp(Instant lastDamageTimestamp) {
        this.lastDamageTimestamp = lastDamageTimestamp;
    }

    public boolean hasAgro() {
        return hasAgro;
    }

    public void setHasAgro(boolean hasAgro) {
        this.hasAgro = hasAgro;
    }

    public boolean isInDarkness() {
        return isInDarkness;
    }

    public void setIsInDarkness(boolean isInDarkness) {
        this.isInDarkness = isInDarkness;
    }

    public float getDamageVisualTimer() {
        return damageVisualTimer;
    }

    public void setDamageVisualTimer(float damageVisualTimer) {
        this.damageVisualTimer = damageVisualTimer;
    }

    public float getGainVisualTimer() {
        return gainVisualTimer;
    }

    public void setGainVisualTimer(float gainVisualTimer) {
        this.gainVisualTimer = gainVisualTimer;
    }

    public float getStaminaVisualTimer() {
        return staminaVisualTimer;
    }

    public void setStaminaVisualTimer(float staminaVisualTimer) {
        this.staminaVisualTimer = staminaVisualTimer;
    }

    public float getStaminaPenaltyTimer() {
        return staminaPenaltyTimer;
    }

    public void setStaminaPenaltyTimer(float staminaPenaltyTimer) {
        this.staminaPenaltyTimer = staminaPenaltyTimer;
    }

    public float getSafeZoneElapsedTime() {
        return safeZoneElapsedTime;
    }

    public void setSafeZoneElapsedTime(float safeZoneElapsedTime) {
        this.safeZoneElapsedTime = safeZoneElapsedTime;
    }

    public void addSafeZoneElapsedTime(float delta) {
        this.safeZoneElapsedTime += delta;
    }

    public float getRandomEventCooldown() {
        return randomEventCooldown;
    }

    public void setRandomEventCooldown(float randomEventCooldown) {
        this.randomEventCooldown = randomEventCooldown;
    }

    public boolean hasTriggeredAtFive() {
        return triggeredAtFive;
    }

    public void setTriggeredAtFive(boolean triggeredAtFive) {
        this.triggeredAtFive = triggeredAtFive;
    }

    @Nonnull
    public static ComponentType<EntityStore, SanityComponent> getComponentType() {
        return AquaSanity.get().getSanityComponentType();
    }
}
