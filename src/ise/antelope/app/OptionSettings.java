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
public class OptionSettings implements Constants {

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

   /**
    * load default settings from preferences    
    */
   public void load() {
      caretBlinks = PREFS.getBoolean( CARET_BLINKS, true );
      caretColor = new Color( PREFS.getInt( CARET_COLOR, 0 ) );

      electricScroll = PREFS.getInt( ELECTRIC_SCROLL_HEIGHT, 3 );
      useSmartHome = PREFS.getBoolean( SMART_HOME, false );
      tabSize = PREFS.getInt( TAB_SIZE, 4 );

      selectionColor = new Color( PREFS.getInt( SELECTION_COLOR, 0xccccff ) );

      lineHighlightColor = new Color( PREFS.getInt( LINE_HILITE_COLOR, 0xe0e0e0 ) );
      showLineHighlight = PREFS.getBoolean( SHOW_LINE_HILITE, true );

      bracketHighlightColor = new Color( PREFS.getInt( BRACKET_HILITE_COLOR, 0 ) );
      showBracketHighlight = PREFS.getBoolean( SHOW_BRACKET_HILITE, false );

      eolMarkerColor = new Color( PREFS.getInt( EOL_MARKER_COLOR, 0x009999 ) );
      showEolMarker = PREFS.getBoolean( SHOW_EOL_MARKER, false );
   }

   /**
    * save default settings to preferences    
    */
   public void save() {
      PREFS.putBoolean( CARET_BLINKS, getCaretBlinks() );
      PREFS.putInt( CARET_COLOR, getCaretColor().getRGB() );

      PREFS.putInt( ELECTRIC_SCROLL_HEIGHT, getElectricScroll() );
      PREFS.putBoolean( SMART_HOME, useSmartHome() );
      PREFS.putInt( TAB_SIZE, tabSize );

      PREFS.putInt( SELECTION_COLOR, getSelectionColor().getRGB() );

      PREFS.putInt( LINE_HILITE_COLOR, getLineHighlightColor().getRGB() );
      PREFS.putBoolean( SHOW_LINE_HILITE, showLineHighlight() );

      PREFS.putInt( BRACKET_HILITE_COLOR, getBracketHighlightColor().getRGB() );
      PREFS.putBoolean( SHOW_BRACKET_HILITE, showBracketHighlight() );

      PREFS.putInt( EOL_MARKER_COLOR, getEolMarkerColor().getRGB() );
      PREFS.putBoolean( SHOW_EOL_MARKER, showEolMarker() );
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
