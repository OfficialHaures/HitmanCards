package nl.usefultime.hitmanCards.listeners;

import nl.usefultime.hitmanCards.HitmanCards;
import nl.usefultime.hitmanCards.game.Game;
import nl.usefultime.hitmanCards.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameListener implements Listener {
    private final HitmanCards plugin;

    public GameListener(HitmanCards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        giveGameItems(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGameManager().leaveGame(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Game game = plugin.getGameManager().getPlayerGame(player);

        if (item == null) return;

        if (item.getType() == Material.COMPASS && item.hasItemMeta() &&
                item.getItemMeta().getDisplayName().equals("§6HitmanCards Menu")) {
            handleGameMenu(player, event.getAction());
            event.setCancelled(true);
            return;
        }

        if (game != null && game.getState() == GameState.PLAYING) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                int slot = player.getInventory().getHeldItemSlot();
                if (item.getType() == Material.HOPPER) {
                    game.drawCard(player);
                } else {
                    game.playCard(player, slot);
                }
                event.setCancelled(true);
            }
        }
    }

    private void handleGameMenu(Player player, Action action) {
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            Game game = plugin.getGameManager().getPlayerGame(player);

            if (game == null) {
                game = plugin.getGameManager().createGame(player);
                player.sendMessage("§6Game created! Share your game ID: §f" + game.getGameId());
            } else if (game.getState() == GameState.WAITING) {
                game.startGame();
            }
        }
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
