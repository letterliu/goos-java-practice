package test.endtoend.auctionsniper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FakeAuctionServer {
	private final String itemId;
	private XMPPConnection connection;
	private Chat currentChat;

	private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

	public FakeAuctionServer(String itemId) {
		this.itemId = itemId;
	}

	public void startSellingItem() throws XMPPException {
		connection = new XMPPConnection("localhost");
		connection.connect();
		connection.login("auction-" + itemId, "auction");
		connection.getChatManager().addChatListener(
				new ChatManagerListener() {
					public void chatCreated(Chat chat, boolean createdLocally) {
						currentChat = chat;
						chat.addMessageListener(new MessageListener() {
							public void processMessage(Chat chat, Message message) {
								messages.add(message);
							}
						});
					}
				});
	}

	public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
		assertThat(
				"Message",
				messages.poll(5, TimeUnit.SECONDS),
				is(notNullValue()));
	}

	public void announceClosed() throws XMPPException {
		Message message = new Message();
		message.setBody("Event: CLOSED");

		currentChat.sendMessage(message);
	}
}
