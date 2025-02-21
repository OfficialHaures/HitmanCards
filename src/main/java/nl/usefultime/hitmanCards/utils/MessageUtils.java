package nl.usefultime.hitmanCards.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageUtils {
    public static void sendMessage(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void broadcastGameMessage(List<Player> players, String message) {
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        players.forEach(player -> player.sendMessage(coloredMessage));
    }
}
