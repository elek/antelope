package ise.antelope.common.builder;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import ise.library.*;

public class ElementPanel extends JPanel implements java.io.Serializable {

   private JList list = null;

   public ElementPanel( String type ) {
      setLayout( new BorderLayout() );
      setBorder( new DropShadowBorder() );
      setTransferHandler( new ElementTransferHandler() );
      add( new JLabel( type ), BorderLayout.NORTH );
      list = new JList( new DefaultListModel() );
      add( list, BorderLayout.CENTER );
      list.setTransferHandler(new ElementTransferHandler());
      list.setDragEnabled(true);
      list.addMouseListener(new AttributeViewer(list));
   }

   Dimension my_size = new Dimension( 200, 100 );
   public Dimension getPreferredSize() {
      return my_size;
   }

   protected JList getList() {
      return list;
   }
}
