package ise.antelope.common.builder;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import ise.library.*;
import com.wutka.dtd.*;


/**
 * Shows a popup.   
 */
public class AttributeViewer extends MouseAdapter {
   private JList parent = null;
   private String element_name = null;
   private JTextArea ta;
   private JPopupMenu pm;
   public AttributeViewer( JList comp ) {
      parent = comp;
      ta = new JTextArea( 10, 40 );
      ta.setLineWrap( true );
      ta.setEditable( false );
   }
   public void mousePressed( MouseEvent me ) {
      doPopup( me );
   }
   public void mouseReleased( MouseEvent me ) {
      doPopup( me );
   }
   private void doPopup( MouseEvent me ) {
      if ( me.isPopupTrigger() ) {
         // from ElementTransferHandler:
         // 'data' is one of two things -- 1) it is a single word, which means
         // it is a raw element name, or 2) it is already formatted as html.
         // Determine the second case by checking for <html> at the start of the
         // string, if not, assume the first case. The html has the element name
         // surrounded by <b> tags. Element attributes are separated by <br>,
         // with attribute name and value enclosed individually in <span> tags.
         // Enumerations or lists have the individual value items wrapped in a
         // <ul> with individual items separated by <li>. There are no line-
         // enders (\n, \r).
         //
         // Everything handled by this handler is already in html format.

         // get the selected item from the JList
         Object o = parent.getSelectedValue();
         if ( o == null )
            return ;
         String data = o.toString();
         if ( data.length() == 0 )
            return ;
         if ( !data.startsWith( "<html>" ) )
            return ;

         // get ready to layout
         final JPanel top = new JPanel( new KappaLayout() );
         KappaLayout.Constraints c = KappaLayout.createConstraint();
         c.p = 3;
         c.a = KappaLayout.W;

         // get the name of the element
         int start = "<html><b>".length();
         int end = data.indexOf( "</b>" );
         element_name = data.substring( start, end );
         top.add( new JLabel( "<html><b>" + element_name + "</b>" ), c );

         // parse the rest of the html
         start = end + "</b>".length();
         data = data.substring( start );

         Pattern p = Pattern.compile( "<br>" );
         String[] attrs = p.split( data );

         c.a = KappaLayout.E;
         ++ c.y;

         Pattern div_pattern = Pattern.compile( "</span>" );
         for ( int i = 0; i < attrs.length; i++ ) {
            if (attrs[i].length() == 0)
               continue;
            String[] pair = div_pattern.split( attrs[ i ] );
            if (pair.length != 2)
               break;
            
            String name = pair[ 0 ];
            if (!name.startsWith("<span>"))
               continue;
            String value = pair[ 1 ];
            
            name = name.substring("<span>".length());
            value = value.substring("<span>".length());

            if ( name.equals( "taskname" ) )
               continue;

            JLabel label = new JLabel( name + ":" );
            JTextField comp = new JTextField( 25 );
            comp.setText( value );

            top.add( label, c );
            c.x = 1;
            top.add( comp, c );
            c.x = 0;
            ++c.y;
         }
         
         KappaLayout kl = new KappaLayout();
         JPanel btn_panel = new JPanel(kl);
         JButton ok_btn = new JButton("OK");
         JButton cancel_btn = new JButton("Cancel");
         btn_panel.add(ok_btn, "0, 0, 1, 1, 0, wh, 3");
         btn_panel.add(cancel_btn, "1, 0, 1, 1, 0, wh, 3");
         kl.makeColumnsSameWidth(0, 1);
         c.x = 0;
         ++c.y;
         c.w = 2;
         c.p = 6;
         top.add(btn_panel, c);
         
         ok_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
               pm.setVisible(false);
               StringBuffer sb = new StringBuffer();
               int cnt = top.getComponentCount();
               JLabel label = (JLabel)top.getComponent(0);
               sb.append(label.getText()).append("<br>");
               
               JTextField tf;
               for (int i = 1; i < cnt - 2; i += 2) {
                  label = (JLabel)top.getComponent(i);
                  tf = (JTextField)top.getComponent(i + 1);
                  String label_text = label.getText();
                  sb.append("<span>").append(label_text.substring(0, label_text.length() - 1)).append("</span>");
                  sb.append("<span>").append(tf.getText()).append("</span>").append("<br>");
               }
               sb.append("</html>");
               ((DefaultListModel)parent.getModel()).set(parent.getSelectedIndex(), sb.toString());
            }
         });
         
         cancel_btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
               pm.setVisible(false);  
            }
         });

         pm = new JPopupMenu();
         pm.add( new JScrollPane( top ) );
         ta.setText( element_name );
         GUIUtils.showPopupMenu( pm, parent, me.getX(), me.getY() );
      }
   }
}

