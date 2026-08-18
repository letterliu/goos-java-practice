package auctionsniper.ui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class MainWindow extends JFrame {
	public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";
	public static final String SNIPER_STATUS_NAME = "sniper status";
	public static final String STATUS_JOINING = "Joining";
	private final JLabel sniperStatus = createLabel(STATUS_JOINING);

	public MainWindow() {
		super("Auction Sniper");
		setName(MAIN_WINDOW_NAME);
		add(sniperStatus);
		pack();
		configureForDebugging();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	// 為了方便 debug 而設定的
	private void configureForDebugging() {
		Dimension packedSize = getSize();
		setMinimumSize(new Dimension(Math.max(300, packedSize.width), packedSize.height));
		setLocation(300, 300);
	}

	private static JLabel createLabel(String initialText) {
		JLabel result = new JLabel(initialText);
		result.setName(SNIPER_STATUS_NAME);
		result.setBorder(new LineBorder(Color.BLACK));
		return result;
	}
}
