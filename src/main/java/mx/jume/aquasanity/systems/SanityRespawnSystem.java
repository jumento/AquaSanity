package mx.jume.aquasanity.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import mx.jume.aquasanity.components.SanityComponent;
import com.hypixel.hytale.component.query.Query;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SanityRespawnSystem extends RefChangeSystem<EntityStore, DeathComponent> {

    @Override
    public ComponentType<EntityStore, DeathComponent> componentType() {
        return DeathComponent.getComponentType();
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }

    @Override
    public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // No hacer nada al morir
    }

    @Override
    public void onComponentSet(@Nonnull Ref<EntityStore> ref, @Nullable DeathComponent oldComponent,
            @Nonnull DeathComponent newComponent,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        // No hacer nada
    }

    @Override
    public void onComponentRemoved(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null)
            return;

        SanityComponent sanity = store.getComponent(ref, SanityComponent.getComponentType());
        if (sanity == null)
            return;

        sanity.setSanityLevel(SanityComponent.maxSanityLevel);
        sanity.setGainVisualTimer(2.0f);
        sanity.setSafeZoneElapsedTime(0);
        sanity.setHasAgro(false);

    }
}