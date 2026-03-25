package main;

import javax.swing.SwingUtilities;

import gui.DangNhapJFrame;
import gui.MainFrame;

public class App {
	// ================= MAIN =================

    public static void main(String[] args) {        
        SwingUtilities.invokeLater(() -> new DangNhapJFrame().setVisible(true));
//    	SwingUtilities.invokeLater(() -> {
//            new MainFrame().setVisible(true);
//      });
    }
}
