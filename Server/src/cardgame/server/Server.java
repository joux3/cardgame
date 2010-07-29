package cardgame.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;

public class Server {
	private static final int PORT = 4592;

	public static final int SERVER_VERSION = 1;

	private static Lobby lobbyInstance = new Lobby();

	private static ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(
			1);

	public static Lobby getLobby() {
		return lobbyInstance;
	}

	public static void main(String[] args) throws IOException {
		// The following two lines change the default buffer type to 'heap',
		// which yields better performance.
		ByteBuffer.setUseDirectBuffers(false);
		ByteBuffer.setAllocator(new SimpleByteBufferAllocator());

		SocketAcceptorConfig cfg = new SocketAcceptorConfig();
		ObjectSerializationCodecFactory codecFactory = new ObjectSerializationCodecFactory();
		cfg.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(codecFactory));

		IoAcceptor acceptor = new SocketAcceptor();
		acceptor
				.bind(new InetSocketAddress(PORT), new ConnectionHandler(), cfg);
		System.out.println("Palvelin käynnistetty");
	}

	// runs the passed runnable after time ms
	public static ScheduledFuture runDelayed(Runnable r, int time) {
		return scheduledExecutor.schedule(r, time,
				java.util.concurrent.TimeUnit.MILLISECONDS);
	}
}
