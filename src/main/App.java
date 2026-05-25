package main;

import gui.DangNhapView;
import gui.MainFrameView;

public class App {
	// ================= MAIN =================
	// Khởi chạy ứng dụng
	
	public static void main(String[] args) {
		javafx.application.Platform.setImplicitExit(false);
		javafx.application.Application.launch(DangNhapView.class, args);
	}
}
