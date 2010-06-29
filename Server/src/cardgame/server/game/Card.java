package cardgame.server.game;

import java.awt.Color;
import java.awt.Image;

public class Card {
    private final Rank rank;
    private final Suit suit;

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        private static final String[] suitStrings = {"c", "d", "h", "s"};

        public static Suit fromString(String string) {
        	for (int i = 0; i < suitStrings.length; i++) {
        		if (string.equals(suitStrings[i])) {
        			return Suit.values()[i];
        		}
        	}
        	throw new RuntimeException("Unkown suit string ("+string+")!");
        }
        
        public String toString() {
            return suitStrings[this.ordinal()];
        }
    }

    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

        private static final String[] rankStrings = {"2", "3", "4", "5", "6", "7", "8", "9", "t", "j", "q", "k", "a"};

        public static Rank fromString(String string) {
        	for (int i = 0; i < rankStrings.length; i++) {
        		if (string.equals(rankStrings[i])) {
        			return Rank.values()[i];
        		}
        	}
        	throw new RuntimeException("Unkown rank string ("+string+")!");
        }
        
        public String toString() {
            return rankStrings[this.ordinal()];
        }
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public String toString() {
        return rank.toString() + suit.toString();
    }

	public boolean equals(Object o) {
		if (!(o instanceof Card)) {
			return false;
		}
		Card other = (Card)o;
		return (this.rank == other.getRank() && this.suit == other.getSuit());
	}		
    
    public static Card fromString(String cardString) {
    	return new Card(Rank.fromString(String.valueOf(cardString.charAt(0))), Suit.fromString(String.valueOf(cardString.charAt(1))));
    }
    
    private static java.util.HashMap<Card, Image> imageCache = new java.util.HashMap<Card, Image>();
    public Image getImage() {
    	if (imageCache.containsKey(this))
    		return imageCache.get(this);
    	
    	Image image = null;
    	try {
    		image = javax.imageio.ImageIO.read(new java.io.File("images/cards/" + toString() + ".gif"));
    	} catch(Exception e) {
			try {
				image = javax.imageio.ImageIO.read(getClass().getResource("/images/cards/" + toString() + ".gif"));
			} catch(Exception err) { }
		}
    	imageCache.put(this, image);
    	return image;
    }

	public Color getHitColor() {
		return new Color(0, (int)rank.toString().charAt(0), (int)suit.toString().charAt(0));
	}

	public static Card fromColor(Color color) {
		if (color.getRed() > 0)
			return null;
		return new Card(Rank.fromString(String.valueOf((char)color.getGreen())), Suit.fromString(String.valueOf((char)color.getBlue())));
	}
    
	private static Image backImage;
    public static Image getBackImage() {
		if (backImage == null) {
			try {
    			backImage = javax.imageio.ImageIO.read(new java.io.File("images/cards/b.gif"));
	    	} catch(Exception e) {
				try {
					backImage = javax.imageio.ImageIO.read(Card.class.getResource("/images/cards/b.gif"));
				} catch(Exception err) { }
			}
		}
		return backImage;
    }
}
