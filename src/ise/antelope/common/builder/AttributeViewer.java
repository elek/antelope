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

         // get the selected item from the JList, it should be an ElementPanel
         Object o = parent.getSelectedValue();
         if ( o == null )
            return ;
         if ( !( o instanceof ElementPanel ) )
            return ;
         final ElementPanel ep = ( ElementPanel ) o;

         final JPanel top = new JPanel( new KappaLayout() );
         KappaLayout.Constraints c = KappaLayout.createConstraint();
         c.p = 3;
         c.a = KappaLayout.W;

         top.add( new JLabel( "<html><b>" + ep.getName() + "</b></html>" ), c );
         c.a = KappaLayout.E;
         ++ c.y;

         final Map attributes = ep.getAttributes();
         Iterator it = attributes.keySet().iterator();
         while ( it.hasNext() ) {
            String name = it.next().toString();
            if ( name.equals( "taskname" ) && ep.isTask() )
               continue;

            DTDAttribute attribute = ( DTDAttribute ) attributes.get( name );
            DTDDecl decl = attribute.getDecl();
            boolean required = decl.equals( DTDDecl.REQUIRED );
            Object type = attribute.getType();

            JLabel label = new JLabel( "<html>" + ( required ? "<font color=red>*</font>" : "" ) + name + ":" );

            JComponent comp = null;
            if ( type instanceof DTDEnumeration || type instanceof DTDNotationList ) {
               String[] items = ( ( DTDEnumeration ) type ).getItem();
               comp = new JComboBox( items );
               ( ( JComboBox ) comp ).setEditable( false );
            }
            else {
               // assume String type
               comp = new JTextField( 25 );
               ( ( JTextField ) comp ).setText( attribute.getDefaultValue() );
            }
            comp.setName( name );

            top.add( label, c );
            c.x = 1;
            top.add( comp, c );
            c.x = 0;
            ++c.y;
         }

         // button panel
         KappaLayout kl = new KappaLayout();
         JPanel btn_panel = new JPanel( kl );
         JButton ok_btn = new JButton( "OK" );
         JButton cancel_btn = new JButton( "Cancel" );
         btn_panel.add( ok_btn, "0, 0, 1, 1, 0, wh, 3" );
         btn_panel.add( cancel_btn, "1, 0, 1, 1, 0, wh, 3" );
         kl.makeColumnsSameWidth( 0, 1 );
         c.x = 0;
         ++c.y;
         c.w = 2;
         c.p = 6;
         top.add( btn_panel, c );

         ok_btn.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     pm.setVisible( false );

                     int cnt = top.getComponentCount();
                     for ( int i = 0; i < cnt; i++ ) {
                        Component c = top.getComponent( i );
                        if ( c instanceof JTextField ) {
                           JTextField tf = ( JTextField ) c;
                           DTDAttribute attr = ( DTDAttribute ) attributes.get( tf.getName() );
                           attr.setDefaultValue( tf.getText() );
                        }
                     }
                     ep.setAttributes( attributes );
                  }
               }
                                 );

         cancel_btn.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     pm.setVisible( false );
                  }
               }
                                     );

         pm = new JPopupMenu();
         pm.add( new JScrollPane( top ) );
         ta.setText( element_name );
         GUIUtils.showPopupMenu( pm, parent, me.getX(), me.getY() );
      }
   }
}

