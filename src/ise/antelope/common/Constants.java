// $Id$
/*
* Based on the Apache Software License, Version 1.1
*
* Copyright (c) 2002 Dale Anson.  All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution, if
*    any, must include the following acknowlegement:
*       "This product includes software developed by Dale Anson,
*        danson@users.sourceforge.net."
*    Alternately, this acknowlegement may appear in the software itself,
*    if and wherever such third-party acknowlegements normally appear.
*
* 4. The name "Antelope" must not be used to endorse or promote products derived
*    from this software without prior written permission. For written
*    permission, please contact danson@users.sourceforge.net.
*
* 5. Products derived from this software may not be called "Antelope"
*    nor may "Antelope" appear in their names without prior written
*    permission of Dale Anson.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL DALE ANSON OR ANY PROJECT
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*/

package ise.antelope.common;

import java.util.prefs.Preferences;

/**
 * A set of constant values for accessing stored properties. The root of the properties
 * for Antelope is "/ise/antelope". The last open build file is stored there. In 
 * addition, a node per build file ever opened is stored. The name of the node is the
 * string value of the hashCode for the build file File object. Within these nodes are
 * stored the individual settings per build file.
 */
public interface Constants {
   
   public final static String NL = System.getProperty("line.separator");
   
   // preferences node
   public final static String PREFS_NODE = "/ise/antelope";
   public static Preferences PREFS = ise.library.UserPreferencesFactory.getUserRoot().node(PREFS_NODE);
   
   // key for last build file open by Antelope
   public final static String LAST_OPEN_FILE = "lastOpenFile";
   
   // key for ANT_HOME
   public final static String ANT_HOME = "ANT_HOME";
   
   // recent file list settings --
   // name of the recent list key
   public final static String RECENT_LIST = "recent_list";
   
   // maximum number of items to be stored in the recent list
   public final static int MAX_RECENT_SIZE = 10;
   
   // an event ID sent when the recent list has changed
   public final static int RECENT_LIST_CHANGED = 810217;
   
   // the font and style settings for Antelope as a stand-alone --
   public final static String FONT_FAMILY = "fontFamily";
   public final static String FONT_STYLE = "fontStyle";
   public final static String FONT_SIZE = "fontSize";
   
   // style keys, append COLOR, BOLD, or ITALIC
   public final static String COMMENT1 = "comment1";
   public final static String COMMENT2 = "comment2";
   public final static String KEYWORD1 = "keyword1";
   public final static String KEYWORD2 = "keyword2";
   public final static String KEYWORD3 = "keyword3";
   public final static String LITERAL1 = "literal1";
   public final static String LITERAL2 = "literal2";
   public final static String LABEL    = "label";
   public final static String OPERATOR = "operator";
   public final static String INVALID  = "invalid";
   
   public final static String COLOR = ".color";
   public final static String BOLD = ".bold";
   public final static String ITALIC = ".italic";
   
   
   // other text area option keys
   public final static String SHOW_EOL_MARKER        = "show_eol_marker";
   public final static String EOL_MARKER_COLOR       = "eol_marker_color";
   public final static String TAB_SIZE               = "tab_size";
   public final static String SHOW_BRACKET_HILITE    = "show_bracket_hilite";
   public final static String BRACKET_HILITE_COLOR   = "bracket_hilite_color";
   public final static String SHOW_LINE_HILITE       = "show_line_hilite";
   public final static String LINE_HILITE_COLOR      = "line_hilite_color";
   public final static String USE_BLOCK_CARET        = "use_block_caret";
   public final static String CARET_COLOR            = "caret_color";
   public final static String CARET_BLINKS           = "caret_blinks";
   public final static String ELECTRIC_SCROLL_HEIGHT = "electric_scroll_height";
   public final static String SMART_HOME             = "smart_home";
   public final static String SELECTION_COLOR        = "selection_color";
                                                       
  
   // keys for individual option settings --
   public final static String SAVE_BEFORE_RUN      = "saveBeforeRun";
   public final static String SHOW_ALL_TARGETS     = "showAllTargets";
   public final static String SHOW_TARGETS_WO_DESC = "showTargetsWODesc";
   public final static String SHOW_TARGETS_W_DOTS  = "showTargetsWDots";
   public final static String SHOW_TARGETS_W_DASH  = "showTargetsWDash";
   public final static String SORT_TARGETS         = "sortTargets";
   public final static String MSG_LEVEL            = "msgLevel";
   public final static String SHOW_BUILD_EVENTS    = "showBuildEvents";
   public final static String SHOW_TARGET_EVENTS   = "showTargetEvents";
   public final static String SHOW_TASK_EVENTS     = "showTaskEvents";
   public final static String SHOW_LOG_MSGS        = "showLogMsgs";
   public final static String USE_ERROR_PARSING    = "useErrorParsing";
   public final static String SHOW_PERFORMANCE_OUTPUT = "showPerformanceOutput";
   public final static String AUTO_RELOAD          = "autoReload";
   public final static String SHOW_BUTTON_TEXT     = "showButtonText";
   public final static String SHOW_BUTTON_ICON     = "showButtonIcon";
   public final static String MULTIPLE_TARGETS     = "multipleTargets";
   public final static String TARGET_LIST          = "targetList";
   
   // node name for Ant user properties
   public final static String ANT_USER_PROPS = "ant_user_props";
}
