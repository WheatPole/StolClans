package org.wple.stolClans;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ClanConfig {
    private final JavaPlugin plugin;
    private File file;
    private FileConfiguration config;
    private String filename;

    public ClanConfig(JavaPlugin plugin, String configFilename) {
        this.plugin = plugin;
        this.filename = configFilename;
        createClanConfig();
    }


    private void createClanConfig() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            file = new File(dataFolder, filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            config = YamlConfiguration.loadConfiguration(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
}
