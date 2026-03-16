package mx.jume.aquasanity.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.ActiveEntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.entity.damage.DamageDataComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import mx.jume.aquasanity.AquaSanity;
import mx.jume.aquasanity.components.SanityComponent;
import mx.jume.aquasanity.ui.SanityHud;
import mx.jume.aquasanity.util.SanityFilters;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.support.MarkedEntitySupport;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.builtin.mounts.MountedComponent;
import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.protocol.BlockMountType;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;

import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.ISpawnableWithModel;
import com.hypixel.hytale.server.spawning.SpawningContext;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.math.vector.Vector3f;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;

public class SanitySystem extends EntityTickingSystem<EntityStore> {

    public static final String insanityEffectId = "Insanity";
    private static EntityEffect insanityEntityEffect;
    private static DamageCause insanityDamageCause;

    public static final String serenityEffectId = "Serenity";
    private static EntityEffect serenityEntityEffect;

    @Nonnull
    public static DamageCause getInsanityDamageCause() {
        if (insanityDamageCause != null)
            return insanityDamageCause;
        // The DamageCause is automatically registered by the engine because it's
        // now defined inside the DamageCalculator of the Insanity.json effect.
        insanityDamageCause = new DamageCause("Insanity", "Insanity", false, true, true);
        return insanityDamageCause;
    }

    @Nullable
    private static EntityEffect getInsanityEntityEffect() {
        if (insanityEntityEffect == null) {
            insanityEntityEffect = EntityEffect.getAssetMap().getAsset(insanityEffectId);
        }
        return insanityEntityEffect;
    }

    @Nullable
    private static EntityEffect getSerenityEntityEffect() {
        if (serenityEntityEffect == null) {
            serenityEntityEffect = EntityEffect.getAssetMap().getAsset(serenityEffectId);
        }
        return serenityEntityEffect;
    }

    private static boolean activeEntityEffectIsInsanity(ActiveEntityEffect effect) {
        return checkEffectId(effect, insanityEffectId);
    }

    private static boolean activeEntityEffectIsSerenity(ActiveEntityEffect effect) {
        return checkEffectId(effect, serenityEffectId);
    }

