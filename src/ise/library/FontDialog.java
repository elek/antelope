// $Id$

package ise.library;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import ise.library.*;

import java.util.*;

/**
 * A font chooser dialog. Typical usage:
 * <p>
 * <code>
 * Font f = FontDialog.showFontDialog(parentFrame, defaultFont);    
 * </code>    
 *
 * Internally, this class creates a single dialog and reuses it on each call to
 * <code>showFontDialog</code>. This minimizes the number of objects created.
 *
 * @author    Dale Anson
 * @version   $Revision$
 */
public class FontDialog extends JDialog {

   /**
    * Description of the Field
    */
   public boolean didCancel = false;
   /**
    * Description of the Field
    */
   private JLabel sample;
   /**
    * Description of the Field
    */
   private String font_name = "Serif";
   /**
    * Description of the Field
    */
   private String font_size = "10";
   /**
    * Description of the Field
    */
   private boolean bold_on = false, italic_on = false;
   /**
    * Description of the Field
    */
   private JList font_list;
   /**
    * Description of the Field
    */
   private JComboBox size_list;
   /**
    * Description of the Field
    */
   private JTextField font_selection;
   /**
    * Description of the Field
    */
   private JTextField size_selection;

   /**
    * Description of the Field
    */
   private static JFrame _parent = null;
   /**
    * Description of the Field
    */
   private static FontDialog _fd = null;

   /**
    * Constructor for FontDialog
    *
    * @param parent
    */
   public FontDialog( JFrame parent ) {
      this( parent, null );
   }

   /**
    *Constructor for FontDialog
    *
    * @param parent
    * @param font
    */
   public FontDialog( JFrame parent, Font font ) {
      super( parent, "Select Font", true );
      createUI( font );
   }

