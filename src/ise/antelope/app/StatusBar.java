package ise.antelope.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import ise.library.*;

public class StatusBar extends JPanel {
    
    private JTextField _field;
    private JTextField _line;
    
    public StatusBar() {
        setLayout(new LambdaLayout());
        _field = new JTextField(80);
        _field.setEditable(false);
        _field.setBackground(Color.WHITE);
        _line = new JTextField();
        _line.setEditable(false);
        _line.setBackground(Color.WHITE);
        add(_field, "0, 0, 10, 1, 0, wh, 1");
        add(_line, "11, 0, R, 1, 0, wh, 1");
    }
    
    public void setStatus(String status) {
        if (status == null)
            status = "";
        _field.setText(status);   
    }
    
    public String getStatus() {
        return _field.getText();   
    }
    
    public void setLine(int line, int total) {
        _line.setText(String.valueOf(line) + ":" + String.valueOf(total));   
    }
    
}
