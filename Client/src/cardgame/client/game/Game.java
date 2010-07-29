package cardgame.client.game;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cardgame.client.Client;
import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;

public abstract class Game implements WindowListener {
	private int gameId;

	private String gameName;

	public JFrame gameWindow;

	private JTextArea chatArea;

	private JTextField chatBox;

	public Game(final int gameId, String gameName) {
		this.gameId = gameId;
		this.gameName = gameName;
	}

	public void constructGUI(JComponent gamePanel) {
		gameWindow = new JFrame(gameName);
		gameWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		gameWindow.addWindowListener(this);

		gameWindow.setResizable(false);
		gameWindow.setLayout(new BorderLayout());

		chatArea = new JTextArea(10, 1);
		chatArea.append("* Joined Katko game " + gameName);
		chatBox = new JTextField();
		chatBox.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10 && !chatBox.getText().isEmpty()) {
					PacketBuilder packet = new PacketBuilder("gamechat");
					packet.addInt(gameId);
					packet.addString(chatBox.getText());
					sendPacket(packet);

					chatBox.setText("");
				}
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}
		});

		gameWindow.add(gamePanel, BorderLayout.NORTH);
		gameWindow.add(new JScrollPane(chatArea), BorderLayout.CENTER);
		gameWindow.add(chatBox, BorderLayout.SOUTH);

		gameWindow.pack();

		gameWindow.setVisible(true);
	}

	public void addText(String line) {
		chatArea.append("\n" + line);
		chatArea.setCaretPosition(chatArea.getDocument().getLength());
	}

	public PacketBuilder createGamePacket(String name) {
		PacketBuilder packet = new PacketBuilder("game");
		packet.addInt(this.gameId);
		packet.addString(name);
		return packet;
	}

	protected abstract void handlePacket(PacketParser packet);

	public final void incomingPacket(PacketParser packet) {
		System.out.println("Got packet " + packet.getPacketName());
		if (packet.getPacketName().equals("gamemsg")) {
			String message = packet.getString();
			addText(message);
		} else {
			handlePacket(packet);
			gameWindow.repaint();
		}
	}

	public int getGameId() {
		return this.gameId;
	}

	public void sendPacket(PacketBuilder packet) {
		Client.instance.sendPacket(packet);
	}

	public void windowClosing(WindowEvent arg0) {
		int answer = JOptionPane.showConfirmDialog(gameWindow,
				"Do you really want to leave the game?", "Confirmation",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			PacketBuilder packet = new PacketBuilder("leavegame");
			packet.addInt(this.gameId);
			Client.instance.sendPacket(packet);
			Client.instance.leftGame(this);
			gameWindow.dispose();
		}
	}

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
