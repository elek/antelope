package ise.antelope.common.dependency;

import org.tigris.gef.base.*;
import org.tigris.gef.demo.*;
import org.tigris.gef.graph.*;
import org.tigris.gef.graph.presentation.*;
import org.tigris.gef.presentation.*;


public class DependencyPort extends NetPort {

   public DependencyPort( NetNode parent ) {
      super( parent );
   }

   protected Class defaultEdgeClass( NetPort otherPort ) {
      try {
         return Class.forName( "ise.antelope.common.DependencyEdge" );
      }
      catch ( java.lang.ClassNotFoundException ignore ) {
         return null;
      }
   }
   public boolean canConnectTo( GraphModel gm, Object anotherPort ) {
      return true;
   }

}
