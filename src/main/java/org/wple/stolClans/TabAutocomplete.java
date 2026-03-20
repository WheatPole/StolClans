package org.wple.stolClans;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TabAutocomplete implements TabCompleter {
    private final StolClans plugin;
    private final ClanConfig clanConfig;
    private final Clans clans;
    private final String PluginName;
    public TabAutocomplete(StolClans plugin, String PluginName) {
        this.plugin = plugin;
        this.clanConfig = plugin.clanConfig;
        this.PluginName = PluginName;
        this.clans = plugin.clans;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PluginName + " You must run this command as a player.");
            return Collections.emptyList();
        }
        Player player = (Player)sender;
        String playerUUID = player.getUniqueId().toString();
        if (command.getName().equalsIgnoreCase("clan") ||
                command.getName().equalsIgnoreCase("clans")
                || command.getName().equalsIgnoreCase("stolclans")) {
            if (args.length == 1) {
                // not in a clan
                List<String> cmds = new ArrayList<>(Arrays.asList("help", "create", "list", "accept", "reject"));
                // if inside a clan
                if (clans.isInsideClan(playerUUID)) {
                    Collections.addAll(cmds, "view", "properties", "leave", "home", "chat");

                    // if is a moderator
                    if (clans.isMod(clans.getClan(clans.getClanFromUUID(playerUUID)), playerUUID)) {
                        Collections.addAll(cmds, "resign", "invite", "kick", "modify");
                    }
                    // if is the owner
                    if (clans.isOwner(clans.getClan(clans.getClanFromUUID(playerUUID)), playerUUID)) {
                        Collections.addAll(cmds, "promote", "demote", "disband");
                    }
                }
                if (player.isOp()) {
                    Collections.addAll(cmds, "fix", "warn", "disable", "enable", "findview", "findproperties", "setowner", "reload"); // line 56
                }
                return cmds;
            }
            else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("invite")) {
                    // autocomplete with names
                    return null;
                }
                else if (args[0].equalsIgnoreCase("create")) {
                    return Collections.singletonList("[<name>]");
                }
                else if (args[0].equalsIgnoreCase("findview")) {
                    return clans.allClanNames();
                }
                else if (args[0].equalsIgnoreCase("findproperties")) {
                    return clans.allClanNames();
                }
                else if (args[0].equalsIgnoreCase("warn") || args[0].equalsIgnoreCase("setowner")) {
                    return clans.allClanNames();
                }
                else if (args[0].equalsIgnoreCase("disable")) {
                    return clans.allClanNames();
                }
                else if (args[0].equalsIgnoreCase("enable")) {
                    return clans.allClanNames();
                }
                else if (args[0].equalsIgnoreCase("promote")) {
                    List<String> members = clanConfig.getConfig().getConfigurationSection("clans")
                            .getConfigurationSection(clans.getClanFromUUID(playerUUID)).getStringList("members");
                    for (int i = 0; i < members.size(); i++) {
                        members.set(i, Bukkit.getOfflinePlayer(UUID.fromString(members.get(i))).getName());
                    }
                    return members;
                }
                else if (args[0].equalsIgnoreCase("demote")) {
                    List<String> moderators = clanConfig.getConfig().getConfigurationSection("clans")
                            .getConfigurationSection(clans.getClanFromUUID(playerUUID)).getStringList("moderators");
                    for (int i = 0; i < moderators.size(); i++) {
                        moderators.set(i, Bukkit.getOfflinePlayer(UUID.fromString(moderators.get(i))).getName());
                    }
                    return moderators;
                }
                else if (args[0].equalsIgnoreCase("kick")) {
                    List<String> members = clanConfig.getConfig().getConfigurationSection("clans")
                            .getConfigurationSection(clans.getClanFromUUID(playerUUID)).getStringList("members");
                    for (int i = 0; i < members.size(); i++) {
                        members.set(i, Bukkit.getOfflinePlayer(UUID.fromString(members.get(i))).getName());
                    }
                    List<String> moderators = clanConfig.getConfig().getConfigurationSection("clans")
                            .getConfigurationSection(clans.getClanFromUUID(playerUUID)).getStringList("moderators");
                    for (int i = 0; i < moderators.size(); i++) {
                        members.add(Bukkit.getOfflinePlayer(UUID.fromString(moderators.get(i))).getName());
                    }
                    return members;
                }
                else if (args[0].equalsIgnoreCase("home")) {
                    if (clans.isMod(clans.getClan(clans.getClanFromUUID(playerUUID)), playerUUID)) {
                        return Arrays.asList("set", "remove");
                    }
                }
                else if (args[0].equalsIgnoreCase("modify")) {
                    if (clans.isMod(clans.getClan(clans.getClanFromUUID(playerUUID)), playerUUID)) {
                        return Arrays.asList("name", "symbol", "color");
                    }
                }
                else if (args[0].equalsIgnoreCase("help")) {
                    return Collections.singletonList("[<page/command>]");
                }
            }
            else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("modify")) {
                    if (args[1].equalsIgnoreCase("color")) {
                        return Arrays.asList("clan", "chat", "user");
                    }
                    else if (args[1].equalsIgnoreCase("symbol")) {
                        return Collections.singletonList("[<symbol>]");
                    }
                    else if (args[1].equalsIgnoreCase("name")) {
                        return Collections.singletonList("[<name>]");
                    }

                }
                else if (args[0].equalsIgnoreCase("kick")) {
                    return Collections.singletonList("[<reason>]");
                }
            }
            else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("modify") && args[1].equalsIgnoreCase("color")) {
                    return Collections.singletonList("[<color>]");
                }
            }
        }
        return null;
    }
}
