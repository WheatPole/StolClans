package org.blulio.stolClans;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;


public final class StolClans extends JavaPlugin {

    public String PluginName = "[Clans]"/* = ChatColor.GRAY + "[" + ChatColor.DARK_GREEN + "StolClans" + ChatColor.GRAY + "]"*/;
    public ClanConfig clanConfig;
    public Clans clans;
    public String clanConfigFilename = "clanData.yml";
    public TabHandler tabHandler;
    public JoinHandler joinHandler;
    public ScoreboardHandler scoreboardHandler;
        String[][] allCommands = new String[][]{
            {"help", "Shows all commands", "[page/command]"},
            {"create", "Creates a new clan", "[name]"},
            {"view", "Lists all members in the clan", ""},
            {"properties", "Lists all clan properties", ""},
            {"list", "Lists all existing clans", ""},
            {"disband", "Disbands the clan and kicks all members", ""},
            {"leave", "Leaves the clan", ""},
            {"invite", "Invites a specified player", "[user]"},
            {"kick", "Kicks a specified player", "[user] [reason]"},
            {"accept", "Accepts a clan invitation", ""},
            {"reject", "Rejects a clan invitation", ""},
            {"resign", "Resigns yourself from your position", ""},
            {"promote", "Promotes a member to a moderator", "[user]"},
            {"demote", "Demotes a moderator to a member", "[user]"},
            {"modify rename", "Renames the clan", "[name]"},
            {"modify symbol", "Sets the clan symbol", "[symbol]"},
            {"modify color clan", "Sets the color of the clan name", "[color]"},
            {"modify color chat", "Sets the color of chat messages", "[color]"},
            {"modify color user", "Sets the color of player's names", "[color]"},
            {"home", "Teleports to the clan home locations", ""},
            {"home set", "Sets the clan home location to your location", ""},
            {"home remove", "Removes the clan home location", ""},
            {"chat", "Sets the clan chat setting to on/off", ""}
    };

    public String clanWebhookURL = "";

