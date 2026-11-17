import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

public class FakeAuction {
	private static final String SOL_VERSION_PREFIX = "SOLVersion: 1.1; ";
	private static final String AUCTION_RESOURCE = "Auction";
	private static final String AUCTION_PASSWORD = "auction";
	private static final String XMPP_HOSTNAME = "localhost";
	private static final String PROMPT = ">>> ";

	// Smack 會在自己的 reader thread 上送來新訊息，跟 main thread 印 ">>> "
	// 提示字元、同時卡在 Scanner 讀輸入這件事會互相搶輸出。用這個 lock 把輸出
	// 序列化，非同步訊息進來時會先清掉/重畫提示字元列（模仿 goos-ts 的
	// printAsync() 用 Node readline.clearLine/cursorTo 的做法），避免兩邊的輸出
	// 混在同一行——細節見下面的 printStandaloneAsync()、printAttachedAsync()。
	private static final Object CONSOLE_LOCK = new Object();

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.err.println("usage: java FakeAuction <itemId>");
			System.exit(1);
		}
		String itemId = args[0];
		String username = "auction-" + itemId;
		String jid = username + "@" + XMPP_HOSTNAME + "/" + AUCTION_RESOURCE;

		XMPPConnection connection = new XMPPConnection(XMPP_HOSTNAME);
		connection.connect();
		connection.login(username, AUCTION_PASSWORD, AUCTION_RESOURCE);

		final Chat[] sniperChat = new Chat[1];
		connection.getChatManager().addChatListener(new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally) {
				sniperChat[0] = chat;
				chat.addMessageListener(new MessageListener() {
					public void processMessage(Chat chat, Message message) {
						Map<String, String> fields = parseCommand(message.getBody());
						String command = fields.get("Command");
						if ("JOIN".equals(command)) {
							printStandaloneAsync("> Sniper joined: " + chat.getParticipant());
						} else if ("BID".equals(command)) {
							printAttachedAsync(
									"< received: Bid " + fields.get("Price") + " from " + chat.getParticipant());
						}
					}
				});
			}
		});

		System.out.println(
				"Selling item " + itemId + " as " + jid + ". Waiting for a sniper to join...");
		System.out.println();
		System.out.println(
				"Type a SOL message body (without the \"" + SOL_VERSION_PREFIX + "\" prefix) to send it, e.g.:");
		System.out.println("  Event: PRICE; CurrentPrice: 90; Increment: 5; Bidder: other bidder;");
		System.out.println("  Event: CLOSE;");
		System.out.println("Type \"quit\" to disconnect and exit.");

		Scanner scanner = new Scanner(System.in);
		while (true) {
			printPrompt();
			if (!scanner.hasNextLine())
				break;
			String line = scanner.nextLine().trim();
			if (line.isEmpty())
				continue;
			if (line.equals("quit"))
				break;

			if (sniperChat[0] == null) {
				synchronized (CONSOLE_LOCK) {
					System.out.println("(no sniper has joined yet)");
				}
				continue;
			}

			String body = SOL_VERSION_PREFIX + line;
			sniperChat[0].sendMessage(body);
			synchronized (CONSOLE_LOCK) {
				System.out.println("> sent: " + body);
			}
		}
		connection.disconnect();
	}

	// 每輪迴圈開頭先印一個空行、再印懸掛的 ">>> " 提示字元——如果使用者打字前
	// 沒有非同步訊息插進來，這個空行就是用來跟上一輪輸出做視覺區隔的。
	private static void printPrompt() {
		synchronized (CONSOLE_LOCK) {
			System.out.println();
			System.out.print(PROMPT);
			System.out.flush();
		}
	}

	// 給「不是在回應剛剛送出指令」的非同步訊息用（例如 sniper 突然加入）：只清掉
	// 懸掛的提示字元、原地印訊息——printPrompt() 印的空行還留著當作跟上一輪的
	// 分隔線——訊息後留一個空行再重畫提示字元。
	private static void printStandaloneAsync(String message) {
		synchronized (CONSOLE_LOCK) {
			System.out.print("\r\033[2K");
			System.out.println(message);
			System.out.println();
			System.out.print(PROMPT);
			System.out.flush();
		}
	}

	// 給「直接回應剛剛送出指令」的非同步訊息用（例如送出 PRICE 後收到 BID）：
	// 連 printPrompt() 印的那個空行也一併清掉，讓回覆緊接在 "> sent: ..." 那行
	// 下面、不留空隙，訊息後留一個空行再重畫提示字元。
	private static void printAttachedAsync(String message) {
		synchronized (CONSOLE_LOCK) {
			System.out.print("\r\033[2K");
			System.out.print("\033[1A\033[2K");
			System.out.println(message);
			System.out.println();
			System.out.print(PROMPT);
			System.out.flush();
		}
	}

	private static Map<String, String> parseCommand(String messageBody) {
		Map<String, String> fields = new LinkedHashMap<String, String>();
		for (String field : messageBody.split(";")) {
			String trimmed = field.trim();
			if (trimmed.isEmpty())
				continue;
			int colonIndex = trimmed.indexOf(':');
			if (colonIndex == -1)
				continue;
			fields.put(trimmed.substring(0, colonIndex).trim(), trimmed.substring(colonIndex + 1).trim());
		}
		return fields;
	}
}