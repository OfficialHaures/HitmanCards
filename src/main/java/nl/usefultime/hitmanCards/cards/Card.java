package nl.usefultime.hitmanCards.cards;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Card {
    private CardType type;
    private String name;
    private String description;
    private CardColor color;


    public Card(CardType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public Card(CardType cardType, CardColor cardColor, String wild) {
        this.type = cardType;
        this.color = cardColor;
        this.name = cardType.name();
    }

    public CardType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemStack createItemStack() {
        Material material = switch (color) {
            case RED -> Material.RED_CONCRETE;
            case BLUE -> Material.BLUE_CONCRETE;
            case GREEN -> Material.GREEN_CONCRETE;
            case YELLOW -> Material.YELLOW_CONCRETE;
            case SPECIAL -> Material.PURPLE_CONCRETE;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color.getColorCode() + type.name());

        List<String> lore = new ArrayList<>();
        lore.add("§7" + name);
        lore.add("§8Click to play this card");
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public String getDisplayName() {
        return color.getColorCode() + name + " " + type.name();
    }

    public CardColor getColor() {
        return color;
    }
}
