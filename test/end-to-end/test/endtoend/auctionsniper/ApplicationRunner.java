package test.endtoend.auctionsniper;

import static com.objogate.wl.swing.driver.ComponentDriver.named;
import static com.objogate.wl.swing.driver.ComponentDriver.showingOnScreen;
import static org.hamcrest.CoreMatchers.equalTo;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

public class ApplicationRunner {
	public void startBiddingIn(FakeAuctionServer auction) {
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