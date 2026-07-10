package gui;

import util.InactivityListener;
import util.InactivityMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class InactivityDialog implements InactivityListener {

    private final JFrame owner;
    private InactivityMonitor monitor;

    private JDialog dialog;
    private JLabel messageLabel;

    public InactivityDialog(JFrame owner) {
        this.owner = owner;
    }

    public void setMonitor(InactivityMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void inactivityWarningTick(final int secondsLeft) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showOrUpdateDialog(secondsLeft);
            }
        });
    }

    @Override
    public void inactivityWarningCancelled() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.setVisible(false);
                }
            }
        });
    }

    @Override
    public void inactivityTimeout() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
                System.exit(0);
            }
        });
    }

    private void showOrUpdateDialog(int secondsLeft) {
        if (dialog == null) {
            buildDialog();
        }
        messageLabel.setText(buildMessage(secondsLeft));
        if (!dialog.isVisible()) {
            dialog.pack();
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
        }
    }

    private void buildDialog() {
        dialog = new JDialog(owner, "Inactivity warning", true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                continueWorking();
            }
        });

        messageLabel = new JLabel(buildMessage(5), SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                continueWorking();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(continueButton);

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(messageLabel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(continueButton);
    }

    private String buildMessage(int secondsLeft) {
        return "<html><div style='text-align:center'>"
                + "Zbog neaktivnosti, program ce se automatski zatvoriti za<br>"
                + "<span style='font-size:20px'><b>" + secondsLeft + "</b></span>"
                + (secondsLeft == 1 ? " sekundu." : " sekundi.")
                + "<br><br>Kliknite na dugme ispod ako zelite da nastavite sa radom."
                + "</div></html>";
    }

    private void continueWorking() {
        if (monitor != null) {
            monitor.registerActivity();
        }
        if (dialog != null) {
            dialog.setVisible(false);
        }
    }
}