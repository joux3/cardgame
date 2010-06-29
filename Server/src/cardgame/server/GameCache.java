package cardgame.server;

import java.util.HashMap;
import java.util.Map;

import cardgame.packets.PacketBuilder;
import cardgame.server.game.katko.Katko;


public class GameCache {
	private static int gameIdCounter = 0;

	private static Map<Integer, Game> gameCache = new HashMap<Integer, Game>();

	public static Game createGame(String name, Player creator, String gameType) {
		// lets find an empty game id
		while (true) {
			if (!gameCache.containsKey(gameIdCounter)) {
				// TODO handle other games too
				Game newGame = new Katko(name, creator, gameIdCounter);
				System.out.println("Player "+creator.getPlayerName()+" created game with id "+gameIdCounter);
				gameCache.put(gameIdCounter, newGame);
				return newGame;
			}
			// TODO handle the case when the whole 2^32 game id space is full ;)
			gameIdCounter++;
		}
	}
	
	public static void removeGame(Game game) {
		if (gameCache.containsValue(game))
			gameCache.remove(game.getGameId());
		else
			throw new RuntimeException("Tried to remove an unexisting game from the cache!");
	}

	public static Game getGame(int id) {
		return gameCache.get(id);
	}

	public static PacketBuilder getLobbyList() {
		PacketBuilder packet = new PacketBuilder("lobbylist");
		packet.addInt(gameCache.size());
		for (Game game : gameCache.values()) {
			packet.addInt(game.getGameId());
			packet.addString(game.getGameName());
			packet.addInt(game.getPlayerCount());
			packet.addInt(game.getMaxPlayerCount());
		}
		return packet;
	}
}
