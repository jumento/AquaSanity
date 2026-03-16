package mx.jume.aquasanity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mx.jume.aquasanity.AquaSanity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    private static final String CONFIG_FILE = "SanityConfig.json";

    private final Path dataDirectory;
    private AquaSanityConfig sanityConfig;

    public ConfigManager(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void load() {
        File dir = dataDirectory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.sanityConfig = loadConfig(CONFIG_FILE, AquaSanityConfig.class, new AquaSanityConfig());

        // Si la version del archivo es menor que la actual, crear backup antes de reescribir
        if (sanityConfig.getConfigVersion() < AquaSanityConfig.CURRENT_VERSION) {
            backupConfig(CONFIG_FILE);
            if (AquaSanity.LOGGER != null) {
                AquaSanity.LOGGER.at(Level.INFO).log(
                        "[AquaSanity] Config upgraded: v" + sanityConfig.getConfigVersion()
                                + " -> v" + AquaSanityConfig.CURRENT_VERSION
                                + " (backup saved as " + CONFIG_FILE + ".bak)");
            }
            sanityConfig.setConfigVersion(AquaSanityConfig.CURRENT_VERSION);
        }

        save();
    }

    /**
     * Copia el config actual a .bak para que el usuario no pierda su configuracion
     * personalizada al actualizar la version del plugin.
     */
    private void backupConfig(String fileName) {
        Path source = dataDirectory.resolve(fileName);
        if (!Files.exists(source)) {
            return;
        }
        Path backup = dataDirectory.resolve(fileName + ".bak");
        try {
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            if (AquaSanity.LOGGER != null) {
                AquaSanity.LOGGER.at(Level.WARNING).withCause(e).log(
                        "Failed to create config backup: " + fileName + ".bak");
            }
        }
    }

    private <T> T loadConfig(String fileName, Class<T> clazz, T defaultValue) {
        File file = new File(dataDirectory.toFile(), fileName);
        if (!file.exists()) {
            return defaultValue;
        }
        try (FileReader reader = new FileReader(file)) {
            T config = GSON.fromJson(reader, clazz);
            return config != null ? config : defaultValue;
        } catch (IOException e) {
            if (AquaSanity.LOGGER != null) {
                AquaSanity.LOGGER.at(Level.SEVERE).withCause(e).log("Failed to load config: " + fileName);
            }
            return defaultValue;
        }
    }

    public void save() {
        File dir = dataDirectory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dataDirectory.toFile(), CONFIG_FILE);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(sanityConfig, writer);
        } catch (IOException e) {
            if (AquaSanity.LOGGER != null) {
                AquaSanity.LOGGER.at(Level.SEVERE).withCause(e).log("Failed to save config: " + CONFIG_FILE);
            }
        }
    }

    public AquaSanityConfig getSanityConfig() {
        return sanityConfig;
    }
}
