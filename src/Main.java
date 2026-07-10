
import gui.MainFrame;

import javax.swing.*;

/**
 * Ulazna tacka aplikacije. Zadaci ove klase su minimalni i iskljucivo
 * infrastrukturni: podesavanje izgleda, hvatanje neocekivanih izuzetaka,
 * i pokretanje glavnog prozora NA SWING NITI DOGADJAJA (invokeLater).
 */
public final class Main {

    private Main() {}

    static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//
//        }

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Unexpected error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        });

        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}