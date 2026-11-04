package auctionsniper;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class Main {
	private static JLabel sniperStatus;

	public static void main(String...args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				sniperStatus = new JLabel("Joining");
				sniperStatus.setName("sniper status");

				JFrame frame = new JFrame("Auction Sniper");
				frame.setName("Auction Sniper Main");
				frame.add(sniperStatus);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
}
