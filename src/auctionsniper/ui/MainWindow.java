package auctionsniper.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainWindow extends JFrame {
	private final JLabel sniperStatus = new JLabel("Joining");

	public MainWindow() {
		super("Auction Sniper");
		setName("Auction Sniper Main");
		sniperStatus.setName("sniper status");
		add(sniperStatus);
		pack();
		setVisible(true);
	}

	public void showStatus(String status) {
		sniperStatus.setText(status);
	}
}
