package cardgame.server.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    public static final int DECK_SIZE = 52;

    private List<Card> cards;

    public Deck() {
        cards = new ArrayList<Card>(DECK_SIZE);
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card deal() {
        return cards.remove(0);
    }

    public void burn() {
        cards.remove(0);
    }
    
    public int cardsLeft() {
    	return cards.size();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            sb.append(card.toString());
        }
        return sb.toString();
    }

}

