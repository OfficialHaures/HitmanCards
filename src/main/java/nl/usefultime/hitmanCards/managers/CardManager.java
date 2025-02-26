package nl.usefultime.hitmanCards.managers;

import nl.usefultime.hitmanCards.cards.Card;
import nl.usefultime.hitmanCards.cards.CardColor;
import nl.usefultime.hitmanCards.cards.CardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardManager {
    private List<Card> deck;

    public CardManager() {
        initializeDeck();
    }

    private void initializeDeck() {
        deck = new ArrayList<>();

        for (CardColor color : new CardColor[]{CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW}) {
            for (CardType type : CardType.values()) {
                if (type.name().startsWith("NUMBER_")) {
                    deck.add(new Card(type, color, ""));
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            deck.add(new Card(CardType.WILD, CardColor.SPECIAL, "Wild"));
            deck.add(new Card(CardType.DRAW_FOUR, CardColor.SPECIAL, "Draw Four"));
        }

        shuffleDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public Card drawCard() {
        if (deck.isEmpty()) {
            initializeDeck();
        }
        return deck.remove(0);
    }
}
