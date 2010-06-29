package cardgame.packets;

import cardgame.server.game.Card;

public class PacketParser {
	private String rawData;
	private int offset;
	private String packetName;
	public PacketParser(String rawData) {
		this.rawData = rawData;
		offset = 0;
		packetName = getString();
	}
	
	public String getPacketName() {
		return this.packetName;
	}
	
	public int getInt() {
		offset += 1;
		int i = offset;
		while(rawData.charAt(++i) != 'e');
		int result = Integer.parseInt(rawData.substring(offset, i));
		offset = i+1;
		return result;
	}
	
	public String getString() {
		int length = getInt();
		String result = rawData.substring(offset, offset+length);
		offset = offset+length;
		return result;
	}
	
	public Card getCard() {
		return Card.fromString(new String(new char[]{rawData.charAt(offset++), rawData.charAt(offset++)}));
	}
}
