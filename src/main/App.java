package main;

import javax.swing.SwingUtilities;

import gui.DangNhapView;
import gui.MainFrameView;

public class App {
	// ================= MAIN =================

	public static void main(String[] args) {
		javafx.application.Platform.setImplicitExit(false);
		javafx.application.Application.launch(DangNhapView.class, args);
	}
}