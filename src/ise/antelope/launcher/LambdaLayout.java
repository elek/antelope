// $Id$

package ise.antelope.launcher;

import java.awt.LayoutManager2;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * LambdaLayout, a Java layout manager.<br>
 * Copyright (C) 2001, Dale Anson<br>
 *<br>
 * This library is free software; you can redistribute it and/or<br>
 * modify it under the terms of the GNU Lesser General Public<br>
 * License as published by the Free Software Foundation; either<br>
 * version 2.1 of the License, or (at your option) any later version.<br>
 *<br>
 * This library is distributed in the hope that it will be useful,<br>
 * but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU<br>
 * Lesser General Public License for more details.<br>
 *<br>
 * You should have received a copy of the GNU Lesser General Public<br>
 * License along with this library; if not, write to the Free Software<br>
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA<br>
 * <p>
 * LambdaLayout -- based on KappaLayout, but handles stretching of components
 * differently. From e-mail I've received about KappaLayout, many people are
 * expecting a different stretching behavior when resizing a Frame. LambdaLayout
 * has this expected behaviour, in that components with the 's' constraint set to
 * 'w', 'h', or 'wh'/'hw' will resize as the frame resizes.  Like KappaLayout,
 * LambdaLayout respects the preferred size of components and will not shrink
 * a component to less than it's preferred size.<br>
 * Example use:<br>
 * This will put a button on a panel in the top of its cell, stretched to
 * fill the cell width, with a 3 pixel pad:<br>
 * <code>
 * Panel p = new Panel(new LambdaLayout());
 * Button b = new Button("OK");
 * p.add(b, "0, 0, 1, 2, 2, w, 3");
 * </code>
 * <br>
 * The constraints string has this layout:<br>
 * "x, y, w, h, a, s, p"<br>
 * defined as follows:<br>
 * <ul>
 * <li>'x' is the column to put the component, default is 0<br>
 * <li>'y' is the row to put the component, default is 0<br>
 * <li>'w' is the width of the component in columns (column span), default is 1.
 * Can also be R or r, which means the component will span the remaining cells
 * in the row.<br>
 * <li>'h' is the height of the component in rows (row span), default is 1.
 * Can also be R or r, which means the component will span the remaining cells
 * in the column.<br>
 * <li>'a' is the alignment within the cell. 'a' can be a value between 0 and 8,
 * inclusive, (default is 0) and causes the alignment of the component within the cell to follow
 * this pattern:<br>
 * 8 1 2<br>
 * 7 0 3<br>
 * 6 5 4<br>, or<br>
 * 0 horizontal center, vertical center,<br>
 * 1 horizontal center, vertical top,<br>
 * 2 horizontal right, vertical top,<br>
 * 3 horizontal right, vertical center,<br>
 * 4 horizontal right, vertical bottom,<br>
 * 5 horizontal center, vertical bottom,<br>
 * 6 horizontal left, vertical bottom,<br>
 * 7 horizontal left, vertical center,<br>
 * 8 horizontal left, vertical top.<br>
 * <p>
 * By popular request, the alignment constraint can also be represented as:<br>
 * NW N NE<br>
 * &nbsp;W 0 E<br>
 * SW S SE<br>
 * which are compass directions for alignment within the cell.
 * <li>'s' is the stretch value. 's' can have these values:<br>
 * 'w' stretch to fill cell width<br>
 * 'h' stretch to fill cell height<br>
 * 'wh' or 'hw' stretch to fill both cell width and cell height<br>
 * '0' (character 'zero') no stretch (default)
 * <li>'p' is the amount of padding to put around the component. This much blank
 * space will be applied on all sides of the component, default is 0.
 * </ul>
 * Parameters may be omitted (default  values will be used), e.g.,
 * <code> p.add(new Button("OK), "1,4,,,w,");</code><br>
 * which means put the button at column 1, row 4, default 1 column wide, default
 * 1 row tall, stretch to fit width of column, no padding. <br>
 * Spaces in the parameter string are ignored, so these are identical:<br>
 * <code> p.add(new Button("OK), "1,4,,,w,");</code><br>
 * <code> p.add(new Button("OK), " 1, 4,   , , w");</code><p>
 * Rather than use a constraints string, a Constraints object may be used
 * directly, similar to how GridBag uses a GridBagConstraint. E.g,<br>
 * <code>
 * Panel p = new Panel();<br>
 * LambdaLayout tl = new LambdaLayout();<br>
 * p.setLayout(tl);<br>
 * LambdaLayout.Constraints con = tl.getConstraint();<br>
 * con.x = 1;<br>
 * con.y = 2;<br>
 * con.w = 2;<br>
 * con.h = 2;<br>
 * con.s = "wh";<br>
 * panel.add(new Button("OK"), con);<br>
 * con.x = 3;<br>
 * panel.add(new Button("Cancel"), con);<br>
 * </code><br>
 * Note that the same Constraints can be reused, thereby reducing the number of
 * objects created.<p>
 * @author Dale Anson
 * @version $Revision$
 */

public class LambdaLayout extends KappaLayout implements LayoutManager2, Serializable {

   /**
    * Required by LayoutManager, does all the real layout work. This is the only
    * method in LambdaLayout, all other methods are in KappaLayout.
    */
   public void layoutContainer(Container parent) {
      synchronized(parent.getTreeLock()) {
         Insets insets = parent.getInsets();
         int max_width = parent.getSize().width - (insets.left + insets.right);
         int max_height = parent.getSize().height - (insets.top + insets.bottom);
         int x = insets.left;    // x and y location to put component in pixels
         int y = insets.top;
         int xfill = 0;          // how much extra space to put between components
         int yfill = 0;          // when stretching to fill entire container
         boolean add_xfill = false;
         boolean add_yfill = false;

         // make sure preferred size is known, a side effect is that countColumns
         // and countRows are automatically called.
         calculateDimensions();

         // if necessary, calculate the amount of padding to add between the
         // components to fill the container
         if ( max_width > _preferred_width ) {
            int pad_divisions = 0;
            for ( int i = 0; i < _col_count; i++ ) {
               if ( _col_widths[i] >= 0 )
                  ++pad_divisions;
            }
            if ( pad_divisions > 0 )
               xfill = (max_width - _preferred_width) / pad_divisions / 2;
         }
         if ( max_height > _preferred_height ) {
            int pad_divisions = 0;
            for ( int i = 0; i < _row_count; i++ ) {
               if ( _row_heights[i] >= 0 )
                  ++pad_divisions;
            }
            if ( pad_divisions > 0 )
               yfill = (max_height - _preferred_height) / pad_divisions / 2;
         }

         // do the layout. Components are handled by columns, top to bottom,
         // left to right. 
         Point cell = new Point();
         for ( int current_col = 0; current_col < _col_count; current_col++ ) {
            // adjust x for previous column widths
            x = insets.left;
            for ( int n = 0; n < current_col; n++ ) {
               x += Math.abs(_col_widths[n]);
               if ( _col_widths[n] > 0 )
                  x += xfill * 2;
            }

            for ( int current_row = 0; current_row < _row_count; current_row++ ) {
               // adjust y for previous row heights
               y = insets.top;
               for ( int n = 0; n < current_row; n++ ) {
                  y += Math.abs(_row_heights[n]);
                  if ( _row_heights[n] > 0 ) {
                     y += yfill * 2;
                  }
               }

               cell.x = current_col;
               cell.y = current_row;
               Component c = (Component)_components.get(cell);
               if ( c != null && c.isVisible() ) {
                  Dimension d = c.getPreferredSize();
                  Constraints q = (Constraints)_constraints.get(c);

                  // calculate width of spanned columns = sum(preferred column
                  // widths) + sum(xfill between columns)
                  int sum_cols = 0;
                  int sum_xfill = xfill * 2;
                  for ( int n = current_col; n < current_col + q.w; n++ ) {
                     sum_cols += Math.abs(_col_widths[n]);
                  }
                  if ( _col_widths[current_col] > 0 ) {
                     for ( int n = current_col; n < current_col + q.w - 1; n++ ) {
                        if ( _col_widths[n] > 0 )
                           sum_xfill += xfill * 2;
                     }
                     sum_cols += sum_xfill;
                  }

                  // calculate height of spanned rows
                  int sum_rows = 0;
                  int sum_yfill = yfill * 2;
                  for ( int n = current_row; n < current_row + q.h; n++ ) {
                     sum_rows += Math.abs(_row_heights[n]);
                  }
                  if ( _row_heights[current_row] > 0 ) {
                     for ( int n = current_row; n < current_row + q.h - 1; n++ ) {
                        if ( _row_heights[n] > 0 )
                           sum_yfill += yfill * 2;
                     }
                     sum_rows += sum_yfill;
                  }

                  int x_adj;
                  int y_adj;

                  // stretch if required
                  if ( q.s.indexOf("w") != -1 && _col_widths[current_col] > 0 ) {
                     d.width = sum_cols - q.p * 2;
                     x_adj = q.p * 2;
                  }
                  else {
                     x_adj = sum_cols - d.width;
                  }

                  if ( q.s.indexOf("h") != -1 && _row_heights[current_row] > 0 ) {
                     d.height = sum_rows - q.p * 2;
                     y_adj = q.p * 2;
                  }
                  else {
                     y_adj = sum_rows - d.height;
                  }

                  // in each case, add the adjustment for the cell, then subtract
                  // the correction after applying it.  This prevents the corrections
                  // from improperly accumulating across cells. Padding must be handled
                  // explicitly for each case.
                  // Alignment follows this pattern within the spanned cells:
                  // 8 1 2   or   NW N NE
                  // 7 0 3         W 0 E
                  // 6 5 4        SW S SE
                  switch ( q.a ) {
                     case N:     // top center
                        x += x_adj / 2 ;
                        y += q.p;
                        c.setBounds(x, y, d.width, d.height);
                        x -= x_adj / 2;
                        y -= q.p;
                        break;
                     case NE:    // top right
                        x += x_adj - q.p;
                        y += q.p;
                        c.setBounds(x, y, d.width, d.height);
                        x -= x_adj - q.p;
                        y -= q.p;
                        break;
                     case E:     // center right
                        x += x_adj - q.p;
                        y += y_adj / 2;
                        c.setBounds(x, y, d.width, d.height);
                        x -= x_adj - q.p;
                        y -= y_adj / 2;
                        break;
                     case SE:    // bottom right
                        x += x_adj - q.p;
                        y += y_adj - q.p;
                        c.setBounds(x, y, d.width, d.height);
                        x -= x_adj - q.p;
                        y -= y_adj - q.p;
                        break;
                     case S:     // bottom center
                        x += x_adj / 2;
                        y += y_adj - q.p;
                        c.setBounds(x, y, d.width, d.height);
                        x -= x_adj / 2;
                        y -= y_adj - q.p;
                        break;
                     case SW:    // bottom left
                        x += q.p;
                        y += y_adj - q.p;
                        c.setBounds(x, y, d.width, d.height);
                        x -= q.p;
                        y -= y_adj - q.p;
                        break;
                     case W:     // center left
                        x += q.p;
                        y += y_adj / 2;
                        c.setBounds(x, y, d.width, d.height);
                        x -= q.p;
                        y -= y_adj / 2;
                        break;
                     case NW:    // top left
                        x += q.p;
                        y += q.p;
                        c.setBounds(x, y, d.width, d.height);
                        x -= q.p;
                        y -= q.p;
                        break;
                     case 0:     // dead center
                     default:
                        x += x_adj / 2;
                        y += y_adj / 2;
                        c.setBounds(x, y, d.width, d.height);
                        x -= x_adj / 2;
                        y -= y_adj / 2;
                        break;
                  }
               }
            }
         }
      }
   }
}


