package ise.library;

import java.awt.*;
import javax.swing.*;
import ise.antelope.common.*;

public class UIDemo extends JFrame {
   public UIDemo() {
      setTitle("Antelope");
      
      JMenuBar menubar = new JMenuBar();
      menubar.add(new JMenu("File"));
      menubar.add(new JMenu("Edit"));
      menubar.add(new JMenu("Output"));
      menubar.add(new JMenu("Options"));
      menubar.add(new JMenu("Help"));
      
      setJMenuBar(menubar);
      
      
      
      
      JSplitPane pane_a = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      JSplitPane pane_b = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      JSplitPane pane_c = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      
      pane_a.setOneTouchExpandable(true);
      pane_b.setOneTouchExpandable(true);
      pane_c.setOneTouchExpandable(true);
      
      pane_a.setTopComponent(pane_b);
      pane_a.setBottomComponent(new JTextArea());
      pane_b.setRightComponent(pane_c);
      pane_b.setLeftComponent(new AntelopePanel());
      pane_c.setLeftComponent(new JPanel());
      pane_c.setRightComponent(new JPanel());
      
      
      getContentPane().add(pane_a, BorderLayout.CENTER);
      
      
      setSize(800, 600);
      setVisible(true);
   }
   
   public static void main (String[] args) {
      new UIDemo();
   }
}
