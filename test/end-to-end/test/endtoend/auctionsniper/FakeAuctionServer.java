package test.endtoend.auctionsniper;

import org.hamcrest.Matcher;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
public class FakeAuctionServer {
	private final SingleMessageListener messageListener = new SingleMessageListener();

	public static final String XMPP_HOSTNAME = "localhost";
	private static final String ITEM_ID_AS_LOGIN = "auction-%s";
	private static final String AUCTION_PASSWORD = "auction";
	public static final String AUCTION_RESOURCE = "Auction";

	private final String itemId;
	private final XMPPConnection connection;
	private Chat currentChat;

	public FakeAuctionServer(String itemId) {
		this.itemId = itemId;
		this.connection = new XMPPConnection(XMPP_HOSTNAME);
	}

	public void startSellingItem() throws Exception {
		connection.connect();
		connection.login(
				String.format(ITEM_ID_AS_LOGIN, itemId),
				AUCTION_PASSWORD,
				AUCTION_RESOURCE);
		connection.getChatManager().addChatListener(
				new ChatManagerListener() {
					public void chatCreated(Chat chat, boolean createdLocally) {
						currentChat = chat;
						chat.addMessageListener(messageListener);
					}
				});
	}

	public String getItemId() {
		return itemId;
	}

	public void reportPrice(int price, int increment, String bidder) throws XMPPException {
		currentChat.sendMessage(String.format("SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;",price,increment,bidder ));
	}

	public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
		messageListener.receivesAMessage(is(anything()));
	}

	public void hasReceivedBid(int bid, String sniperId) throws InterruptedException {
		assertThat(currentChat.getParticipant(), equalTo(sniperId));
		messageListener.receivesAMessage(equalTo(String.format("SOLVersion: 1.1; Command: BID; Price: %d;",bid)));
	}

	public void announceClosed() throws XMPPException {
		currentChat.sendMessage(new Message());
	}

	public void stop() {
		connection.disconnect();
	}

	public class SingleMessageListener implements MessageListener {
		private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1);

		@Override
		public void processMessage(Chat chat, Message message) {
			messages.add(message);
		}

		@SuppressWarnings("unchecked")
		public void receivesAMessage(Matcher<? super String> messageMatcher) throws InterruptedException {
			final Message message = messages.poll(5, TimeUnit.SECONDS);
			assertThat("Message", message, is(notNullValue()));
			assertThat(message.getBody(), messageMatcher);
		}
	}
}
