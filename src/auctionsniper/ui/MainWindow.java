package auctionsniper.ui;

import java.awt.Dimension;

import javax.swing.JFrame;

public class MainWindow extends JFrame {
	public static final String MAIN_WINDOW_NAME = "Auction Sniper Main";

	public MainWindow() {
		super("Auction Sniper");
		setName(MAIN_WINDOW_NAME);
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
}
