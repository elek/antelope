
package ise.antelope.common.dependency;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import org.xml.sax.*;
import org.tigris.gef.base.*;
import org.tigris.gef.demo.*;
import org.tigris.gef.graph.*;
import org.tigris.gef.graph.presentation.*;
import org.tigris.gef.presentation.*;

import ise.antelope.common.*;

public class DependencyViewer {

   private File _build_file = null;

   public DependencyViewer( File build_file ) {
      _build_file = build_file;

      // load the build file and get all target nodes
      SAXTreeModel model = new SAXTreeModel( _build_file );
      HashMap target_nodes = model.getTargets();

      // make corresponding graph nodes
      HashMap graph_nodes = new HashMap();
      Iterator it = target_nodes.keySet().iterator();
      while ( it.hasNext() ) {
         String name = ( String ) it.next();
         SAXTreeNode child = ( SAXTreeNode ) target_nodes.get( name );
         DependencyNode graph_node = new DependencyNode( child );
         String child_name = child.getAttributeValue( "name" );
         if ( child.isImported() )
            child_name = ( ( SAXTreeNode ) child.getParent() ).getAttributeValue( "name" ) + "." + child_name;
         graph_nodes.put( child_name, graph_node );
      }

      DefaultGraphModel graph_model = new DefaultGraphModel();

      JGraph graph = new JGraph( graph_model );
      graph.setForeground( java.awt.Color.white );

      //JFrame.setDefaultLookAndFeelDecorated( true );
      JFrame frame = new JFrame();
      frame.setTitle(build_file.getAbsolutePath());
      frame.setContentPane( graph );
      frame.setSize( 600, 600 );
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      frame.setVisible( true );

      LayerManager lm = graph.getEditor().getLayerManager();
      lm.replaceLayer( lm.findLayerNamed( "Grid" ), new LayerGrid( new Color( 180, 180, 180 ), Color.white, 16, false ) );
      LayerPerspective active_layer = ( LayerPerspective ) lm.getActiveLayer();
      active_layer.addNodeTypeRegion( DependencyNode.class, new java.awt.Rectangle( 100, 100, 800, 600 ) );

      // build the graph edges
      it = graph_nodes.keySet().iterator();
      ArrayList edges = new ArrayList();
      while ( it.hasNext() ) {
         String target_name = ( String ) it.next();
         DependencyNode target_node = ( DependencyNode ) graph_nodes.get( target_name );
         Attributes attrs = ( Attributes ) target_node.getNode().getAttributes();
         if ( attrs != null ) {
            int index = attrs.getIndex( "depends" );
            String value = attrs.getValue( index );
            if ( value != null ) {
               StringTokenizer st = new StringTokenizer( value, "," );
               for ( int i = 0; i < st.countTokens(); i++ ) {
                  String dname = st.nextToken();
                  DependencyNode dnode = ( DependencyNode ) graph_nodes.get( dname );
                  if ( dnode == null )
                     continue;

                  DependencyPort dp1 = new DependencyPort( target_node );
                  target_node.addPort( dp1 );
                  DependencyPort dp2 = new DependencyPort( dnode );
                  dnode.addPort( dp2 );

                  DependencyEdge dedge2 = new DependencyEdge( String.valueOf( i + 1 ) );
                  dedge2.connect( graph_model, dp1, dp2 );
                  edges.add( dedge2 );
               }
            }
         }
      }
      
      // populate the graph
      Iterator itr = graph_nodes.values().iterator();
      while ( itr.hasNext() )
         graph_model.addNode( ( NetNode ) itr.next() );
      itr = edges.iterator();
      while ( itr.hasNext() ) {
         NetEdge edge = ( NetEdge ) itr.next();
         graph_model.addEdge( edge );
         lm.sendToBack( edge.presentationFor( active_layer ) );
      }
      graph.layoutGraph();

   }

   public static void main ( String[] args ) {
      try {
         File f = new File( "c:/apache-ant-1.6.0/src/apache-ant-1.6.0/build.xml" );
         if ( !f.exists() )
            throw new FileNotFoundException( f.getAbsolutePath() );
         new DependencyViewer( f );
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
}
