package ise.antelope.common.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import ise.library.*;

public class TargetBuilder extends JPanel {
   private String name = null;
   
   public TargetBuilder(String name) {
      this.name = name;
      setLayout(new LambdaLayout());
      setBackground(Color.WHITE);
      setBorder(new DropShadowBorder());

      JPanel title_panel = new JPanel();
      JLabel label = new JLabel(name);
      title_panel.add(label);
      
      JPanel task_panel = new JPanel();
      
      add(title_panel, "0, 0, 1, 1, 0, wh, 3");
      add(task_panel,  "0, 1, 1, 1, 0, wh, 3");
   }
}
