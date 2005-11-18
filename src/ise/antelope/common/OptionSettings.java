package ise.antelope.common;

import java.io.File;
import java.util.prefs.Preferences;
import java.util.*;
import org.apache.tools.ant.Project;

/**
 * Loads options per build file and general application settings.
 */
public class OptionSettings {

   private Preferences _prefs;
   private boolean _save_before_run = true;
   private boolean _show_all_targets = true;
   private boolean _show_targets_wo_desc = false;
   private boolean _show_targets_w_dot = false;
   private boolean _show_targets_w_dash = false;
   private boolean _sort_targets = true;
   private int _message_level = Project.MSG_INFO;
   private boolean _show_build_events = true;
   private boolean _show_target_events = false;
   private boolean _show_task_events = false;
   private boolean _show_log_messages = true;
   private boolean _use_error_parsing = true;
   private boolean _show_performance_output = false;
   private boolean _auto_reload = true;
   private boolean _show_button_text = false;
   private boolean _show_button_icon = true;
   private boolean _multiple_targets = false;
   private String _target_list = "";


   /**
    * Loads the option settings for the given build file from the persistent
    * storage for settings. 
    */
   public OptionSettings( File build_file ) {
      load( build_file );
   }

   public void load( File build_file ) {
      // application settings
      _show_button_text = Constants.PREFS.getBoolean( Constants.SHOW_BUTTON_TEXT, false );
      _show_button_icon = Constants.PREFS.getBoolean( Constants.SHOW_BUTTON_ICON, true );

      // per build file settings
      if ( build_file != null ) {
         int hashCode = build_file.hashCode();
         _prefs = Constants.PREFS.node( String.valueOf( hashCode ) );
         _save_before_run = _prefs.getBoolean( Constants.SAVE_BEFORE_RUN, true );
         _show_all_targets = _prefs.getBoolean( Constants.SHOW_ALL_TARGETS, true );
         _show_targets_wo_desc = _prefs.getBoolean( Constants.SHOW_TARGETS_WO_DESC, false );
         _show_targets_w_dot = _prefs.getBoolean( Constants.SHOW_TARGETS_W_DOTS, false );
         _show_targets_w_dash = _prefs.getBoolean( Constants.SHOW_TARGETS_W_DASH, false );
         _sort_targets = _prefs.getBoolean(Constants.SORT_TARGETS, true);
         _message_level = _prefs.getInt( Constants.MSG_LEVEL, Project.MSG_INFO );
         _show_build_events = _prefs.getBoolean( Constants.SHOW_BUILD_EVENTS, true );
         _show_target_events = _prefs.getBoolean( Constants.SHOW_TARGET_EVENTS, false );
         _show_task_events = _prefs.getBoolean( Constants.SHOW_TASK_EVENTS, false );
         _show_log_messages = _prefs.getBoolean( Constants.SHOW_LOG_MSGS, true );
         _use_error_parsing = _prefs.getBoolean( Constants.USE_ERROR_PARSING, true );
         _show_performance_output = _prefs.getBoolean( Constants.SHOW_PERFORMANCE_OUTPUT, false );
         _auto_reload = _prefs.getBoolean( Constants.AUTO_RELOAD, true );
         _multiple_targets = _prefs.getBoolean( Constants.MULTIPLE_TARGETS, false );
         _target_list = _prefs.get( Constants.TARGET_LIST, "" );
      }
   }

   public void save() {
      // application settings
      Constants.PREFS.putBoolean( Constants.SHOW_BUTTON_TEXT, _show_button_text );
      Constants.PREFS.putBoolean( Constants.SHOW_BUTTON_ICON, _show_button_icon );

      // per build file settings
      if ( _prefs == null )
         return ;
      _prefs.putBoolean( Constants.SAVE_BEFORE_RUN, _save_before_run );
      _prefs.putInt( Constants.MSG_LEVEL, _message_level );
      _prefs.putBoolean( Constants.SHOW_BUILD_EVENTS, _show_build_events );
      _prefs.putBoolean( Constants.SHOW_LOG_MSGS, _show_log_messages );
      _prefs.putBoolean( Constants.SHOW_PERFORMANCE_OUTPUT, _show_performance_output );
      _prefs.putBoolean( Constants.SHOW_TARGET_EVENTS, _show_target_events );
      _prefs.putBoolean( Constants.SHOW_ALL_TARGETS, _show_all_targets );
      _prefs.putBoolean( Constants.SHOW_TARGETS_W_DASH, _show_targets_w_dash );
      _prefs.putBoolean( Constants.SHOW_TARGETS_W_DOTS, _show_targets_w_dot );
      _prefs.putBoolean( Constants.SHOW_TARGETS_WO_DESC, _show_targets_wo_desc );
      _prefs.putBoolean( Constants.SORT_TARGETS, _sort_targets);
      _prefs.putBoolean( Constants.SHOW_TASK_EVENTS, _show_task_events );
      _prefs.putBoolean( Constants.USE_ERROR_PARSING, _use_error_parsing );
      _prefs.putBoolean( Constants.AUTO_RELOAD, _auto_reload );
      _prefs.putBoolean( Constants.MULTIPLE_TARGETS, _multiple_targets );
      _prefs.put( Constants.TARGET_LIST, _target_list );
      
   }

