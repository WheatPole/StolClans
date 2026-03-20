package org.wple.stolClans;


import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;


public class OnChat implements Listener {

    private final StolClans plugin;
    private final ClanConfig clanConfig;
    private final String template;  // Cached template
    private final String clanChatTemplate;  // Cached template
    public OnChat(StolClans plugin) {
        this.plugin = plugin;
        String rawTemplate = plugin.getConfig().getString("template");
        assert rawTemplate != null;
        String clanChatTemplate = plugin.getConfig().getString("clanchat_template");
        assert clanChatTemplate != null;
        this.template = ChatColor.translateAlternateColorCodes('&', rawTemplate);
        this.clanChatTemplate = ChatColor.translateAlternateColorCodes('&', clanChatTemplate);
        this.clanConfig = plugin.clanConfig;
    }

    @EventHandler
    public void onSpeak(AsyncPlayerChatEvent e) {
        String chatFormat = e.getFormat();
        String clanName = plugin.getConfig().getString("noclanname");
        String clanColor = plugin.getConfig().getString("noclancol");
        String nameColor = plugin.getConfig().getString("noclannamecol");
		String chatColor = plugin.getConfig().getString("noclanchat");
        String clanSymbol = plugin.getConfig().getString("noclansym");
        boolean enabledClan = true;
        ConfigurationSection players = clanConfig.getConfig().getConfigurationSection("players");
        assert players != null;
        if (players.contains(e.getPlayer().getUniqueId().toString())) {
            clanName = players.getString(e.getPlayer().getUniqueId().toString());
            if (clanName != null) {
                ConfigurationSection clanSection = clanConfig.getConfig().getConfigurationSection("clans").getConfigurationSection(clanName);
                if (clanSection != null) {
                    int state = clanSection.getInt("state");
                    if (state == Clans.ClanState.DISABLED.val) {
                        enabledClan = false;
                        clanName = ChatColor.MAGIC + clanName + ChatColor.RESET;
                        clanSymbol = clanSection.getString("clansymbol");
                        //clanColor = clanSection.getString("clancolor");
                    }
                    else if (state == Clans.ClanState.DEACTIVATED.val) {
                        enabledClan = false;
                        clanName = ChatColor.STRIKETHROUGH + clanName + ChatColor.RESET;
                        clanSymbol = clanSection.getString("clansymbol");
                        clanColor = clanSection.getString("clancolor");
                    } else {
                        clanSymbol = clanSection.getString("clansymbol");
                        clanColor = clanSection.getString("clancolor");
                        nameColor = clanSection.getString("namecolor");
                        chatColor = clanSection.getString("chatcolor");
                    }
                }
                else {
                    clanName = plugin.getConfig().getString("noclanname");
                }
            }
        }
        //System.out.println("OnChat: " + clanName + " " + clanColor + " " + nameColor + " " + chatColor + " " + clanSymbol);
        // if clan chat enabled (and clan isn't disabled/deactivated)
        if (enabledClan && plugin.clans.getChatType().containsKey(e.getPlayer().getUniqueId().toString())
        && plugin.clans.getChatType().get(e.getPlayer().getUniqueId().toString()) == Clans.ChatType.CLAN) {
            String templateFormatted = clanChatTemplate.replace("<clan_name>", Clans.getColor(clanColor) + clanName).replace("<username>", e.getPlayer().getName());
            templateFormatted = templateFormatted.replace("<user_color>", Clans.getColor(nameColor)).replace("<chat_color>", Clans.getColor(chatColor)).replace("<message>", e.getMessage());

            e.setCancelled(true);
            Bukkit.getLogger().info(e.getPlayer().getName() + " sent through clan chat: " + e.getMessage());
            ConfigurationSection clanSection = plugin.clans.getClan(plugin.clans.getClanFromUUID(e.getPlayer().getUniqueId().toString()));
            String rank = plugin.clans.getRank(clanSection, e.getPlayer().getUniqueId().toString());
            templateFormatted = templateFormatted.replace("<rank>", rank);
            plugin.clans.sendAll(clanSection, ChatColor.BOLD + ChatColor.translateAlternateColorCodes('&', templateFormatted));
        }
        else {
            String templateFormatted = template.replace("<clan_name>", Clans.getColor(clanColor) + clanName).replace("<clan_symbol>", clanSymbol);
            templateFormatted = templateFormatted.replace("<user_color>", Clans.getColor(nameColor)).replace("<chat_color>", Clans.getColor(chatColor));
            e.setFormat(ChatColor.translateAlternateColorCodes('&', templateFormatted));
        }
    }
}
