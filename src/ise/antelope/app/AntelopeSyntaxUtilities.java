package ise.antelope.app;

import java.awt.Color;

import ise.antelope.common.Constants;
import ise.antelope.app.jedit.SyntaxUtilities;
import ise.antelope.app.jedit.SyntaxStyle;
import ise.antelope.app.jedit.Token;

public class AntelopeSyntaxUtilities extends SyntaxUtilities {

    public static SyntaxStyle getDefaultSyntaxStyle( int id ) {
        SyntaxStyle[] defaults = getDefaultSyntaxStyles();
        return defaults[ id ];
    }

    public static SyntaxStyle getSyntaxStyle( int id ) {
        SyntaxStyle[] styles = getStoredStyles();
        return styles[ id ];
    }
    
    public static int getStyleId(SyntaxStyle ss) {
        SyntaxStyle[] styles = getStoredStyles();
        for (int i = 0; i < styles.length; i++) {
            if (ss.equals(styles[i]))
                return i;
        }
        styles = getDefaultSyntaxStyles();
        for (int i = 0; i < styles.length; i++) {
            if (ss.equals(styles[i]))
                return i;
        }
        return -1;
    }

    public static SyntaxStyle[] getStoredStyles() {
        SyntaxStyle[] defaults = getDefaultSyntaxStyles();
        SyntaxStyle[] styles = new SyntaxStyle[ Token.ID_COUNT ];

        styles[ Token.COMMENT1 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.COMMENT1 + Constants.COLOR, defaults[ Token.COMMENT1 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.COMMENT1 + Constants.ITALIC, defaults[ Token.COMMENT1 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.COMMENT1 + Constants.BOLD, defaults[ Token.COMMENT1 ].isBold() ) );

        styles[ Token.COMMENT2 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.COMMENT2 + Constants.COLOR, defaults[ Token.COMMENT2 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.COMMENT2 + Constants.ITALIC, defaults[ Token.COMMENT2 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.COMMENT2 + Constants.BOLD, defaults[ Token.COMMENT2 ].isBold() ) );

        styles[ Token.KEYWORD1 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.KEYWORD1 + Constants.COLOR, defaults[ Token.KEYWORD1 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.KEYWORD1 + Constants.ITALIC, defaults[ Token.KEYWORD1 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.KEYWORD1 + Constants.BOLD, defaults[ Token.KEYWORD1 ].isBold() ) );

        styles[ Token.KEYWORD2 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.KEYWORD2 + Constants.COLOR, defaults[ Token.KEYWORD2 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.KEYWORD2 + Constants.ITALIC, defaults[ Token.KEYWORD2 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.KEYWORD2 + Constants.BOLD, defaults[ Token.KEYWORD2 ].isBold() ) );

        styles[ Token.KEYWORD3 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.KEYWORD3 + Constants.COLOR, defaults[ Token.KEYWORD3 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.KEYWORD3 + Constants.ITALIC, defaults[ Token.KEYWORD3 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.KEYWORD3 + Constants.BOLD, defaults[ Token.KEYWORD3 ].isBold() ) );

        styles[ Token.LITERAL1 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.LITERAL1 + Constants.COLOR, defaults[ Token.LITERAL1 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.LITERAL1 + Constants.ITALIC, defaults[ Token.LITERAL1 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.LITERAL1 + Constants.BOLD, defaults[ Token.LITERAL1 ].isBold() ) );

        styles[ Token.LITERAL2 ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.LITERAL2 + Constants.COLOR, defaults[ Token.LITERAL2 ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.LITERAL2 + Constants.ITALIC, defaults[ Token.LITERAL2 ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.LITERAL2 + Constants.BOLD, defaults[ Token.LITERAL2 ].isBold() ) );

        styles[ Token.LABEL ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.LABEL + Constants.COLOR, defaults[ Token.LABEL ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.LABEL + Constants.ITALIC, defaults[ Token.LABEL ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.LABEL + Constants.BOLD, defaults[ Token.LABEL ].isBold() ) );

        styles[ Token.OPERATOR ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.OPERATOR + Constants.COLOR, defaults[ Token.OPERATOR ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.OPERATOR + Constants.ITALIC, defaults[ Token.OPERATOR ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.OPERATOR + Constants.BOLD, defaults[ Token.OPERATOR ].isBold() ) );

        styles[ Token.INVALID ] = new SyntaxStyle(
                    new Color( Constants.PREFS.getInt( Constants.INVALID + Constants.COLOR, defaults[ Token.INVALID ].getColor().getRGB() ) ),
                    Constants.PREFS.getBoolean( Constants.INVALID + Constants.ITALIC, defaults[ Token.INVALID ].isItalic() ),
                    Constants.PREFS.getBoolean( Constants.INVALID + Constants.BOLD, defaults[ Token.INVALID ].isBold() ) );

        return styles;
    }

    public static void storeStyles( SyntaxStyle[] styles ) {

        Constants.PREFS.putInt( Constants.COMMENT1 + Constants.COLOR, styles[ Token.COMMENT1 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.COMMENT1 + Constants.ITALIC, styles[ Token.COMMENT1 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.COMMENT1 + Constants.BOLD, styles[ Token.COMMENT1 ].isBold() );

        Constants.PREFS.putInt( Constants.COMMENT2 + Constants.COLOR, styles[ Token.COMMENT2 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.COMMENT2 + Constants.ITALIC, styles[ Token.COMMENT2 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.COMMENT2 + Constants.BOLD, styles[ Token.COMMENT2 ].isBold() );

        Constants.PREFS.putInt( Constants.KEYWORD1 + Constants.COLOR, styles[ Token.KEYWORD1 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.KEYWORD1 + Constants.ITALIC, styles[ Token.KEYWORD1 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.KEYWORD1 + Constants.BOLD, styles[ Token.KEYWORD1 ].isBold() );

        Constants.PREFS.putInt( Constants.KEYWORD2 + Constants.COLOR, styles[ Token.KEYWORD2 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.KEYWORD2 + Constants.ITALIC, styles[ Token.KEYWORD2 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.KEYWORD2 + Constants.BOLD, styles[ Token.KEYWORD2 ].isBold() );

        Constants.PREFS.putInt( Constants.KEYWORD3 + Constants.COLOR, styles[ Token.KEYWORD3 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.KEYWORD3 + Constants.ITALIC, styles[ Token.KEYWORD3 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.KEYWORD3 + Constants.BOLD, styles[ Token.KEYWORD3 ].isBold() );

        Constants.PREFS.putInt( Constants.LITERAL1 + Constants.COLOR, styles[ Token.LITERAL1 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.LITERAL1 + Constants.ITALIC, styles[ Token.LITERAL1 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.LITERAL1 + Constants.BOLD, styles[ Token.LITERAL1 ].isBold() );

        Constants.PREFS.putInt( Constants.LITERAL2 + Constants.COLOR, styles[ Token.LITERAL2 ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.LITERAL2 + Constants.ITALIC, styles[ Token.LITERAL2 ].isItalic() );
        Constants.PREFS.putBoolean( Constants.LITERAL2 + Constants.BOLD, styles[ Token.LITERAL2 ].isBold() );

        Constants.PREFS.putInt( Constants.LABEL + Constants.COLOR, styles[ Token.LABEL ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.LABEL + Constants.ITALIC, styles[ Token.LABEL ].isItalic() );
        Constants.PREFS.putBoolean( Constants.LABEL + Constants.BOLD, styles[ Token.LABEL ].isBold() );
        
        Constants.PREFS.putInt( Constants.OPERATOR + Constants.COLOR, styles[ Token.OPERATOR ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.OPERATOR + Constants.ITALIC, styles[ Token.OPERATOR ].isItalic() );
        Constants.PREFS.putBoolean( Constants.OPERATOR + Constants.BOLD, styles[ Token.OPERATOR ].isBold() );

        Constants.PREFS.putInt( Constants.INVALID + Constants.COLOR, styles[ Token.INVALID ].getColor().getRGB() );
        Constants.PREFS.putBoolean( Constants.INVALID + Constants.ITALIC, styles[ Token.INVALID ].isItalic() );
        Constants.PREFS.putBoolean( Constants.INVALID + Constants.BOLD, styles[ Token.INVALID ].isBold() );
    }
}
