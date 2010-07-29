package cardgame.client.game.katko;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JLayeredPane;

import cardgame.client.game.Game;
import cardgame.client.game.JCard;
import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;
import cardgame.server.game.Card;

import org.pushingpixels.trident.Timeline;

public class Katko extends Game {
	JKatkoPanel gamePanel;

	private int playerCount;

	private int myPos;

	private int turnPos;

	private ArrayList<Card> myCards;

	private SittingPlayer[] playersIn;

	private Card definingCard;

	public Katko(int gameId, String gameName) {
		super(gameId, gameName);

		gamePanel = new JKatkoPanel(this);
		gamePanel.setPreferredSize(new Dimension(700, 400));

		// allow absolute positioning
		gamePanel.setLayout(null);

		constructGUI(gamePanel);
	}

	public ArrayList<Card> getPlayableHandCards() {
		ArrayList<Card> playableCards = new ArrayList<Card>();
		if (turnPos != myPos)
			return playableCards;

		if (definingCard != null) {
			for (Card card : myCards) {
				if (card.getSuit().equals(definingCard.getSuit()))
					playableCards.add(card);
			}
		}
		if (playableCards.size() == 0)
			return myCards;
		return playableCards;
	}

	public void createHandComponents() {
		int x = playerCardPoints[0].x;
		int y = playerCardPoints[0].y;
		int i = 0;
		for (Card card : myCards) {
			JCard jcard = new JCard(card);
			Dimension size = jcard.getPreferredSize();
			jcard.setBounds(x, y, size.width, size.height);
			gamePanel.add(jcard, new Integer(i++));
			jcard.addMouseListener(gamePanel);
			x += 25;
		}
	}

	public void cardClicked(JCard jcard) {
		if (turnPos != myPos)
			return;

		Card card = jcard.getCard();
		if (getPlayableHandCards().contains(card)) {
			myCards.remove(card);
			gamePanel.remove(jcard);
			PacketBuilder p = createGamePacket("selectcard");
			p.addCard(card);
			sendPacket(p);
		}
	}

	private JCard getComponentForCard(Card card) {
		for (Component component : gamePanel.getComponents()) {
			if (component instanceof JCard
					&& ((JCard) component).getCard().equals(card))
				return (JCard) component;
		}
		return null;
	}

	@Override
	protected void handlePacket(PacketParser packet) {
		String gamePacketName = packet.getString();
		if (gamePacketName.equals("gamestart")) {
			myCards = new ArrayList<Card>(5);
			playerCount = packet.getInt();
			playersIn = new SittingPlayer[playerCount];
			for (int i = 0; i < playerCount; i++) {
				int pos = packet.getInt();
				playersIn[pos] = new SittingPlayer();
				playersIn[pos].tablePos = pos;
				playersIn[pos].handCardCount = 5;
				playersIn[pos].name = packet.getString();
				playersIn[pos].wins = packet.getInt();
				// addText(playersIn[pos].name+" sits at "+pos);
			}
			myPos = packet.getInt();
			// addText("You sit at "+myPos);
			String handCardInfo = "You got hand cards: ";
			for (int i = 0; i < 5; i++) {
				Card card = packet.getCard();
				myCards.add(card);
				handCardInfo += card.toString() + (i == 4 ? "." : ", ");
			}
			addText(handCardInfo);
			Collections.sort(myCards, new Comparator<Card>() {
				public int compare(Card c1, Card c2) {
					int suitCompare = c1.getSuit().compareTo(c2.getSuit());
					if (suitCompare != 0)
						return suitCompare;
					return c1.getRank().compareTo(c2.getRank());
				}
			});
			createHandComponents();
		} else if (gamePacketName.equals("table")) {
			int numPlayers = packet.getInt();
			for (int i = 0; i < numPlayers; i++) {
				int pos = packet.getInt();
				SittingPlayer p = playersIn[pos];
				p.tablePos = pos;
				p.handCardCount = packet.getInt();
				int playedCount = packet.getInt();
				p.playedCards = new ArrayList<Card>(5);
				for (int j = 0; j < playedCount; j++) {
					p.playedCards.add(packet.getCard());
				}
			}
			turnPos = packet.getInt();
			definingCard = null;
			if (packet.getInt() == 1)
				definingCard = packet.getCard();

			ArrayList<Card> playableCards = getPlayableHandCards();
			for (Card card : myCards) {
				JCard jcard = getComponentForCard(card);
				Point p = jcard.getLocation();
				Timeline timeline = new Timeline(jcard);
				timeline.setDuration(250);
				if (playableCards.contains(card)) {
					timeline.addPropertyToInterpolate("location", p, new Point(
							p.x, playerCardPoints[0].y - 5));
				} else {
					timeline.addPropertyToInterpolate("location", p, new Point(
							p.x, playerCardPoints[0].y));
				}
				timeline.play();
			}
			gamePanel.repaint();
		} else {
			addText("Got unhandled packet: " + gamePacketName);
		}
	}

