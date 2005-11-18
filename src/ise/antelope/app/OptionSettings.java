package ise.antelope.app;

import ise.antelope.app.jedit.JEditTextArea;
import ise.antelope.app.jedit.TextAreaPainter;
import ise.antelope.app.jedit.InputHandler;
import ise.antelope.common.Constants;
import java.awt.Color;

/**
 * Bad, Dale, bad. You have another class named OptionSettings in the 
 * ise.antelope.common package.
 */
public class OptionSettings {

   private boolean caretBlinks = true;
   private Color caretColor = Color.black;
   private int electricScroll = 3;
   private int tabSize = 4;
   private boolean useSmartHome = false;
   private Color selectionColor = new Color( 0xccccff ) ;
   private Color lineHighlightColor = new Color( 0xe0e0e0 ) ;
   private boolean showLineHighlight = true;
   private Color bracketHighlightColor = Color.black;
   private boolean showBracketHighlight = false;
   private Color eolMarkerColor = new Color( 0x009999 ) ;
   private boolean showEolMarker = false;
   private boolean useNativeLF = false;

   public boolean getCaretBlinks() {
      return caretBlinks;
   }

   public void setCaretBlinks( boolean b ) {
      caretBlinks = b;
   }

   public Color getCaretColor() {
      return caretColor;
   }

   public void setCaretColor( Color c ) {
      caretColor = c;
   }

   public int getElectricScroll() {
      return electricScroll;
   }

   public void setElectricScroll( int i ) {
      electricScroll = i;
   }

   public int getTabSize() {
      return tabSize;
   }

   public void setTabSize( int i ) {
      tabSize = i;
   }

   public boolean useSmartHome() {
      return useSmartHome;
   }

   public void setUseSmartHome( boolean b ) {
      useSmartHome = b;
   }

   public Color getSelectionColor() {
      return selectionColor;
   }

   public void setSelectionColor( Color c ) {
      selectionColor = c;
   }

   public Color getLineHighlightColor() {
      return lineHighlightColor;
   }

   public void setLineHighlightColor( Color c ) {
      lineHighlightColor = c;
   }

   public boolean showLineHighlight() {
      return showLineHighlight;
   }

   public void setShowLineHighlight( boolean b ) {
      showLineHighlight = b;
   }

   public Color getBracketHighlightColor() {
      return bracketHighlightColor;
   }

   public void setBracketHighlightColor( Color c ) {
      bracketHighlightColor = c;
   }

   public boolean showBracketHighlight() {
      return showBracketHighlight;
   }

   public void setShowBracketHighlight( boolean b ) {
      showBracketHighlight = b;
   }

   public Color getEolMarkerColor() {
      return eolMarkerColor;
   }

   public void setEolMarkerColor( Color c ) {
      eolMarkerColor = c;
   }

   public boolean showEolMarker() {
      return showEolMarker;
   }

   public void setShowEolMarker( boolean b ) {
      showEolMarker = b;
   }
   
   public void setUseNativeLookAndFeel(boolean b) {
        useNativeLF = b;   
   }
   
   public boolean getUseNativeLookAndFeel() {
        return useNativeLF;   
   }

   /**
    * load default settings from preferences    
    */
   public void load() {
      caretBlinks = Constants.PREFS.getBoolean( Constants.CARET_BLINKS, true );
      caretColor = new Color( Constants.PREFS.getInt( Constants.CARET_COLOR, 0 ) );

      electricScroll = Constants.PREFS.getInt( Constants.ELECTRIC_SCROLL_HEIGHT, 3 );
      useSmartHome = Constants.PREFS.getBoolean( Constants.SMART_HOME, false );
      tabSize = Constants.PREFS.getInt( Constants.TAB_SIZE, 4 );

      selectionColor = new Color( Constants.PREFS.getInt( Constants.SELECTION_COLOR, 0xccccff ) );

      lineHighlightColor = new Color( Constants.PREFS.getInt( Constants.LINE_HILITE_COLOR, 0xe0e0e0 ) );
      showLineHighlight = Constants.PREFS.getBoolean( Constants.SHOW_LINE_HILITE, true );

      bracketHighlightColor = new Color( Constants.PREFS.getInt( Constants.BRACKET_HILITE_COLOR, 0 ) );
      showBracketHighlight = Constants.PREFS.getBoolean( Constants.SHOW_BRACKET_HILITE, false );

      eolMarkerColor = new Color( Constants.PREFS.getInt( Constants.EOL_MARKER_COLOR, 0x009999 ) );
      showEolMarker = Constants.PREFS.getBoolean( Constants.SHOW_EOL_MARKER, false );
      
      useNativeLF = Constants.PREFS.getBoolean(Constants.USE_NATIVE_LF, false);
   }

   /**
    * save default settings to preferences    
    */
   public void save() {
      Constants.PREFS.putBoolean( Constants.CARET_BLINKS, getCaretBlinks() );
      Constants.PREFS.putInt( Constants.CARET_COLOR, getCaretColor().getRGB() );

      Constants.PREFS.putInt( Constants.ELECTRIC_SCROLL_HEIGHT, getElectricScroll() );
      Constants.PREFS.putBoolean( Constants.SMART_HOME, useSmartHome() );
      Constants.PREFS.putInt( Constants.TAB_SIZE, tabSize );

      Constants.PREFS.putInt( Constants.SELECTION_COLOR, getSelectionColor().getRGB() );

      Constants.PREFS.putInt( Constants.LINE_HILITE_COLOR, getLineHighlightColor().getRGB() );
      Constants.PREFS.putBoolean( Constants.SHOW_LINE_HILITE, showLineHighlight() );

      Constants.PREFS.putInt( Constants.BRACKET_HILITE_COLOR, getBracketHighlightColor().getRGB() );
      Constants.PREFS.putBoolean( Constants.SHOW_BRACKET_HILITE, showBracketHighlight() );

      Constants.PREFS.putInt( Constants.EOL_MARKER_COLOR, getEolMarkerColor().getRGB() );
      Constants.PREFS.putBoolean( Constants.SHOW_EOL_MARKER, showEolMarker() );
      
      Constants.PREFS.putBoolean( Constants.USE_NATIVE_LF, getUseNativeLookAndFeel());
   }

   /**
    * Apply the current settings to the give editor.    
    */
   public void apply( JEditTextArea editor ) {
      TextAreaPainter tap = editor.getPainter();
      tap.setCaretColor( getCaretColor() );
      editor.setElectricScroll( getElectricScroll() );
      editor.getDocument().putProperty( javax.swing.text.PlainDocument.tabSizeAttribute, new Integer( tabSize ) );
      editor.putClientProperty( InputHandler.SMART_HOME_END_PROPERTY, new Boolean( useSmartHome() ) );
      tap.setSelectionColor( getSelectionColor() );
      tap.setLineHighlightColor( getLineHighlightColor() );
      tap.setLineHighlightEnabled( showLineHighlight() );
      tap.setBracketHighlightEnabled( showBracketHighlight() );
      tap.setBracketHighlightColor( getBracketHighlightColor() );
      tap.setEOLMarkersPainted( showEolMarker() );
      tap.setEOLMarkerColor( getEolMarkerColor() );
   }
}