    private static boolean checkEffectId(ActiveEntityEffect effect, String effectId) {
        try {
            Field f = ActiveEntityEffect.class.getDeclaredField("entityEffectId");
            f.setAccessible(true);
            String id = (String) f.get(effect);
            return id != null && id.equals(effectId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String[] HALLUCINATION_PHRASES = {
            "GG", "F", "AFK", "OMG", "LOL", "XD", "help", "s.o.s", "tp", "bro", "men", "hello", "hi", ";)", "^_^",
            "???", ":P", ":(", ">:(", "UwU", "<3", "+_+", "=("
    };

    private static final String[] RANDOM_MOBS = {
            "Toad_Rhino", "Larva_Silk", "Larva_Void", "Rat", "Spider", "Spirit_Ember", "Raptor_Cave",
            "Toad_Rhino_Magma", "Skeleton_Fighter_Wander", "Skeleton_Mage", "Skeleton_Fighter",
            "Skeleton_Archmage", "Skeleton_Archer", "Skeleton_Ranger", "Skeleton_Knight",
            "Skeleton_Soldier", "Skeleton_Scout", "Skeleton_Pirate_Gunner", "Skeleton_Pirate_Captain",
            "Skeleton_Sand_Mage", "Skeleton_Sand_Guard", "Skeleton_Sand_Ranger", "Skeleton_Sand_Archer",
            "Skeleton_Sand_Soldier", "Skeleton_Sand_Scout", "Goblin_Scrapper", "Skeleton_Sand_Assassin",
            "Goblin_Miner_Patrol", "Wolf_Trork_Shaman",
            "Wolf_Trork_Hunter", "Skeleton_Frost_Fighter", "Skeleton_Frost_Scout", "Goblin_Scrapper_Patrol",
            "Skeleton_Sand_Archmage", "Goblin_Miner", "Skeleton_Frost_Ranger", "Skeleton_Incandescent_Fighter",
            "Skeleton_Incandescent_Footman", "Skeleton_Frost_Archer", "Skeleton_Incandescent_Head",
            "Skeleton_Incandescent_Mage", "Skeleton_Frost_Archmage", "Skeleton_Frost_Mage",
            "Skeleton_Frost_Knight", "Skeleton_Frost_Soldier"
    };

    private static final String[] RANDOM_SOUNDS = {
            "SFX_072744_ascend_With_Us_Creepy_Whisper", "SFX_075283_behind_You_Whisper",
            "SFX_076545_it39s_Your_Fault_Whisper",
            "SFX_15_Arana_Acercandose_Creapy_Spider_Approaching", "SFX_1_An_ominous_Crow_Call", "SFX_Accordion_Whisper",
            "SFX_Animalistic_Snort", "SFX_Beast_Hiss", "SFX_Bird_Creature_Screams",
            "SFX_Boulder_Impact", "SFX_Cartoon_Scream_1", "SFX_Creaky_Old_Door",
            "SFX_Creepy_Ghost_Sound", "SFX_Creepy_Ghost_Whisper", "SFX_Creepy_Moan",
            "SFX_Creepy_Whisper", "SFX_Creepy_Whispering", "SFX_Crow_Sfx",
            "SFX_Demonic_Whisper", "SFX_Demonic_Woman_Scream", "SFX_Dinosaur_Growl",
            "SFX_Door_Close", "SFX_Door_Slam", "SFX_Echo_Jumpscare",
            "SFX_Eerie_Sudden_Shock", "SFX_Epic_Dragon_Roar", "SFX_Evil_Laugh",
            "SFX_Evil_Laugh_With_Reverb", "SFX_Evil_Laughing", "SFX_Exhale",
            "SFX_Explosion_Effect", "SFX_Explosion_With_Debris", "SFX_Eyesaur_Jumpscare_Sound",
            "SFX_Falling_Man_Scream", "SFX_Fast_Knocking_On_Door", "SFX_Ghost_Horror_Sound",
            "SFX_Ghostly_Tone", "SFX_Glass_Shatter_7", "SFX_Halloween_Ghost_Whisper",
            "SFX_Heartbeat", "SFX_Heartbeat_1", "SFX_Hellx27s_Kitchen_Violin",
            "SFX_Horror_Hit", "SFX_I_See_You_Creepy_Ghost_Whisper", "SFX_Jump_Scare_Sound_2",
            "SFX_Knocking_Door_1", "SFX_Large_Monster_Attack", "SFX_Long_Howl_Whale_And_Monster",
            "SFX_Male_Death_Scream_Horror", "SFX_Monster_Growl", "SFX_Monster_Jump_Scare",
            "SFX_Old_Church_Bell", "SFX_Open_Door_Stock_Sfx", "SFX_Owl_2",
            "SFX_Possessed_Laugh", "SFX_Scary_Ghost_Whisper", "SFX_Scary_Riser",
            "SFX_Scary_Scream", "SFX_Scary_Sound", "SFX_Scary_Sound_Effect",
            "SFX_Scary_Transition", "SFX_Scream_With_Echo", "SFX_Scream_With_Echo_46585 1",
            "SFX_Sinister_Laugh", "SFX_Spider_Attack_1", "SFX_Spider_Attack_2",
            "SFX_Spider_Attack_3", "SFX_Spider_Attack_4", "SFX_Spooky_Movement_Violin_4",
            "SFX_Spooky_Movement_Violin_6", "SFX_Spooky_Scary_Sound", "SFX_Spooky_Transition",
            "SFX_Spooky_Whisper", "SFX_Spooky_Wolf_Howl", "SFX_Susurro_Conjuro",
            "SFX_Swoosh_Whisper_1", "SFX_Swoosh_Whisper_2", "SFX_Thud_Impact_Sound_Sfx",
            "SFX_Vampires_Monster_Horror_Orchestra_Warning", "SFX_Violin_Glissando_10", "SFX_Violin_Glissando_2",
            "SFX_Violin_Spiccato_G2", "SFX_Violindanger", "SFX_Whisper_Trail_3",
            "SFX_Whispering_Swoosh_3", "SFX_Whispers_And_Screams", "SFX_Whoosh_Whisper_3",
            "SFX_Witch_Laugh", "SFX_Zombie_Sfx"
    };

    private final Random random = new Random();

    public SanitySystem() {
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        // Clon exacto de ThirstSystem.getGroup()
        return DamageModule.get().getGatherDamageGroup();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Clon exacto de ThirstSystem.getQuery() (adaptado a SanityComponent)
        return Query.and(
                SanityComponent.getComponentType(),
                PlayerRef.getComponentType(),
                com.hypixel.hytale.server.core.entity.entities.Player.getComponentType(),
                TransformComponent.getComponentType(),
                DamageDataComponent.getComponentType(),
                Query.not(DeathComponent.getComponentType()),
                Query.not(Invulnerable.getComponentType()));
    }

    @Override
    public void tick(
            float dt,
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        SanityComponent sanity = archetypeChunk.getComponent(index, SanityComponent.getComponentType());
        com.hypixel.hytale.server.core.entity.entities.Player playerComp = archetypeChunk.getComponent(index,
                com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        if (sanity == null)
            return;

        // --- CREATIVE MODE BYPASS ---
        if (playerComp != null && playerComp.getGameMode() == com.hypixel.hytale.protocol.GameMode.Creative) {
            if (sanity.getSanityLevel() < 100.0f) {
                sanity.setSanityLevel(100.0f);
            }
            // Update HUD to 100 if we just changed or if it was different
            PlayerRef pRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
            if (pRef != null) {
                SanityHud.updatePlayerSanityLevel(pRef, 100.0f, false, false, false, false, false, false);
            }
            return; // Skip sanity loss logic
        }

        // 0. Sincronización y Timers Visuales
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        float damageTimer = sanity.getDamageVisualTimer();
        if (damageTimer > 0) {
            sanity.setDamageVisualTimer(Math.max(0, damageTimer - dt));
        }
        float gainTimer = sanity.getGainVisualTimer();
        if (gainTimer > 0) {
            sanity.setGainVisualTimer(Math.max(0, gainTimer - dt));
        }
        float staminaVisualTimer = sanity.getStaminaVisualTimer();
        if (staminaVisualTimer > 0) {
            sanity.setStaminaVisualTimer(Math.max(0, staminaVisualTimer - dt));
        }

        // Logic for jumpscare timer removed as it's handled by EntityEffect duration.

        // 0.5 Detección de Descanso (Camas, Sillas y Animales)
        float gainAmount = 0.0f;

        // Bloques (Camas/Sillas) - Usan MountedComponent en el jugador
        MountedComponent mounted = store.getComponent(ref, MountedComponent.getComponentType());
        if (mounted != null && !sanity.hasAgro()) {
            if (mounted.getMountedToBlock() != null && mounted.getMountedToBlock().isValid()) {
                gainAmount = (mounted.getBlockMountType() == BlockMountType.Bed) ? 10.0f : 2.5f;
            } else if (mounted.getMountedToEntity() != null && mounted.getMountedToEntity().isValid()) {
                gainAmount = 10.0f; // Vínculo animal si está vía MountedComponent
            }
        }

        // Animales (NPCs) - Búsqueda de reserva por NPC dueño
        if (gainAmount <= 0 && !sanity.hasAgro()) {
            final PlayerRef currentPlayerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
            if (currentPlayerRef != null) {
                boolean isRidingNPC = store.forEachChunk(Query.and(NPCMountComponent.getComponentType()),
                        (chunk, cmd) -> {
                            for (int i = 0; i < chunk.size(); i++) {
                                NPCMountComponent npcMount = chunk.getComponent(i,
                                        NPCMountComponent.getComponentType());
                                if (npcMount != null && currentPlayerRef.equals(npcMount.getOwnerPlayerRef())) {
                                    return true;
                                }
                            }
                            return false;
                        });

                if (isRidingNPC) {
                    gainAmount = 10.0f;
                }
            }
        }

        if (gainAmount > 0) {
            float currentLevel = sanity.getSanityLevel();
            if (currentLevel < SanityComponent.maxSanityLevel) {
                float newLevel = Math.min(SanityComponent.maxSanityLevel, currentLevel + gainAmount * dt);
                sanity.setSanityLevel(newLevel);
                sanity.setGainVisualTimer(0.5f); // Brillo amarillo constante
            }
        }

        // 1. Detección de Daño (Pérdida instantánea)
        DamageDataComponent damageData = archetypeChunk.getComponent(index, DamageDataComponent.getComponentType());
        if (damageData != null) {
            java.time.Instant currentDamageTime = damageData.getLastDamageTime();
            if (currentDamageTime.isAfter(sanity.getLastDamageTimestamp())) {
                float damageLoss = AquaSanity.get().getConfigManager().getSanityConfig().getDamageSanityLoss();
                sanity.setSanityLevel(Math.max(0, sanity.getSanityLevel() - damageLoss));
                sanity.setLastDamageTimestamp(currentDamageTime);
                sanity.setDamageVisualTimer(1.5f); // Activa el rojo intenso
            }
        }

        // 1.5 Detección de Estamina (Pérdida por agotamiento)
        EntityStatMap stats = archetypeChunk.getComponent(index, EntityStatMap.getComponentType());
        if (stats != null) {
            EntityStatValue staminaStat = stats.get(DefaultEntityStatTypes.getStamina());
            float stamina = (staminaStat != null) ? staminaStat.get() : 10.0f;
            if (stamina <= 0.1f) {
                sanity.setStaminaPenaltyTimer(sanity.getStaminaPenaltyTimer() + dt);
                if (sanity.getStaminaPenaltyTimer() >= 1.2f) { // Valor "pulido" por el usuario
                    sanity.setSanityLevel(Math.max(0, sanity.getSanityLevel() - 2.0f));
                    sanity.setStaminaPenaltyTimer(0.0f);
                    sanity.setStaminaVisualTimer(1.0f); // Activa el naranja en el HUD
                }
            } else {
                sanity.setStaminaPenaltyTimer(0.0f);
            }
        }

        // 2. Detección de Luz (Visual instantáneo)
        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        World world = store.getExternalData().getWorld();
        WorldTimeResource worldTime = store.getResource(WorldTimeResource.getResourceType());

        boolean isInDarkness = true;
        byte currentLightLevel = 0;
        byte blockLightLevel = 0;
        float threshold = AquaSanity.get().getConfigManager().getSanityConfig().getLightThreshold(); // ← mover aquí
        if (world != null && transform != null) {
            Vector3d pos = transform.getPosition();
            int lx = MathUtil.floor(pos.getX());
            int ly = MathUtil.floor(pos.getY());
            int lz = MathUtil.floor(pos.getZ());
            WorldChunk lChunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(lx, lz));
            if (lChunk != null) {
                BlockChunk blockChunk = lChunk.getBlockChunk();
                if (blockChunk != null) {
                    blockLightLevel = blockChunk.getBlockLightIntensity(lx, ly, lz); // sin "byte" aquí
                    byte skyLightLevel = (byte) ((double) blockChunk.getSkyLight(lx, ly, lz)
                            * worldTime.getSunlightFactor());
                    currentLightLevel = (byte) Math.max(blockLightLevel, skyLightLevel);
                    isInDarkness = (currentLightLevel <= threshold);
                }
            }
        }
        sanity.setIsInDarkness(isInDarkness);

        // 3. Detección de Agro (Filtrado por Hostilidad)
        final Ref<EntityStore> playerRefForAgro = ref;
        final Vector3d playerPos = transform != null ? transform.getPosition() : null;

        boolean hasAgro = store.forEachChunk(Query.and(NPCEntity.getComponentType()),
                (archetypeChunkAgro, commandBufferAgro) -> {
                    for (int i = 0; i < archetypeChunkAgro.size(); ++i) {
                        NPCEntity npc = archetypeChunkAgro.getComponent(i, NPCEntity.getComponentType());
                        if (npc == null || npc.getRole() == null)
                            continue;

                        // Solo mobs que no sean amigos y no estén muertos/muriendo
                        if (npc.getRole().isFriendly(playerRefForAgro, store) || 
                            archetypeChunkAgro.getComponent(i, DeathComponent.getComponentType()) != null)
                            continue;

                        // FILTRO DE HOSTILES (Hostilidad Real):
                        // Usamos el sistema de actitudes de Hytale, pero con protecciones para evitar
                        // crashes
                        // y descartar mobs pasivos que puedan estar "siguiendo" al jugador (como vacas
                        // con trigo).

                        // 1. Usar el filtro centralizado de pasivos
                        if (SanityFilters.isPassive(npc)) {
                            continue;
                        }

                        // 2. Comprobar Actitud (con catch para evitar el NPE del motor si el cache es
                        // null)
                        Ref<EntityStore> npcRef = archetypeChunkAgro.getReferenceTo(i);
                        try {
                            if (npc.getRole().getWorldSupport() != null) {
                                Attitude attitude = npc.getRole().getWorldSupport().getAttitude(npcRef,
                                        playerRefForAgro, store);
                                if (attitude != Attitude.HOSTILE)
                                    continue;
                            }
                        } catch (Exception e) {
                            // Si el sistema de actitud del motor falla, confiamos en el filtro de nombres y
                            // IsFriendly
                        }

                        MarkedEntitySupport markedEntities = npc.getRole().getMarkedEntitySupport();
                        if (markedEntities == null)
                            continue;

                        Ref<EntityStore> target = markedEntities.getMarkedEntityRef("LockedTarget");
                        if (playerRefForAgro.equals(target)) {
                            if (playerPos != null) {
                                TransformComponent npcTransform = archetypeChunkAgro.getComponent(i,
                                        TransformComponent.getComponentType());
                                if (npcTransform != null) {
                                    Vector3d npcPos = npcTransform.getPosition();
                                    double distSq = Vector3d.distanceSquared(playerPos.getX(), playerPos.getY(),
                                            playerPos.getZ(), npcPos.getX(), npcPos.getY(), npcPos.getZ());

                                    // 24 bloques de rango
                                    if (distSq <= 576) {
                                        // Cálculo de ojos dinámico para LOS
                                        double npcEyeHeight = 1.6;
                                        ModelComponent modelComp = archetypeChunkAgro.getComponent(i,
                                                ModelComponent.getComponentType());
                                        if (modelComp != null && modelComp.getModel() != null) {
                                            npcEyeHeight = modelComp.getModel().getEyeHeight();
                                        }

                                        Vector3d npcEye = new Vector3d(npcPos.getX(), npcPos.getY() + npcEyeHeight,
                                                npcPos.getZ());
                                        Vector3d playerEye = new Vector3d(playerPos.getX(), playerPos.getY() + 1.6,
                                                playerPos.getZ());

                                        if (isLineOfSightClear(world, npcEye, playerEye)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return false;
                });
        sanity.setHasAgro(hasAgro);

        // 4. Actualización del HUD (Visual instantáneo en cada tick)
        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        float insanityThreshold = AquaSanity.get().getConfigManager().getSanityConfig().getInsanity();
        if (playerRef != null) {
            boolean isTakingDamage = sanity.getDamageVisualTimer() > 0;
            boolean isGainingSanity = sanity.getGainVisualTimer() > 0;
            boolean isStaminaDepleted = sanity.getStaminaVisualTimer() > 0;
            boolean isInsane = sanity.getSanityLevel() <= insanityThreshold;
            SanityHud.updatePlayerSanityLevel(playerRef, sanity.getSanityLevel(), hasAgro, isInDarkness, isTakingDamage,
                    isGainingSanity, isStaminaDepleted, isInsane);
        }

        // 5. Aplicación de Efectos de Locura y Serenidad
        EffectControllerComponent effectController = archetypeChunk.getComponent(index,
                EffectControllerComponent.getComponentType());

        // 6. COMPUERTA TEMPORAL PARA PÉRDIDA DE CORDURA
        sanity.addElapsedTime(dt);
        float lossInterval = AquaSanity.get().getConfigManager().getSanityConfig().getSanityLossInterval();
        float currentSanity = sanity.getSanityLevel();
        if (sanity.getElapsedTime() >= lossInterval) {
            sanity.resetElapsedTime();

            if (isInDarkness || hasAgro) {
                // Zona peligrosa: pierde cordura y reinicia el acumulador de zona segura
                sanity.setSanityLevel(Math.max(0, sanity.getSanityLevel() - 0.5f));
                sanity.setSafeZoneElapsedTime(0);
            }

            // --- DAMAGE AT 0 SANITY ---
            if (sanity.getSanityLevel() <= 0.01f
                    && AquaSanity.get().getConfigManager().getSanityConfig().isEnableDamage()) {
                Damage damage = new Damage(Damage.NULL_SOURCE, getInsanityDamageCause(), 2.0f);
                DamageSystems.executeDamage(ref, commandBuffer, damage);
            }
        }
        currentSanity = sanity.getSanityLevel();

        // --- INSANITY EFFECT ---
        if (currentSanity <= 0.01f) {
            if (effectController != null) {
                EntityEffect insanityEffect = getInsanityEntityEffect();
                if (insanityEffect != null)
                    effectController.addEffect(ref, insanityEffect, commandBuffer);
            }
        } else {
            // Remove Insanity Effect if not at 0 sanity
            if (effectController != null) {
                final ActiveEntityEffect[] activeEffects = effectController.getAllActiveEntityEffects();
                if (activeEffects != null && activeEffects.length > 0) {
                    Arrays.stream(activeEffects)
                            .filter(SanitySystem::activeEntityEffectIsInsanity)
                            .forEach(effect -> effectController.removeEffect(ref, effect.getEntityEffectIndex(),
                                    commandBuffer));
                }
            }
        }

        // --- 1.7 Critical Threshold Event (Level 5) ---
        if (currentSanity <= 5.0f && !sanity.hasTriggeredAtFive()) {
            triggerRandomInsanityEvent(ref, sanity, commandBuffer);
            if (random.nextDouble() < 0.50) {
                float recovery = 15.0f + random.nextFloat() * 25.0f;
                sanity.setSanityLevel(Math.min(SanityComponent.maxSanityLevel, sanity.getSanityLevel() + recovery));
                sanity.setGainVisualTimer(2.5f);
                currentSanity = sanity.getSanityLevel(); // Actualizar nivel actual tras recuperación
            }
            sanity.setTriggeredAtFive(true);
            sanity.setRandomEventCooldown(15.0f); // Pausa para el jugador
        } else if (currentSanity > 5.5f) {
            sanity.setTriggeredAtFive(false);
        }

        // 2. Random Events between Threshold and 0 (Jumpscares & Random Damage)
        if (currentSanity <= insanityThreshold && currentSanity > 0.01f) {
            float eventCooldown = sanity.getRandomEventCooldown();
            if (eventCooldown > 0) {
                sanity.setRandomEventCooldown(eventCooldown - dt);
            } else {
                triggerRandomInsanityEvent(ref, sanity, commandBuffer);
                // regeneracion de cordura por evento
                float recoveryChance = AquaSanity.get().getConfigManager().getSanityConfig().getEventRecoveryChance() / 100.0f;
                if (random.nextDouble() < recoveryChance) {
                    float recovery = 15.0f + random.nextFloat() * 25.0f; // 15 a 40
                    sanity.setSanityLevel(Math.min(SanityComponent.maxSanityLevel, sanity.getSanityLevel() + recovery));
                }
                // tiempo de las alucinaciones
                float maxPause = AquaSanity.get().getConfigManager().getSanityConfig().getMaxEventPause();
                sanity.setRandomEventCooldown((float) (Math.random() * maxPause));
            }
        }

        if (effectController != null) {
            if (sanity.getGainVisualTimer() > 0) {
                EntityEffect serenityEffect = getSerenityEntityEffect();
                if (serenityEffect != null)
                    effectController.addEffect(ref, serenityEffect, commandBuffer);
            } else {
                final ActiveEntityEffect[] activeEffects = effectController.getAllActiveEntityEffects();
                if (activeEffects != null && activeEffects.length > 0) {
                    Arrays.stream(activeEffects)
                            .filter(SanitySystem::activeEntityEffectIsSerenity)
                            .forEach(effect -> effectController.removeEffect(ref, effect.getEntityEffectIndex(),
                                    commandBuffer));
                }
            }
        }

        // Regeneración de zona segura: acumula dt directamente (independiente de
        // lossInterval)
        if (!isInDarkness && !hasAgro) {
            float L = (blockLightLevel & 0xFF);

            float L_min = threshold + 1.0f;
            float L_max = threshold + (15.0f - threshold) * 0.6f;
            float factor = Math.max(0f, Math.min(1f, (L - L_min) / (L_max - L_min + 0.1f)));

            float targetInterval = 15.0f - (factor * 14.0f); // 15s → 1s
            float targetAmount = 5.0f + (factor * 10.0f); // 5pts → 15pts

            sanity.addSafeZoneElapsedTime(dt);

            if (sanity.getSafeZoneElapsedTime() >= targetInterval) {
                sanity.setSafeZoneElapsedTime(0);
                if (sanity.getSanityLevel() < SanityComponent.maxSanityLevel) {
                    sanity.setSanityLevel(
                            Math.min(SanityComponent.maxSanityLevel, sanity.getSanityLevel() + targetAmount));
                    sanity.setGainVisualTimer(2.0f);
                }
            }
        } else {
            sanity.setSafeZoneElapsedTime(0);
        }
    }

    private void triggerRandomInsanityEvent(Ref<EntityStore> ref, SanityComponent sanity,
            CommandBuffer<EntityStore> commandBuffer) {

        // 7 eventos con probabilidad uniforme (~14.28% cada uno)
        int event = random.nextInt(7);
        triggerHallucinationSound(ref, commandBuffer);

        switch (event) {
            case 0 -> { // Jumpscare
                int idx = random.nextInt(7) + 1;
                applyEntityEffect(ref, "Jumpscare" + idx, commandBuffer);
            }
            case 1 -> // Mensaje falso
                triggerFakeMessageHallucination(ref);
            case 2 -> // Teleport aleatorio
                triggerRandomTeleport(ref, commandBuffer);
            case 3 -> // Camera shake
                triggerRandomCameraShake(ref, commandBuffer);
            case 4 -> { // Efecto de estado aleatorio (Burn, Poison o Slow)
                String[] effects = { "Burn", "Poison", "Slow" };
                applyEntityEffect(ref, effects[random.nextInt(effects.length)], commandBuffer);
            }
            case 5 -> // Spawn de mob aleatorio
                triggerRandomMobSpawn(ref, commandBuffer);
            case 6 -> { // Daño aleatorio
                int[] damageAmounts = { 5, 10, 3 };
                int amount = damageAmounts[random.nextInt(damageAmounts.length)];
                Damage damage = new Damage(Damage.NULL_SOURCE, getInsanityDamageCause(), (float) amount);
                DamageSystems.executeDamage(ref, commandBuffer, damage);
                sanity.setDamageVisualTimer(1.5f);
            }
        }
    }

    private void triggerHallucinationSound(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        if (RANDOM_SOUNDS.length == 0)
            return;

        String soundName = RANDOM_SOUNDS[random.nextInt(RANDOM_SOUNDS.length)];

        // Obtener el índice del SoundEvent por nombre
        int soundIndex = com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent
                .getAssetMap().getIndex(soundName);

        if (soundIndex < 0) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null)
            return;

        Vector3d pos = transform.getPosition();
        com.hypixel.hytale.server.core.universe.world.SoundUtil.playSoundEvent3d(
                ref, soundIndex, pos.getX(), pos.getY(), pos.getZ(), false, store);

    }

    private void triggerRandomMobSpawn(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        String mobName = RANDOM_MOBS[random.nextInt(RANDOM_MOBS.length)];

        final Store<EntityStore> store = ref.getStore();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null)
            return;

        final Vector3d spawnPos = transform.getPosition().add(0.5, 0, 0.5);
        final int roleIndex = NPCPlugin.get().getIndex(mobName);
        Builder<Role> roleBuilder = NPCPlugin.get().tryGetCachedValidRole(roleIndex);

        if (roleBuilder == null) {
            return;
        }

        try {
            ISpawnableWithModel spawnable = (ISpawnableWithModel) roleBuilder;
            SpawningContext context = new SpawningContext();
            context.setSpawnable(spawnable);
            final Model model = context.getModel();

            // ECS Rule: No podemos modificar el Store (spawnEntity) mientras el sistema
            // está tickeando.
            // Usamos world.execute para diferir el spawn de forma segura.
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
                try {
                    NPCPlugin.get().spawnEntity(store, roleIndex, spawnPos, new Vector3f(0, 0, 0), model, null);
                } catch (Exception ex) {
                }
            });

        } catch (Exception e) {
        }
    }

    private void applyEntityEffect(Ref<EntityStore> ref, String effectId, CommandBuffer<EntityStore> commandBuffer) {
        EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
        EffectControllerComponent effectController = ref.getStore().getComponent(ref,
                EffectControllerComponent.getComponentType());
        if (effectController != null && effect != null) {
            effectController.addEffect(ref, effect, commandBuffer);
        } else if (effect == null) {
        }
    }

    private void triggerRandomCameraShake(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        String[] shakes = { "InsanityShake_Camera", "MildShake_Camera", "MicroShake_Camera" };
        String chosen = shakes[random.nextInt(shakes.length)];
        triggerNamedCameraShake(ref, chosen);
    }

    private void triggerNamedCameraShake(Ref<EntityStore> ref, String effectName) {
        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null)
            return;

        com.hypixel.hytale.server.core.asset.type.camera.CameraEffect cameraEffectAsset = com.hypixel.hytale.server.core.asset.type.camera.CameraEffect
                .getAssetMap()
                .getAsset(effectName);

        if (cameraEffectAsset == null) {
            return;
        }

        com.hypixel.hytale.protocol.packets.camera.CameraShakeEffect packet = cameraEffectAsset
                .createCameraShakePacket();

        playerRef.getPacketHandler().write(packet);
    }

    private int findSafeSurfaceY(World world, int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; y--) {
            try {
                int ground = world.getBlock(x, y, z);
                if (ground != 0
                        && world.getBlock(x, y + 1, z) == 0
                        && world.getBlock(x, y + 2, z) == 0
                        && hasSolidAround(world, x, y, z)) {
                    return y;
                }
            } catch (Exception e) {
                // skip
            }
        }
        return -1;
    }

    private boolean hasSolidAround(World world, int x, int y, int z) {
        return world.getBlock(x + 1, y, z) != 0
                && world.getBlock(x - 1, y, z) != 0
                && world.getBlock(x, y, z + 1) != 0
                && world.getBlock(x, y, z - 1) != 0;
    }

    private void triggerRandomTeleport(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        tryRandomTeleport(ref, commandBuffer, 0);
    }

    private void tryRandomTeleport(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, int attempt) {
        if (attempt >= 5) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null)
            return;

        Vector3d pos = transform.getPosition();
        double angle = random.nextDouble() * 2.0 * Math.PI;
        double dist = 3.0 + random.nextDouble() * 12.0;
        double nx = pos.getX() + Math.cos(angle) * dist;
        double nz = pos.getZ() + Math.sin(angle) * dist;

        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            int startY = (int) Math.floor(pos.getY());
            int minY = Math.max(0, startY - 5);
            int maxY = Math.min(319, startY + 5);
            int safeY = findSafeSurfaceY(world, (int) Math.floor(nx), (int) Math.floor(nz), minY, maxY);

            if (safeY < 0) {
                tryRandomTeleport(ref, commandBuffer, attempt + 1);
                return;
            }

            double teleportY = safeY + 1.0;
            Vector3d target = new Vector3d(nx, teleportY, nz);
            Teleport teleport = Teleport.createForPlayer(target, transform.getRotation());
            store.addComponent(ref, Teleport.getComponentType(), teleport);

        });
    }

