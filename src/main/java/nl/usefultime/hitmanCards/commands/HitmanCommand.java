package nl.usefultime.hitmanCards.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import nl.usefultime.hitmanCards.HitmanCards;
import nl.usefultime.hitmanCards.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HitmanCommand implements CommandExecutor {
    private final HitmanCards plugin;

    public HitmanCommand(HitmanCards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                Game newGame = plugin.getGameManager().createGame(player);
                plugin.getGameManager().createGame(player);
                TextComponent message = new TextComponent("§6Game created successfully! Game ID: ");
                TextComponent idComponent = new TextComponent("§e[Click to Copy ID]");
                idComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, newGame.getGameId().toString()));
                idComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to copy game ID")));
                message.addExtra(idComponent);
                player.spigot().sendMessage(message);

                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage("Usage: /hitman join <gameId>");
                    return true;
                }
                try {
                    UUID gameId = UUID.fromString(args[1]);
                    if (plugin.getGameManager().joinGame(gameId, player)) {
                        player.sendMessage("Joined game successfully!");
                    } else {
                        player.sendMessage("Could not join game!");
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid game ID!");
                }
                break;
            case "leave":
                plugin.getGameManager().leaveGame(player);
                player.sendMessage("Left the game!");
                break;
            case "start":
                Game game = plugin.getGameManager().getPlayerGame(player);
                if (game != null) {
                    game.startGame();
                    player.sendMessage("Game started!");
                } else {
                    player.sendMessage("You're not in a game!");
                }
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== HitmanCards Commands ===");
        player.sendMessage("§f/hitman create §7- Create a new game");
        player.sendMessage("§f/hitman join <gameId> §7- Join a game");
        player.sendMessage("§f/hitman leave §7- Leave current game");
        player.sendMessage("§f/hitman start §7- Start the game");
    }
}
