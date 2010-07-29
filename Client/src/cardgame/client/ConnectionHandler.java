package cardgame.client;

import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoFuture;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;

public class ConnectionHandler extends IoHandlerAdapter implements
		IoFutureListener {
	private IoSession connection;

	public void closeConnection() {
		CloseFuture fc = connection.close();
		fc.join();
	}

	// send the packet to the server
	public void sendPacket(PacketBuilder packet) {
		if (this.connection != null) {
			connection.write(packet.toString());
		}
	}

	// session got opened, lets send the init packet
	public void sessionOpened(IoSession session) {
		System.out.println("Opened session");
		this.connection = session;
		PacketBuilder packetBuilder = new PacketBuilder("join");
		packetBuilder.addInt(Client.CLIENT_VERSION);
		packetBuilder.addString(Client.instance.playerName);
		this.sendPacket(packetBuilder);
		Client.instance.connected();
	}

	public void messageReceived(IoSession session, Object message) {
		PacketParser packet = new PacketParser((String) message);
		Client.instance.packetArrived(packet);
	}

	public void exceptionCaught(IoSession session, Throwable cause) {
		cause.printStackTrace();
		session.close();
	}

	public void messageSent(IoSession session, Object message) {
	}

	public void sessionClosed(IoSession session) {
		System.out.println("I got closed");
	}

	// this is only for reseting the gui after unsuccessfull connection try
	public void operationComplete(IoFuture ioFuture) {
		if (!((ConnectFuture) ioFuture).isConnected())
			Client.instance.connectionTryFailed();
	}
}
