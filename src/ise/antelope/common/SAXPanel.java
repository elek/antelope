package ise.antelope.common;

import ise.library.Nav;
import ise.library.Navable;
import ise.library.LambdaLayout;
import ise.library.GUIUtils;

import java.awt.BorderLayout;
import java.io.*;
import java.util.*;
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
                              if ( node.getFile() != null )
                                 _helper.openFile( node.getFile() );
                              _helper.actionPerformed( new ActionEvent( node.getLocation(), CommonHelper.EDIT_EVENT, "" ) );
                              _nav.update( node.getLocation() );
                           }
                        }
                     }
                  }
                  public void mousePressed( MouseEvent me ) {
                      System.out.println("mousePressed");
                      System.out.println("me.isPopupTrigger = " + me.isPopupTrigger());
                     if ( me.isPopupTrigger() ) {
                        showPopup( me );
                     }
                  }
                  public void mouseReleased( MouseEvent me ) {
                      System.out.println("mouseReleased");
                      System.out.println("me.isPopupTrigger = " + me.isPopupTrigger());
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
                              if ( node.isTarget() ) {
                                 TreeModel tm = SAXPanel.this.getDependencyModel( node, ( SAXTreeNode ) node.getRoot() );
                                 if ( tm != null ) {
                                    JPanel panel = new JPanel( new BorderLayout() );
                                    JTree dt = new JTree( tm );
                                    dt.addMouseListener( this );
                                    dt.setCellRenderer( new SAXTreeCellRenderer() );
                                    for ( int i = 0; i < dt.getRowCount(); i++ ) {
                                       dt.expandRow( i );
                                    }
                                    panel.add( new JScrollPane( dt ) );
                                    final JDialog dialog = new JDialog( GUIUtils.getRootJFrame( SAXPanel.this ), "Dependency Tree", true );
                                    dialog.getContentPane().add( new JScrollPane( panel ), BorderLayout.CENTER );
                                    JButton close_btn = new JButton( "Close" );
                                    JPanel btn_panel = new JPanel();
                                    btn_panel.add( close_btn );
                                    dialog.getContentPane().add( btn_panel, BorderLayout.SOUTH );
                                    close_btn.addActionListener( new ActionListener() {
                                             public void actionPerformed( ActionEvent ae ) {
                                                dialog.hide();
                                                dialog.dispose();
                                             }
                                          }
                                                               );
                                    dialog.pack();
                                    dialog.setSize( 300, 300 );
                                    java.awt.Point p = SAXPanel.this.getLocation();
                                    SwingUtilities.convertPointToScreen( p, SAXPanel.this );
                                    dialog.setLocation( GUIUtils.getBestAnchorPoint( dialog, p.x + me.getX(), p.y + me.getY() ) );
                                    dialog.setVisible( true );
                                 }
                                 return ;
                              }

                              // show an "open" popup for certain tasks that use property files
                              String filename = null;
                              if ( node.getName().equals( "property" ) ) {
                                 filename = node.getAttributeValue( "file" );
                              }
                              else if ( node.getName().equals( "loadproperties" ) ) {
                                 filename = node.getAttributeValue( "srcfile" );
                              }
                              if ( filename == null )
                                 return ;
                              try {
                                 File ft = new File( filename );
                                 if ( !ft.exists() ) {
                                    File dir = node.getFile();
                                    if ( dir == null )
                                       return ;
                                    ft = new File( dir.getParentFile(), filename );
                                 }
                                 final File f = ft;

                                 JPopupMenu popup = new JPopupMenu();
                                 JMenuItem mi = new JMenuItem( "Open" );
                                 popup.add( mi );

                                 mi.addActionListener( new ActionListener() {
                                          public void actionPerformed( ActionEvent ae ) {
                                             _helper.openFile( f );
                                          }
                                       }
                                                     );

                                 GUIUtils.showPopupMenu( popup, SAXPanel.this, me.getX(), me.getY() );
                              }
                              catch ( Exception e ) {}
                           }
                        }
                     }
                  }
               }
               ;
               
         tree.addMouseListener( ma );
         System.out.println("added mouse listener");
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
   
   public SAXTreeModel getModel() {
      return (SAXTreeModel)tree.getModel();  
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
      SAXTreeNode initial_target = getTargetNode( target_name, ( SAXTreeNode ) tree.getModel().getRoot() );
      return getDependencyModel( initial_target, ( SAXTreeNode ) tree.getModel().getRoot() );
   }

   /**
    * @return a TreeModel representing the dependency tree of the given
    * target. Returns null if the target is not found.
    */
   public TreeModel getDependencyModel( SAXTreeNode node, SAXTreeNode tree_root ) {
      if ( node == null )
         return null;
      SAXTreeNode root = ( SAXTreeNode ) node.clone(); //new SAXTreeNode( node.getName(), node.getLocation(), node.getAttributes() );
      root.setFile( node.getFile() );
      addDependentTargetNodes( root, tree_root );
      addCalledTargetNodes( node, root, tree_root );
      return new DefaultTreeModel( root );
   }

   private void addCalledTargetNodes( SAXTreeNode src, SAXTreeNode dest, SAXTreeNode tree_root ) {
      SAXTreeNode[] called_targets = getCalledTargetNodes( src, tree_root );
      if ( called_targets.length > 0 ) {
         SAXTreeNode called_root = new SAXTreeNode( "Calls", dest.getLocation(), null, dest.getFile() );
         for ( int i = 0; i < called_targets.length; i++ ) {
            called_root.add( called_targets[ i ] );
         }
         dest.add( called_root );
      }
   }

   /**
    * Looks through the given node for ant and antcall (and call, runtarget, and antcallback) tasks.   
    * @return an array of child nodes representing ant and/or antcall tasks.
    * The array may be empty, but won't be null.
    */
   private SAXTreeNode[] getCalledTargetNodes( SAXTreeNode node, SAXTreeNode tree_root ) {
      int child_count = node.getChildCount();
      ArrayList children = new ArrayList();
      for ( int i = 0; i < child_count; i++ ) {
         SAXTreeNode child_node = ( SAXTreeNode ) node.getChildAt( i );
         String child_name = child_node.getName();
         Attributes attrs = child_node.getAttributes();
         if ( child_name.equals( "antcall" ) || child_name.equals( "call" ) || child_name.equals( "runtarget" ) || child_name.equals( "antcallback" ) ) {
            int index = attrs.getIndex( "target" );
            if ( index == -1 )
               continue;
            String called_target = attrs.getValue( "target" );
            SAXTreeNode called_node = getTargetNode( called_target, tree_root );
            if ( called_node != null ) {
               called_node.setCalled( true );
               children.add( called_node );
            }
         }
         else if ( child_name.equals( "ant" ) ) {
            // get the build file name, default is build.xml
            String antfile = "build.xml";
            int index = attrs.getIndex( "antfile" );
            if ( index > -1 )
               antfile = attrs.getValue( "antfile" );

            // get the directory, default is the basedir of the current project
            String dir = node.getFile() != null ? node.getFile().getParent() : "";
            index = attrs.getIndex( "dir" );
            if ( index > -1 )
               dir = attrs.getValue( "dir" );

            // make sure the build file exists
            File build_file = new File( dir, antfile );
            if ( !build_file.exists() )
               continue;

            // load the build file using the SAXNodeHandler, get a project node
            SAXTreeNode ant_root = ( SAXTreeNode ) new SAXTreeModel( build_file ).getRoot();

            // get the target name, default is the default target in the build file
            String ant_target_name = null;
            index = attrs.getIndex( "target" );
            if ( index > -1 )
               ant_target_name = attrs.getValue( "target" );
            else {
               Attributes ant_attrs = ant_root.getAttributes();
               index = ant_attrs.getIndex( "default" );
               if ( index > -1 )
                  ant_target_name = ant_attrs.getValue( "default" );
            }
            if ( ant_target_name == null )
               continue;

            // get the target dependencies and called targets
            SAXTreeNode ant_target = getTargetNode( ant_target_name, ant_root );
            SAXTreeNode ant_target_copy = ( SAXTreeNode ) ant_target.clone(); //new SAXTreeNode( node.getName(), node.getLocation(), node.getAttributes() );
            addDependentTargetNodes( ant_target_copy, tree_root );
            addCalledTargetNodes( ant_target, ant_target_copy, tree_root );
            children.add( ant_target_copy );
         }
      }
      SAXTreeNode[] nodes = new SAXTreeNode[ children.size() ];
      Iterator it = children.iterator();
      for ( int i = 0; it.hasNext(); i++ ) {
         nodes[ i ] = ( SAXTreeNode ) it.next();
      }
      return nodes;
   }

   private void addDependentTargetNodes( SAXTreeNode node, SAXTreeNode tree_root ) {
      SAXTreeNode depends_root = new SAXTreeNode( "Depends on", node.getLocation(), null, node.getFile() );
      SAXTreeNode[] depends = getDependentTargetNodes( node, tree_root );
      for ( int i = 0; i < depends.length; i++ ) {
         if ( depends[ i ] != null ) {
            depends_root.add( depends[ i ] );
            //addDependentTargetNodes( depends[ i ], tree_root );
         }
      }
      if ( depends_root.getChildCount() > 0 )
         node.add( depends_root );
   }

   /**
    * @param target_name the name of the target to find
    * @param root the root node of the tree to look for the target. The target
    * may be a child of this node, or in the case of imported files, may be in
    * any depth of imported projects.
    * @return a clone of the SAXTreeNode corresponding to the given target name or
    * null if no target with that name is found. Note that in the case of
    * duplicate target names, the first one found will be returned.
    */
   private SAXTreeNode getTargetNode( String target_name, SAXTreeNode tree_root ) {
      if ( target_name == null )
         return null;
      try {
         int child_count = tree_root.getChildCount( );
         SAXTreeNode candidate = null;
         for ( int i = 0; i < child_count; i++ ) {
            SAXTreeNode node = ( SAXTreeNode ) tree_root.getChildAt( i );

            // check targets in imported projects
            if ( node.getName().equals( "project" ) && candidate == null ) {
               Attributes attrs = node.getAttributes();
               int index = attrs.getIndex( "name" );
               if ( index > -1 ) {
                  String subproject_name = attrs.getValue( index );
                  int sub_child_count = node.getChildCount();
                  for ( int j = 0; j < sub_child_count; j++ ) {
                     SAXTreeNode sub_node = ( SAXTreeNode ) node.getChildAt( j );
                     if ( !sub_node.getName().equals( "target" ) )
                        continue;
                     attrs = sub_node.getAttributes();
                     index = attrs.getIndex( "name" );
                     if ( index == -1 )
                        continue;
                     String name = attrs.getValue( index );
                     if ( target_name.startsWith( subproject_name ) ) {
                        String tn = target_name.substring( subproject_name.length() + 1 ); // +1 for the dot
                        if ( tn.equals( name ) ) {
                           candidate = ( SAXTreeNode ) sub_node.clone();
                           candidate.setImported( sub_node.isImported() );
                        }
                     }
                  }
               }
            }

            // check targets in top-level build file, targets here have priority
            if ( !node.getName().equals( "target" ) )
               continue;
            Attributes attrs = node.getAttributes();
            int index = attrs.getIndex( "name" );
            if ( index == -1 )
               continue;
            String name = attrs.getValue( index );
            if ( target_name.equals( name ) ) {
               candidate = ( SAXTreeNode ) node.clone();
               addDependentTargetNodes( candidate, tree_root );
               addCalledTargetNodes( node, candidate, tree_root );
               return candidate;
            }
         }
         return candidate;
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
   private SAXTreeNode[] getDependentTargetNodes( SAXTreeNode target_node, SAXTreeNode tree_root ) {
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
         SAXTreeNode node = getTargetNode( st.nextToken().trim(), tree_root );
         nodes[ i ] = node;
      }
      return nodes;
   }

   /**
    * @return true if any property file tracked by the model is newer than when it
    * was first looked at.
    */
   public boolean shouldReload() {
      return ( ( SAXTreeModel ) tree.getModel() ).shouldReload();
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

   /**
    * Returns a map of targets in the current build file, including any imported
    * or included targets from other files.  Key is the target name,
    * value is a SAXTreeNode.
    * @return a list of targets in the current build file. Key is the target name,
    * value is a SAXTreeNode.
    */
   protected Map getTargets() {
      SAXTreeModel model = (SAXTreeModel)tree.getModel();
      return model.getTargets();
   }
}
