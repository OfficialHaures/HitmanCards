package nl.usefultime.hitmanCards.listeners;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import nl.usefultime.hitmanCards.HitmanCards;
import nl.usefultime.hitmanCards.game.Game;
import nl.usefultime.hitmanCards.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GameListener implements Listener {
    private final HitmanCards plugin;

    public GameListener(HitmanCards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveGameItems(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGameManager().leaveGame(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null && game.isPlayerTurn(player) && game.getState() == GameState.PLAYING) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> game.showPlayerCards(player), 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null && event.getView().getTitle().equals("§6Your Turn - Select a Card")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null) {
                if (event.getCurrentItem().getType() == Material.HOPPER) {
                    game.drawCard(player);
                    game.nextTurn();
                } else {
                    game.playCard(player, event.getSlot());
                }
            }
        }

    }

    private void handleMenuClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        Player player = (Player) event.getWhoClicked();

        switch (event.getCurrentItem().getType()) {
            case EMERALD_BLOCK:
                createGame(player);
                break;
            case DIAMOND:
                player.sendMessage("§6Use the game compass to join a game!");
                break;
            case REDSTONE_BLOCK:
                leaveGame(player);
                break;
            case GOLD_BLOCK:
                startGame(player);
                break;
        }
        player.closeInventory();
    }

    private void createGame(Player player) {
        if (plugin.getGameManager().getPlayerGame(player) != null) {
            player.sendMessage("§cYou're already in a game!");
            return;
        }
        Game newGame = plugin.getGameManager().createGame(player);
        TextComponent message = new TextComponent("§6Game created! ");
        TextComponent copyButton = new TextComponent("§e[Click to Copy ID]");
        copyButton.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, newGame.getGameId().toString()));
        copyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to copy game ID")));
        message.addExtra(copyButton);
        player.spigot().sendMessage(message);
    }

    private void leaveGame(Player player) {
        if (plugin.getGameManager().getPlayerGame(player) == null) {
            player.sendMessage("§cYou're not in a game!");
            return;
        }
        plugin.getGameManager().leaveGame(player);
        player.sendMessage("§aYou left the game!");
        giveGameItems(player);
    }

    private void startGame(Player player) {
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game == null) {
            player.sendMessage("§cYou're not in a game!");
            return;
        }
        if (game.getState() != GameState.WAITING) {
            player.sendMessage("§cGame has already started!");
            return;
        }
        if (game.getPlayers().size() < 2) {
            player.sendMessage("§cNeed at least 2 players to start!");
            return;
        }
        game.startGame();
    }

    private void giveGameItems(Player player) {
        player.getInventory().clear();
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§6HitmanCards Menu");
        compass.setItemMeta(meta);
        player.getInventory().setItem(4, compass);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.COMPASS &&
                item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§6HitmanCards Menu")) {
            openGameMenu(player);
            event.setCancelled(true);
        }

        if (item != null && item.getType() == Material.PAPER) {
            Game game = plugin.getGameManager().getPlayerGame(player);
            if (game != null && game.isPlayerTurn(player)) {
                game.drawCard(player);
                game.nextTurn();
            }
        }

        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game != null && game.getState() == GameState.PLAYING) {
            game.handlePlayerInteraction(player, event.getAction());
        }
    }

    private void openGameMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "§6HitmanCards Menu");

        ItemStack createGame = createMenuItem(Material.EMERALD_BLOCK, "§aCreate Game", "§7Click to create a new game");
        ItemStack joinGame = createMenuItem(Material.DIAMOND, "§bJoin Game", "§7Click to join an existing game");
        ItemStack leaveGame = createMenuItem(Material.REDSTONE_BLOCK, "§cLeave Game", "§7Click to leave your current game");
        ItemStack startGame = createMenuItem(Material.GOLD_BLOCK, "§6Start Game", "§7Click to start the game");

        menu.setItem(10, createGame);
        menu.setItem(12, joinGame);
        menu.setItem(14, leaveGame);
        menu.setItem(16, startGame);

        player.openInventory(menu);
    }

    private ItemStack createMenuItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Game game = plugin.getGameManager().getPlayerGame(damaged);
        if (game != null && game.getState() == GameState.PLAYING) {
            game.handleCombat(damager, damaged);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Game game = plugin.getGameManager().getPlayerGame(player);

        if (game != null && game.getState() == GameState.PLAYING) {
            game.handlePlayerMovement(player, event.getFrom(), event.getTo());
        }
    }
}
