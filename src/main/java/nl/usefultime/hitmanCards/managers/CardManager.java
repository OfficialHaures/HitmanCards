package nl.usefultime.hitmanCards.managers;

import nl.usefultime.hitmanCards.cards.Card;
import nl.usefultime.hitmanCards.cards.CardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardManager {
    private final List<Card> cardTemplates;

    public CardManager() {
        this.cardTemplates = initializeCards();
    }

    private List<Card> initializeCards() {
        List<Card> cards = new ArrayList<>();

        cards.add(new Card(CardType.ELIMINATION, "Silent Takedown", "Eliminate a target without alerting others"));
        cards.add(new Card(CardType.ELIMINATION, "Accident Kill", "Make the elimination look like an accident"));

        cards.add(new Card(CardType.STEALTH, "Blend In", "Become undetectable for one turn"));
        cards.add(new Card(CardType.STEALTH, "Disguise", "Take on another player's appearance"));

        cards.add(new Card(CardType.DISTRACTION, "Coin Toss", "Distract guards in target area"));
        cards.add(new Card(CardType.DISTRACTION, "Fire Alarm", "Cause chaos in the current area"));

        cards.add(new Card(CardType.COUNTER, "Instinct", "Detect incoming elimination attempt"));
        cards.add(new Card(CardType.COUNTER, "Quick Escape", "Avoid elimination and move to safe location"));

        return cards;
    }

    public List<Card> createNewDeck() {
        List<Card> deck = new ArrayList<>(cardTemplates);
        Collections.shuffle(deck);
        return deck;
    }
}
