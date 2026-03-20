package org.blulio.stolClans;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import net.md_5.bungee.api.ChatColor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class JoinHandler implements Listener {
    //public final String fileName;
    public final StolClans plugin;
    private File file;
    private FileConfiguration config;
    private boolean fileExists = false;
    private File coreFile;
    public JoinHandler (StolClans plugin) {
        this.plugin = plugin;
        coreFile = new File(plugin.getServer().getWorldContainer().getAbsolutePath() + "/plugins/StolCore/config.yml");
        fileExists = coreFile.exists();
        if (!fileExists) {
            plugin.getLogger().log(Level.INFO, "[StolClans] StolCore config file not found!");
        }
        /*try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            file = new File(dataFolder, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            config = YamlConfiguration.loadConfiguration(file);
            config.createSection("players");
            save();
        } catch (IOException exception) {
            exception.printStackTrace();
        }*/
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        String msg = evt.getJoinMessage();
        //String joinMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("join_msg"));
        String playerUUID = player.getUniqueId().toString();

        ConfigurationSection stolCore = YamlConfiguration.loadConfiguration(coreFile);
        ConfigurationSection plus = stolCore.getConfigurationSection("plus");
        if (plus.contains(playerUUID)) {
            if (stolCore.getConfigurationSection("plusCustomJoinMessages").contains(playerUUID)) {
                // custom join message, don't do any modifications
                plugin.tabHandler.updateTabPlayers();
                return;
            };
        }

        evt.setJoinMessage(
                msg.replaceAll(player.getDisplayName(), plugin.clans.previewText(player))
        );
        plugin.tabHandler.updateTabPlayers();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(PlayerQuitEvent evt) {
        Player player = evt.getPlayer();
        String msg = evt.getQuitMessage();
        //String leaveMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("quit_msg"));
        String playerUUID = player.getUniqueId().toString();

        ConfigurationSection stolCore = YamlConfiguration.loadConfiguration(coreFile);
        ConfigurationSection plus = stolCore.getConfigurationSection("plus");
        if (plus.contains(playerUUID)) {
            if(stolCore.getConfigurationSection("plusCustomLeaveMessages").contains(playerUUID)) {
                // custom join message, don't do any modifications
                plugin.tabHandler.updateTabPlayers();
                return;
            };
        }

        evt.setQuitMessage(
                msg.replaceAll(player.getDisplayName(), plugin.clans.previewText(player))
        );
        plugin.tabHandler.updateTabPlayers();
    }

    /*public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
