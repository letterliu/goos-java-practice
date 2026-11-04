package test.endtoend.auctionsniper;

import static com.objogate.wl.swing.driver.ComponentDriver.named;
import static com.objogate.wl.swing.driver.ComponentDriver.showingOnScreen;
import static org.hamcrest.CoreMatchers.equalTo;

import com.objogate.wl.swing.AWTEventQueueProber;
import com.objogate.wl.swing.driver.JFrameDriver;
import com.objogate.wl.swing.driver.JLabelDriver;
import com.objogate.wl.swing.gesture.GesturePerformer;

import auctionsniper.Main;

public class ApplicationRunner {

	private JFrameDriver driver;

	public void startBiddingIn(FakeAuctionServer auction) {
		Thread thread = new Thread("Test application") {
			@Override
			public void run() {
				try {
					Main.main();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();

		driver = new JFrameDriver(
				new GesturePerformer(),
				JFrameDriver.topLevelFrame(named("Auction Sniper Main"), showingOnScreen()),
				new AWTEventQueueProber(1000, 100));

		showsSniperStatus("Joining");
	}

	private void showsSniperStatus(String statusText) {
		new JLabelDriver(driver, named("sniper status")).hasText(equalTo(statusText));
	}

	public void showsSniperHasLostAuction() {
		showsSniperStatus("Lost");
	}
}