
package ise.antelope.common.dependency;

import java.awt.Color;
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
import ise.library.ListOps;

public class DependencyViewer {

   private File _build_file = null;

   public DependencyViewer( File build_file ) {
      _build_file = build_file;

      // load the build file and get all target nodes
      SAXTreeModel model = new SAXTreeModel( _build_file );
      Map target_nodes = model.getTargets();

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
      frame.setTitle( build_file.getAbsolutePath() );
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
      List edges = new ArrayList();
      while ( it.hasNext() ) {
         String target_name = ( String ) it.next();
         DependencyNode target_node = ( DependencyNode ) graph_nodes.get( target_name );
         SAXTreeNode snode = target_node.getNode();
         String depends = snode.getAttributeValue( "depends" );
         if ( depends != null && depends.length() > 0 ) {
            StringTokenizer st = new StringTokenizer( depends, "," );
            int token_count = st.countTokens();
            for ( int i = 0; i < token_count; i++ ) {
               String name = st.nextToken().trim();
               DependencyNode dnode = ( DependencyNode ) graph_nodes.get( name );
               if ( dnode == null )
                  continue;
               System.out.println( target_name + " depends on " + name );

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

      // populate and layout the graph
      // first, sort the nodes. This hashmap has an Integer key, which indicates
      // how many ports a node has. The value is a list of nodes with that number
      // of ports.
      it = graph_nodes.keySet().iterator();
      HashMap port_map = new HashMap();
      while ( it.hasNext() ) {
         String name = ( String ) it.next();
         DependencyNode dnode = ( DependencyNode ) graph_nodes.get( name );
         Integer port_count = new Integer( dnode.getPorts().size() );
         List nodes = ( List ) port_map.get( port_count );
         if ( nodes == null ) {
            nodes = new ArrayList();
            port_map.put( port_count, nodes );
         }
         nodes.add( dnode );
      }

      // lay out the nodes radially, nodes with more ports go closer to the
      // middle, nodes with no ports on the outside edge.

      // the center:
      int cx = port_map.size() * 75;
      int cy = cx;
      double r = 75;

      for ( int i = 0; i < port_map.size(); i++ ) {
         List nodes = ( List ) port_map.get( new Integer( i ) );
         if ( nodes == null )
            continue;
         double radius = r * ( double ) ( port_map.size() - i );
         double interval = ( double ) 360 / ( double ) nodes.size();
         if ( interval == 360 )
            interval = 0;
         double angle = 360 / (double)(i + 1);
         for ( int j = 0; j < nodes.size(); j++ ) {
            int x = ( int ) ( radius * Math.sin( Math.toRadians( angle ) ) ) + cx;
            int y = ( int ) ( radius * Math.cos( Math.toRadians( angle ) ) ) + cy;
            DependencyNode node = ( DependencyNode ) nodes.get( j );
            graph_model.addNode( node );
            node.makePresentation( null ).setLocation( x, y );
            angle += interval;
         }
      }


      // add the edges
      it = edges.iterator();
      while ( it.hasNext() ) {
         NetEdge edge = ( NetEdge ) it.next();
         graph_model.addEdge( edge );
         lm.sendToBack( edge.presentationFor( active_layer ) );
      }
      //graph.layoutGraph();
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
