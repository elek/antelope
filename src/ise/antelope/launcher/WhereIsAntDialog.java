package ise.antelope.launcher;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.BackingStoreException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * A dialog to allow the user to specify a location for ant.
 *
 * @author    Dale Anson
 * @version   $Revision$
 */
public class WhereIsAntDialog extends JDialog implements Constants {
    private JPanel panel = null;
    private String ant_home;
    private JButton ok_btn = null;
    private ArrayList actionListeners = new ArrayList();

    /** Constructor for WhereIsAntDialog */
    public WhereIsAntDialog() {
        super();
        setModal(true);
        setTitle("Where is Ant?");
        setContentPane(getPanel());
        pack();
        GUIUtils.centerOnScreen(this);
    }

    /**
     * Gets the panel attribute of the WhereIsAntDialog object
     *
     * @return   The panel value
     */
    private JPanel getPanel() {
        if (panel != null)
            return panel;
        KappaLayout panel_layout = new KappaLayout();
        panel = new JPanel(panel_layout);
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JLabel label = new JLabel("<html><b>Ant Home</b><br>Change or specify the location of Ant:");

        // 0123456
        // LLLLLLL
        // TTTTTTB
        // S
        //      BB
        ant_home = AntUtils.getAntHome();
        final JTextField ant_home_field = new JTextField(25);
        ant_home_field.setEditable(false);
        if (ant_home != null)
            ant_home_field.setText(ant_home);

        JButton choose_btn = new JButton("Choose...");
        ok_btn = new JButton("OK");
        JButton cancel_btn = new JButton("Cancel");

        panel.add(label, "0, 0, 6, 1, W, , 3");
        panel.add(ant_home_field, "0, 1, 6, 1, 0, wh, 3");
        panel.add(choose_btn, "6, 1, 1, 1, 0, wh, 3");
        panel.add(KappaLayout.createVerticalStrut(21), "0, 2, 1, 2, 0, h, 3");

        panel.add(ok_btn, "5, 3, 1, 1, 0, w, 3");
        panel.add(cancel_btn, "6, 3, 1, 1, 0, w, 3");
        panel_layout.makeColumnsSameWidth(5, 6);

        choose_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (ant_home == null || ant_home.equals("")) {
                        ant_home = System.getProperty("user.home");
                    }
                    JFileChooser chooser = new JFileChooser(ant_home);
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    chooser.setDialogTitle("Select Ant Home directory");
                    int rtn = chooser.showOpenDialog(null);
                    if (rtn == JFileChooser.APPROVE_OPTION) {
                        try {
                            File f = chooser.getSelectedFile();
                            if (!f.exists()) {
                                JOptionPane.showMessageDialog(null, "Directory " + f.toString() + " does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            File ant_jar = new File(f.getAbsolutePath(), "ant.jar");
                            if (!ant_jar.exists()) {
                                ant_jar = new File(f.getAbsolutePath() + File.separator + "lib", "ant.jar");
                                if (!ant_jar.exists()) {
                                    JOptionPane.showMessageDialog(null, "Directory " + f.toString() + " does not appear to contain Ant.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }

                            if (f.equals(new File(ant_home))) {
                                return;
                            }
                            ant_home = f.getAbsolutePath();
                            ant_home_field.setText(ant_home);
                        }
                        catch (Exception e) {
                        }
                    }
                }
            }
                );

        ok_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    try {
                        PREFS.put(ANT_HOME, ant_home);
                        PREFS.flush();
                    }
                    catch(BackingStoreException e) {
                    }
                    setVisible(false);
                    dispose();
                    for ( Iterator it = actionListeners.iterator(); it.hasNext(); ) {
                        ActionListener al = (ActionListener)it.next();
                        al.actionPerformed(ae);
                    }
                }
            });
        cancel_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                }
            });
        return panel;
    }

    /**
     * Gets the antHome attribute of the WhereIsAntDialog object
     *
     * @return   The antHome value
     */
    public String getAntHome() {
        return ant_home;
    }
    
    public void addActionListener(ActionListener al) {
        actionListeners.add(al);
    }
}

