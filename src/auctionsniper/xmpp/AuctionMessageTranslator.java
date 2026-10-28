package auctionsniper.xmpp;

import auctionsniper.AuctionEventListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.HashMap;
import java.util.Map;

public class AuctionMessageTranslator implements MessageListener {
	private final AuctionEventListener listener;

	public AuctionMessageTranslator(AuctionEventListener listener) {
		this.listener = listener;
	}

	public void processMessage(Chat chat, Message message) {
		AuctionEvent event = AuctionEvent.from(message.getBody());

		String type = event.type();

		if ("CLOSE".equals(type)) {
			listener.auctionClosed();
		} else if ("PRICE".equals(type)) {
			listener.currentPrice(event.currentPrice(), event.increment());
		}
	}

	private static class AuctionEvent {
		private final Map<String, String> fields = new HashMap<String, String>();

		private String type() {
			return fields.get("Event");
		}

		private int currentPrice() {
			return getInt("CurrentPrice");
		}

		private int increment() {
			return getInt("Increment");
		}

		private int getInt(String field) {
			return Integer.parseInt(get(field));
		}

		private String get(String field) {
			return fields.get(field);
		}

		private void addField(String field) {
			String[] pair = field.split(":");
			fields.put(pair[0].trim(), pair[1].trim());
		}

		static AuctionEvent from(String messageBody) {
			AuctionEvent event = new AuctionEvent();

			for (String field : fieldsIn(messageBody)) {
				event.addField(field);
			}

			return event;
		}

		static String[] fieldsIn(String messageBody) {
			return messageBody.split(";");
		}
	}
}
