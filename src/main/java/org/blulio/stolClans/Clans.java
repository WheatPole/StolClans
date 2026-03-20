package org.blulio.stolClans;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;

// class to do operations on the yaml file
public final class Clans {
    public enum ChatType {
        GLOBAL,
        CLAN
    }
    public enum ClanState {
        DEACTIVATED(0),
        ACTIVATED(1),
        DISABLED(2);

        public final int val;

        ClanState(int val) {
            this.val = val;
        }

    }

    private final StolClans plugin;
    private final ClanConfig cfg;
    private final String PluginName;

    // { username, clanName }
    private final HashMap<String, String> pendingRequests;
    private final HashMap<String, Boolean> pendingDisbands;
    private final HashMap<String, LocalDateTime> setHomeCooldown;
    private final HashMap<String, LocalDateTime> clanCreationCooldown;
    // { uuid, [ 0 - global, 1 - clan ] }
    private final HashMap<String, ChatType> chatType;

    public HashMap<String, ChatType> getChatType() {
        return chatType;
    }

    public Clans(StolClans plugin, String PluginName) {
        this.plugin = plugin;
        this.PluginName = PluginName;
        this.cfg = plugin.clanConfig;
        pendingRequests = new HashMap<>();
        pendingDisbands = new HashMap<>();
        setHomeCooldown = new HashMap<>();
        clanCreationCooldown = new HashMap<>();
        chatType = new HashMap<>();
    }

    public String getClanFromUUID(String playerUUID) {
        return Objects.requireNonNull(cfg.getConfig().getConfigurationSection("players")).getString(playerUUID);
    }

    public ConfigurationSection getClan(String clanName) {
        return Objects.requireNonNull(cfg.getConfig().getConfigurationSection("clans")).getConfigurationSection(clanName);
    }

    public boolean isInsideClan(String playerUUID) {
        return (Objects.requireNonNull(cfg.getConfig().getConfigurationSection("players"))).getString(playerUUID) != null &&
                !(Objects.requireNonNull(cfg.getConfig().getConfigurationSection("players"))).getString(playerUUID).equalsIgnoreCase("noclan");
    }
    public boolean isDisabled(ConfigurationSection clanSection) {
        return clanSection.getInt("state") == ClanState.DISABLED.val;
    }
    public boolean isDisabledOrDeactivated(ConfigurationSection clanSection) {
        return clanSection.getInt("state") != ClanState.ACTIVATED.val;
    }