    @Override
    public void onEnable() {
        clanWebhookURL = getConfig().getString("webhookurl");
        PluginName = org.bukkit.ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("prefix", "[StolClans]")));;
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(PluginName + ChatColor.GREEN + " plugin has been activated.");
        saveDefaultConfig();

        clanConfig = new ClanConfig(this, clanConfigFilename);

        if (clanConfig.getConfig().getConfigurationSection("clans") == null) {
            clanConfig.getConfig().createSection("clans");
            clanConfig.save();
        }

        if (clanConfig.getConfig().getConfigurationSection("players") == null) {
            clanConfig.getConfig().createSection("players");
            clanConfig.save();
        }

        clans = new Clans(this, PluginName);
        tabHandler = new TabHandler(this);
        joinHandler = new JoinHandler(this);
        scoreboardHandler = new ScoreboardHandler(this);

        getServer().getPluginManager().registerEvents(new OnChat(this), this);
        getServer().getPluginManager().registerEvents(new JoinHandler(this), this);

        TabAutocomplete tabauto = new TabAutocomplete(this, PluginName);
        getCommand("clan").setTabCompleter(tabauto);
        getCommand("clans").setTabCompleter(tabauto);
        getCommand("stolclans").setTabCompleter(tabauto);
        tabHandler.updateTabPlayers();
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage(PluginName + ChatColor.GREEN + " plugin has been " + ChatColor.RED + " deactivated.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // update in case of changes
        PluginName = org.bukkit.ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(getConfig().getString("prefix", "[StolClans]")));;
        if (command.getName().equalsIgnoreCase("clan") || command.getName().equalsIgnoreCase("clans") || command.getName().equalsIgnoreCase("stolclans")) {
            //String sender_id = sender.getUniqueId().toString();
            // operator cmds
            if (args[0].equalsIgnoreCase("fix")) {
                scoreboardHandler.debug();
                String res = clans.clanFix(sender);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("warn")) {
                if (args.length < 2) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return false;
                }
                String res = clans.warnClan(sender, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("disable")) {
                if (args.length < 2) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return false;
                }
                String res = clans.disableClan(sender, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("enable")) {
                if (args.length < 2) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return false;
                }
                String res = clans.enableClan(sender, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                clans.reload();
                sender.sendMessage(PluginName + " Reloaded!");
                return true;
            }
            else if (args[0].equalsIgnoreCase("findview")) {
                if (args.length < 2) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return false;
                }
                String res = clans.opViewClan(sender, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("findproperties")) {
                if (args.length < 2) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return false;
                }
                String res = clans.opClanProperties(sender, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(PluginName + " You must run this command as a player.");
                return true;
            }

            Player player = (Player) sender;
            String sender_id = player.getUniqueId().toString();
            String line = ChatColor.GREEN + "--------------------------------";
            if (args.length == 0) {
                String header = line + "\n" + ChatColor.GREEN + ChatColor.BOLD + " - STOLICIJA CLANS -\n";
                String msg = ChatColor.RESET + "" + ChatColor.GRAY + "- Clans have 3 main ranks - " + ChatColor.GREEN + "Owner, Moderator "
                            + ChatColor.GRAY +"and "+ChatColor.GREEN+ "Member"
                            + ChatColor.GRAY + ". " + ChatColor.GREEN + ChatColor.BOLD + "\n- Clan Owner " + ChatColor.RESET + ChatColor.GRAY +
                            "is the person who initially created the clan, and there is exactly 1 owner in each clan. He is the only person who can edit clan properties, disband the clan " +
                            "and promote other members into " + ChatColor.GREEN + "clan moderators" + ChatColor.GRAY + ".\n" + ChatColor.GREEN + ChatColor.BOLD + "- Clan Moderators" + ChatColor.RESET + ChatColor.GRAY +
                            " can invite other players into the clan, kick them and set the clan home location.\n";
                String footer = "- Type " + ChatColor.GREEN + "/clan help [page]" + ChatColor.GRAY + " to see specific commands, or " +
                        ChatColor.GREEN + "/clan help [command]" + ChatColor.GRAY + " for specific command descriptions!";
                sender.sendMessage(header + msg + footer + "\n" + line);
                return true;
            }
            // commands run by all members
            if (args[0].equalsIgnoreCase("view")) {
                String res = clans.viewClan(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("properties")) {
                String res = clans.clanProperties(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("help")) {
                if (args.length == 1) {
                    // make it multiple page
                    String header = line + "\n" + ChatColor.GREEN + ChatColor.BOLD + " - STOLICIJA CLANS -\n";
                    String msg = ChatColor.RESET + "" + ChatColor.GRAY + "- Clans have 3 main ranks - " + ChatColor.GREEN + "Owner, Moderator "
                            + ChatColor.GRAY +"and "+ChatColor.GREEN+ "Member"
                            + ChatColor.GRAY + ". " + ChatColor.GREEN + ChatColor.BOLD + "\n- Clan Owner " + ChatColor.RESET + ChatColor.GRAY +
                            "is the person who created the clan, and there is exactly 1 owner in each clan. He is the only person who can edit clan properties, disband the clan " +
                            "and promote other members into " + ChatColor.GREEN + "clan moderators" + ChatColor.GRAY + ".\n" + ChatColor.GREEN + ChatColor.BOLD + "- Clan Moderators" + ChatColor.RESET + ChatColor.GRAY +
                            " can invite other players into the clan, kick them and set the clan home location.\n";
                    String footer = "- Type " + ChatColor.GREEN + "/clan help [page]" + ChatColor.GRAY + " to see specific commands, or " +
                            ChatColor.GREEN + "/clan help [command]" + ChatColor.GRAY + " for specific command descriptions!";
                    sender.sendMessage(header + msg + footer + "\n" + line);
                    return true;
                }
                else {
                    String cmd = args[1];
                    try {
                        int page = Integer.parseInt(cmd);
                        int cmdsPerPage = 8;
                        int maxPage = Math.ceilDiv(allCommands.length, cmdsPerPage);
                        if (cmdsPerPage * page > allCommands.length) {
                            page = maxPage;
                        }
                        else if (page <= 0) page = 1;
                        String msg = ChatColor.GREEN + " Clan help - page " + page + "/" + maxPage + ChatColor.GRAY + "\n";

                        int startingInd = (page-1) * cmdsPerPage;
                        for (int i = startingInd; i < startingInd + cmdsPerPage && i < allCommands.length; i++) {
                            msg += ChatColor.GRAY + "- " + ChatColor.GREEN + allCommands[i][0] + ": " + ChatColor.GRAY + allCommands[i][1] + "\n";
                        }
                        sender.sendMessage(line + "\n" + msg +"\n" + line);
                        return true;
                    }
                    catch (NumberFormatException ex) {
                        // certain command
                        cmd = cmd.toLowerCase();
                        String msg = "";
                        for (String[] listCmd : allCommands) {
                            if (listCmd[0].equalsIgnoreCase(cmd)) {
                                msg += " Showing help for " + ChatColor.GREEN + cmd + ":\n" +
                                ChatColor.GRAY + "- Command: /clan " + ChatColor.GREEN + listCmd[0] + "\n" +
                                ChatColor.GRAY + "- Description: " + listCmd[1] + "\n" +
                                ChatColor.GRAY + "- Usage: " + ChatColor.GREEN + "/clan " + cmd + " " + listCmd[2];
                                break;
                            }
                        }
                        if (msg.isEmpty()) {
                            sender.sendMessage(PluginName + " Unknown command name");
                            return true;
                        }
                        else {
                            sender.sendMessage(line + msg + line);
                        }
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("create")) {
                if (args.length == 1) {
                    sender.sendMessage(PluginName + " You must specify the clan name. Example: /clan create MyClan");
                    return true;
                }
                String clanName = args[1];
                String res = clans.addClan(clanName, sender_id);
                if (!res.isEmpty()) {
                    sender.sendMessage(res);
                }
                tabHandler.updateTabPlayers();
                return true;
            }
            else if (args[0].equalsIgnoreCase("leave")) {
                String res = clans.leaveClan(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                tabHandler.updateTabPlayers();
                return true;
            }
            else if (args[0].equalsIgnoreCase("list")) {
                String res = clans.list();
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("accept")) {
                String res = clans.acceptClan(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                tabHandler.updateTabPlayers();
                return true;
            }
            else if (args[0].equalsIgnoreCase("reject")) {
                String res = clans.rejectClan(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("home")) {
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("set")) {
                        String res = clans.setHome(player);
                        if (!res.isEmpty()) sender.sendMessage(res);
                        return true;
                    }
                    else if (args[1].equalsIgnoreCase("remove")) {
                        String res = clans.removeHome(player);
                        if (!res.isEmpty()) sender.sendMessage(res);
                        return true;
                    }
                }
                String res = clans.tpHome(player);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("chat")) {
                String res = clans.clanChat(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }

            // mods only
            else if (args[0].equalsIgnoreCase("resign")) {
                String res = clans.resignClan(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("invite")) {
                if (args.length == 1) {
                    sender.sendMessage(PluginName + " You must specify the user to invite!");
                    return true;
                }
                String userToInvite = args[1];
                String res = clans.inviteClan(sender_id, userToInvite, 30);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("kick")) {
                if (args.length == 1) {
                    sender.sendMessage(PluginName + " Please specify the person you want to kick!");
                    return true;
                }
                String reason = "";
                if (args.length >= 3) {
                    for (int i = 2; i < args.length; i++) {
                        reason += args[i] + " ";
                    }
                }
                String res = clans.kickClan(sender_id, args[1], reason);
                if (!res.isEmpty()) sender.sendMessage(res);
                tabHandler.updateTabPlayers();
                return true;
            }

            //owner only
            else if (args[0].equalsIgnoreCase("disband")) {
                String res = clans.disbandClan(sender_id);
                if (!res.isEmpty()) sender.sendMessage(res);
                tabHandler.updateTabPlayers();
                return true;
            }
            else if (args[0].equalsIgnoreCase("promote")) {
                if (args.length == 1) {
                    sender.sendMessage(PluginName + " Please specify the person you want to promote!");
                    return true;
                }
                String res = clans.promoteClan(sender_id, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("demote")) {
                if (args.length == 1) {
                    sender.sendMessage(PluginName + " Please specify the person you want to demote!");
                    return true;
                }
                String res = clans.demoteClan(sender_id, args[1]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
            else if (args[0].equalsIgnoreCase("modify")) {
                if (args.length == 1) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return true;
                }
                if (args[1].equalsIgnoreCase("name")) {
                    if (args.length == 2) {
                        sender.sendMessage(PluginName + " Please specify the new name!");
                        return true;
                    }
                    String res = clans.updateName(sender_id, args[2]);
                    if (!res.isEmpty()) sender.sendMessage(res);
                    tabHandler.updateTabPlayers();
                    return true;
                }
                else if (args[1].equalsIgnoreCase("symbol")) {
                    if (args.length == 2) {
                        sender.sendMessage(PluginName + " Please specify the symbol!");
                        return true;
                    }
                    String res = clans.updateSymbol(sender_id, args[2]);
                    if (!res.isEmpty()) sender.sendMessage(res);
                    tabHandler.updateTabPlayers();
                    return true;
                }
                else if (args[1].equalsIgnoreCase("color")) {
                    if (args.length < 4) {
                        sender.sendMessage(PluginName + " Please specify details!");
                        return true;
                    }
                    if (args[2].equalsIgnoreCase("clan")) {
                        String res = clans.updateClanColor(sender_id, args[3]);
                        if (!res.isEmpty()) sender.sendMessage(res);
                        tabHandler.updateTabPlayers();
                        return true;
                    }
                    else if (args[2].equalsIgnoreCase("chat")) {
                        String res = clans.updateChatColor(sender_id, args[3]);
                        if (!res.isEmpty()) sender.sendMessage(res);
                        tabHandler.updateTabPlayers();
                        return true;
                    }
                    else if (args[2].equalsIgnoreCase("user")) {
                        String res = clans.updateNameColor(sender_id, args[3]);
                        if (!res.isEmpty()) sender.sendMessage(res);
                        tabHandler.updateTabPlayers();
                        return true;
                    }
                }


            }
            else if (args[0].equalsIgnoreCase("setowner")) {
                if (args.length < 3) {
                    sender.sendMessage(PluginName + " Please specify details!");
                    return false;
                }
                String res = clans.setOwner(sender_id, args[1], args[2]);
                if (!res.isEmpty()) sender.sendMessage(res);
                return true;
            }
        }
        return false;
    }
}
