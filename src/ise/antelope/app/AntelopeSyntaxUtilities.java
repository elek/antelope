package ise.antelope.app;

import java.awt.Color;

import ise.antelope.common.Constants;
import ise.antelope.app.jedit.SyntaxUtilities;
import ise.antelope.app.jedit.SyntaxStyle;
import ise.antelope.app.jedit.Token;

public class AntelopeSyntaxUtilities extends SyntaxUtilities
    implements Constants {

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
                    new Color( PREFS.getInt( COMMENT1 + COLOR, defaults[ Token.COMMENT1 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( COMMENT1 + ITALIC, defaults[ Token.COMMENT1 ].isItalic() ),
                    PREFS.getBoolean( COMMENT1 + BOLD, defaults[ Token.COMMENT1 ].isBold() ) );

        styles[ Token.COMMENT2 ] = new SyntaxStyle(
                    new Color( PREFS.getInt( COMMENT2 + COLOR, defaults[ Token.COMMENT2 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( COMMENT2 + ITALIC, defaults[ Token.COMMENT2 ].isItalic() ),
                    PREFS.getBoolean( COMMENT2 + BOLD, defaults[ Token.COMMENT2 ].isBold() ) );

        styles[ Token.KEYWORD1 ] = new SyntaxStyle(
                    new Color( PREFS.getInt( KEYWORD1 + COLOR, defaults[ Token.KEYWORD1 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( KEYWORD1 + ITALIC, defaults[ Token.KEYWORD1 ].isItalic() ),
                    PREFS.getBoolean( KEYWORD1 + BOLD, defaults[ Token.KEYWORD1 ].isBold() ) );

        styles[ Token.KEYWORD2 ] = new SyntaxStyle(
                    new Color( PREFS.getInt( KEYWORD2 + COLOR, defaults[ Token.KEYWORD2 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( KEYWORD2 + ITALIC, defaults[ Token.KEYWORD2 ].isItalic() ),
                    PREFS.getBoolean( KEYWORD2 + BOLD, defaults[ Token.KEYWORD2 ].isBold() ) );

        styles[ Token.KEYWORD3 ] = new SyntaxStyle(
                    new Color( PREFS.getInt( KEYWORD3 + COLOR, defaults[ Token.KEYWORD3 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( KEYWORD3 + ITALIC, defaults[ Token.KEYWORD3 ].isItalic() ),
                    PREFS.getBoolean( KEYWORD3 + BOLD, defaults[ Token.KEYWORD3 ].isBold() ) );

        styles[ Token.LITERAL1 ] = new SyntaxStyle(
                    new Color( PREFS.getInt( LITERAL1 + COLOR, defaults[ Token.LITERAL1 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( LITERAL1 + ITALIC, defaults[ Token.LITERAL1 ].isItalic() ),
                    PREFS.getBoolean( LITERAL1 + BOLD, defaults[ Token.LITERAL1 ].isBold() ) );

        styles[ Token.LITERAL2 ] = new SyntaxStyle(
                    new Color( PREFS.getInt( LITERAL2 + COLOR, defaults[ Token.LITERAL2 ].getColor().getRGB() ) ),
                    PREFS.getBoolean( LITERAL2 + ITALIC, defaults[ Token.LITERAL2 ].isItalic() ),
                    PREFS.getBoolean( LITERAL2 + BOLD, defaults[ Token.LITERAL2 ].isBold() ) );

        styles[ Token.LABEL ] = new SyntaxStyle(
                    new Color( PREFS.getInt( LABEL + COLOR, defaults[ Token.LABEL ].getColor().getRGB() ) ),
                    PREFS.getBoolean( LABEL + ITALIC, defaults[ Token.LABEL ].isItalic() ),
                    PREFS.getBoolean( LABEL + BOLD, defaults[ Token.LABEL ].isBold() ) );

        styles[ Token.OPERATOR ] = new SyntaxStyle(
                    new Color( PREFS.getInt( OPERATOR + COLOR, defaults[ Token.OPERATOR ].getColor().getRGB() ) ),
                    PREFS.getBoolean( OPERATOR + ITALIC, defaults[ Token.OPERATOR ].isItalic() ),
                    PREFS.getBoolean( OPERATOR + BOLD, defaults[ Token.OPERATOR ].isBold() ) );

        styles[ Token.INVALID ] = new SyntaxStyle(
                    new Color( PREFS.getInt( INVALID + COLOR, defaults[ Token.INVALID ].getColor().getRGB() ) ),
                    PREFS.getBoolean( INVALID + ITALIC, defaults[ Token.INVALID ].isItalic() ),
                    PREFS.getBoolean( INVALID + BOLD, defaults[ Token.INVALID ].isBold() ) );

        return styles;
    }

    public static void storeStyles( SyntaxStyle[] styles ) {

        PREFS.putInt( COMMENT1 + COLOR, styles[ Token.COMMENT1 ].getColor().getRGB() );
        PREFS.putBoolean( COMMENT1 + ITALIC, styles[ Token.COMMENT1 ].isItalic() );
        PREFS.putBoolean( COMMENT1 + BOLD, styles[ Token.COMMENT1 ].isBold() );

        PREFS.putInt( COMMENT2 + COLOR, styles[ Token.COMMENT2 ].getColor().getRGB() );
        PREFS.putBoolean( COMMENT2 + ITALIC, styles[ Token.COMMENT2 ].isItalic() );
        PREFS.putBoolean( COMMENT2 + BOLD, styles[ Token.COMMENT2 ].isBold() );

        PREFS.putInt( KEYWORD1 + COLOR, styles[ Token.KEYWORD1 ].getColor().getRGB() );
        PREFS.putBoolean( KEYWORD1 + ITALIC, styles[ Token.KEYWORD1 ].isItalic() );
        PREFS.putBoolean( KEYWORD1 + BOLD, styles[ Token.KEYWORD1 ].isBold() );

        PREFS.putInt( KEYWORD2 + COLOR, styles[ Token.KEYWORD2 ].getColor().getRGB() );
        PREFS.putBoolean( KEYWORD2 + ITALIC, styles[ Token.KEYWORD2 ].isItalic() );
        PREFS.putBoolean( KEYWORD2 + BOLD, styles[ Token.KEYWORD2 ].isBold() );

        PREFS.putInt( KEYWORD3 + COLOR, styles[ Token.KEYWORD3 ].getColor().getRGB() );
        PREFS.putBoolean( KEYWORD3 + ITALIC, styles[ Token.KEYWORD3 ].isItalic() );
        PREFS.putBoolean( KEYWORD3 + BOLD, styles[ Token.KEYWORD3 ].isBold() );

        PREFS.putInt( LITERAL1 + COLOR, styles[ Token.LITERAL1 ].getColor().getRGB() );
        PREFS.putBoolean( LITERAL1 + ITALIC, styles[ Token.LITERAL1 ].isItalic() );
        PREFS.putBoolean( LITERAL1 + BOLD, styles[ Token.LITERAL1 ].isBold() );

        PREFS.putInt( LITERAL2 + COLOR, styles[ Token.LITERAL2 ].getColor().getRGB() );
        PREFS.putBoolean( LITERAL2 + ITALIC, styles[ Token.LITERAL2 ].isItalic() );
        PREFS.putBoolean( LITERAL2 + BOLD, styles[ Token.LITERAL2 ].isBold() );

        PREFS.putInt( LABEL + COLOR, styles[ Token.LABEL ].getColor().getRGB() );
        PREFS.putBoolean( LABEL + ITALIC, styles[ Token.LABEL ].isItalic() );
        PREFS.putBoolean( LABEL + BOLD, styles[ Token.LABEL ].isBold() );

        PREFS.putInt( OPERATOR + COLOR, styles[ Token.OPERATOR ].getColor().getRGB() );
        PREFS.putBoolean( OPERATOR + ITALIC, styles[ Token.OPERATOR ].isItalic() );
        PREFS.putBoolean( OPERATOR + BOLD, styles[ Token.OPERATOR ].isBold() );

        PREFS.putInt( INVALID + COLOR, styles[ Token.INVALID ].getColor().getRGB() );
        PREFS.putBoolean( INVALID + ITALIC, styles[ Token.INVALID ].isItalic() );
        PREFS.putBoolean( INVALID + BOLD, styles[ Token.INVALID ].isBold() );
    }
}
