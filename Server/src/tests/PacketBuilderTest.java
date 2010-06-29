package tests;

import static org.junit.Assert.*;

import cardgame.packets.PacketBuilder;

public class PacketBuilderTest {
	@org.junit.Test public void cloneTest() {
		PacketBuilder packet = new PacketBuilder("lololol");
		packet.addString("kebab");
		PacketBuilder packet2 = packet.clone();
		assertTrue(packet.toString().equals(packet2.toString()));
		packet2.addInt(29);
		assertFalse(packet.toString().equals(packet2.toString()));
	}
}
