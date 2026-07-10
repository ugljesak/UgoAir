package gui;

import javax.swing.*;
import java.awt.*;

public class Dialog {

    private Dialog() {}

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirm(Component parent, String message) {
        int returnVal = JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return returnVal == JOptionPane.YES_OPTION;
    }
}
