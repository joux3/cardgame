package cardgame.server;

import java.util.Collection;
import java.util.Hashtable;

import org.apache.mina.common.IoSession;

import cardgame.packets.PacketBuilder;

public class Player {
	private String playerName;
	private Hashtable<Integer, Game> games; // game which this player is attending
	
	private final IoSession ioSession;
	
	public Player(String _playerName, IoSession ioSession) {
		this.playerName = _playerName;
		this.ioSession = ioSession;
		System.out.println("New player with the cool name "+_playerName);
		games = new Hashtable<Integer,Game>();
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void addGame(Game game) {
		this.games.put(game.getGameId(), game);
	}
	
	public Collection<Game> getGames() {
		return games.values();
	}
	
	public void removeGame(Game game) {
		games.remove(game.getGameId());
	}
	
	public void sendPacket(PacketBuilder packet) {
		this.ioSession.write(packet.toString());
	}
	
	public IoSession getSession() {
		return this.ioSession;
	}

	public boolean isInGame() {
		return !this.games.isEmpty();
	}
}