   /**
    * Gets the preferences for the current build file.
    *
    * @return   the preferences for the current build file
    */
   public Preferences getPrefs() {
      return _prefs;
   }

   public void setSaveBeforeRun( boolean b ) {
      _save_before_run = b;
   }

   public boolean getSaveBeforeRun() {
      return _save_before_run;
   }

   public void setUseErrorParsing( boolean b ) {
      _use_error_parsing = b;
   }

   public void setShowPerformanceOutput( boolean b ) {
      _show_performance_output = b;
   }

   public boolean getUseErrorParsing() {
      return _use_error_parsing;
   }

   public boolean getShowPerformanceOutput() {
      return _show_performance_output;
   }

   public boolean getAutoReload() {
      return _auto_reload;
   }

   public void setAutoReload( boolean b ) {
      _auto_reload = b;
   }

   public void setShowAllTargets( boolean b ) {
      _show_all_targets = b;
   }

   public boolean getShowAllTargets() {
      return _show_all_targets;
   }

   public void setShowTargetsWODesc( boolean b ) {
      _show_targets_wo_desc = b;
   }

   public boolean getShowTargetsWODesc() {
      return _show_targets_wo_desc;
   }

   public void setShowTargetsWDot( boolean b ) {
      _show_targets_w_dot = b;
   }

   public void setShowTargetsWDash( boolean b ) {
      _show_targets_w_dash = b;
   }
   
   public void setSortTargets(boolean b) {
      _sort_targets = b;  
   }

   public boolean getShowTargetsWDot() {
      return _show_targets_w_dot;
   }

   public boolean getShowTargetsWDash() {
      return _show_targets_w_dash;
   }
   
   public boolean getSortTargets() {
      return _sort_targets;  
   }

   public void setMessageOutputLevel( int level ) {
      _message_level = level;
   }

   public int getMessageOutputLevel() {
      return _message_level;
   }

   public void setShowBuildEvents( boolean b ) {
      _show_build_events = b;
   }

   public boolean getShowBuildEvents() {
      return _show_build_events;
   }

   public void setShowTargetEvents( boolean b ) {
      _show_target_events = b;
   }

   public boolean getShowTargetEvents() {
      return _show_target_events;
   }

   public void setShowTaskEvents( boolean b ) {
      _show_task_events = b;
   }

   public boolean getShowTaskEvents() {
      return _show_task_events;
   }

   public void setShowLogMessages( boolean b ) {
      _show_log_messages = b;
   }

   public boolean getShowLogMessages() {
      return _show_log_messages;
   }

   public void setShowButtonText( boolean b ) {
      _show_button_text = b;
   }

   public boolean getShowButtonText() {
      return _show_button_text;
   }

   public void setShowButtonIcon( boolean b ) {
      _show_button_icon = b;
   }

   public boolean getShowButtonIcon() {
      return _show_button_icon;
   }

   public void setMultipleTargets( boolean b ) {
      _multiple_targets = b;
   }

   public boolean getMultipleTargets() {
      return _multiple_targets;
   }

   /**
    * @param a list of target names as Strings.   
    */
   public void setMultipleTargetList( ArrayList list ) {
      if ( list == null ) {
         _target_list = "";
         return ;
      }
      StringBuffer sb = new StringBuffer();
      Iterator it = list.iterator();
      while ( it.hasNext() ) {
         sb.append( ( String ) it.next() );
         if ( it.hasNext() )
            sb.append( "," );
      }
      _target_list = sb.toString();
   }

   /**
    * @return a list of target names (as Strings), the returned ArrayList may be
    * empty, but will not be null.
    */
   public ArrayList getMultipleTargetList() {
      if ( _target_list == null || _target_list.equals("") )
         return new ArrayList();
      return new ArrayList( Arrays.asList( _target_list.split( "," ) ) );
   }
}
