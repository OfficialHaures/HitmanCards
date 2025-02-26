package nl.usefultime.hitmanCards;

import nl.usefultime.hitmanCards.commands.HitmanCommand;
import nl.usefultime.hitmanCards.listeners.GameListener;
import nl.usefultime.hitmanCards.managers.CardManager;
import nl.usefultime.hitmanCards.managers.GameManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HitmanCards extends JavaPlugin {
    private static HitmanCards instance;
    private GameManager gameManager;
    private CardManager cardManager;

    @Override
    public void onEnable() {
        instance = this;

        this.cardManager = new CardManager();
        this.gameManager = new GameManager(cardManager);

        getCommand("hitman").setExecutor(new HitmanCommand(this));

        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        saveDefaultConfig();

        getLogger().info("HitmanCards enabled successfully!");
    }

    @Override
    public void onDisable() {
        gameManager.endAllGames();
        instance = null;
    }

    public static HitmanCards getInstance() {
        return instance;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public CardManager getCardManager() {
        return cardManager;
    }
}
