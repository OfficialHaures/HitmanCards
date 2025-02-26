package nl.usefultime.hitmanCards.managers;

import nl.usefultime.hitmanCards.cards.Card;
import nl.usefultime.hitmanCards.cards.CardType;
import nl.usefultime.hitmanCards.game.Game;
import nl.usefultime.hitmanCards.game.GameState;
import org.bukkit.entity.Player;

import java.util.*;

public class GameManager {
    private final CardManager cardManager;
    private final Map<Player, List<Card>> playerHands;
    private Map<UUID, Game> activeGames = new HashMap<>();
    private Map<UUID, Game> playerGames = new HashMap<>();
    private Card topCard;
    private Player currentPlayer;
    private List<Player> players;
    private boolean gameInProgress;

    public GameManager(CardManager cardManager) {
        this.cardManager = cardManager;
        this.playerHands = new HashMap<>();
        this.players = new ArrayList<>();
        this.gameInProgress = false;
    }

    public void startGame(List<Player> players) {
        if (gameInProgress) return;

        this.players = new ArrayList<>(players);
        gameInProgress = true;

        // Deal initial cards
        for (Player player : players) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                hand.add(cardManager.drawCard());
            }
            playerHands.put(player, hand);
        }

        topCard = cardManager.drawCard();
        currentPlayer = players.get(0);
    }

    public boolean playCard(Player player, Card card) {
        if (!gameInProgress || !currentPlayer.equals(player)) return false;

        List<Card> hand = playerHands.get(player);
        if (hand.contains(card) && isValidPlay(card)) {
            hand.remove(card);
            topCard = card;
            nextTurn();
            return true;
        }
        return false;
    }

    private boolean isValidPlay(Card card) {
        return card.getColor() == topCard.getColor() ||
               card.getType() == topCard.getType() ||
               card.getType() == CardType.WILD ||
               card.getType() == CardType.DRAW_FOUR;
    }

    private void nextTurn() {
        int currentIndex = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentIndex + 1) % players.size());
    }
    public void endAllGames() {
        gameInProgress = false;
        players.clear();
        playerHands.clear();
        topCard = null;
        currentPlayer = null;
        cardManager.shuffleDeck();
    }

    public void leaveGame(Player player) {
        Game game = playerGames.get(player.getUniqueId());
        if (game != null) {
            game.removePlayer(player);
            playerGames.remove(player.getUniqueId());

            if (game.getPlayers().isEmpty()) {
                activeGames.remove(game.getGameId());
            }

            player.getInventory().clear();
        }
    }

    public Game getPlayerGame(Player player) {
        return playerGames.get(player.getUniqueId());
    }

    public Game createGame(Player player) {
        Game game = new Game(player);
        activeGames.put(game.getGameId(), game);
        playerGames.put(player.getUniqueId(), game);
        return game;
    }

    public boolean joinGame(UUID gameId, Player player) {
        Game game = activeGames.get(gameId);

        if (game != null && game.getState() == GameState.WAITING) {
            if (game.addPlayer(player)) {
                playerGames.put(player.getUniqueId(), game);
                return true;
            }
        }
        return false;
    }

}