package ise.antelope.common;

import ise.library.Nav;
import ise.library.Navable;
import ise.library.LambdaLayout;
import ise.library.GUIUtils;

import java.awt.BorderLayout;
import java.io.*;
import javax.swing.*;
import java.beans.Beans;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.*;
import org.xml.sax.Attributes;
import java.util.StringTokenizer;

/**
 * Shows an xml file in a tree.
 * @author Dale Anson
 */
public class SAXPanel extends JPanel implements Navable {

   private JTree tree = null;
   private CommonHelper _helper;
   private Nav _nav;

   public SAXPanel( CommonHelper ch ) {
      _helper = ch;
      setLayout( new BorderLayout() );
      try {
         tree = new JTree();
         tree.setCellRenderer( new SAXTreeCellRenderer() );

         add( new JScrollPane( tree ), BorderLayout.CENTER );


         JCheckBox show_attr_mi = new JCheckBox( "Show Attributes" );
         show_attr_mi.setSelected( true );
         add( show_attr_mi, BorderLayout.SOUTH );
         show_attr_mi.addActionListener( new ActionListener() {
                  public void actionPerformed( ActionEvent ae ) {
                     JCheckBox mi = ( JCheckBox ) ae.getSource();
                     SAXTreeCellRenderer renderer = ( SAXTreeCellRenderer ) tree.getCellRenderer();
                     renderer.setShowAttributes( mi.isSelected() );
                     tree.repaint();
                  }
               }
                                       );

         _nav = new Nav( this );
         JPanel bottom_panel = new JPanel( new LambdaLayout() );
         add( bottom_panel, BorderLayout.SOUTH );
         bottom_panel.add( show_attr_mi, "0,0" );
         bottom_panel.add( _nav, "1, 0" );


         MouseAdapter ma = new MouseAdapter() {
                  public void mouseClicked( MouseEvent evt ) {
                     int clicks = evt.getClickCount();
                     boolean rightClick = evt.isMetaDown();
                     if ( evt.getSource() instanceof JTree ) {
                        JTree tree = ( JTree ) evt.getSource();
                        TreePath path = tree.getClosestPathForLocation( evt.getX(),
                              evt.getY() );
                        if ( path != null ) {
                           int row = tree.getRowForPath( path );
                           Object object = path.getLastPathComponent();
                           if ( object instanceof SAXTreeNode ) {
                              SAXTreeNode node = ( SAXTreeNode ) object;
                              System.out.println("node file = " + node.getFile());
                              if (node.getFile() != null)
                                 _helper.openFile(node.getFile());
                              _helper.actionPerformed( new ActionEvent( node.getLocation(), CommonHelper.EDIT_EVENT, "" ) );
                              _nav.update( node.getLocation() );
                           }
                        }
                     }
                  }
                  public void mousePressed( MouseEvent me ) {
                     if ( me.isPopupTrigger() ) {
                        showPopup( me );
                     }
                  }
                  public void mouseReleased( MouseEvent me ) {
                     if ( me.isPopupTrigger() ) {
                        showPopup( me );
                     }
                  }

                  private void showPopup( MouseEvent me ) {
                     if ( me.getSource().equals( tree ) ) {
                        JTree tree = ( JTree ) me.getSource();
                        TreePath path = tree.getClosestPathForLocation( me.getX(),
                              me.getY() );
                        if ( path != null ) {
                           int row = tree.getRowForPath( path );
                           Object object = path.getLastPathComponent();
                           if ( object instanceof SAXTreeNode ) {
                              SAXTreeNode node = ( SAXTreeNode ) object;
                              TreeModel tm = getDependencyModel( node );
                              if ( tm != null ) {
                                 JPanel panel = new JPanel(new BorderLayout());
                                 panel.add(new JLabel("Dependency Tree"), BorderLayout.NORTH);
                                 JTree dt = new JTree( tm );
                                 dt.addMouseListener( this );
                                 dt.setCellRenderer( new SAXTreeCellRenderer() );
                                 for ( int i = 0; i < dt.getRowCount(); i++ ) {
                                    dt.expandRow( i );
                                 }
                                 panel.add( new JScrollPane( dt ) );
                                 JPopupMenu pm = new JPopupMenu();
                                 pm.add( panel );
                                 GUIUtils.showPopupMenu( pm, SAXPanel.this, me.getX(), me.getY() );
                              }
                           }
                        }
                     }
                  }

               }
               ;
         tree.addMouseListener( ma );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }

   public void setPosition( Object o ) {
      if ( o instanceof java.awt.Point ) {
         _helper.actionPerformed( new ActionEvent( o, CommonHelper.EDIT_EVENT, "" ) );
      }
   }

   /**
    * @return a TreeModel representing the dependency tree of the given
    * target. Returns null if the target is not found.
    */
   public TreeModel getDependencyModel( String target_name ) {
      SAXTreeNode initial_target = getTargetNode( target_name );
      return getDependencyModel( initial_target );
   }

   public TreeModel getDependencyModel( SAXTreeNode node ) {
      if ( node == null )
         return null;
      SAXTreeNode root = new SAXTreeNode( node.getName(), node.getLocation(), node.getAttributes() );
      addDependentTargetNodes( root );
      return new DefaultTreeModel( root );
   }

   private void addDependentTargetNodes( SAXTreeNode node ) {
      SAXTreeNode[] depends = getDependentTargetNodes( node );
      for ( int i = 0; i < depends.length; i++ ) {
         if ( depends[ i ] != null ) {
            node.add( depends[ i ] );
            addDependentTargetNodes( depends[ i ] );
         }
      }
   }

   /**
    * @return a clone of the SAXTreeNode corresponding to the given target name or
    * null if no target with that name is found. Note that in the case of
    * duplicate target names, the first one found will be returned.
    */
   private SAXTreeNode getTargetNode( String target_name ) {
      if ( target_name == null )
         return null;
      try {
         DefaultTreeModel model = ( DefaultTreeModel ) tree.getModel();
         SAXTreeNode root = ( SAXTreeNode ) model.getRoot();
         int child_count = model.getChildCount( root );
         for ( int i = 0; i < child_count; i++ ) {
            SAXTreeNode node = ( SAXTreeNode ) model.getChild( root, i );

            // check targets in imported projects
            if ( node.getName().equals( "project" ) ) {
               int sub_child_count = model.getChildCount( node );
               for ( int j = 0; j < sub_child_count; j++ ) {
                  SAXTreeNode sub_node = ( SAXTreeNode ) model.getChild( node, j );
                  if ( !sub_node.getName().equals( "target" ) )
                     continue;
                  Attributes attrs = sub_node.getAttributes();
                  int index = attrs.getIndex( "name" );
                  if ( index == -1 )
                     continue;
                  String name = attrs.getValue( index );
                  if ( target_name.equals( name ) ){
                     SAXTreeNode stn = new SAXTreeNode( sub_node.getName(), sub_node.getLocation(), sub_node.getAttributes(), sub_node.getFile() );
                     stn.setImported(sub_node.isImported());
                     return stn;
                  }
               }
            }

            // check targets in top-level build file
            if ( !node.getName().equals( "target" ) )
               continue;
            Attributes attrs = node.getAttributes();
            int index = attrs.getIndex( "name" );
            if ( index == -1 )
               continue;
            String name = attrs.getValue( index );
            if ( target_name.equals( name ) ){
               SAXTreeNode stn = new SAXTreeNode( node.getName(), node.getLocation(), node.getAttributes(), node.getFile() );
               stn.setImported(node.isImported());
               return stn;
            }
         }
      }
      catch ( Exception e ) {
         // ignored
      }
      return null;
   }

   /**
    * Get a list of target nodes that the given target depends on.   
    * @param target_node a node representing a target   
    * @return an array of tree nodes that represent dependency targets. This
    * array may be empty.
    */
   private SAXTreeNode[] getDependentTargetNodes( SAXTreeNode target_node ) {
      Attributes attrs = target_node.getAttributes();
      int index = attrs.getIndex( "depends" );
      if ( index == -1 )
         return new SAXTreeNode[ 0 ];
      String depends = attrs.getValue( index );
      StringTokenizer st = new StringTokenizer( depends, "," );
      int size = st.countTokens();
      if ( size <= 0 )
         return new SAXTreeNode[ 0 ];
      SAXTreeNode[] nodes = new SAXTreeNode[ size ];
      for ( int i = 0; st.hasMoreTokens(); i++ ) {
         SAXTreeNode node = getTargetNode( st.nextToken().trim() );
         nodes[ i ] = node;
      }
      return nodes;
   }

   /**
    * @return true if the file is an Ant build file, false if not.   
    */
   public boolean openBuildFile( File f ) {
      SAXTreeModel model = new SAXTreeModel( f );
      tree.setModel( model );
      model.nodeChanged( ( TreeNode ) model.getRoot() );
      tree.repaint();
      SAXTreeNode root = ( SAXTreeNode ) model.getRoot();
      if ( root == null )
         return false;
      if ( root.getName() == null )
         return false;
      return root.getName().equals( "project" );
   }
}
