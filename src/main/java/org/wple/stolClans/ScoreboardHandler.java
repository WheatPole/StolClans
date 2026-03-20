package org.wple.stolClans;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

public class ScoreboardHandler {
    enum Rank {
        MEMBER,
        PLUS,
        ADMIN
    }
    public StolClans plugin;
    public Scoreboard scoreboard;

    private boolean fileExists = false;
    private final File coreFile;

    public boolean isPlus(Player player) {
        String plusPrefix = "";
        if (fileExists) {
            ConfigurationSection stolCore = YamlConfiguration.loadConfiguration(coreFile);
            ConfigurationSection plus = stolCore.getConfigurationSection("plus");
            if (plus.contains(player.getUniqueId().toString())) {
                return true;
                //String plusColor = stolCore.getString( "plus." + player.getName());
                //String plusPrefixFormat = Objects.requireNonNull(stolCore.getString("plusPrefix")).replaceAll("_", plusColor);
                //plusPrefix = org.bukkit.ChatColor.translateAlternateColorCodes('&',plusPrefixFormat) + " ";
            }
        }
        return false;
    }
    public String rankPrefix(Rank rank) {
        switch (rank) {
            case MEMBER: return "c";
            case PLUS: return "b";
            case ADMIN: return "a";
        }
        return "c";
    }
    public ScoreboardHandler(StolClans plugin) {
        this.plugin = plugin;

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();//.getNewScoreboard();
        Objective objective = scoreboard.getObjective("td2core");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("td2core", "dummy");
        }
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        coreFile = new File(plugin.getServer().getWorldContainer().getAbsolutePath() + "/plugins/StolCore/config.yml");
        fileExists = coreFile.exists();
        if (!fileExists) {
            plugin.getLogger().log(Level.INFO, "[StolClans] StolCore config file not found!");
        }
        //members.setPrefix("");
    }
    public void debug() {
        Set<Team> teams = scoreboard.getTeams();
        for (Team team : teams) {
            plugin.getLogger().log(Level.INFO, "[StolClans] Team: " + team.getPrefix() + " " + team.getName());
            Set<String> players = team.getEntries();
            for (String player : players) {
                plugin.getLogger().log(Level.INFO, "[StolClans] Player in team: " + player);
            }
        }
    }
    public void updateClanUser(Player player) {
        Rank playerRank = Rank.MEMBER;
        if (isPlus(player)) playerRank = Rank.PLUS;
        //if (player.isOp()) playerRank = Rank.ADMIN;

        String clanName = plugin.clans.getClanFromUUID(player.getUniqueId().toString());
        if (clanName == null || clanName.isEmpty()) {
            //last team in order
            clanName = "zz";
        }
        String teamName = rankPrefix(playerRank) + clanName;
        Team team = scoreboard.getTeam(teamName);
        //System.out.println(teamName + " " + (team == null));
        if (team == null) {
            //System.out.println(team.toString());
            team = scoreboard.registerNewTeam(teamName);
        }
        //System.out.println(player.getName() + " " + team.hasEntry(player.getUniqueId().toString()));

        if (playerRank == Rank.PLUS) {
            team.setPrefix("(+) ");
        }
        if (!team.hasEntry(player.getName()))
            team.addEntry(player.getName());

    }
    public void removeClanUser(Player player) {
        Rank playerRank = Rank.MEMBER;
        if (isPlus(player)) playerRank = Rank.PLUS;
        //if (player.isOp()) playerRank = Rank.ADMIN;

        String teamName = rankPrefix(playerRank) + "zz";
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        if (playerRank == Rank.PLUS) {
            team.setPrefix("(+) ");
        }

        if (!team.hasEntry(player.getName()))
            team.addEntry(player.getName());

    }
}