	private void drawCards(Graphics2D g, java.util.Collection<Card> cards,
			int x, int y) {
		for (Card card : cards) {
			g.drawImage(card.getImage(), x, y, null);
			x += 25;
		}
	}

	private static Point[] playerCardPoints = { new Point(303, 367),
			new Point(303, -45) };

	private static Point[] playerNamePoints = { new Point(220, 380),
			new Point(220, 40) };

	public void drawGame(Graphics2D g) {
		g.setColor(Color.BLACK);
		if (playersIn != null) {
			for (SittingPlayer player : playersIn) {
				if (player == null)
					continue;

				// perform some magic table pos swapping
				// ie. always show local player on bottom
				if (player.tablePos >= playerCardPoints.length) {
					throw new RuntimeException("Tablepos " + player.tablePos
							+ " out of bounds!");
				}

				int mappedPos = player.tablePos - myPos;
				mappedPos = (mappedPos < 0) ? mappedPos
						+ playerCardPoints.length : mappedPos;

				Point p = playerCardPoints[mappedPos];
				Point namePoint = playerNamePoints[mappedPos];
				if (player.tablePos == myPos) {
					// g.drawString("My cards: "+myCards.toString(), p.x, p.y);
					drawCards(g, player.playedCards, p.x, p.y - 110);
					g.drawString("You", namePoint.x, namePoint.y);
					g.drawString("(" + player.wins + " victories)",
							namePoint.x, namePoint.y + 12);
					// g.drawString("Cards I've played:
					// "+player.playedCards.toString(), p.x, p.y+20);
				} else {
					for (int i = 0; i < player.handCardCount; i++) {
						g.drawImage(Card.getBackImage(), p.x + i * 25, p.y,
								null);
					}
					drawCards(g, player.playedCards, p.x, p.y + 110);
					g.drawString(player.name, namePoint.x, namePoint.y);
					g.drawString("(" + player.wins + " victories)",
							namePoint.x, namePoint.y + 12);
					// g.drawString("Cards played:
					// "+player.playedCards.toString(), p.x, p.y+20);
				}
			}
		}
	}

	public void mouseClicked(int x, int y) {
	}

	private class SittingPlayer {
		int tablePos;

		int handCardCount;

		int wins;

		String name;

		ArrayList<Card> playedCards = new ArrayList<Card>(5);
	}

	@SuppressWarnings("serial")
	private class JKatkoPanel extends JLayeredPane implements MouseListener {
		Katko katkoInstance;

		public JKatkoPanel(Katko katkoInstance) {
			super();
			addMouseListener(this);
			this.katkoInstance = katkoInstance;
		}

		protected void paintComponent(Graphics gOld) {
			super.paintComponent(gOld);
			Graphics2D g = (Graphics2D) gOld;
			g.setColor(new Color(0, 128, 0));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			katkoInstance.drawGame(g);
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getSource() instanceof JCard) {
				katkoInstance.cardClicked((JCard) e.getSource());
			} else {
				katkoInstance.mouseClicked(e.getX(), e.getY());
			}
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
	}
}
