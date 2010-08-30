package ise.antelope.common;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingWorker;

public class CommonHelperWrapper implements CommonHelper {

    private CommonHelper wrapped = null;

    public CommonHelperWrapper(Object ch) {
        wrapped = (CommonHelper)ch;
    }


   /**
    * AntelopePanel will pass the target execution thread to the helper.
    * Implementors of this interface may interrupt the thread to cause
    * AntelopePanel to stop running a target.
    *
    * @param thread  the execution thread
    */
    public void setTargetExecutionThread( SwingWorker thread ) {
        wrapped.setTargetExecutionThread(thread);
    }

   /**
    * Check if the helper can save before running a target.
    *
    * @return   true if the helper can save files.
    */
    public boolean canSaveBeforeRun() {
        return wrapped.canSaveBeforeRun();
    }

   /**
    * Tell the helper to save now.
    */
    public void saveBeforeRun() {
        wrapped.saveBeforeRun();
    }

   /**
    * Tell the helper to clear its error source. This was implemented to
    * support the ErrorList plugin for jEdit, other editors may hava a similar
    * need.
    */
    public void clearErrorSource() {
        wrapped.clearErrorSource();
    }

   /**
    * Should the AntelopePanel show its Edit button? Clicking the Edit button
    * should cause AntelopePanel to show the build file in an editor.
    *
    * @return  true if the AntelopePanel should show an edit button.
    */
    public boolean canShowEditButton() {
        return wrapped.canShowEditButton();
    }

   /**
    * An action that the helper would like to have happen when the
    * Edit button is clicked.
    *
    * @return   The edit button action
    */
    public ActionListener getEditButtonAction() {
        return wrapped.getEditButtonAction();
    }


   /**
    * The action that the helper would like to have happen when the
    * Run button is clicked.
    *
    * @return   The run button action
    */
    public ActionListener getRunButtonAction() {
        return wrapped.getRunButtonAction();
    }

   /**
    * The action that the helper would like to have happen when the
    * Trace button is clicked.
    *
    * @return   The trace button action
    */
    public ActionListener getTraceButtonAction() {
        return wrapped.getTraceButtonAction();
    }

   /**
    * Opens the given file in an editor.
    * @param the file to open.
    */
    public void openFile( java.io.File f ) {
        wrapped.openFile(f);
    }

   /**
    * Generally, the classloader returned by the helper will probably be null,
    * but some apps, like jEdit, use special classloaders. As AntProject needs
    * direct access to the classloader that loads Ant, the helper should pass
    * the classloader via this method.
    *
    * @return   The classloader that loaded Ant.
    */
    public ClassLoader getAntClassLoader() {
        return wrapped.getAntClassLoader();
    }

   /**
    * The Ant installation that the helper uses may not be in the application classpath.
    * AntelopePanel needs to know where the Ant jars are located so it can run Ant
    * properly. Implementers may return null, meaning that the Ant jars are already
    * in the classpath.
    * <p>
    * <strong>WARNING:</strong> this method is likely to change. The helper should
    * not need to provide a list of jars, rather, it should provide an ANT_HOME
    * directory. Antelope should be smart enough to find the jars given the
    * directory, plus should automatically look in the standard Ant library
    * locations.
    * @return a list of the jars used by Ant. The individual list items must be Strings
    * representing the file names of the jar files. Note that other jars may be included,
    * such as custom Ant task libraries.
    */
    public java.util.List getAntJarList() {
        return wrapped.getAntJarList();
    }

    public void actionPerformed(ActionEvent ae) {
        wrapped.actionPerformed(ae);
    }

}
