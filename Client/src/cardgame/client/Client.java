package cardgame.client;

import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetSocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

import cardgame.client.game.Game;
import cardgame.client.game.katko.Katko;
import cardgame.client.lobbylist.LobbyList;
import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;
import cardgame.packets.PrefixedStringCodecFactory;

public class Client implements WindowListener {
	private enum ClientState {
		CONNECT, LOBBY
	};

	public static Client instance;

	private static final int PORT = 4592;

	public static final int CLIENT_VERSION = 1;

	private ClientState clientState;

	private SocketConnector connector;

	private SocketConnectorConfig cfg;

	private ConnectionHandler connHandler;

	private JFrame window;

	private LobbyList lobbyList;

	private ConnectPanel connectPanel;

	public String playerName;

	private Hashtable<Integer, Game> games;

	public static int debugLevel = 0;

	public Client() {
		games = new Hashtable<Integer, Game>();
		clientState = ClientState.CONNECT;
		connector = new SocketConnector();
		cfg = new SocketConnectorConfig();
		PrefixedStringCodecFactory codecFactory = new PrefixedStringCodecFactory();
		cfg.getFilterChain().addLast("codec",
				new ProtocolCodecFilter(codecFactory));
		connHandler = new ConnectionHandler();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				constructGUI();
			}
		});
	}

	public void leftGame(Game game) {
		if (games.containsValue(game)) {
			games.remove(game.getGameId());
		} else {
			throw new RuntimeException(
					"Tried to remove a game which isn't in the list!");
		}
	}

	// this gets called when a packet arrives
	public void packetArrived(final PacketParser packet) {
		// run all the packet handling code on the swing side for easy threading
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (packet.getPacketName().equals("lobbylist")) {
					lobbyList.gameListPacket(packet);
				} else if (packet.getPacketName().equals("showgame")) {
					String gameType = packet.getString();
					int gameId = packet.getInt();
					String gameName = packet.getString();
					if (gameType.equals("katko")) {
						Katko gameKatko = new Katko(gameId, gameName);
						games.put(gameId, gameKatko);
					}
				} else if (packet.getPacketName().equals("game")
						|| packet.getPacketName().equals("gamemsg")) {
					int gameId = packet.getInt();
					Game game = games.get(gameId);
					if (game != null) {
						game.incomingPacket(packet);
					}
				}
			}
		});
	}

	// connection handler will call this if a connection is successfully made
	public void connected() {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connectPanel.setVisible(false);
				lobbyList.setVisible(true);
				clientState = ClientState.LOBBY;
				window.pack();
				if (debugLevel == 1) {
					PacketBuilder packet = new PacketBuilder("creategame");
					packet.addString("katko");
					packet.addString("Testipeli");
					instance.sendPacket(packet);
				} else if (debugLevel == 2) {
					PacketBuilder packet = new PacketBuilder("joingame");
					packet.addInt(0); // might be 1?
					instance.sendPacket(packet);
				}
			}
		});
	}

	// forward the packet to the connectionhandler and let it send it
	public void sendPacket(PacketBuilder packet) {
		connHandler.sendPacket(packet);
	}

	// connectorpanel calls this when the user presses connect button
	public void connect(String playerName, String hostName) {
		this.playerName = playerName;
		try {
			ConnectFuture cf = connector.connect(new InetSocketAddress(
					hostName, PORT), connHandler, cfg);
			cf.addListener(connHandler);
		} catch (UnresolvedAddressException exception) {
			JOptionPane.showMessageDialog(window,
					"Failed to resolve the game server address",
					"Connection error", JOptionPane.ERROR_MESSAGE);
			connectPanel.reset();
		}
	}

	// this gets called after connection try if the connection try fails
	public void connectionTryFailed() {
		JOptionPane.showMessageDialog(window,
				"Failed to connect the game server", "Connection error",
				JOptionPane.ERROR_MESSAGE);
		connectPanel.reset();
	}

	private void constructGUI() {
		window = new JFrame("Maija");
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.setResizable(false);
		// so the panels don't remove the panel added before
		window.getContentPane().setLayout(new FlowLayout());

		connectPanel = new ConnectPanel();
		window.add(connectPanel);

		lobbyList = new LobbyList();
		lobbyList.setVisible(false);
		window.add(lobbyList);

		window.pack();
		window.addWindowListener(this);
		window.setVisible(true);
		if (debugLevel > 0)
			connect(debugLevel == 1 ? "Pelaaja A" : "Pelaaja B", "localhost");
	}

	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("-debug1"))
			debugLevel = 1;
		if (args.length > 0 && args[0].equals("-debug2"))
			debugLevel = 2;
		instance = new Client();
	}

	public void windowClosing(WindowEvent arg0) {
		if (!games.isEmpty()) {
			int answer = JOptionPane
					.showConfirmDialog(
							window,
							"Do you really want to close the client and leave all the games?",
							"Confirmation", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.NO_OPTION) {
				return;
			}
		}
		if (clientState != ClientState.CONNECT) {
			connHandler.closeConnection();
		}
		System.exit(0);
	}

	// the following exist to implement windowlistener
	public void windowActivated(WindowEvent arg0) {
	}

	public void windowClosed(WindowEvent arg0) {
	}

	public void windowDeactivated(WindowEvent arg0) {
	}

	public void windowDeiconified(WindowEvent arg0) {
	}

	public void windowIconified(WindowEvent arg0) {
	}

	public void windowOpened(WindowEvent arg0) {
	}
}
