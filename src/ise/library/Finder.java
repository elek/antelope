package ise.library;

import javax.swing.text.JTextComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.regex.*;

/**
 * Action to do a find in a text component. 
Does regular expression searching.
 * @author Dale Anson
 */
public class Finder implements ActionListener {

    private JTextComponent _to_find;
    private JTextComponent _textarea;
    private boolean _from_caret;
    private boolean _wrap;

    /**
     * Will search for <code>look_for.getText()</code> in <code>look_in</code>.
     * If found, will select the found text. 
     * @param look_for a text component that contains some text to look for. The
     * text may be any string acceptable to java.util.Matcher.
     * @param look_in the text component in which to find the look_for text.
     * @param from_caret if true, find from current caret position, otherwise,
     * from start of document.
     * @param wrap if true, wrap searching to top of document.
     */
    public Finder(JTextComponent look_for, JTextComponent look_in, boolean from_caret, boolean wrap) {
        _to_find = look_for;
        _textarea = look_in;
        _from_caret = from_caret;
        _wrap = wrap;
    }

    public void actionPerformed( ActionEvent ae ) {
        String text_to_find = _to_find.getText();
        if ( text_to_find == null || text_to_find.length() == 0 ) {
            return ;
        }
        try {
            String doc = _textarea.getDocument().getText(0, _textarea.getDocument().getLength());
            Pattern pattern = Pattern.compile( text_to_find, Pattern.DOTALL );
            Matcher matcher = pattern.matcher( doc );
            int find_from = _from_caret ? _textarea.getCaretPosition() : 0;
            if (!matcher.find(find_from) && _wrap)
                find_from = 0;
            if ( matcher.find(find_from) ) {
                int start = matcher.start();
                int end = matcher.end();
                String found = doc.substring( start, end );
                _textarea.setCaretPosition( start );
                _textarea.select( start, end );
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

