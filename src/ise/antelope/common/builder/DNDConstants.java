package ise.antelope.common.builder;

import java.net.*;
import com.wutka.dtd.*;

public class DNDConstants {
   public static final String PROJECT = "project";
   public static final String TARGET = "target";
   public static final String TASK = "tasks";
   public static final String TYPE = "types";
   public static final String ELEMENT = "element";

   public static DTD ANT_DTD = null;
   static {
      try {
         URL ant_dtd = ClassLoader.getSystemResource("ise/antelope/common/builder/ant.dtd");
         if (ant_dtd == null)
            throw new Exception("ant_dtd is null");
         DTDParser parser = new DTDParser( ant_dtd );
         ANT_DTD = parser.parse();
      }
      catch ( Exception e ) {
         e.printStackTrace();
      }
   }
}