   /**
    * Description of the Method
    *
    * @param font
    */
   private void createUI( Font font ) {

      // main panel
      JPanel content_pane = new JPanel();
      content_pane.setLayout( new KappaLayout() );
      content_pane.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

      ListSelectionListener lsl =
         new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e ) {
               if ( e.getSource().equals( font_list ) ) {
                  font_name = font_list.getSelectedValue().toString();
                  font_selection.setText( font_name );
               }
               setSample();
            }
         };

      ActionListener al =
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               font_size = size_list.getSelectedItem().toString();
               setSample();
            }
         };

      // font name selection panel
      JPanel font_panel = new JPanel();
      font_panel.setLayout( new BorderLayout() );
      font_panel.setBorder( new TitledBorder( "Font:" ) );
      font_selection = new JTextField( font_name );
      font_selection.setEditable( false );
      font_panel.add( font_selection, BorderLayout.NORTH );
      String[] font_names = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      font_list = new JList( font_names );
      font_list.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
      font_list.setSelectedIndex( 0 );
      font_list.addListSelectionListener( lsl );
      font_panel.add( new JScrollPane( font_list ), BorderLayout.CENTER );

      // font size selection panel
      JPanel size_panel = new JPanel();
      size_panel.setLayout( new BorderLayout() );
      size_panel.setBorder( new TitledBorder( "Size:" ) );
      String[] size_names = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "38", "48", "72"};
      size_list = new JComboBox( size_names );
      size_list.setEditable( true );
      size_list.addActionListener( al );
      size_list.setSelectedIndex( 2 );
      size_panel.add( new JScrollPane( size_list ), BorderLayout.CENTER );

      // font style selection panel
      JPanel style_panel = new JPanel();
      style_panel.setLayout( new KappaLayout() );
      style_panel.setBorder( new TitledBorder( "Style:" ) );
      final JCheckBox bold_style = new JCheckBox( "Bold" );
      bold_style.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               bold_on = bold_style.isSelected();
               setSample();
            }
         }
      );
      final JCheckBox italic_style = new JCheckBox( "Italic" );
      italic_style.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               italic_on = italic_style.isSelected();
               setSample();
            }
         }
      );
      style_panel.add( bold_style, "0, 0, 1, 1, W, wh, 5" );
      style_panel.add( italic_style, "0, 1, 1, 1, W, wh, 5" );

      // font sample panel
      JPanel sample_panel = new JPanel();
      sample_panel.setLayout( new BorderLayout() );
      sample_panel.setBorder( new TitledBorder( "Sample" ) );
      sample = new JLabel( "AaBbYyZz", SwingConstants.CENTER );
      sample_panel.add( sample, BorderLayout.CENTER );
      setSample();

      // button panel
      KappaLayout layout = new KappaLayout();
      JPanel btn_panel = new JPanel( layout );
      JButton ok_btn = new JButton( "Ok" );
      JButton cancel_btn = new JButton( "Cancel" );
      ok_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               didCancel = false;
               setVisible( false );
            }
         }
      );
      cancel_btn.addActionListener(
         new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
               didCancel = true;
               setVisible( false );
            }
         }
      );
      btn_panel.add( "0,0,1,1,0,w", ok_btn );
      btn_panel.add( "1,0", KappaLayout.createHorizontalStrut( 5, true ) );
      btn_panel.add( "2,0,1,1,0,w", cancel_btn );
      layout.makeColumnsSameWidth( 0, 2 );

      // layout main panel
      content_pane.add( "0, 0, 2, 3, N, wh", font_panel );
      content_pane.add( "2, 0, 1, 1", KappaLayout.createHorizontalStrut( 5, true ) );
      content_pane.add( "3, 0, 2, 1, N, w", size_panel );
      content_pane.add( "3, 1, 2, 2, S, wh", style_panel );
      content_pane.add( "0, 3, 1, 1", KappaLayout.createVerticalStrut( 5, true ) );
      content_pane.add( "0, 4, 5, 1, 0, wh", sample_panel );
      content_pane.add( "5, 4, 1, 1", KappaLayout.createVerticalStrut( 72 ) );
      content_pane.add( "0, 5, 1, 1", KappaLayout.createVerticalStrut( 11 ) );
      content_pane.add( "0, 6, 5, 1", btn_panel );

      // set defaults if possible
      //System.out.println("FontDialog, font = " + font);
      if ( font != null ) {
         String font_family = font.getFamily();
         int font_style = font.getStyle();
         int font_size = font.getSize();

         font_list.setSelectedValue( font_family, true );
         size_list.setSelectedItem( String.valueOf( font_size ) );

         switch ( font_style ) {
            case Font.BOLD:
               bold_style.setSelected( true );
               break;
            case Font.ITALIC:
               italic_style.setSelected( true );
               break;
            case Font.BOLD + Font.ITALIC:
               bold_style.setSelected( true );
               italic_style.setSelected( true );
               break;
         }
      }

      setContentPane( content_pane );
      pack();
   }

   /**
    * Sets the sample text to use the selected font characteristics.
    */
   private void setSample() {
      SwingUtilities.invokeLater(
         new Runnable() {
            public void run() {
               try {
                  int style = Font.PLAIN;
                  if ( bold_on )
                     style += Font.BOLD;
                  if ( italic_on )
                     style += Font.ITALIC;
                  int size = Integer.valueOf( font_size ).intValue();
                  sample.setFont( new Font( font_name, style, size ) );
               }
            catch ( Exception e ) {}
            }
         }
      );
   }

   /**
    * @return   the currently selected font
    */
   public Font getSelectedFont() {
      return sample.getFont();
   }

   /**
    * For static <code>showFontDialog</code>.
    *
    * @return   true if user selected "Cancel" button.
    */
   public boolean didCancel() {
      return didCancel;
   }

   /**
    * Convenience method to show a FontDialog.   
    * @return the selected font or <code>null</code> if the user cancelled.
    */
   public static Font showFontDialog() {
      return showFontDialog( null );
   }

   /**
    * Convenience method to show a FontDialog.
    *
    * @param parent The parent JFrame for the dialog
    * @return the selected font or <code>null</code> if the user cancelled.
    */
   public static Font showFontDialog( JFrame parent ) {
      return showFontDialog( parent, null );
   }

   /**
    * Convenience method to show a FontDialog.
    *
    * @param parent  the parent frame
    * @param font    the default font
    * @return        the selected font or <code>null</code> if the user cancelled.
    */
   public static Font showFontDialog( JFrame parent, Font default_font ) {
      if ( parent == null || !parent.equals( _parent ) || _fd == null ) {
         _fd = new FontDialog( parent, default_font );
      }
      _parent = parent;
      GUIUtils.center( parent, _fd );
      _fd.pack();
      _fd.show();
      Font font = _fd.getSelectedFont();
      _fd.dispose();
      if ( !_fd.didCancel() )
         return font;
      return null;
   }

   // for testing
   /**
    * The main program for the FontDialog class
    *
    * @param args  The command line arguments
    */
   public static void main( String[] args ) {
      JFrame f = new JFrame();
      FontDialog fd = new FontDialog( f );
      fd.show();
      System.out.println( fd.getSelectedFont() );
      System.exit( 0 );
   }
}

