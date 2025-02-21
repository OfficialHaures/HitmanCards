package nl.usefultime.hitmanCards.game;

import nl.usefultime.hitmanCards.HitmanCards;
import nl.usefultime.hitmanCards.cards.Card;
import nl.usefultime.hitmanCards.cards.CardColor;
import nl.usefultime.hitmanCards.cards.CardType;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Game {
    private final UUID gameId;
    private static List<Player> players;
    private final Map<UUID, List<Card>> playerHands;
    private Card topCard;
    private int currentPlayerIndex;
    private boolean reversed;
    private static GameState state;
    private final List<Card> deck;
    private final List<Card> discardPile;

    public Game(Player host) {
        this.gameId = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.playerHands = new HashMap<>();
        this.players.add(host);
        this.state = GameState.WAITING;
        this.currentPlayerIndex = 0;
        this.reversed = false;
        this.deck = initializeDeck();
        this.discardPile = new ArrayList<>();
    }

    private List<Card> initializeDeck() {
        List<Card> newDeck = new ArrayList<>();
        for (CardColor color : CardColor.values()) {
            if (color != CardColor.SPECIAL) {
                for (int i = 0; i < 2; i++) {
                    newDeck.add(new Card(CardType.ELIMINATION, color, "Eliminate"));
                    newDeck.add(new Card(CardType.STEALTH, color, "Hide"));
                    newDeck.add(new Card(CardType.DISTRACTION, color, "Distract"));
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            newDeck.add(new Card(CardType.COUNTER, CardColor.SPECIAL, "Block"));
            newDeck.add(new Card(CardType.SPECIAL, CardColor.SPECIAL, "Wild"));
        }
        Collections.shuffle(newDeck);
        return newDeck;
    }

    public void startGame() {
        if (players.size() >= 2 && state == GameState.WAITING) {
            state = GameState.PLAYING;
            dealInitialCards();
            topCard = drawCard();
            while (topCard.getColor() == CardColor.SPECIAL) {
                deck.add(topCard);
                Collections.shuffle(deck);
                topCard = drawCard();
            }

            for (Player player : players) {
                givePlayerCards(player);
            }

            Player firstPlayer = players.get(currentPlayerIndex);
            broadcastMessage("§6Game started! First player: §f" + firstPlayer.getName());
            broadcastMessage("§6Top card: §f" + topCard.getDisplayName());
        }
    }

    private void dealInitialCards() {
        for (Player player : players) {
            List<Card> hand = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                hand.add(drawCard());
            }
            playerHands.put(player.getUniqueId(), hand);
        }
    }

    private Card drawCard() {
        if (deck.isEmpty()) {
            Card top = topCard;
            deck.addAll(discardPile);
            discardPile.clear();
            discardPile.add(top);
            Collections.shuffle(deck);
        }
        return deck.remove(0);
    }

    public void drawCard(Player player) {
        List<Card> hand = playerHands.get(player.getUniqueId());
        hand.add(drawCard());
        givePlayerCards(player);
    }

    public void playCard(Player player, int cardIndex) {
        if (!isPlayerTurn(player)) {
            player.sendMessage("§cIt's not your turn!");
            return;
        }

        List<Card> hand = playerHands.get(player.getUniqueId());
        if (cardIndex >= 0 && cardIndex < hand.size()) {
            Card playedCard = hand.get(cardIndex);

            if (canPlayCard(playedCard)) {
                hand.remove(cardIndex);
                discardPile.add(topCard);
                topCard = playedCard;

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                broadcastMessage("§6" + player.getName() + " played: §f" + playedCard.getDisplayName());

                executeCardEffect(playedCard);

                if (hand.isEmpty()) {
                    endGame();
                    return;
                }

                nextTurn();
                givePlayerCards(player);
            } else {
                player.sendMessage("§cYou can't play this card!");
            }
        }
    }

    private boolean canPlayCard(Card card) {
        return card.getColor() == CardColor.SPECIAL ||
                card.getColor() == topCard.getColor() ||
                card.getType() == topCard.getType();
    }

    private void executeCardEffect(Card card) {
        switch (card.getType()) {
            case ELIMINATION:
                nextTurn();
                break;
            case STEALTH:
                reversed = !reversed;
                break;
            case DISTRACTION:
                Player nextPlayer = players.get((currentPlayerIndex + 1) % players.size());
                drawCards(nextPlayer, 2);
                break;
            case COUNTER:
                nextTurn();
                break;
        }
    }

    private void drawCards(Player player, int amount) {
        List<Card> hand = playerHands.get(player.getUniqueId());
        for (int i = 0; i < amount; i++) {
            hand.add(drawCard());
        }
        givePlayerCards(player);
    }

    public void handlePlayerInteraction(Player player, Action action) {
        if (state != GameState.PLAYING) return;

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (isPlayerTurn(player)) {
                int slot = player.getInventory().getHeldItemSlot();
                playCard(player, slot);
            } else {
                player.sendMessage("§cIt's not your turn!");
            }
        }
    }

    private void givePlayerCards(Player player) {
        player.getInventory().clear();
        List<Card> hand = playerHands.get(player.getUniqueId());

        player.sendMessage("§6Current card: §f" + topCard.getDisplayName());
        player.sendMessage("§6Required: §f" + topCard.getColor() + " or " + topCard.getType());

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            ItemStack cardItem = card.createItemStack();
            ItemMeta meta = cardItem.getItemMeta();
            List<String> lore = new ArrayList<>();

            if (canPlayCard(card)) {
                lore.add("§a➤ Valid move!");
                lore.add("§7Right-click to play");
                meta.addEnchant(Enchantment.LUCK, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add("§c✖ Cannot play this card");
                lore.add("§7Must match: " + topCard.getColor() + " or " + topCard.getType());
            }

            meta.setLore(lore);
            cardItem.setItemMeta(meta);
            player.getInventory().setItem(i, cardItem);
        }

        ItemStack drawCard = new ItemStack(Material.HOPPER);
        ItemMeta meta = drawCard.getItemMeta();
        meta.setDisplayName("§eDraw a Card");
        drawCard.setItemMeta(meta);
        player.getInventory().setItem(8, drawCard);
    }

    private void nextTurn() {
        if (reversed) {
            currentPlayerIndex--;
            if (currentPlayerIndex < 0) currentPlayerIndex = players.size() - 1;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }

        Player nextPlayer = players.get(currentPlayerIndex);
        broadcastMessage("§6Current turn: §f" + nextPlayer.getName());

        for (Player player : players) {
            if (player.equals(nextPlayer)) {
                givePlayerCards(player);
            } else {
                player.getInventory().clear();
            }
        }
    }

    public void handleCombat(Player damager, Player damaged) {
        if (state != GameState.PLAYING) return;

        List<Card> damagerHand = playerHands.get(damager.getUniqueId());
        List<Card> damagedHand = playerHands.get(damaged.getUniqueId());

        if (hasCardType(damagerHand, CardType.ELIMINATION)) {
            drawCards(damaged, 2);
            broadcastMessage("§c" + damager.getName() + " eliminated " + damaged.getName() + "!");
            damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
        }

        if (hasCardType(damagedHand, CardType.COUNTER)) {
            drawCards(damager, 1);
            broadcastMessage("§e" + damaged.getName() + " countered the attack!");
            damaged.playSound(damaged.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        }
    }

    private boolean hasCardType(List<Card> hand, CardType type) {
        return hand.stream().anyMatch(card -> card.getType() == type);
    }

    public void handlePlayerMovement(Player player, Location from, Location to) {
        if (state != GameState.PLAYING) return;

        List<Card> playerHand = playerHands.get(player.getUniqueId());

        if (hasCardType(playerHand, CardType.STEALTH)) {
            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, from, 1, 0, 0, 0, 0);
        }
        if (hasCardType(playerHand, CardType.DISTRACTION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 0));
        }
        if (hasCardType(playerHand, CardType.ELIMINATION)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0));
        }
    }

    public void endGame() {
        state = GameState.ENDED;
        Player winner = null;
        int highestScore = -1;

        for (Player player : players) {
            int score = playerHands.get(player.getUniqueId()).size();
            if (score > highestScore) {
                highestScore = score;
                winner = player;
            }
        }

        broadcastMessage("§6§l============");
        broadcastMessage("§6§lGame Over!");
        broadcastMessage("§6§lThe winner is §f" + winner.getName() + "§6§l!");
        broadcastMessage("§6§l============");

        winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        winner.getWorld().spawnParticle(Particle.TOTEM, winner.getLocation(), 50);

        for (Player player : players) {
            player.getInventory().clear();
        }

        players.clear();
        playerHands.clear();
    }

    private void broadcastMessage(String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public boolean addPlayer(Player player) {
        if (state == GameState.WAITING && players.size() < 4) {
            players.add(player);
            return true;
        }
        return false;
    }

    public void removePlayer(Player player) {
        players.remove(player);
        playerHands.remove(player.getUniqueId());
    }

    public UUID getGameId() {
        return gameId;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public GameState getState() {
        return state;
    }

    public boolean isPlayerTurn(Player player) {
        return players.get(currentPlayerIndex).equals(player);
    }
}
