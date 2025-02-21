package nl.usefultime.hitmanCards.cards;

public enum CardColor {
    RED("§c"),
    BLUE("§9"),
    GREEN("§a"),
    YELLOW("§e"),
    SPECIAL("§5");

    private final String colorCode;

    CardColor(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return colorCode;
    }
}
