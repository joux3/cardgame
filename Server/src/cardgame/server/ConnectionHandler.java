package cardgame.server;

import java.util.ArrayList;


import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import cardgame.packets.PacketParser;

/*
 * This class handles the connections from clients and dispatches the messages to
 * lobby/game if the player has correctly joined
 */
public class ConnectionHandler extends IoHandlerAdapter {
	public synchronized void exceptionCaught(IoSession session, Throwable t) throws Exception {
		t.printStackTrace();
		session.close();
	}
	
	public synchronized void messageReceived(IoSession session, Object rawData) throws Exception {
		// client hasn't send the join packet yet
		PacketParser packet = new PacketParser((String)rawData);
		if (session.getAttachment() == null) { 
			if (packet.getPacketName().equals("join")) {
				if (packet.getInt() != Server.SERVER_VERSION) {
					System.out.println("Mismatch in protocol version");
					session.close();
				}
				session.setAttachment(new Player(packet.getString(), session));
				Server.getLobby().joinedLobby((Player)session.getAttachment());
				return;
			} else {
				System.out.println("Didn't get the expected packet, closing the session");
				session.close();
				return;
			}
		} else {
			Player player = (Player)session.getAttachment();
			if (!Server.getLobby().handlePacket(player, packet)) {
				if (packet.getPacketName().equals("leavegame")) {
					int gameId = packet.getInt();
					Game game = GameCache.getGame(gameId);
					if (game != null && player.getGames().contains(game)) {
						game.playerLeft(player);
					}
				} else if (packet.getPacketName().equals("game") || packet.getPacketName().equals("gamechat")) {
					int gameId = packet.getInt();
					Game game = GameCache.getGame(gameId);
					if (game != null && player.getGames().contains(game)) {
						game.incomingPacket(player, packet);
					}
				}
			}
		}
	}
	
	public synchronized void sessionClosed(IoSession session) {
		if (session.getAttachment() == null) // ok, player hasn't even authenticated
			return;
		Player player = (Player)session.getAttachment();
		System.out.println("Player "+player.getPlayerName()+" left");
		if (!player.isInGame()) // player is in lobby 
			return;

		// can't use the getGames collection directly, because playerLeft touches it
		ArrayList<Game> playerGames = new ArrayList<Game>(player.getGames());
		for (Game game : playerGames) {
			game.playerLeft((Player)session.getAttachment());
		}
	}
	
	public synchronized void sessionCreated(IoSession session) throws Exception {
		System.out.println("Session created...");
	}
}
