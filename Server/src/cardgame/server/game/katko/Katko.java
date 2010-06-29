package cardgame.server.game.katko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;
import cardgame.server.Server;
import cardgame.server.Game;
import cardgame.server.Player;
import cardgame.server.game.Card;
import cardgame.server.game.Deck;

public class Katko extends Game {
	private boolean gameRunning = false;
	
	private int turnPos;

	// the first played card of the current round
	// or the last round if a new round hasn't been started yet
	private Card definingCard; 
	
	private HashMap<Player, SittingPlayer> playersIn;
	public Katko(String gameName, Player gameCreator, int gameId)
	{
		super(gameName, gameCreator, gameId);
	}
	
	private void startGame() {
		gameRunning = true;
		definingCard = null;
		
		Deck curDeck = new Deck();
		curDeck.shuffle();
		
		turnPos = 0;
		int curPos = 0;
		playersIn = new HashMap<Player, SittingPlayer>();
		for (Player player : players) {
			SittingPlayer info = new SittingPlayer();
			playersIn.put(player, info);
			info.player = player;
			
			info.tablePos = curPos++;
			System.out.println(player.getPlayerName() + " sits at "+info.tablePos);
			// deal five cards
			for (int i = 0; i < 5; i++) {
				info.handCards.add(curDeck.deal());
			}
		}
		System.out.println("Players in this game: "+playersIn.size());
		
		PacketBuilder packet = createGamePacket("gamestart");
		packet.addInt(playersIn.size());
		for (Map.Entry<Player, SittingPlayer> entry : playersIn.entrySet()) {
			packet.addInt(entry.getValue().tablePos);
			packet.addString(entry.getKey().getPlayerName());
		}
	
		for (Map.Entry<Player, SittingPlayer> entry : playersIn.entrySet()) {
			PacketBuilder privatePacket = packet.clone();
			privatePacket.addInt(entry.getValue().tablePos);
			for (Card handCard : entry.getValue().handCards) {
				privatePacket.addCard(handCard);
			}
			entry.getKey().sendPacket(privatePacket);
		}
		
		sendTableData();
	}
	
	private void sendTableData() {
		PacketBuilder tablePacket = createGamePacket("table");
		tablePacket.addInt(playersIn.size());
		for (SittingPlayer entry : playersIn.values()) {
			tablePacket.addInt(entry.tablePos);
			tablePacket.addInt(entry.handCards.size());
			tablePacket.addInt(entry.cardsPlayed.size());
			for (Card card : entry.cardsPlayed) 
				tablePacket.addCard(card);
		}
		tablePacket.addInt(gameRunning ? turnPos : -1);
		tablePacket.addInt(definingCard != null ? 1 : 0);
		if (definingCard != null) 
			tablePacket.addCard(definingCard);

		for (Player p : playersIn.keySet()) {
			p.sendPacket(tablePacket);
		}
	}
	
	@Override
	protected void playerJoined(Player player) {
		PacketBuilder pb = new PacketBuilder("showgame");
		pb.addString("katko");
		pb.addInt(this.getGameId());
		pb.addString(this.getGameName());
		player.sendPacket(pb);
		
		if (!gameRunning && this.getPlayerCount() == this.getMaxPlayerCount()) {
			startGame();
		}
	}
	
	@Override
	protected void playerLeftGame(Player disconnecter) {
		if (gameRunning && playersIn.containsKey(disconnecter)) {
			// TODO handle if the disconnecter has the turn
			playersIn.remove(disconnecter);
		}
	}

	private boolean isRoundOver() {
		int last = -1;
		for (SittingPlayer p : playersIn.values()) {
			if (p.cardsPlayed.size() == last || last == -1)
				last = p.cardsPlayed.size();
			else
				return false;
		}
		return true;
	}

	@Override
	protected void handlePacket(Player sender, PacketParser packet) {
		String name = packet.getString();
		System.out.println("Got game packet: "+name);
		if (name.equals("selectcard") && turnPos == playersIn.get(sender).tablePos) {
			Card card = packet.getCard();
			SittingPlayer player = playersIn.get(sender);
			System.out.println(sender.getPlayerName()+" tried to play the card "+card.toString());
			System.out.println("Players in "+playersIn.size());
			if (player.handCards.contains(card)) {
				if (definingCard == null) {
					definingCard = card;
				}
				System.out.println(sender.getPlayerName()+" played the card "+card.toString());
				player.cardsPlayed.add(card);
				player.handCards.remove(card);
	
				if (isRoundOver()) {
					// figure out who won the round	
					int roundNumber = player.cardsPlayed.size() - 1;
					SittingPlayer leadingPlayer = null;
					Card leadingCard = null;
					for (SittingPlayer p : playersIn.values()) {
						// wrong suit
						if (!definingCard.getSuit().equals(p.cardsPlayed.get(roundNumber).getSuit()))
							continue;
						// beats the leading card or there's no leader
						if (leadingPlayer == null || leadingCard.getRank().ordinal() < p.cardsPlayed.get(roundNumber).getRank().ordinal()) {
							leadingPlayer = p;		
							leadingCard = p.cardsPlayed.get(roundNumber);	
						}
					}
					turnPos = leadingPlayer.tablePos;
					definingCard = null;

					if (player.cardsPlayed.size() == 5) {
						// the game is over
						gameRunning = false;
						broadcastText(leadingPlayer.player.getPlayerName()+" won the round! New game starting in 10 seconds.");
						Server.runDelayed(new Runnable() {
							public void run() {
								startGame();
							}
						}, 10000);
					}
				} else {
					// TODO add proper next seat finding
					turnPos++;
					turnPos = turnPos % playersIn.size();
				}
				sendTableData(); 
			}
		}
	}
	
	private class SittingPlayer {
		public ArrayList<Card> handCards = new ArrayList<Card>(5);
		public ArrayList<Card> cardsPlayed = new ArrayList<Card>(5);
		int tablePos;
		Player player;
	}
}
