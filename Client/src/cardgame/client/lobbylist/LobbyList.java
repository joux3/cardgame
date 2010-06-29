package cardgame.client.lobbylist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import cardgame.client.Client;
import cardgame.packets.PacketBuilder;
import cardgame.packets.PacketParser;


@SuppressWarnings("serial")
public class LobbyList extends JPanel implements ActionListener {
	private JTable gameList; 
	private GameListModel gameListModel;
	private JButton createGameButton;
	private JTextField gameNameField;
	public LobbyList() {
		super();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		
		JPanel joinGamePanel = new JPanel();
		joinGamePanel.setLayout(new BorderLayout());
		
		gameListModel = new GameListModel();
		gameList = new JTable(gameListModel);
		gameList.getColumnModel().getColumn(1).setMaxWidth(60);
		gameList.getColumnModel().getColumn(1).setMinWidth(60);
		gameList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		gameList.setRowSelectionAllowed(true);
		gameList.setColumnSelectionAllowed(false);
		JScrollPane scrollPane = new JScrollPane(gameList);
		scrollPane.setPreferredSize(new Dimension(500, 400));
		
		joinGamePanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		
		JButton joinGameButton = new JButton("Join");
		joinGameButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (gameList.getSelectedRow() != -1) {
						int gameId = gameListModel.getGameId(gameList.getSelectedRow());
						PacketBuilder packet = new PacketBuilder("joingame");
						packet.addInt(gameId);
						Client.instance.sendPacket(packet);
					}
				}			
			}
		);
		
		JButton refreshListButton = new JButton("Refresh list");
		refreshListButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					PacketBuilder packet = new PacketBuilder("requestlobbylist");
					Client.instance.sendPacket(packet);
				}			
			}
		);
		
		buttonPanel.add(joinGameButton);
		buttonPanel.add(refreshListButton);
		
		joinGamePanel.add(buttonPanel, BorderLayout.SOUTH);
		
		tabbedPane.add("Join a game", joinGamePanel);
		
		// now the create game tab
		
		JPanel createGamePanel = new JPanel(new GridLayout(2, 2));

		createGamePanel.add(new JLabel("Game name:"));
		gameNameField = new JTextField();
		createGamePanel.add(gameNameField);
		createGameButton = new JButton("Create");
		createGameButton.addActionListener(this);
		createGamePanel.add(createGameButton);
		
		tabbedPane.add("Create a game", createGamePanel);
		
		super.add(tabbedPane);
	}
	
	public void gameListPacket(PacketParser packet) {
		int gameCount = packet.getInt();
		int[] gameIds = new int[gameCount];
		int[] playerCounts = new int[gameCount];
		int[] maxPlayerCounts = new int[gameCount];
		String[] gameNames = new String[gameCount];
		for (int i = 0; i < gameCount; i++) {
			gameIds[i] = packet.getInt();
			gameNames[i] = packet.getString();
			playerCounts[i] = packet.getInt();
			maxPlayerCounts[i] = packet.getInt();
		}
		gameListModel.updateData(gameIds, gameNames, playerCounts, maxPlayerCounts);
			/*int[] gameIDs = new int[]{49, 48, 29, 42, 4};
			String[] gameNames = new String[]{"kebabila", "kik", "känkky", "extratäyte", "lok"};
			int[] playerCounts = new int[]{1, 0, 2, 3, 4};
			int[] maxPlayerCounts = new int[]{3, 5, 5, 4, 5};
			
			gameListModel.updateData(gameIDs, gameNames, playerCounts, maxPlayerCounts);
			*/
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == createGameButton) {
			if (gameNameField.getText().length() > 0) {
				PacketBuilder packet = new PacketBuilder("creategame");
				packet.addString("katko"); //TODO needs the other games too
				packet.addString(gameNameField.getText());
				Client.instance.sendPacket(packet);
			}
		}
	}
}
