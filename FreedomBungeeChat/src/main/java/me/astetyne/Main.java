package me.astetyne;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import me.astetyne.commands.AdminChatCommand;
import me.astetyne.commands.IgnoreCommand;
import me.astetyne.commands.KickCommand;
import me.astetyne.commands.MuteAllCommand;
import me.astetyne.commands.MuteCommand;
import me.astetyne.commands.NickCommand;
import me.astetyne.commands.PrivateMessageBackCommand;
import me.astetyne.commands.PrivateMessageCommand;
import me.astetyne.commands.ReloadCommand;
import me.astetyne.commands.SetColorCommand;
import me.astetyne.commands.SocialSpyCommand;
import me.astetyne.commands.UnmuteCommand;
import me.astetyne.config.GlobalChatConfiguration;
import me.astetyne.message.GlobalMessage;
import me.astetyne.playerdata.LoaderRunnable;
import me.astetyne.playerdata.LuckPermsManager;
import me.astetyne.playerdata.GPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class Main extends Plugin implements Listener {
	
	public static final Logger LOG = ProxyServer.getInstance().getLogger();
	public static List<GPlayer> gPlayers = new ArrayList<>();
	public static Plugin plugin;
	
	@Override
    public void onEnable() {
		
		Plugin luckPerms = getProxy().getPluginManager().getPlugin("LuckPerms");
		
		if(luckPerms==null) {
			LOG.warning("-----------------------------");
			LOG.warning("GlobalChat requires LuckPerms installed.");
			LOG.warning("Plugin will NOT WORK please install LuckPerms v5 and restart the server.");
			LOG.warning("-----------------------------");
			return;
		}
		
		plugin = this;
		GlobalChatConfiguration.load(this);

		ProxyServer.getInstance().getScheduler().schedule(this, new LoaderRunnable(), 5, 5,TimeUnit.SECONDS);
        
        getProxy().getPluginManager().registerListener(this, this); 
        
        getProxy().getPluginManager().registerCommand(this, new AdminChatCommand());
        getProxy().getPluginManager().registerCommand(this, new KickCommand());
        getProxy().getPluginManager().registerCommand(this, new MuteCommand());
        getProxy().getPluginManager().registerCommand(this, new MuteAllCommand());
        getProxy().getPluginManager().registerCommand(this, new PrivateMessageBackCommand());
        getProxy().getPluginManager().registerCommand(this, new PrivateMessageCommand());
        getProxy().getPluginManager().registerCommand(this, new SetColorCommand());
        getProxy().getPluginManager().registerCommand(this, new SocialSpyCommand());
        getProxy().getPluginManager().registerCommand(this, new IgnoreCommand());
        getProxy().getPluginManager().registerCommand(this, new UnmuteCommand());
        getProxy().getPluginManager().registerCommand(this, new NickCommand());
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
    }
	
	@Override
	public void onDisable() {
		
		for(GPlayer gp : gPlayers) {
			LuckPermsManager.saveAllDataFromPlayer(gp);
		}
		
		if(GlobalChatConfiguration.config.getBoolean("removeMutesOnServerRestart")) {
			LuckPermsManager.removeAllMuteNodes();
		}
		
	}

	@EventHandler
	public void onChat(ChatEvent e) {
		
		if(e.isCancelled()) {
			return;
		}
		
		String line = e.getMessage();
		if(line.startsWith("/")) {
			return;
		}

		if(!(e.getSender() instanceof ProxiedPlayer pp)) {
			return;
		}
		GPlayer pd = getPlayerData(pp);
		
		if(pd == null) {
			return;
		}
		
		GlobalMessage gs = new GlobalMessage(pd, line);
		gs.wantsToSendMessage();

		e.setCancelled(true);
	}
	
	public static GPlayer getPlayerData(ProxiedPlayer pp) {
		for(GPlayer gp : gPlayers) {
			if(gp.getProxiedPlayer().equals(pp)) {
				return gp;
			}
		}
		return null;
	}

	@EventHandler
	public void onPostJoin(PostLoginEvent e) {
		
		GPlayer gPlayer = new GPlayer(e.getPlayer());
		gPlayers.add(gPlayer);
	}

	@EventHandler
	public void onLeave(PlayerDisconnectEvent e) {

		GPlayer gPlayer = getPlayerData(e.getPlayer());
		gPlayers.remove(gPlayer);
		
		if(!gPlayer.isAlreadyJoinedFromAuth()) {
			return;
		}

		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()){
			p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY+"["+ChatColor.GRAY+"WoF"+ChatColor.DARK_GRAY+"] "+ChatColor.WHITE+e.getPlayer().getName()+ChatColor.GRAY+" left the game."));
		}
		LuckPermsManager.saveAllDataFromPlayer(gPlayer);
	}

	@EventHandler
	public void onServerSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		GPlayer gPlayer = getPlayerData(player);
		assert gPlayer != null;
		ServerInfo origin = event.getFrom();

		// if initial connection to proxy
		if(origin == null) {
			return;
		}

		// player already connected from auth to somewhere else
		if(!origin.getName().startsWith("auth") || gPlayer.isAlreadyJoinedFromAuth()) {
			return;
		}

		gPlayer.setAlreadyJoinedFromAuth(true);
		for(ProxiedPlayer p : ProxyServer.getInstance().getPlayers()){
			p.sendMessage(TextComponent.fromLegacyText(ChatColor.DARK_GRAY+"["+ChatColor.GRAY+"WoF"+ChatColor.DARK_GRAY+"] "+ChatColor.WHITE+player.getName()+ChatColor.GRAY+" joined the game."));
		}
		LuckPermsManager.loadAllDataToPlayer(gPlayer);
	}
	
	public static void logMessage(String msg) {
		LOG.info(ChatUtils.PLUGIN_PREFIX + msg);
	}
}
