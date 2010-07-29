package cardgame.server;

import cardgame.packets.PacketParser;

public class Lobby {
	public boolean handlePacket(Player sender, PacketParser packet) {
		if (packet.getPacketName().equals("creategame")) {
			String gameType = packet.getString();
			String gameName = packet.getString();
			GameCache.createGame(gameName, sender, gameType);
		} else if (packet.getPacketName().equals("joingame")) {
			Game gameToJoin = GameCache.getGame(packet.getInt());
			if (gameToJoin != null && !sender.getGames().contains(gameToJoin)
					&& gameToJoin.canJoin()) {
				gameToJoin.addPlayer(sender);
			}
		} else if (packet.getPacketName().equals("requestlobbylist")) {
			sendGameList(sender);
		} else {
			return false;
		}
		return true;
	}

	public void sendGameList(Player sendTo) {
		System.out.println("Sent gamelist to " + sendTo.getPlayerName());
		sendTo.sendPacket(GameCache.getLobbyList());
	}

	public void joinedLobby(Player joiner) {
		sendGameList(joiner);
	}
}
