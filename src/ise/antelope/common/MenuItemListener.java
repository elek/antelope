package ise.antelope.common;

import java.awt.event.*;

/**
 * Description of the Class
 *
 * @version   $Revision$
 */
public class MenuItemListener implements ActionListener {
   /**
    * Description of the Field
    */
   private AntelopePanel antelopePanel;

   /**
    * Description of the Field
    */
   private String filename;

   /**
    *Constructor for MenuItemListener
    *
    * @param filename
    * @param ap
    */
   public MenuItemListener( AntelopePanel ap, String filename ) {
      this.antelopePanel = ap;
      this.filename = filename;
   }

   /**
    * Description of the Method
    *
    * @param ae
    */
   public void actionPerformed( ActionEvent ae ) {
      antelopePanel.openBuildFile( filename );
   }
}

