package auctionsniper;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

public class Main {
	private static JLabel sniperStatus;

	public static void main(String... args) throws Exception {
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

		XMPPConnection connection = new XMPPConnection("localhost");
		connection.connect();
		connection.login("sniper", "sniper", "Auction");

		String itemId = "item-54321";

		Chat chat = connection.getChatManager().createChat(
				"auction-" + itemId + "@" + connection.getServiceName() + "/Auction",
				null);

		Message message = new Message();
		message.setBody("Command: JOIN");

		chat.sendMessage(message);
	}
}
