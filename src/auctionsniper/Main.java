package auctionsniper;

import javax.swing.SwingUtilities;
import auctionsniper.ui.MainWindow;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

public class Main {
	private static MainWindow ui;

	@SuppressWarnings("unused")
	private static Chat notToBeGCd;

	public static void main(String... args) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				ui = new MainWindow();
			}
		});

		XMPPConnection connection = new XMPPConnection("localhost");
		connection.connect();
		connection.login("sniper", "sniper", "Auction");

		String itemId = "item-54321";

		Chat chat = connection.getChatManager().createChat(
				"auction-" + itemId + "@" + connection.getServiceName() + "/Auction",
				new MessageListener() {
					public void processMessage(Chat chat, Message message) {
						if ("Event: CLOSED".equals(message.getBody())) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									ui.showStatus("Lost");
								}
							});
						}
					}
				});

		notToBeGCd = chat;

		Message message = new Message();
		message.setBody("Command: JOIN");

		chat.sendMessage(message);
	}
}
