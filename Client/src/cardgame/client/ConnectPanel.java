package cardgame.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ConnectPanel extends JPanel implements ActionListener {
	private JButton connectButton;

	private JTextField hostNameField;

	private JTextField playerNameField;

	public ConnectPanel() {
		super();
		super.setLayout(new BorderLayout());

		JPanel playerNamePanel = new JPanel();
		playerNamePanel.setLayout(new FlowLayout());
		playerNamePanel.add(new JLabel("Player name:"));
		playerNameField = new JTextField(20);
		playerNamePanel.add(playerNameField);
		super.add(playerNamePanel, BorderLayout.NORTH);

		JPanel hostNamePanel = new JPanel();
		hostNamePanel.setLayout(new FlowLayout());
		hostNamePanel.add(new JLabel("Hostname:"));
		hostNameField = new JTextField(20);
		hostNameField.setText("localhost");
		hostNamePanel.add(hostNameField);
		super.add(hostNamePanel, BorderLayout.CENTER);

		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		super.add(connectButton, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == connectButton) {
			if (playerNameField.getText().isEmpty())
				return;
			if (hostNameField.getText().isEmpty())
				return;

			playerNameField.setEnabled(false);
			connectButton.setEnabled(false);
			hostNameField.setEnabled(false);

			Client.instance.connect(playerNameField.getText(), hostNameField
					.getText());
		}
	}

	public void reset() {
		playerNameField.setEnabled(true);
		connectButton.setEnabled(true);
		hostNameField.setEnabled(true);
	}
}
