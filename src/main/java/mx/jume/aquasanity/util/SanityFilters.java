package mx.jume.aquasanity.util;

import com.hypixel.hytale.server.npc.entities.NPCEntity;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SanityFilters {

    private static final Set<String> PASSIVE_IDS = new HashSet<>(Arrays.asList(
            "rabbit", "bunny", "sheep", "lamb", "ram", "cow", "calf", "pig", "piglet",
            "chicken", "chick", "duck", "goose", "turkey", "deer", "doe", "fawn", "stag",
            "moose", "horse", "foal", "camel", "bison", "goat", "kid", "mouflon",
            "sparrow", "pigeon", "owl", "parrot", "crow", "raven", "penguin", "flamingo",
            "hawk", "woodpecker", "bluebird", "greenfinch", "canary",
            "squirrel", "mouse", "rat", "frog", "gecko", "squirrel", "meerkat",
            "tortoise", "crab", "lobster", "snail", "eel", "whale", "dolphin",
            "trout", "salmon", "catfish", "pike", "bluegill", "bass", "clownfish",
            "tang", "piranha", "minnow", "jellyfish", "butterfly", "bee", "kweebec",
            "klops", "trillodon", "tetrabird", "boar", "pterodactyl", "bat"));

    public static boolean isPassive(NPCEntity npc) {
        if (npc == null || npc.getRole() == null)
            return false;

        String npcId = npc.getNPCTypeId() != null ? npc.getNPCTypeId() : "unknown";
        String roleName = npc.getRole().getClass().getSimpleName();
        String lowerId = npcId.toLowerCase();

        // 1. Check against hardcoded passive list
        for (String passive : PASSIVE_IDS) {
            if (lowerId.contains(passive)) {
                return true;
            }
        }

        // 2. Check roles and keywords as fallback
        return lowerId.contains("animal") || lowerId.contains("passive") || lowerId.contains("ambient") ||
                roleName.contains("Passive") || roleName.contains("Animal") ||
                roleName.contains("Ambient") || roleName.contains("Mount") ||
                roleName.contains("Pet") || roleName.contains("Quest") ||
                roleName.contains("Flee") || roleName.contains("Critter");
    }
}
