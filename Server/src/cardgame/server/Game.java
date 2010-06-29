package cardgame.server;

import java.util.ArrayList;

import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;

public abstract class Game {
	protected String gameName;
	protected ArrayList<Player> players;
	protected int gameId;
	protected int maxPlayers;
	public Game(String gameName, Player gameCreator, int gameId) {
		this.gameName = gameName;
		this.gameId = gameId;
		this.maxPlayers = 2;
		players = new ArrayList<Player>();
		addPlayer(gameCreator);
	}
	
	public PacketBuilder createGamePacket(String name) {
		PacketBuilder packet = new PacketBuilder("game");
		packet.addInt(this.gameId);
		packet.addString(name);
		return packet;
	}
	
	protected abstract void playerJoined(Player player);
	
	public final void addPlayer(Player player) {
		players.add(player);
		player.addGame(this);
		playerJoined(player);
	}
	
	protected abstract void handlePacket(Player sender, PacketParser packet);

	public final void broadcastText(String text) {
		PacketBuilder packetBuilder = new PacketBuilder("gamemsg");
		packetBuilder.addInt(this.gameId);
		packetBuilder.addString(text);
		for (Player player : players) {
			player.sendPacket(packetBuilder);
		}
	}
	
	public final void incomingPacket(Player sender, PacketParser packet) {
		System.out.println("Got packet "+packet.getPacketName()+" from "+sender.getPlayerName());
		if (packet.getPacketName().equals("gamechat")) {
			String message = packet.getString();
			System.out.println("Player "+sender.getPlayerName()+" (game "+this.gameName+") said: "+message);
			broadcastText(sender.getPlayerName()+": "+message);
		} else {
			handlePacket(sender, packet);
		}
	}
	
	protected abstract void playerLeftGame(Player disconnecter);
	
	public void playerLeft(Player disconnecter) {
		playerLeftGame(disconnecter);
		if (!players.remove(disconnecter)) {
			throw new RuntimeException("Player who isn't in the player list left!");
		}
		disconnecter.removeGame(this);
		if (players.isEmpty()) {
			GameCache.removeGame(this);
		}
	}
	
	public int getPlayerCount() {
		return players.size();
	}
	
	public boolean canJoin() {
		return players.size() < maxPlayers;
	}
	
	public int getMaxPlayerCount() {
		return maxPlayers;
	}
	
	public int getGameId() {
		return this.gameId;
	}
	
	public String getGameName() {
		return this.gameName;
	}
}
