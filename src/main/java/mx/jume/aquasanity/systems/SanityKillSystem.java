package mx.jume.aquasanity.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.components.SanityComponent;
import mx.jume.aquasanity.ui.SanityHud;
import mx.jume.aquasanity.util.SanityFilters;
import com.hypixel.hytale.component.query.Query;
import javax.annotation.Nonnull;

/**
 * System that rewards player sanity when an NPC actually dies.
 * This is 100% accurate as it triggers only when the DeathComponent is added.
 */
public class SanityKillSystem extends DeathSystems.OnDeathSystem {

    public SanityKillSystem() {
        super();
    }

    @Override
    public Query<EntityStore> getQuery() {
        // We need both DeathComponent (triggered) and NPCEntity (to identify)
        return Query.and(DeathComponent.getComponentType(), NPCEntity.getComponentType());
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> npcRef, @Nonnull DeathComponent deathComp,
            @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // 1. Get NPC data
        NPCEntity targetNpc = store.getComponent(npcRef, NPCEntity.getComponentType());
        if (targetNpc == null || targetNpc.getRole() == null)
            return;

        // 2. Identify Type (Passive vs Hostile)
        boolean isPassive = SanityFilters.isPassive(targetNpc);

        // 3. Identify Killer (The entity that dealt the most damage)
        Ref<EntityStore> killerRef = targetNpc.getDamageData().getMostDamagingAttacker();
        if (killerRef == null || !killerRef.isValid())
            return;

        // 4. Grant Reward/Penalty to the Player
        SanityComponent sanity = store.getComponent(killerRef, SanityComponent.getComponentType());
        if (sanity == null)
            return;

        PlayerRef pRef = store.getComponent(killerRef, PlayerRef.getComponentType());
        float oldLevel = sanity.getSanityLevel();
        float newSanity = oldLevel;

        if (isPassive) {
            // PENALTY for killing passives
            float penalty = AquaSanity.get().getConfigManager().getSanityConfig().getPassiveKillSanityLoss();
            newSanity = Math.max(0.0f, oldLevel - penalty);
            sanity.setSanityLevel(newSanity);
            sanity.setDamageVisualTimer(1.5f); // Red flash
        } else {
            // REWARD for killing hostiles (only if not friendly)
            if (targetNpc.getRole().isFriendly(killerRef, store))
                return;

            float reward = AquaSanity.get().getConfigManager().getSanityConfig().getKillSanityRestore();
            if (reward > 0) {
                newSanity = Math.min(SanityComponent.maxSanityLevel, oldLevel + reward);
                sanity.setSanityLevel(newSanity);
                sanity.setGainVisualTimer(1.5f); // Gold flash
            } else {
                return;
            }
        }

        if (pRef != null) {
            // Sync with HUD
            float threshold = AquaSanity.get().getConfigManager().getSanityConfig().getInsanity();
            boolean isInsane = newSanity <= threshold;
            SanityHud.updatePlayerSanityLevel(pRef, newSanity, sanity.hasAgro(), sanity.isInDarkness(),
                    sanity.getDamageVisualTimer() > 0, sanity.getGainVisualTimer() > 0,
                    sanity.getStaminaVisualTimer() > 0, isInsane);
        }
    }
}
