package test.endtoend.auctionsniper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.jivesoftware.smack.packet.Message;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FakeAuctionServer {
	private final String itemId;
	private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

	public FakeAuctionServer(String itemId) {
		this.itemId = itemId;
	}

	public void startSellingItem() {
	}

	public void hasReceivedJoinRequestFromSniper() throws InterruptedException {
		assertThat(
				"Message",
				messages.poll(5, TimeUnit.SECONDS),
				is(notNullValue()));
	}

	public void announceClosed() {
	}
}
