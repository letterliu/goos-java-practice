package test.endtoend.auctionsniper;

import static com.objogate.wl.swing.driver.ComponentDriver.named;
import static com.objogate.wl.swing.driver.ComponentDriver.showingOnScreen;
import static org.hamcrest.CoreMatchers.equalTo;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.jivesoftware.smack.packet.Message;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

public class ApplicationRunner {
	public void startBiddingIn(FakeAuctionServer auction) {
		 JFrame frame = new JFrame("Auction Sniper Main");
    frame.setName("Auction Sniper Main");
    frame.setSize(400, 300);

		JLabel status = new JLabel("Lost");
    status.setName("sniper status");
    frame.add(status);

    frame.setVisible(true);
	}

	public void showsSniperHasLostAuction() {
		JFrameDriver driver = new JFrameDriver(
				new GesturePerformer(),
				JFrameDriver.topLevelFrame(
						named("Auction Sniper Main"),
						showingOnScreen()),
				new AWTEventQueueProber(1000, 100));

		new JLabelDriver(
				driver,
				named("sniper status"))
				.hasText(equalTo("Lost"));
	}
}