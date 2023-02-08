package me.astetyne.message;

import java.util.List;

import me.astetyne.ChatUtils;
import me.astetyne.Main;
import me.astetyne.Permissions;
import me.astetyne.config.GlobalChatConfiguration;
import me.astetyne.playerdata.GPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class GlobalMessage {
	
	private ProxiedPlayer sender;
	private GPlayer pd;
	private String message;
	private String prefix;
	private String suffix;
	private Server server;
	private String nickname;
	
	public GlobalMessage(GPlayer pd, String message) {
		this.sender = pd.getProxiedPlayer();
		this.message = message;
		this.pd = pd;
		this.prefix = pd.getPrefix();
		this.suffix = pd.getSuffix();
		this.server = sender.getServer();
		String customNickname = pd.getCustomNick();
		this.nickname = (customNickname == null) ? pd.getProxiedPlayer().getName() : customNickname;
	}
	
	public void wantsToSendMessage() {
		if(pd.isMuted()) {
			sender.sendMessage(ChatUtils.SILENCE_CANT_TALK);
			return;
		}
		
		if(pd.hasActivedAdminChat()) {
			for(GPlayer pdd : Main.gPlayers) {
				if(pdd.getProxiedPlayer().hasPermission(Permissions.ADMINCHAT)) {
					ProxiedPlayer admin = pdd.getProxiedPlayer();
					if(admin.hasPermission(Permissions.COLORED_MESSAGES)) {
						message = ChatColor.translateAlternateColorCodes('&', message);
					}
					admin.sendMessage(TextComponent.fromLegacyText(ChatUtils.ADMIN_CHAT + ChatColor.GRAY + nickname + "> " + ChatColor.RED + message));
				}
			}
			Main.logMessage("[AC] "+pd.getProxiedPlayer().getName()+">"+message);
			return;
		}
		
		if(!pd.getProxiedPlayer().hasPermission(Permissions.NO_MESSAGE_CHECK)) {
			if(isMessageSpam(message)) {
				return;
			}
			message = createCleanMessage(message);
			message = message.trim();
			if(message.isEmpty()) {
				return;
			}
		}
		message = message.trim();
		char msgColorIndex = pd.getColorIndex();
		message = ChatColor.getByChar(msgColorIndex) + message;
		
		if(pd.getProxiedPlayer().hasPermission(Permissions.COLORED_MESSAGES)) {
			message = ChatColor.translateAlternateColorCodes('&', message);
		}
		
		createAndSendFinalMessage();
	}
	
	private void createAndSendFinalMessage() {
		
		pd.setLastMessage(message);
		pd.setLastTimestamp(System.currentTimeMillis());

		StringBuilder finalMessage = new StringBuilder();
		appendServerShortName(finalMessage, server);

		finalMessage.append(ChatColor.translateAlternateColorCodes('&', prefix));
		
		if(prefix.isEmpty()) {
			finalMessage.append(ChatColor.DARK_GRAY);
		}else {
			finalMessage.append(" ");
		}
		
		finalMessage.append(ChatColor.translateAlternateColorCodes('&', nickname));
		
		if(!suffix.isEmpty()) {
			finalMessage.append(" ");
		}
		
		finalMessage.append(ChatColor.RESET);
		finalMessage.append(ChatColor.translateAlternateColorCodes('&', suffix));
		
		char c = GlobalChatConfiguration.config.getString("defaultArrowColor").charAt(0);
		
		finalMessage.append(ChatColor.getByChar(c)).append(ChatColor.BOLD).append(" > ");
		finalMessage.append(message);

		outer:
		for(GPlayer pdd : Main.gPlayers) {
			for(String ignoreName : pdd.getIgnoredPlayersNames()) {
				if(ignoreName.equals(sender.getName())) {
					continue outer;
				}
			}
			pdd.getProxiedPlayer().sendMessage(TextComponent.fromLegacyText(finalMessage.toString()));
		}
		Main.logMessage(sender.getName() + "> " + message);
	}
	
	private boolean isMessageSpam(String msg) {
		
		int waitTime = GlobalChatConfiguration.config.getInt("waitSpamTime");
		
		if(pd.getLastTimestamp() + waitTime * 1000 > System.currentTimeMillis()) {
			sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.SPAM + ChatColor.RED + "Calm down! Wait few seconds before next message."));
			return true;
		}
		
		if(ChatColor.stripColor(pd.getLastMessage()).equalsIgnoreCase(ChatColor.stripColor(message))) {
			sender.sendMessage(TextComponent.fromLegacyText(ChatUtils.SPAM + ChatColor.RED + "Do not send similar messages!"));
			return true;
		}
		return false;
	}
	
	private String createCleanMessage(String msg) {
		//msg = msg.replace('.', ' ');
		
		/*while(msg.contains("  ")) {
			msg = msg.replaceAll("  ", " ");
		}*/
		
		msg = stripBadWords(msg);
		
		return msg;
	}

	private void appendServerShortName(StringBuilder sb, Server server) {
		char firstChar = Character.toUpperCase(server.getInfo().getName().charAt(0));
		char secondChar = server.getInfo().getName().charAt(1);

		sb.append(ChatColor.GRAY);
		sb.append('[');
		sb.append(ChatColor.YELLOW);
		sb.append(firstChar);
		sb.append(secondChar);
		sb.append(ChatColor.GRAY);
		sb.append(']');
		sb.append(ChatColor.RESET);
	}

	private String stripBadWords(String msg) {
		
		List<String> badWords = GlobalChatConfiguration.config.getStringList("badWords");
		
		for(String badWord : badWords) {
			if(msg.contains(badWord)) {
				msg = msg.replaceAll(badWord, "***");
			}
		}
		return msg;
	}
	
	
}