    private void triggerFakeMessageHallucination(Ref<EntityStore> ref) {
        Store<EntityStore> store = ref.getStore();
        PlayerRef targetPlayer = store.getComponent(ref, PlayerRef.getComponentType());
        if (targetPlayer == null)
            return;

        String phrase = HALLUCINATION_PHRASES[random.nextInt(HALLUCINATION_PHRASES.length)];
        List<String> possibleNames = new ArrayList<>();
        possibleNames.add(targetPlayer.getUsername());
        possibleNames.add("Simon");
        possibleNames.add("Jume");

        store.forEachChunk(Query.and(PlayerRef.getComponentType()), (chunk, cmd) -> {
            for (int i = 0; i < chunk.size(); i++) {
                PlayerRef p = chunk.getComponent(i, PlayerRef.getComponentType());
                if (p != null && !p.equals(targetPlayer)) {
                    possibleNames.add(p.getUsername());
                }
            }
            return false;
        });

        String senderName = possibleNames.get(random.nextInt(possibleNames.size()));
        targetPlayer.sendMessage(Message.empty().insert(senderName + ": " + phrase));
    }

    /**
     * Verifica si hay línea de visión clara entre dos puntos (raycasting simple).
     */
    private boolean isLineOfSightClear(World world, Vector3d start, Vector3d end) {
        double distSq = Vector3d.distanceSquared(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(),
                end.getZ());
        if (distSq < 0.25)
            return true; // Muy cerca

        double dist = Math.sqrt(distSq);
        int steps = Math.max(1, (int) (dist * 2.0)); // 2 muestras por bloque para precisión

        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            double px = start.getX() + (end.getX() - start.getX()) * t;
            double py = start.getY() + (end.getY() - start.getY()) * t;
            double pz = start.getZ() + (end.getZ() - start.getZ()) * t;

            int bx = MathUtil.floor(px);
            int by = MathUtil.floor(py);
            int bz = MathUtil.floor(pz);

            if (by < 0 || by >= 320)
                continue;

            long chunkIndex = ChunkUtil.indexChunkFromBlock(bx, bz);
            WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);
            if (worldChunk != null) {
                BlockChunk blockChunk = worldChunk.getBlockChunk();
                if (blockChunk != null) {
                    int blockId = blockChunk.getBlock(bx & 31, by, bz & 31);
                    if (blockId != 0) {
                        BlockType type = (BlockType) BlockType.getAssetMap().getAsset(blockId);
                        // Si el bloque no es transparente, bloquea la visión
                        if (type != null && type.getOpacity() != Opacity.Transparent) {
                            return false;
                        }
                    }
                }
            } else {
                // Si el chunk no está cargado, asumimos bloqueo por seguridad
                return false;
            }
        }
        return true;
    }
}
