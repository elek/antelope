
package ise.antelope.common;

import java.awt.event.ActionListener;

/**
 * Objects that want to manipulate AntelopePanel must implement this
 * interface.
 *
 * @version   $Revision$
 */
public interface CommonHelper extends ActionListener {

   /**
    * Event ID for trace event.
    */
   public final static int TRACE_EVENT = 550927;
   /**
    * Event ID for edit event.
    */
   public final static int EDIT_EVENT = 470226;

   /**
    * AntelopePanel will pass the target execution thread to the helper.
    *
    * @param thread  the execution thread
    */
   public void setTargetExecutionThread( Thread thread );

   /**
    * Check if the helper can save before running a target.
    *
    * @return   true if the helper can save files.
    */
   public boolean canSaveBeforeRun();

   /**
    * Tell the helper to save now.
    */
   public void saveBeforeRun();

   /**
    * Tell the helper to clear its error source.
    */
   public void clearErrorSource();

   /**
    * Should the AntelopePanel show an edit button?
    *
    * @return  true if the AntelopePanel should show an edit button.
    */
   public boolean canShowEditButton();

   /**
    * An action that the helper would like to have happen when the
    * Edit button is clicked.
    *
    * @return   The edit button action
    */
   public ActionListener getEditButtonAction();


   /**
    * The action that the helper would like to have happen when the
    * Run button is clicked.
    *
    * @return   The run button action
    */
   public ActionListener getRunButtonAction();

   /**
    * Opens the given file in an editor.
    * @param the file to open.
    */
   public void openFile( java.io.File f );

   /**
    * Generally, the classloader returned by the helper should be null, but
    * some apps, like jEdit, use special classloaders. As AntProject needs
    * direct access to the classloader that loads Ant, the helper should pass
    * the classloader via this method.
    *
    * @return   The classloader that loaded Ant.
    */
   public ClassLoader getAntClassLoader();

   public String getAntJarLocation();

   public java.util.List getAntJarList();

   public void updateGUI();
}

