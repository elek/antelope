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
 * Shows the attributes of an element and provides the UI necessary to edit.
 * @author Dale Anson
 * @version $Revision$
 */
public class AttributeViewer extends JDialog {
   private ElementPanel parent = null;
   private String element_name = null;

   public AttributeViewer( ElementPanel comp ) {
      super( GUIUtils.getRootFrame( comp ), "Properties", true );

      parent = comp;
      JPanel contents = getContents();
      if ( contents == null )
         return ;

      setContentPane( contents );
      pack();
      setVisible( true );
   }

   private JPanel getContents() {
      final ElementPanel ep = ( ElementPanel ) parent;

      final JPanel top = new JPanel( new KappaLayout() );
      KappaLayout.Constraints c = KappaLayout.createConstraint();
      c.p = 3;
      c.a = KappaLayout.W;

      top.add( new JLabel( "<html><b><i><u>" + ep.getName() + "</u></i></b></html>" ), c );

      final Map attributes = ep.getAttributes();
      Iterator it = attributes.keySet().iterator();
      while ( it.hasNext() ) {
         ++ c.y;
         c.a = KappaLayout.E;
         String name = it.next().toString();
         if ( name.equals( "taskname" ) && ep.isTaskOrType() )
            continue;

         NodeAttribute attribute = (NodeAttribute)attributes.get(name);
         boolean required = attribute.isRequired();
         
         JLabel label = new JLabel( "<html>" + ( required ? "<font color=red>*</font>" : "" ) + name + ":" );

         JComponent comp = null;
         String[] items = attribute.getItems();
         if (items != null) {
            comp = new JComboBox( items );
            ( ( JComboBox ) comp ).setEditable( false );
            ( ( JComboBox ) comp ).setSelectedItem( attribute.getValue() );
              
         }
         else {
            comp = new JTextField( 25 );
            ( ( JTextField ) comp ).setText( attribute.getValue() );
         }
         comp.setName( name );

         top.add( label, c );
         c.x = 1;
         c.a = KappaLayout.W;
         top.add( comp, c );
         c.x = 0;
      }
      c.a = KappaLayout.E;

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
                  AttributeViewer.this.setVisible( false );

                  int cnt = top.getComponentCount();
                  for ( int i = 0; i < cnt; i++ ) {
                     Component c = top.getComponent( i );
                     if ( c instanceof JTextField ) {
                        JTextField tf = ( JTextField ) c;
                        NodeAttribute attr = (NodeAttribute)attributes.get(tf.getName());
                        attr.setValue( tf.getText() );
                     }
                     if (c instanceof JComboBox) {
                        JComboBox cb = (JComboBox)c;
                        NodeAttribute attr = (NodeAttribute)attributes.get(cb.getName());
                        attr.setValue( cb.getSelectedItem().toString() );
                     }
                  }
                  ep.setAttributes( attributes );

                  AttributeViewer.this.dispose();
               }
            }
                              );

      cancel_btn.addActionListener( new ActionListener() {
               public void actionPerformed( ActionEvent ae ) {
                  AttributeViewer.this.setVisible( false );
                  AttributeViewer.this.dispose();
               }
            }
                                  );
      return top;
   }
}

