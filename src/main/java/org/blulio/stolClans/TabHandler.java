package org.blulio.stolClans;

import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.ObjectInputFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class TabHandler {
    public StolClans plugin = null;
    private boolean fileExists = false;
    private final File coreFile;
    public TabHandler(StolClans plugin) {
        coreFile = new File(plugin.getServer().getWorldContainer().getAbsolutePath() + "/plugins/StolCore/config.yml");
        fileExists = coreFile.exists();
        if (!fileExists) {
            plugin.getLogger().log(Level.INFO, "[StolClans] StolCore config file not found!");
        }

        this.plugin = plugin;
    }

    public boolean updateTabPlayers() {
        try {
            List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
            for (Player player : playerList) {
                String playerUUID = player.getUniqueId().toString();
                /*String footer = plugin.getConfig().getString("listfooter");
                String header = plugin.getConfig().getString("listheader");
                player.setPlayerListHeader(ChatColor.translateAlternateColorCodes('&', header));
                player.setPlayerListFooter(ChatColor.translateAlternateColorCodes(
                        '&', footer.replaceAll("<player_count>", Integer.toString(playerList.size()))
                ));*/
                String plusPrefix = "";
                if (fileExists) {
                    ConfigurationSection stolCore = YamlConfiguration.loadConfiguration(coreFile);
                    ConfigurationSection plus = stolCore.getConfigurationSection("plus");
                    if (plus.contains(player.getUniqueId().toString())) {
                        String plusColor = stolCore.getString( "plus." + playerUUID );
                        String plusPrefixFormat = Objects.requireNonNull(stolCore.getString("plusPrefix")).replaceAll("_", plusColor);
                        plusPrefix = org.bukkit.ChatColor.translateAlternateColorCodes('&',plusPrefixFormat) + " ";
                    }

                }

                /*String tab = player.getPlayerListName();
                if (tab.charAt(0) == '(') {

                }
                tab = tab.replace(player.getDisplayName(), plugin.clans.previewText(player));*/
                //player.setPlayerListOrder(1);


                player.setPlayerListName(plusPrefix + plugin.clans.previewText(player));
                plugin.scoreboardHandler.updateClanUser(player);
                player.setScoreboard(plugin.scoreboardHandler.scoreboard);
            }
            return true;
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
            return false;
        }
    }
}