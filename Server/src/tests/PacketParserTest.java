package tests;

import static org.junit.Assert.*;

import cardgame.packets.PacketParser;
import cardgame.server.game.Card;

public class PacketParserTest {
	@org.junit.Test public void cardRead() {
		PacketParser packet = new PacketParser("i2eLL5dah");
		assertTrue(packet.getPacketName().equals("LL"));
		Card card = packet.getCard();
		assertTrue(card.toString().equals("5d"));
		card = packet.getCard();
		assertTrue(card.toString().equals("ah"));
	}
	
	@org.junit.Test public void randomReads() {
		PacketParser packet = new PacketParser("i5eloooli29e2ci2eKK");
		assertTrue(packet.getPacketName().equals("loool"));
		assertTrue(packet.getInt() == 29);
		assertTrue(packet.getCard().toString().equals("2c"));
		assertTrue(packet.getString().equals("KK"));
	}
}
