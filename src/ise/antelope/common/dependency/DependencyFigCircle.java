package ise.antelope.common.dependency;

import java.awt.Color;
import org.tigris.gef.presentation.*;

public class DependencyFigCircle extends FigCircle {
    public DependencyFigCircle(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public DependencyFigCircle(int x, int y, int w, int h, Color lColor, Color fColor) {
        super(x, y, w, h, lColor, fColor);
    }

    public DependencyFigCircle(int x, int y, int w, int h, boolean resizable) {
        super(x, y, w, h);
        setResizable(resizable);
    }

    public DependencyFigCircle(int x, int y, int w, int h, boolean resizable, Color lColor, Color fColor) {
        super(x, y, w, h, lColor, fColor);
        setResizable(resizable);
    }
}
