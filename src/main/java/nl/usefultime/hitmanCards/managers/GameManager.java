package nl.usefultime.hitmanCards.managers;

import nl.usefultime.hitmanCards.HitmanCards;
import nl.usefultime.hitmanCards.game.Game;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {
    private final HitmanCards plugin;
    private final Map<UUID, Game> activeGames;
    private final Map<UUID, UUID> playerGameMap;

    public GameManager(HitmanCards plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
        this.playerGameMap = new HashMap<>();
    }

    public Game createGame(Player host) {
        Game game = new Game(host);
        activeGames.put(game.getGameId(), game);
        playerGameMap.put(host.getUniqueId(), game.getGameId());
        return game;
    }

    public boolean joinGame(UUID gameId, Player player) {
        Game game = activeGames.get(gameId);
        if (game != null && game.addPlayer(player)) {
            playerGameMap.put(player.getUniqueId(), gameId);
            return true;
        }
        return false;
    }

    public void leaveGame(Player player) {
        UUID gameId = playerGameMap.get(player.getUniqueId());
        if (gameId != null) {
            Game game = activeGames.get(gameId);
            if (game != null) {
                game.removePlayer(player);
                if (game.getPlayers().isEmpty()) {
                    activeGames.remove(gameId);
                }
            }
            playerGameMap.remove(player.getUniqueId());
        }
    }

    public void endAllGames() {
        activeGames.values().forEach(Game::endGame);
        activeGames.clear();
        playerGameMap.clear();
    }

    public Game getPlayerGame(Player player) {
        UUID gameId = playerGameMap.get(player.getUniqueId());
        return gameId != null ? activeGames.get(gameId) : null;
    }
}
