package cardgame.client.game;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import cardgame.server.game.Card;

public class JCard extends JComponent {
	private Card card;
	
	// this creates a back card
	public JCard() {
		super();
		init(card);
	}

	public JCard(Card card) {
		super();
		init(card);
	}

	public void init(Card card) {
		this.card = card;
		setPreferredSize(new Dimension(73, 97));	
	}

	public Card getCard() {
		return card;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Image image = (card == null) ? Card.getBackImage() : card.getImage();
		g.drawImage(image, 0, 0, null);
	}
}