    public String username(String playerUUID) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        return player.getName();
    }
    public boolean isOwner(ConfigurationSection clanSection, String playerUUID) {
        if (clanSection == null) return false;
        String owner = clanSection.getString("owner");
        if (owner == null) return false;
        return owner.equalsIgnoreCase(playerUUID);
    }
    public boolean isMod(ConfigurationSection clanSection, String playerUUID) {
        assert clanSection != null;
        boolean isPlayerOwner = isOwner(clanSection, playerUUID);
        if (!isPlayerOwner) {

            List<String> mods = clanSection.getStringList("moderators");
            for (String mod : mods) {
                if (mod.equalsIgnoreCase(playerUUID)) {
                    isPlayerOwner = true;
                    break;
                }
            }
        }
        return isPlayerOwner;
    }

    public void webhookLog(String msg) {
        WebhookRequest url = new WebhookRequest();
        try {
            url.sendToDiscord(plugin.clanWebhookURL, msg);
        }
        catch (IOException | InterruptedException ex) {
            plugin.getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    public void sendAll(ConfigurationSection clanSection, String msg, Sound playSound) {

        List<String> members = clanSection.getStringList("members");
        for (String member : members) {
            Player p = Bukkit.getPlayer(UUID.fromString(member));
            if (p != null) {
                p.sendMessage(msg);
                if (playSound != null) p.playSound(p.getLocation(), playSound, 1.0f, 1.0f);
            }
        }

        List<String> mods = clanSection.getStringList("moderators");
        for (String mod : mods) {
            Player p = Bukkit.getPlayer(UUID.fromString(mod));
            if (p != null) {
                p.sendMessage(msg);
                if (playSound != null) p.playSound(p.getLocation(), playSound, 1.0f, 1.0f);
            }
        }

        String owner = clanSection.getString("owner");
        assert owner != null;
        Player p = Bukkit.getPlayer(UUID.fromString(owner));
        if (p != null) {
            p.sendMessage(msg);
            if (playSound != null) p.playSound(p.getLocation(), playSound, 1.0f, 1.0f);
        }
    }
    public void sendAll(ConfigurationSection clanSection, String msg) {

        List<String> members = clanSection.getStringList("members");
        for (String member : members) {
            Player p = Bukkit.getPlayer(UUID.fromString(member));
            if (p != null) p.sendMessage(msg);
        }

        List<String> mods = clanSection.getStringList("moderators");
        for (String mod : mods) {
            Player p = Bukkit.getPlayer(UUID.fromString(mod));
            if (p != null) p.sendMessage(msg);
        }

        String owner = clanSection.getString("owner");
        assert owner != null;
        Player p = Bukkit.getPlayer(UUID.fromString(owner));
        if (p != null) p.sendMessage(msg);
    }
    public void sendAll(ConfigurationSection clanSection, String msg, Sound playSound, boolean lined) {
        String line = "";
        if (lined) line = ChatColor.GREEN + "--------------------------------";
        List<String> members = clanSection.getStringList("members");
        for (String member : members) {
            Player p = Bukkit.getPlayer(UUID.fromString(member));
            if (p != null) {
                p.sendMessage(line + "\n" + msg + "\n" + line);
                if (playSound != null) p.playSound(p.getLocation(), playSound, 1.0f, 1.0f);
            }
        }

        List<String> mods = clanSection.getStringList("moderators");
        for (String mod : mods) {
            Player p = Bukkit.getPlayer(UUID.fromString(mod));
            if (p != null) {
                p.sendMessage(line + "\n" + msg + "\n" + line);
                if (playSound != null) p.playSound(p.getLocation(), playSound, 1.0f, 1.0f);
            }
        }

        String owner = clanSection.getString("owner");
        assert owner != null;
        Player p = Bukkit.getPlayer(UUID.fromString(owner));
        if (p != null) {
            p.sendMessage(line + "\n" + msg + "\n" + line);
            if (playSound != null) p.playSound(p.getLocation(), playSound, 1.0f, 1.0f);
        }
    }
    public int getPlayerCount(ConfigurationSection clanSection) {
        return clanSection.getStringList("members").size() + clanSection.getStringList("moderators").size() + 1;
    }

    // colour code -> colour (text)
    public String getColorFromColorCode(String color) {
        color = color.toLowerCase();
        if (color.isEmpty()) return "";
        else if (color.length() < 3) {
            // classic code
            char colorCode = color.charAt(0);
            if (color.length() == 2) {
                if (color.charAt(0) == '&') colorCode = color.charAt(1);
                else return "";
            }

            return switch (colorCode) {
                case '0' -> "Black";
                case '1' -> "Dark Blue";
                case '2' -> "Dark Green";
                case '3' -> "Dark Aqua";
                case '4' -> "Dark Red";
                case '5' -> "Dark Purple";
                case '6' -> "Gold";
                case '7' -> "Grey";
                case '8' -> "Dark Grey";
                case '9' -> "Blue";
                case 'a' -> "Green";
                case 'b' -> "Aqua";
                case 'c' -> "Red";
                case 'd' -> "Light Purple";
                case 'e' -> "Yellow";
                case 'f' -> "White";
                default -> "";
            };
        }
        else if (color.length() == 6 || color.length() == 7) {
            // hex code
            if (color.length() == 7 && color.charAt(0) != '#') {
                return "";
            }
            if (color.length() == 6) color = "#" + color;
            return "hex[" + color + "]";

        }
        else {
            System.out.println("Invalid colour " + color);
            return "";
        }
    }
    // colour -> colour code
    public String parseColors(String color) {
        color = color.toLowerCase();
        if (color.isEmpty()) return "";

        if (color.equalsIgnoreCase("black")) return "0";
        else if (color.equalsIgnoreCase("dark blue")) return "1";
        else if (color.equalsIgnoreCase("dark green")) return "2";
        else if (color.equalsIgnoreCase("dark aqua")) return "3";
        else if (color.equalsIgnoreCase("dark red")) return "4";
        else if (color.equalsIgnoreCase("dark purple")) return "5";
        else if (color.equalsIgnoreCase("gold")) return "6";
        else if (color.equalsIgnoreCase("grey") || color.equalsIgnoreCase("gray")) return "7";
        else if (color.equalsIgnoreCase("dark grey") || color.equalsIgnoreCase("dark gray")) return "8";
        else if (color.equalsIgnoreCase("blue")) return "9";
        else if (color.equalsIgnoreCase("green")) return "a";
        else if (color.equalsIgnoreCase("aqua")) return "b";
        else if (color.equalsIgnoreCase("red")) return "c";
        else if (color.equalsIgnoreCase("light purple")) return "d";
        else if (color.equalsIgnoreCase("yellow")) return "e";
        else if (color.equalsIgnoreCase("white")) return "f";

        List<String> validColorChars = Arrays.asList("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f");
        if (color.length() < 3) {
            // classic code
            char colorCode = color.charAt(0);
            if (color.length() == 2) {
                if (color.charAt(0) == '&') colorCode = color.charAt(1);
                else return "";
            }
            if (!validColorChars.contains(Character.toString(colorCode))) {
                return "";
            }
            return Character.toString(colorCode);
        }
        else if (color.length() == 6 || color.length() == 7) {
            // hex code
            if (color.length() == 7 && color.charAt(0) != '#') {
                return "";
            }
            if (color.length() == 6) color = "#" + color;
            String hexCode = color.substring(1, 7);

            for (int i = 0; i < hexCode.length(); i++) {
                char hexChar = hexCode.charAt(i);
                if (!validColorChars.contains(Character.toString(hexChar))) {
                    System.out.println("Invalid char " + hexChar + " in string " + hexCode);
                    return "";
                }
            }
            return color;

        }
        // add support for gradients?
        return "";
    }
    public static String getColor(String color) {
        if (color.length() < 3) {
            // classic code
            if (color.length() == 1) {
                color = "&" + color;
            }
            return ChatColor.translateAlternateColorCodes('&', color);
        }
        else if (color.length() == 6 || color.length() == 7) {
            // hex code
            if (color.length() == 6) color = "#" + color;
            // why does this work???
            return ChatColor.of(new java.awt.Color(
                Integer.valueOf( color.substring( 1, 3 ), 16 ),
                Integer.valueOf( color.substring( 3, 5 ), 16 ),
                Integer.valueOf( color.substring( 5, 7 ), 16 )
            )) + "";
        }
        return ChatColor.translateAlternateColorCodes('&', "&f");
    }

    public String previewText(Player player) {
        String clanName = getClanFromUUID(player.getUniqueId().toString());
        if (clanName == null) {
            String clanStr = "&" + plugin.getConfig().getString("noclancol")
                    + "[" + plugin.getConfig().getString("noclanname") +
                    "]";
            return ChatColor.translateAlternateColorCodes('&', clanStr + " &" +
                    plugin.getConfig().getString("noclannamecol") + player.getDisplayName());
        }
        else {

            ConfigurationSection clanSection = getClan(clanName);
            String clanColor = clanSection.getString("clancolor");
            assert clanColor != null;
            String nameColor = clanSection.getString("namecolor");
            assert nameColor != null;

            int state = clanSection.getInt("state");
            if (state == Clans.ClanState.DISABLED.val) {
                clanName = ChatColor.MAGIC + clanName + ChatColor.RESET;
                nameColor = "f";
            }
            else if (state == Clans.ClanState.DEACTIVATED.val) {
                clanName = ChatColor.STRIKETHROUGH + clanName + ChatColor.RESET;
                nameColor = "f";
            }
            String clanStr = getColor(clanColor) +
                    "[" + clanName + getColor(clanColor) +
                    "]";
            return clanStr + " " + getColor(nameColor) + player.getName();
        }
    }

    public boolean validName(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c != '-' && c != '_') {
                if (c < '0' || (c >'9' && c < 'A') || (c > 'Z' && c < 'a') || c > 'z') return false;
            }
        }
        return true;
    }

    public void evaluateState(ConfigurationSection clanSection) {
        int state = clanSection.getInt("state");
        if (state == ClanState.DISABLED.val) return;
        int playerCount = getPlayerCount(clanSection);
        if (playerCount < plugin.getConfig().getInt("min_player_requirement") && state == ClanState.ACTIVATED.val) {
            // ACTIVATED -> DEACTIVATED
            clanSection.set("state", ClanState.DEACTIVATED.val);
            sendAll(clanSection, PluginName + " This clan is now " + ChatColor.RED + ChatColor.BOLD +
            "deactivated" + ChatColor.RESET + ChatColor.GRAY + ", since there aren't enough players!");
            cfg.save();
            removePlayersFromClanChat(clanSection);
        }
        else if (playerCount >= plugin.getConfig().getInt("min_player_requirement") && state == ClanState.DEACTIVATED.val) {
            // DEACTIVATED -> ACTIVATED
            clanSection.set("state", ClanState.ACTIVATED.val);
            sendAll(clanSection, PluginName + " This clan is now " + ChatColor.GREEN + ChatColor.BOLD +
                    "activated" + ChatColor.RESET + ChatColor.GRAY + "!");
            webhookLog("Clan **" + clanSection.getName() + "** has been **activated**! <:excellent:1236268868025061386>");
            cfg.save();
        }
    }

    public List<String> allClanNames() {
        ConfigurationSection clans = cfg.getConfig().getConfigurationSection("clans");

        assert clans != null;
        return new ArrayList<>(clans.getKeys(false));
    }
    public String getRank(ConfigurationSection clanSection, String playerUUID) {
        if (isOwner(clanSection, playerUUID)) {
            return ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Owner" + ChatColor.GRAY + "]";
        }
        else if (isMod(clanSection, playerUUID)) {
            return ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "Mod" + ChatColor.GRAY + "]";
        }
        else {
            if (clanSection.getStringList("members").contains(playerUUID)) {
                return ChatColor.GRAY + "[" + ChatColor.GRAY + "Member" + ChatColor.GRAY + "]";
            }
            else {
                return ChatColor.GRAY + "[" + ChatColor.BLACK + "Unknown" + ChatColor.GRAY + "]";
            }
        }
    }
    public void removePlayersFromClanChat(ConfigurationSection clanSection) {
        List<String> members = clanSection.getStringList("members");
        List<String> moderators = clanSection.getStringList("moderators");
        String owner = clanSection.getString("owner");
        if (chatType.get(owner) == ChatType.CLAN) chatType.put(owner, ChatType.GLOBAL);
        for (String member : members) {
            if (chatType.get(member) == ChatType.CLAN) chatType.put(member, ChatType.GLOBAL);
        }
        for (String moderator : moderators) {
            if (chatType.get(moderator) == ChatType.CLAN) chatType.put(moderator, ChatType.GLOBAL);
        }
    }




    public String addClan(String clanName, String playerUUID/*, String clanSymbol, String clanColor, String nameColor, String chatColor*/) {
        if (clanName.length() < 3) {
            return PluginName + " Clan name must be at least 3 letters long";
        }
        else if (clanName.length() > 15) {
            return PluginName + " Clan name must be fewer than 15 letters long";
        }
        if (!validName(clanName)) {
            return PluginName + " Invalid characters used!";
        }

        if (isInsideClan(playerUUID)) {
            return PluginName + " You are already inside a clan. Please leave the clan using: "
                    + ChatColor.GREEN + "/clan leave" + ChatColor.GRAY + " or disband it by using "
                    + ChatColor.GREEN + "/clan disband";
        }

        for (String key : cfg.getConfig().getConfigurationSection("clans").getKeys(false)) {
            if (key.equalsIgnoreCase(clanName)) {
                return PluginName + " A clan with that name already exists. Please try another name.";
            }
        }
        if (cfg.getConfig().getConfigurationSection("clans") == null) {
            System.out.println("Error!");
        }

        if (clanCreationCooldown.containsKey(playerUUID)) {
            LocalDateTime endTime = clanCreationCooldown.get(playerUUID);
            LocalDateTime now = LocalDateTime.now();
            //plugin.getLogger().log(Level.INFO, endTime.toString() + " " + now.toString());
            if (now.isAfter(endTime)) {
                //System.out.println("is after");
                clanCreationCooldown.remove(playerUUID);
            }
            else {
                Duration diff = Duration.between(now, endTime);
                return PluginName + ChatColor.RED + " You can use this command again in " + diff.getSeconds() + " seconds.";
            }
        }

        ConfigurationSection newClan = cfg.getConfig().getConfigurationSection("clans").createSection(clanName);
        /*    owner: ''
        moderators: []
        clancolor: a
        namecolor: e
        chatcolor: 3
        clansymbol: -
        datecreated: 1740954592514
        members: []*/

        newClan.set("owner", playerUUID);
        newClan.set("moderators", new ArrayList<>());
        newClan.set("clancolor", "8");
        newClan.set("namecolor", "f");
        newClan.set("chatcolor", "f");
        newClan.set("clansymbol", "-");
        newClan.set("warnings", 0);
        newClan.set("state", ClanState.DEACTIVATED.val);
        newClan.set("datecreated", System.currentTimeMillis());
        newClan.set("members", new ArrayList<>());
        newClan.set("home", "");

        cfg.getConfig().getConfigurationSection("players").set(playerUUID, clanName);

        cfg.save();
        Bukkit.broadcastMessage(PluginName + ChatColor.GRAY + ChatColor.ITALIC + " A brand new clan has been created: " +
                ChatColor.GREEN + ChatColor.BOLD + clanName +ChatColor.GRAY + ChatColor.ITALIC + " by " + ChatColor.GREEN + ChatColor.BOLD + username(playerUUID) + "!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != null)
            //player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 2.0f, 2.0f);
        }
        webhookLog("NEW clan created: **" + clanName + "** by **" + Bukkit.getPlayer(UUID.fromString(playerUUID)).getName() + "**! :star_struck:");
        clanCreationCooldown.put(playerUUID, LocalDateTime.now().plusSeconds(600));

        return (PluginName + " Congrats! You have created your clan "
                + ChatColor.GREEN + clanName + ChatColor.GRAY + "! Your clan is currently "
                + ChatColor.RED + ChatColor.BOLD + "deactivated " + ChatColor.RESET + ChatColor.GRAY
                + "and it will automatically activate after a minimum of " + plugin.getConfig().getInt("min_player_requirement") + " players join your clan. "
                + "You can modify its properties like clan color etc using " + ChatColor.GREEN + "/clan modify" + ChatColor.GRAY + ". "
                + "To check all properties write: " + ChatColor.GREEN + "/clan properties");
    }
    public String clanProperties(String playerUUID) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        String clanSymbol = clanSection.getString("clansymbol");
        List<String> mods = clanSection.getStringList("moderators");
        List<String> members = clanSection.getStringList("members");
        String clancol = clanSection.getString("clancolor");
        String namecol = clanSection.getString("namecolor");
        String chatcol = clanSection.getString("chatcolor");
        assert clancol != null;
        assert namecol != null;
        assert chatcol != null;

        int state = clanSection.getInt("state");
        String stateString = "";
        if (state == ClanState.DEACTIVATED.val) stateString = " (deactivated)";
        else if (state == ClanState.DISABLED.val) stateString = " (disabled)";

        String line = ChatColor.GREEN + "--------------------------------";
        String msg = ChatColor.GRAY + "Clan name: " + clanName + stateString;
        msg += "\n\n";
        msg += ChatColor.GRAY + "Clan symbol: " + ChatColor.GREEN + clanSymbol;
        msg += "\n";
        msg += ChatColor.GRAY + "Moderator count: " + ChatColor.GREEN + mods.size();
        msg += "\n";
        msg += ChatColor.GRAY + "Member count: " + ChatColor.GREEN + members.size();
        msg += "\n";
        msg += ChatColor.GRAY + "Clan color: " + getColor(clancol) + getColorFromColorCode(clancol);
        msg += "\n";
        msg += ChatColor.GRAY + "Name color: " + getColor(namecol) + getColorFromColorCode(namecol);
        msg += "\n";
        msg += ChatColor.GRAY + "Chat color: " + getColor(chatcol) + getColorFromColorCode(chatcol);
        msg += "\n";

        String home = clanSection.getString("home");
        assert home != null;
        if (!home.isEmpty()) msg += ChatColor.GRAY + "Home: " + ChatColor.GREEN + home + "\n";
        msg += " \n";
        msg += ChatColor.GRAY + "Tip: use " + ChatColor.BOLD + ChatColor.GREEN + "/clan view " + ChatColor.RESET + ChatColor.GRAY + "to list all players!";
        return line + "\n" + msg + "\n" + line;
    }
    public String viewClan(String playerUUID) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + "Couldn't find the clan " + clanName;

        String clanOwner = clanSection.getString("owner");
        String clanColor = clanSection.getString("clancolor");
        String clanSymbol = clanSection.getString("clansymbol");
        List<String> mods = clanSection.getStringList("moderators");
        List<String> members = clanSection.getStringList("members");

        int state = clanSection.getInt("state");
        String stateString = "";
        if (state == ClanState.DEACTIVATED.val) stateString = " (deactivated)";
        else if (state == ClanState.DISABLED.val) stateString = " (disabled)";

        String line = ChatColor.GREEN + "--------------------------------";
        String msg = ChatColor.GRAY + "Clan: " + getColor(clanColor) + clanSymbol + " " + clanName + stateString;
        msg += "\n\n";
        msg += ChatColor.GRAY + "Owner: " + ChatColor.GREEN + username(clanOwner);
        msg += "\n";
        msg += ChatColor.GRAY + "Moderators: " + ChatColor.GREEN;
        for (String mod : mods) {
            msg += username(mod) + " ";
        }
        msg += "\n";
        msg += ChatColor.GRAY + "Members: " + ChatColor.GREEN;
        for (String member : members) {
            msg += username(member) + " ";
        }
        String warningMsg = "";
        if (clanSection.getInt("warnings") > 0) {
            warningMsg = ChatColor.GRAY + "Your clan currently has " + ChatColor.RED + ChatColor.BOLD + Integer.toString(clanSection.getInt("warnings")) + ChatColor.RESET + ChatColor.GRAY + " warnings.\n";
        }

        return line + "\n" + msg + "\n" + warningMsg + line;
    }

    public String disbandClan(String playerUUID) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + "Couldn't find the clan " + clanName;

        if (!isOwner(clanSection, playerUUID)) {
            return PluginName + " You are not the owner of this clan!";
        }
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        if (!pendingDisbands.containsKey(playerUUID)) {
            pendingDisbands.put(playerUUID, true);

            Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
            if (player == null) return PluginName + " Error: PluginError";
            player.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                public void run() {
                    if (pendingDisbands.containsKey(playerUUID)) {
                        pendingDisbands.remove(playerUUID);
                        player.sendMessage(PluginName + " Clan disband timed out");
                    }
                }
            }, 200L);

            return PluginName + " Are you sure you want to delete " + clanName + "? Type the command again within 10s to execute it.";
        }

        sendAll(clanSection, PluginName + " The clan has been disbanded.", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, true);

        webhookLog("Clan **" + clanName + "** was **disbanded**! :scream:");

        List<String> mods = clanSection.getStringList("moderators");
        List<String> members = clanSection.getStringList("members");

        for (String mod : mods) {
            cfg.getConfig().getConfigurationSection("players").set(mod, null);
        }
        for (String member : members) {
            cfg.getConfig().getConfigurationSection("players").set(member, null);
        }
        cfg.getConfig().getConfigurationSection("players").set(playerUUID, null);
        cfg.getConfig().getConfigurationSection("clans").set(clanName, null);

        cfg.save();

        pendingDisbands.remove(playerUUID);

        return "";
    }

    public String leaveClan(String playerUUID) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }
        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + "Couldn't find the clan " + clanName;
        if (isOwner(clanSection, playerUUID)) {
            return (PluginName + ChatColor.RED + " You are the owner of this clan! \n" +
                    ChatColor.GRAY + "You may only disband it by using /clan disband");
        }

        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";


        List<String> mods = clanSection.getStringList("moderators");
        List<String> members = clanSection.getStringList("members");

        boolean found = false;
        for (String mod : mods) {
            if (mod.equalsIgnoreCase(playerUUID)) {
                found = true;
                mods.remove(mod);
                clanSection.set("moderators", mods);
                cfg.getConfig().getConfigurationSection("players").set(playerUUID, null);
                break;
            }
            ;
        }
        if (!found) {
            for (String member : members) {
                if (member.equalsIgnoreCase(playerUUID)) {
                    found = true;
                    members.remove(member);
                    clanSection.set("members", members);
                    cfg.getConfig().getConfigurationSection("players").set(playerUUID, null);
                    break;
                }
                ;
            }
        }

        cfg.save();

        sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + username(playerUUID) +
                ChatColor.RESET + ChatColor.GRAY + " left the clan!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, true);

        webhookLog("**" + username(playerUUID) + "** left the clan **" + clanName + "**. :pensive:");

        // sets deactivated or activated
        evaluateState(clanSection);
        return PluginName + " Successfully left " + clanName + "!";
    }

    public String resignClan(String playerUUID) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + "Couldn't find the clan " + clanName;
        if (isOwner(clanSection, playerUUID)) {
            return (PluginName + ChatColor.RED + " You are the owner of this clan! " +
                    ChatColor.GRAY + "You cannot resign yourself!");
        }

        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        if (isMod(clanSection, playerUUID)) {
            List<String> mods = clanSection.getStringList("moderators");

            mods.remove(playerUUID);
            clanSection.set("moderators", mods);

            List<String> members = clanSection.getStringList("members");
            members.add(playerUUID);
            clanSection.set("members", members);

            cfg.save();

            sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + username(playerUUID) +
                    ChatColor.RESET + ChatColor.GRAY + " resigned from Clan Moderator!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, true);
            return "";
        }

        return PluginName + ChatColor.RED + " You are not a Clan Moderator!";
    }

    public String promoteClan(String playerUUID, String personName) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        if (username(playerUUID).equalsIgnoreCase(personName)) {
            return PluginName + " You cannot promote yourself!";
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isOwner(clanSection, playerUUID)) {
            return PluginName + " You are not the owner of this clan!";
        }

        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        /*boolean isMod = clanSection.getString("owner").equalsIgnoreCase(playerUUID);
        if (!isMod) {
            List<String> mods = clanSection.getStringList("moderators");
            for (String mod : mods) {
                if (mod.equalsIgnoreCase(playerUUID)) {
                    isMod = true;
                    break;
                }
            }
        }
        if (!isMod) return PluginName + " You are not a Clan Moderator!";*/

        List<String> mods = clanSection.getStringList("moderators");
        for (String mod : mods) {
            if (personName.equalsIgnoreCase(username(mod))) {
                return PluginName + " You cannot promote a Clan Moderator!";
            }
        }

        List<String> members = clanSection.getStringList("members");
        for (String member : members) {
            if (personName.equalsIgnoreCase(username(member))) {
                members.remove(member);
                mods.add(member);
                clanSection.set("members", members);
                clanSection.set("moderators", mods);

                cfg.save();

                /*Player playerInv = Bukkit.getPlayer(personName);
                String msg = PluginName + " You have been promoted to " + ChatColor.BOLD + "Clan Moderator!";

                playerInv.sendMessage(msg);
                playerInv.playSound(playerInv.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);*/

                sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + personName +
                        ChatColor.RESET + ChatColor.GRAY + " was promoted to Clan Moderator!", Sound.BLOCK_NOTE_BLOCK_HARP, true);
                return "";
            }
        }
        return PluginName + " Couldn't find user " + personName;
    }

    public String demoteClan(String playerUUID, String personName) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        if (username(playerUUID).equalsIgnoreCase(personName)) {
            return PluginName + " You cannot demote yourself!";
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isOwner(clanSection, playerUUID)) {
            return PluginName + " You are not the owner of this clan!";
        }

        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        /*boolean isMod = clanSection.getString("owner").equalsIgnoreCase(playerUUID);
        if (!isMod) {
            List<String> mods = clanSection.getStringList("moderators");
            for (String mod : mods) {
                if (mod.equalsIgnoreCase(playerUUID)) {
                    isMod = true;
                    break;
                }
            }
        }
        if (!isMod) return PluginName + " You are not a Clan Moderator!";*/

        List<String> members = clanSection.getStringList("members");
        for (String member : members) {
            if (personName.equalsIgnoreCase(username(member))) {
                return PluginName + " " + personName + " is not a Clan Moderator!";
            }
        }

        List<String> mods = clanSection.getStringList("moderators");
        for (String mod : mods) {
            if (personName.equalsIgnoreCase(username(mod))) {
                mods.remove(mod);
                members.add(mod);
                clanSection.set("moderators", mods);
                clanSection.set("members", members);

                cfg.save();
                /*Player playerInv = Bukkit.getPlayer(personName);
                String msg = PluginName + " You have been demoted to " + ChatColor.BOLD + "Clan Member!";
                playerInv.sendMessage(msg);
                playerInv.playSound(playerInv.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);*/

                sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + personName +
                        ChatColor.RESET + ChatColor.GRAY + " was demoted to Clan Member!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, true);
                return "";
            }
        }
        return PluginName + " Couldn't find user " + personName + " inside the clan";
    }

    public String kickClan(String playerUUID, String personName, String reason) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        if (username(playerUUID).equalsIgnoreCase(personName)) {
            return PluginName + " You cannot kick yourself!";
        }

        String clanName = getClanFromUUID(playerUUID);

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";

        List<String> mods = clanSection.getStringList("moderators");
        boolean isOwner = clanSection.getString("owner").equalsIgnoreCase(playerUUID);
        for (String mod : mods) {
            if (personName.equalsIgnoreCase(username(mod))) {
                if (!isOwner) {
                    return PluginName + " You cannot kick a Clan Moderator!";
                } else {
                    mods.remove(mod);
                    clanSection.set("moderators", mods);
                    cfg.getConfig().getConfigurationSection("players").set(mod, null);

                    cfg.save();

                    Player playerInv = Bukkit.getPlayer(personName);
                    String playerName = username(mod);
                    if (playerInv != null) {
                        String msg = PluginName + " You have been kicked from " + ChatColor.BOLD + clanName + ChatColor.RESET + ChatColor.GRAY + " by " + username(playerUUID);
                        if (!reason.isEmpty()) msg += " for: " + reason;
                        msg += "!";
                        playerInv.sendMessage(msg);
                        playerInv.playSound(playerInv.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
                    }
                    sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + personName +
                            ChatColor.RESET + ChatColor.GRAY + " left the clan!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, true);
                    webhookLog(playerName + " left the clan **" + clanName + "**. :pensive:");

                    return "";
                }
            }
        }

        List<String> members = clanSection.getStringList("members");
        for (String member : members) {
            if (personName.equalsIgnoreCase(username(member))) {
                members.remove(member);
                clanSection.set("members", members);
                cfg.getConfig().getConfigurationSection("players").set(member, null);

                cfg.save();
                Player playerInv = Bukkit.getPlayer(personName);
                String playerName = username(member);
                if (playerInv != null) {
                    String msg = PluginName + " You have been kicked from " + ChatColor.BOLD + clanName + ChatColor.RESET + ChatColor.GRAY + " by " + username(playerUUID);
                    if (!reason.isEmpty()) msg += " for: " + reason;
                    msg += "!";
                    playerInv.sendMessage(msg);
                    playerInv.playSound(playerInv.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
                }
                sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + personName +
                        ChatColor.RESET + ChatColor.GRAY + " left the clan!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, true);
                webhookLog( playerName + " left the clan **" + clanName + "**. :pensive:");
                return "";
            }
        }
        return PluginName + " Couldn't find user " + personName;
    }

    public String inviteClan(String playerUUID, String personName, int awaitSeconds) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        if (username(playerUUID).equalsIgnoreCase(personName)) {
            return PluginName + " You cannot invite yourself!";
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        Player playerInv = Bukkit.getPlayer(personName);
        if (playerInv == null) {
            return PluginName + " Couldn't find user " + personName;
        }
        if (!playerInv.isOnline()) {
            return PluginName + personName + " is not online!";
        }
        String playerInvUUID = playerInv.getUniqueId().toString();
        if (pendingRequests.containsKey(playerInvUUID) && pendingRequests.get(playerInvUUID) != null) {
            return (PluginName + " Player " + personName + " already has a pending request!");
        }

        // ?
        for (String key : cfg.getConfig().getConfigurationSection("clans").getKeys(false)){
            try {
                if (getClan(key).getString("owner").equals(playerInvUUID)) {
                    return (PluginName + " Player " + personName + " is already an owner of a clan!");
                }
            }
            catch (NullPointerException ex) {
                System.out.println("Error while looking at " + key);
                System.out.println(ex);
            }
        }

        String clanColor = clanSection.getString("clancolor");
        TextComponent clickHere = new TextComponent("HERE");
        String msg = PluginName + ChatColor.GOLD + " " + username(playerUUID) +
                ChatColor.GRAY + " invited you to join " + getColor(clanColor) + clanName + ChatColor.GRAY +
                "! You have 20 seconds to accept by typing " +
                ChatColor.GREEN + ChatColor.BOLD + "/clan accept" + ChatColor.RESET + ChatColor.GRAY + " or " + ChatColor.GREEN + ChatColor.BOLD + "/clan reject";
        playerInv.sendMessage(msg);
        playerInv.playSound(playerInv.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);

        pendingRequests.put(playerInvUUID, clanName);
        playerInv.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {
                Object removed = pendingRequests.remove(playerInvUUID);
                if (removed != null) {
                    playerInv.sendMessage(PluginName + " Clan request timed out.");
                    //Bukkit.getPlayer(playerUUID).sendMessage(PluginName + " Clan request for " + playerInv.getName() + " timed out.");
                }
            }
        }, 400L);

        return PluginName + " Successfully invited " + personName + "!";
    }

    public String acceptClan(String playerUUID) {
        if (!pendingRequests.containsKey(playerUUID)) {
            return (PluginName + " You have no clan requests.");
        }

        if (isInsideClan(playerUUID)) {
            return (PluginName + " You are already inside a clan! Leave it by using " + ChatColor.GREEN + ChatColor.BOLD + "/clan leave" +
                    ChatColor.RESET + ChatColor.GRAY + ", or disband it by using "+ ChatColor.GREEN + ChatColor.BOLD + "/clan disband");
        }

        String clanName = pendingRequests.get(playerUUID);
        pendingRequests.remove(playerUUID);

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        List<String> members = clanSection.getStringList("members");

        members.add(playerUUID);
        clanSection.set("members", members);
        cfg.getConfig().getConfigurationSection("players").set(playerUUID, clanName);
        cfg.save();

        sendAll(clanSection, PluginName + " " + ChatColor.GREEN + ChatColor.BOLD + username(playerUUID) +
                ChatColor.RESET + ChatColor.GRAY + " joined the clan!", Sound.BLOCK_NOTE_BLOCK_HARP, true);
        evaluateState(clanSection);

        webhookLog("**" + username(playerUUID) + "** joined the clan **" + clanName + "**! :smile:");

        return PluginName + " You have successfully joined clan " + ChatColor.BOLD + clanName + "!";

    }

    public String rejectClan(String playerUUID) {
        if (!pendingRequests.containsKey(playerUUID)) {
            return (PluginName + " You have no clan requests.");
        }

        pendingRequests.remove(playerUUID);

        return PluginName + " You have rejected the request.";

    }
    public String list() {
        String msg = PluginName + " List of clans: \n";
        for (String name : cfg.getConfig().getConfigurationSection("clans").getKeys(false)) {
            ConfigurationSection clanSection = getClan(name);
            String color = clanSection.getString("clancolor");
			String symbol = clanSection.getString("clansymbol");
            int state = clanSection.getInt("state");
            if (state == ClanState.DEACTIVATED.val) name = ChatColor.ITALIC + name + ChatColor.GRAY+ " (deactivated)";
            else if (state == ClanState.DISABLED.val) name = ChatColor.ITALIC + name + ChatColor.GRAY+ " (disabled)";
            if (color.length() > 0 && color.charAt(0) != '&') color = "&" + color;
            msg += getColor(color) + symbol + " " + name + ChatColor.RESET + ", ";
        }
        msg = msg.substring(0, msg.length() - 3);
        return msg;
    }
    public String updateName(String playerUUID, String newName) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String oldName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(oldName);
        if (clanSection == null) return PluginName + "Couldn't find the clan " + oldName;
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        if (!isOwner(clanSection, playerUUID)) {
            return PluginName + " You are not the owner of this clan!";
        }
        if (newName.length() < 3) {
            return PluginName + " Clan name must be at least 3 letters long";
        }
        ConfigurationSection clans = cfg.getConfig().getConfigurationSection("clans");
        if (clans.contains(newName)) {
            return PluginName + " Clan name already exists!";
        }

        ConfigurationSection newClan = clans.createSection(newName);
        for (String key : clanSection.getKeys(false)) {
            Object val = clanSection.get(key);

            newClan.set(key, val);
        }

        //old name
        clans.set(oldName, null);

        for (String player : cfg.getConfig().getConfigurationSection("players").getKeys(false)) {
            String playerClan = cfg.getConfig().getConfigurationSection("players").getString(player);
            if (playerClan.equalsIgnoreCase(oldName)) {
                // new name
                cfg.getConfig().getConfigurationSection("players").set(player, newName);
            }
        }
        cfg.save();

        return PluginName + " Successfully renamed clan!";
    }

    public String updateNameColor(String playerUUID, String newColor) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);

        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        /*List<String> validColorChars = Arrays.asList("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f");
        if (!validColorChars.contains(newColor.toLowerCase()) ||
        newColor.length() == 0 || newColor.length() > 2) {
            return PluginName + " Invalid color syntax!";
        }*/

        String newColorParsed = parseColors(newColor);
        if (newColorParsed.isEmpty()) {
            return PluginName + " Invalid color syntax!";
        }
        /*if (newColorParsed.length() == 2) {
            if (newColorParsed.charAt(0) == '&') {
                // second character is definitely inside validColorChars
                newColorParsed = Character.toString(newColorParsed.charAt(1));
            }
            else {
                return PluginName + " Invalid color syntax!";
            }
        }*/

        clanSection.set("namecolor", newColorParsed);
        cfg.save();
        int state = clanSection.getInt("state");
        String stateString = "";
        if (state == ClanState.DEACTIVATED.val) stateString = "\n" + ChatColor.RED + "Name color is deactivated until the clan is activated";

        return PluginName + " Successfully changed clan name colour!" + stateString;
    }

    public String updateClanColor(String playerUUID, String newColor) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        String newColorParsed = parseColors(newColor);
        if (newColorParsed.isEmpty()) {
            return PluginName + " Invalid color syntax!";
        }

        clanSection.set("clancolor", newColorParsed);
        cfg.save();

        return PluginName + " Successfully changed clan colour!";
    }

    public String updateChatColor(String playerUUID, String newColor) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        String newColorParsed = parseColors(newColor);
        if (newColorParsed.isEmpty()) {
            return PluginName + " Invalid color syntax!";
        }
        clanSection.set("chatcolor", newColorParsed);
        cfg.save();
        int state = clanSection.getInt("state");
        String stateString = "";
        if (state == ClanState.DEACTIVATED.val) stateString = "\n" + ChatColor.RED + "Chat color is deactivated until the clan is activated";

        return PluginName + " Successfully changed chat colour!" + stateString;
    }
    public String updateSymbol(String playerUUID, String newSymbol) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (isDisabled(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";

        if (newSymbol.length() > 3) {
            return PluginName + " Symbols must be less than 3 character long!";
        }

        clanSection.set("clansymbol", newSymbol);
        cfg.save();

        return PluginName + " Successfully changed clan symbol!";
    }

    public String setHome(Player player) {
        String playerUUID = player.getUniqueId().toString();

        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";
        if (isDisabledOrDeactivated(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        if (setHomeCooldown.containsKey(clanName)) {
            LocalDateTime endTime = setHomeCooldown.get(clanName);
            LocalDateTime now = LocalDateTime.now();
            //plugin.getLogger().log(Level.INFO, endTime.toString() + " " + now.toString());
            if (now.isAfter(endTime)) {
                //System.out.println("is after");
                setHomeCooldown.remove(clanName);
            }
            else {
                Duration diff = Duration.between(now, endTime);
                return PluginName + ChatColor.RED + " You can use this command again in " + diff.getSeconds() + " seconds.";
            }
        }

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return PluginName + ChatColor.RED + " You can only run this command in the overworld!";
        }
        String worldName = player.getWorld().getName();
        Location playerLocation = player.getLocation();
        String locationText = String.format ("%.2f", playerLocation.getX()) + " " +
                String.format ("%.2f", playerLocation.getY()) + " " +
                String.format ("%.2f", playerLocation.getZ());
        String homeString = worldName + " " + locationText;
        clanSection.set("home", homeString);

        setHomeCooldown.put(clanName, LocalDateTime.now().plusSeconds(120));

        return PluginName + " Set home location.";
    }
    public String removeHome(Player player) {
        String playerUUID = player.getUniqueId().toString();

        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (!isMod(clanSection, playerUUID)) return PluginName + " You are not a Clan Moderator!";
        if (isDisabledOrDeactivated(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        clanSection.set("home", "");
        return PluginName + " Removed home location.";
    }
    public String tpHome(Player player) {
        String playerUUID = player.getUniqueId().toString();

        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }

        String clanName = getClanFromUUID(playerUUID);
        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;
        if (isDisabledOrDeactivated(clanSection))  return PluginName + ChatColor.RED + " Your clan is disabled!";

        String homeString = clanSection.getString("home");
        if (homeString.isEmpty()) {
            return (PluginName + " Your clan doesn't have a home location set up!");
        }
        String[] args = homeString.split(" ");
        if (args.length < 4) {
            return (PluginName + " Invalid location arguments.");
        }
        String worldName = args[0];
        double x, y, z;
        try {
            x = Double.parseDouble(args[1]);
            y = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
            return (PluginName + " Invalid location arguments.");
        }
        World world = Bukkit.getServer().getWorld(worldName);
        if (world == null) {
            return (PluginName + " Couldn't get world '" + worldName + "'");
        }
        // do teleportation
        player.teleport(new Location(world, x, y, z));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return (PluginName + " Teleported!");
    }
    public String clanChat(String playerUUID) {
        if (!isInsideClan(playerUUID)) {
            return (PluginName + " You are not inside a clan.");
        }
        if (isDisabledOrDeactivated(getClan(getClanFromUUID(playerUUID))))  return PluginName + ChatColor.RED + " Your clan is not activated!";

        if (!chatType.containsKey(playerUUID)) {
            // assume it's deactivated
            chatType.put(playerUUID, ChatType.CLAN);
            return (PluginName + " Enabled clan chat!");
        }
        if (chatType.get(playerUUID) == ChatType.GLOBAL) {
            chatType.put(playerUUID, ChatType.CLAN);
            return (PluginName + " Enabled clan chat!");
        }
        else {
            chatType.put(playerUUID, ChatType.GLOBAL);
            return (PluginName + " Disabled clan chat!");
        }
    }

    private void logFixes(CommandSender sender, String type, String oldData, String newData) {
        if (oldData == null) oldData = "null";
        sender.sendMessage("Found an invalid " + type + " '" + oldData + "', replaced with '" + newData +"'");
    }
    // fixes potential errors in the yaml file (missing color, wrong clanNames...)
    public String clanFix(CommandSender sender) {
        ConfigurationSection clans = cfg.getConfig().getConfigurationSection("clans");


        for (String clanName : clans.getKeys(false)) {
            ConfigurationSection clanSection = clans.getConfigurationSection(clanName);
            String owner = clanSection.getString("owner");
            //plugin.getLogger().log(Level.INFO, owner);
            String clanColor = clanSection.getString("clancolor");
            //plugin.getLogger().log(Level.INFO, clanColor);
            String nameColor = clanSection.getString("namecolor");
            //plugin.getLogger().log(Level.INFO, nameColor);
            String chatColor = clanSection.getString("chatcolor");
            //plugin.getLogger().log(Level.INFO, chatColor);
            String clanSymbol = clanSection.getString("clansymbol");
            //plugin.getLogger().log(Level.INFO, clanSymbol);
            List<String> members = clanSection.getStringList("members");
            //plugin.getLogger().log(Level.INFO, Integer.toString(members.size()));
            List<String> moderators = clanSection.getStringList("moderators");
            //plugin.getLogger().log(Level.INFO, Integer.toString(moderators.size()));
            int state = clanSection.getInt("state");
            //plugin.getLogger().log(Level.INFO, Integer.toString(state));

            if (state == ClanState.DEACTIVATED.val && (members.size() + moderators.size() + 1) >= plugin.getConfig().getInt("min_player_requirement")) {
                evaluateState(clanSection);
                logFixes(sender, "state", Integer.toString(state), "fixed");
            }
            else if (state == ClanState.ACTIVATED.val && (members.size() + moderators.size() + 1) < plugin.getConfig().getInt("min_player_requirement")) {
                evaluateState(clanSection);
                logFixes(sender, "state", Integer.toString(state), "fixed");
            }

            if (owner == null || owner.isEmpty()) {
                clanSection.set("owner", "SERVER"); cfg.save();
                logFixes(sender, "owner", owner, "SERVER");
            }
            /*List<String> validColorChars = Arrays.asList("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f");
            if (clanColor == null || !validColorChars.contains(clanColor)) {
                clanSection.set("clancolor", "f");cfg.save();
                logFixes(sender, "clan color", clanColor, "f");
            }
            else if (clanColor.length() == 3 && clanColor.charAt(0) == '\'' && clanColor.charAt(2) == '\'') {
                clanSection.set("clancolor", String.valueOf(clanColor.charAt(1)));cfg.save();
                logFixes(sender, "clan color", clanColor, String.valueOf(clanColor.charAt(1)));
            }
            if (clanColor == null || !validColorChars.contains(nameColor)) {
                clanSection.set("namecolor", "f");cfg.save();
                logFixes(sender, "name color", nameColor, "f");
            }
            else if (nameColor.length() == 3 && nameColor.charAt(0) == '\'' && nameColor.charAt(2) == '\'') {
                clanSection.set("namecolor", String.valueOf(nameColor.charAt(1)));cfg.save();
                logFixes(sender, "name color", nameColor, String.valueOf(nameColor.charAt(1)));
            }
            if (clanColor == null || !validColorChars.contains(chatColor)) {
                clanSection.set("chatcolor", "f");cfg.save();
                logFixes(sender, "chat color", chatColor, "f");
            }
            else if (chatColor.length() == 3 && chatColor.charAt(0) == '\'' && chatColor.charAt(2) == '\'') {
                clanSection.set("chatcolor", String.valueOf(chatColor.charAt(1)));cfg.save();
                logFixes(sender, "chat color", chatColor, String.valueOf(chatColor.charAt(1)));
            }
            if (clanColor != null && clanSymbol.length() > 1) {
                clanSection.set("clansymbol", String.valueOf(clanSymbol.charAt(0)));cfg.save();
                logFixes(sender, "symbol", clanSymbol, String.valueOf(clanSymbol.charAt(0)));
            }
            else if (clanColor == null || clanSymbol.isEmpty()) {
                clanSection.set("clansymbol", "-");cfg.save();
                logFixes(sender, "symbol", clanSymbol, "-");
            }*/
        }

        ConfigurationSection playerSection = cfg.getConfig().getConfigurationSection("players");

        for (String playername : playerSection.getKeys(false)) {
            String clan = playerSection.getString(playername);
            if (clan == null || clan.length() == 0) {
                playerSection.set(playername, null);cfg.save();
                logFixes(sender, "clan in playerlist", clan, "deleted");
            }
        }

        cfg.save();

        return PluginName + " Fixed all data";
    }

    public String warnClan(CommandSender sender, String clanName) {
        // TODO: list clans on the command
        boolean operator = sender.isOp();
        if (!operator) return PluginName + " Missing permissions.";

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        int warnings = clanSection.getInt("warnings");
        warnings++;
        clanSection.set("warnings", warnings);
        cfg.save();

        sendAll(clanSection, PluginName + " Your clan has been " + ChatColor.BOLD + "warned!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        return PluginName + " Successfully warned " + clanName + "!";
    }
    public String setOwner(String playerUUID, String clanName, String playerName) {
        // TODO: list clans on the command
        boolean operator = Bukkit.getPlayer(UUID.fromString(playerUUID)).isOp();
        if (!operator) return PluginName + " Missing permissions.";

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return PluginName + " PluginError player not found";
        }

        // remove playerUUID player from clans
        if (isInsideClan(playerUUID)) {
            String ursurperClanName = getClanFromUUID(playerUUID);
            ConfigurationSection ursurperClan = getClan(ursurperClanName);
            List<String> mods = ursurperClan.getStringList("moderators");
            List<String> members = ursurperClan.getStringList("members");
            String owner = ursurperClan.getString("owner");
            if (owner.equalsIgnoreCase(playerUUID)) {
                ursurperClan.set("owner", "SERVER");
            } else if (mods.contains(playerUUID)) {
                mods.remove(playerUUID);
                ursurperClan.set("moderators", mods);
            } else if (members.contains(playerUUID)) {
                members.remove(playerUUID);
                ursurperClan.set("members", members);
            }
        }

        String originalOwner = clanSection.getString("owner");
        assert originalOwner != null;
        clanSection.set("owner", player.getUniqueId().toString());
        cfg.getConfig().getConfigurationSection("players").set(originalOwner, null);
        cfg.getConfig().getConfigurationSection("players").set(player.getUniqueId().toString(), clanName);

        cfg.save();

        return PluginName + " Set " + player.getDisplayName() + " to owner.";
    }
    public String disableClan(CommandSender sender, String clanName) {
        // TODO: list clans on the command
        boolean operator = sender.isOp();
        if (!operator) return PluginName + " Missing permissions.";

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        int state = clanSection.getInt("state");
        if (state != ClanState.DISABLED.val) {
            state = ClanState.DISABLED.val;
        }
        else {
            return PluginName + " Clan already disabled!";
        }

        clanSection.set("state", state);
        removePlayersFromClanChat(clanSection);
        cfg.save();

        sendAll(clanSection, PluginName + " Your clan has been " + ChatColor.BOLD + "disabled! " + ChatColor.RESET + ChatColor.GRAY, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        Player owner = Bukkit.getPlayer(UUID.fromString(clanSection.getString("owner")));
        if (owner != null) {
            owner.sendMessage(PluginName + ChatColor.RED + " You no longer have control over the clan.");
        }

        webhookLog("Clan **" + clanName + "** was **disabled**. <:blunder:1236268861859434567>");

        return PluginName + " Successfully disabled " + clanName + "!";
    }
    public String enableClan(CommandSender sender, String clanName) {
        // TODO: list clans on the command
        boolean operator = sender.isOp();
        if (!operator) return PluginName + " Missing permissions.";

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        int state = clanSection.getInt("state");
        if (state == ClanState.DISABLED.val) {
            int count = getPlayerCount(clanSection);
            if (count >= plugin.getConfig().getInt("min_player_requirement")) state = ClanState.ACTIVATED.val;
            else state = ClanState.DEACTIVATED.val;
        }
        else {
            return PluginName + " Clan already enabled!";
        }

        clanSection.set("state", state);
        cfg.save();

        sendAll(clanSection, PluginName + " Your clan has been " + ChatColor.BOLD + "reactivated!", Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);

        webhookLog("Clan **" + clanName + "** was **reactivated**. <:brilliant:1236268866251001856>");

        return PluginName + " Successfully reactivated " + clanName + "!";
    }
    public String opViewClan(CommandSender sender, String clanName) {
        // TODO: list clans on the command
        boolean operator = sender.isOp();
        if (!operator) return PluginName + " Missing permissions.";

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        String ownerUUID = clanSection.getString("owner");
        return viewClan(ownerUUID);
    }
    public String opClanProperties(CommandSender sender, String clanName) {
        // TODO: list clans on the command
        boolean operator = sender.isOp();
        if (!operator) return PluginName + " Missing permissions.";

        ConfigurationSection clanSection = getClan(clanName);
        if (clanSection == null) return PluginName + " Couldn't find the clan " + clanName;

        String ownerUUID = clanSection.getString("owner");
        return clanProperties(ownerUUID);
    }
    public void reload() {
        cfg.reload();
    }
}