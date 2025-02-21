package nl.usefultime.hitmanCards.gui;

import nl.usefultime.hitmanCards.cards.Card;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class CardGUI {
    private final Inventory inventory;

    public CardGUI(Player player, List<Card> cards) {
        this.inventory = Bukkit.createInventory(null, 27, "Your Cards");
        updateCards(cards);
    }

    public void updateCards(List<Card> cards) {
        inventory.clear();
        for (int i = 0; i < cards.size(); i++) {
            inventory.setItem(i, createCardItem(cards.get(i)));
        }
    }

    private ItemStack createCardItem(Card card) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6" + card.getName());
        meta.setLore(Arrays.asList(
            "§7Type: " + card.getType(),
            "§7" + card.getDescription()
        ));
        item.setItemMeta(meta);
        return item;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
