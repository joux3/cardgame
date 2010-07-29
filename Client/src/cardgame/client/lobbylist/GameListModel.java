package cardgame.client.lobbylist;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class GameListModel extends AbstractTableModel {
	private int[] gameIDs;

	private String[] gameNames;

	private int[] playerCounts;

	private int[] maxPlayerCounts;

	private final String[] columnNames = new String[] { "Name", "Players" };

	public void updateData(int[] gameIDs, String[] gameNames,
			int[] playerCounts, int[] maxPlayerCounts) {
		this.gameIDs = gameIDs;
		this.gameNames = gameNames;
		this.playerCounts = playerCounts;
		this.maxPlayerCounts = maxPlayerCounts;
		this.fireTableDataChanged();
	}

	public int getGameId(int rowIndex) {
		return gameIDs[rowIndex];
	}

	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		if (gameIDs == null)
			return 0;
		return gameIDs.length;
	}

	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return gameNames[row];
		} else if (col == 1) {
			return new String(playerCounts[row] + "/" + maxPlayerCounts[row]);
		} else {
			throw new RuntimeException("getValue wants col that doesn't exist");
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
}
