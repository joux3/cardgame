package cardgame.packets;

import cardgame.server.game.Card;

public class PacketBuilder {
	private StringBuilder rawData;
	public PacketBuilder(String packetName) {
		rawData = new StringBuilder();
		addString(packetName);
	}
	
	public PacketBuilder(StringBuilder rawData) {
		this.rawData = rawData;
	}
	
	public void addString(String string) {
		addInt(string.length());
		rawData.append(string);
	}
	
	public void addInt(int number) {
		rawData.append("i");
		rawData.append(number);
		rawData.append("e");
	}
	
	public void addCard(Card card) {
		rawData.append(card.toString());
	}
	
	public String toString() {
		return rawData.toString();
	}
	
	public PacketBuilder clone() {
		return new PacketBuilder(new StringBuilder(rawData.toString()));
	}
}
