
package ise.library;

import java.awt.Point;
import java.util.*;
import java.io.Serializable;

/**
 * A table-like storage structure. Items are stored in a two dimensional array and
 * are accessed by a two-dimensional index. Indexes can be negative or positive, so
 * calling put(-6, 12, myobject) is legal. This class is particularly nice for sparse
 * arrays as it minimizes memory usage.
 * <p>
 * Example usage:
 * <p>
 * Populate a table from a ResultSet:
 * <pre>
 * ResultSet rs = stmt.executeQuery("select something from somewhere");
 * int col_count = rs.getMetaData().getColumnCount();
 * int y = 0;
 * TableArray table = new TableArray();
 * while(rs.next()) {
 *    for (int x = 0; x < col_count; x++) {
 *       table.put(x, y, rs.getObject(x));       
 *    }
 *    ++y;
 * }
 * </pre>
 * <p>
 * Use items from the ResultSet:
 * <pre>
 * String first_name = (String)table.get(1, 0);
 * String last_name = (String)table.get(2, 0);
 * </pre>
 *
 * @author Dale Anson, January 2000
 */
public class TableArray implements Map, Serializable, Cloneable {

   // revision 1.2:
   // changed from Hashtable, TreeMap sorts itself using the point comparator.
   private TreeMap table;

   // the default value for all cells in the table. If the table does not hold
   // a value for a particular point, this value will be used.
   private Object _default = null;

   /**
    * TableArray with null default value.   

    */
   public TableArray() {
      this( null );
   }

   /**
    * TableArray with default value. 

    * @param default_value this value will be returned by the various "get" methods

    * in the event the table does not have a value stored at the requested location.

    * In other words, all cells in the table are assumed to have this value unless

    * otherwise set.

    */
   public TableArray( Object default_value ) {
      table = new TreeMap( new PointComparator() );
      _default = default_value;
   }

   /**
    * Set the default value for all cells in the table. If the table does not hold
    * a value for a particular point, this value will be used.
    * @param value the new default value, may be null.
    * @see #getDefaultValue
    */
   public void setDefaultValue( Object value ) {
      _default = value;
   }

   /**
    * Get the default value for all cells in the table. If the table does not hold
    * a value for a particular point, this value will be used.
    * @return the default value, may be null.
    * @see #setDefaultValue
    */
   public Object getDefaultValue() {
      return _default;
   }

   /**
    * Store an object at a given location in the table.
    * @param x column index
    * @param y row index
    * @param o the object to store
    * @return the object that was at that position, if any. Null values are allowed,
    * putting a null value causes the old value to be removed.
    */
   public Object put( int x, int y, Object o ) {
      Object old_object = get( x, y );
      Point p = new Point( x, y );
      if ( o != null && o.equals( getDefaultValue() ) )
         remove( p );
      else if ( o == null && getDefaultValue() == null )
         remove( p );
      else
         table.put( p, o );
      return old_object;
   }

   /**
    * Store an object at a given location in the table.
    * @param x column index
    * @param y row index
    * @param o the object to store
    * @return the object that was at that position, if any. Null values are allowed,
    * putting a null value causes the old value to be removed.
    */
   public Object put( Object o, int x, int y ) {
      return put( x, y, o );
   }

   /**
    * Gets the object from the given location in the table.
    * @param x column index
    * @param y row index
    * @return the object stored at the given (x, y) coordinate or null if there
    * is no object stored there.
    */
   public Object get( int x, int y ) {
      Point p = new Point( x, y );
      Object o = table.get( p );
      if ( o == null )
         o = _default;
      return o;
   }

   /**
    * This class represents a finite set of points in a Cartesian coordinate system.
    * Since the set is finite, there is a smallest rectangle that bounds the set of
    * points. (This rectangle has sides parallel to the x- and y-axes.) This method
    * returns the width of that rectangle.
    * @return the width of the bounding rectangle
    */
   public int getWidth() {
      if ( table.size() == 0 )
         return 0;
      return getMaxX() - getMinX() + 1;
   }

   /**
    * Returns the number of columns in the table, same as <code>getWidth</code>.
    * @see #getWidth
    * @return getWidth
    */
   public int getColumnCount() {
      return getWidth();
   }

   /**
    * This class represents a finite set of points in a Cartesian coordinate system.
    * Since the set is finite, there is a smallest rectangle that bounds the set of
    * points. (This rectangle has sides parallel to the x- and y-axes.) This method
    * returns the height of that rectangle.
    * @return the height of the bounding rectangle
    */
   public int getHeight() {
      if ( table.size() == 0 )
         return 0;
      return getMaxY() - getMinY() + 1;
   }

   /**
    * Returns the number of rows in the table, same as <code>getHeight</code>.
    * @see #getHeight
    * @return getHeight
    */
   public int getRowCount() {
      return getHeight();
   }

   /**
    * See getWidth and getHeight. This method returns the x coordinate of the point
    * that has the smallest x-coordinate. Beware: there may not actually be an
    * object stored at (getMinX(), getMinY()). If there are no objects stored in
    * the array, this method will return 0.
    * @return the column index of the left side of the bounding rectangle, or
    * 0 if the table is empty.
    */
   public int getMinX() {
      if ( table.size() == 0 )
         return 0;
      int smallest_x = 0;
      Iterator it = table.keySet().iterator();
      if ( it.hasNext() ) {
         Point p = ( Point ) it.next();
         smallest_x = p.x;
      }
      while ( it.hasNext() ) {
         Point p = ( Point ) it.next();
         if ( p.x < smallest_x )
            smallest_x = p.x;
      }
      return smallest_x;
   }

   /**
    * See getWidth and getHeight. This method returns the y coordinate of the point
    * that has the smallest y-coordinate. Beware: there may not actually be an
    * object stored at (getMinX(), getMinY()). If there are no objects stored in
    * the array, this method will return 0.
    * @return the row index of the top side of the bounding rectangle, or 0 if the
    * table is empty.
    */
   public int getMinY() {
      // revision 1.3:
      // think about it -- since I changed the storage container from a Hashtable
      // to a TreeMap and added a PointComparator to the TreeMap, isn't the
      // minimum y-coordinate the y-coordinate of the first point in the tree?
      // Need to check this out, if it's true, then an optimization can be made
      // here.
      // -- it is true, new code follows, made similar change to getMaxY.
      if ( table.size() == 0 )
         return 0;
      Point p = ( Point ) table.firstKey();
      return p.y;
   }

   /**
    * See getWidth and getHeight. This method returns the x coordinate of the point
    * that has the largest x-coordinate. Beware: there may not actually be an
    * object stored at (getMaxX(), getMaxY()).
    * @return the column index of the right side of the bounding rectangle.
    */
   public int getMaxX() {
      if ( table.size() == 0 )
         return 0;
      int largest_x = 0;
      Iterator it = table.keySet().iterator();
      if ( it.hasNext() ) {
         Point p = ( Point ) it.next();
         largest_x = p.x;
      }
      while ( it.hasNext() ) {
         Point p = ( Point ) it.next();
         if ( p.x > largest_x )
            largest_x = p.x;
      }
      return largest_x;
   }

   /**
    * See getWidth and getHeight. This method returns the y coordinate of the point
    * that has the largest y-coordinate. Beware: there may not actually be an
    * object stored at (getMaxX(), getMaxY()).
    * @return the row index of the bottom of the bounding rectangle.
    */
   public int getMaxY() {
      // revision 1.3:
      // think about it -- since I changed the storage container from a Hashtable
      // to a TreeMap and added a PointComparator to the TreeMap, isn't the
      // maximum y-coordinate the y-coordinate of the last point in the tree?
      // Need to check this out, if it's true, then an optimization can be made
      // here.
      // -- it is true, new code follows, made similar change to getMinY.
      if ( table.size() == 0 )
         return 0;
      Point p = ( Point ) table.lastKey();
      return p.y;
   }

   /**
    * Required by Map interface, removes all items from the table.
    */
   public void clear() {
      table.clear();
   }

   /**
    * Required by Map interface, keys for the table are java.awt.Points.
    * @param key a java.awt.Point
    * @return true if the table contains the given key.
    */
   public boolean containsKey( Object key ) {
      return table.containsKey( key );
   }

   /**
    * Required by Map interface, checks if the given value is in the table.
    * @param value the object to find
    * @return true if the value exists in the table.
    */
   public boolean containsValue( Object value ) {
      return table.containsValue( value );
   }

   /**
    * Required by Map interface. Returns a set view of the mappings contained in 
    * this table. Each element in the returned set is a Map.Entry. The set is backed 
    * by the table, so changes to the map are reflected in the set, and vice-versa. 
    * If the table is modified while an iteration over the set is in progress, the 
    * results of the iteration are undefined. The set supports element removal, 
    * which removes the corresponding mapping from the table, via the Iterator.remove, 
    * Set.remove, removeAll, retainAll and clear operations. It does not support 
    * the add or addAll operations.
    * @return a set view of the mappings contained in this map.
    */
   public Set entrySet() {
      return table.entrySet();
   }

   /**
    * Required by Map interface, checks if this TableArray is equal to 
    * the given object by comparing data values.
    */
   public boolean equals( Object o ) {
      if ( o == null )
         return false;
      try {
         TableArray ta = ( TableArray ) o;
         if ( ta.size() != size() )
            return false;
         Iterator it = pointIterator();
         while ( it.hasNext() ) {
            Point p = ( Point ) it.next();
            if ( !get( p ).equals( ta.get( p ) ) )
               return false;
         }
         return true;
      }
      catch ( ClassCastException cce ) {
         return false;
      }
   }

   /**
    * Required by Map interface, returns the value at the given point.
    * @param key must be a Point
    */
   public Object get( Object key ) {
      Point p = ( Point ) key;
      return get( p.x, p.y );
   }

   /**
    * Returns a List containing the values of a single row. The list may contain nulls.
    * @return a List containing the values of a single row. If a particular cell is
    * empty, the List will contain an empty String to represent the value for that 
    * cell.
    */
   public List getRow( int row ) {
      List list = new ArrayList();
      for ( int x = getMinX(); x <= getMaxX(); x++ ) {
         list.add( get( x, row ) );
      }
      return list;
   }

   /**
    * Inserts the given data into the row. This will overwrite any data already in
    * the row. If the data is longer than the current row, the row will be extended
    * to hold all data. If the data is shorter than the current row, the row will be
    * nulled out.
    */
   public void putRow( int row, List data ) {
      removeRow( row );
      Iterator it = data.iterator();
      for ( int x = 0; it.hasNext(); x++ ) {
         put( x + getMinX(), row, it.next() );
      }
   }

   /**
    * Returns a List containing the values of a single column. The list may contain nulls.
    * @return a List containing the values of a single column. If a particular cell is
    * empty, the List will contain a <code>null</code> as the value for that 
    * cell.
    */
   public List getColumn( int col ) {
      List list = new ArrayList();
      for ( int y = getMinY(); y <= getMaxY(); y++ ) {
         list.add( get( col, y ) );
      }
      return list;
   }

   /**
    * Inserts the given data into the column. This will overwrite any data already in
    * the column. If the data is longer than the current column, the column will be extended
    * to hold all data. If the data is shorter than the current column, the column will be
    * nulled out.
    */
   public void putColumn( int col, List data ) {
      for ( int y = getMinY(); y <= getMaxY(); y++ ) {
         remove( col, y );
      }
      Iterator it = data.iterator();
      for ( int y = getMinY(); it.hasNext(); y++ ) {
         put( col, y, it.next() );
      }
   }


   /**
    * Required by Map interface, returns the hash code for this TableArray.
    */
   public int hashCode() {
      return table.hashCode();
   }

   /**
    * Required by Map interface, returns true if the table is empty.
    * @return true if the table is empty.
    */
   public boolean isEmpty() {
      return table.isEmpty();
   }

   /**
    * Required by Map interface.
    * Returns a set view of the keys contained in this table. The set is backed by 
    * the table, so changes to the table are reflected in the set, and vice-versa. If 
    * the table is modified while an iteration over the set is in progress, the 
    * results of the iteration are undefined. The set supports element removal, 
    * which removes the corresponding mapping from the table, via the Iterator.remove, 
    * Set.remove, removeAll retainAll, and clear operations. It does not support the 
    * add or addAll operations.
    * <p>
    * <b>The returned set is a dense set, that is, the set will contain only those
    * points in the table that are mapped to a non-null value.</b>
    * This means that the Set will have TableArray.getSize() Points.
    * The Set is sorted, see PointComparator for details of the sorting algorithm.
    * <p>
    * If you need access to all points in the table, use the Iterator provided 
    * by the <code>pointIterator</code> method.
    *
    * @return a set view of the keys contained in this map.
    */
   public Set keySet() {
      return table.keySet();
   }

   /**
    * Same as <code>keySet</code>.
    * Returns a set view of the keys contained in this table. The set is backed by 
    * the table, so changes to the table are reflected in the set, and vice-versa. If 
    * the table is modified while an iteration over the set is in progress, the 
    * results of the iteration are undefined. The set supports element removal, 
    * which removes the corresponding mapping from the table, via the Iterator.remove, 
    * Set.remove, removeAll retainAll, and clear operations. It does not support the 
    * add or addAll operations.
    * <p>
    * <b>The returned set is a dense set, that is, the set will contain only those
    * points in the table that are mapped to a non-null value.</b>
    * This means that the Set will have TableArray.getSize() Points.
    * The Set is sorted, see PointComparator for details of the sorting algorithm.
    * <p>
    * If you need access to all points in the table, use the Iterator provided 
    * by the <code>pointIterator</code> method.
    *
    * @return a set view of the keys contained in this map.
    */
   public Set pointSet() {
      return keySet();
   }

   /**
    * Returns an iterator over the points in this table. The points are 
    * returned in ascending order. <b>This is a sparse iterator, that is, it
    * iterates over all cells that the table contains whether or not there is a
    * value in that cell or not.</b>
    */
   public Iterator pointIterator() {
      return new Iterator() {
                private int min_x = getMinX();
                private int min_y = getMinY();
                private int max_x = getMaxX();
                private int max_y = getMaxY();
                private int x = min_x;
                private int y = min_y;

                public boolean hasNext() {
                   return x <= max_x && y <= max_y;
                }

                public Object next() {
                   if ( hasNext() ) {
                      Point p = new Point( x, y );

                      // prep for next call
                      ++x;
                      if ( x > max_x ) {
                         x = min_x;
                         ++y;
                      }

                      // return the next point
                      return p;
                   }
                   else
                      throw new NoSuchElementException();
                }

                public void remove() {
                   throw new UnsupportedOperationException();
                }
             };
   }

   /**
    * Returns an iterator over the objects in this table. The objects are 
    * returned in ascending order by point. <b>This is a sparse iterator, that is, it
    * iterates over all cells that the table contains whether there is a
    * value contained in that cell or not.</b> This means that calling the 
    * <code>next</code> method of this iterator may return null.
    */
   public Iterator valueIterator() {
      return new Iterator() {
                private int min_x = getMinX();
                private int min_y = getMinY();
                private int max_x = getMaxX();
                private int max_y = getMaxY();
                private int x = min_x;
                private int y = min_y;
                public boolean hasNext() {
                   return x <= max_x && y <= max_y;
                }
                public Object next() {
                   if ( hasNext() ) {
                      // get the point
                      Object o = get( x, y );

                      // prep for next call
                      x += 1;
                      if ( x > max_x ) {
                         x = min_x;
                         y += 1;
                      }
                      return o;
                   }
                   throw new NoSuchElementException();
                }
                public void remove() {
                   throw new UnsupportedOperationException();
                }
             };
   }

   /**
    * An iterator over a single column of the table.
    * @return an iterator over the given column.    
    */
   public Iterator columnIterator( int col ) {
      final int c = col;
      return new Iterator() {
                private int min_y = getMinY();
                private int max_y = getMaxY();
                private int y = min_y;
                public boolean hasNext() {
                   return y <= max_y;
                }
                public Object next() {
                   if ( hasNext() ) {
                      Object o = get( c, y );
                      y += 1;
                      return o;
                   }
                   throw new NoSuchElementException();
                }
                public void remove() {
                   throw new UnsupportedOperationException();
                }
             };
   }

   /**
    * Returns an iterator over the values of contained in a single column in the table.
    * @return an iterator over the given row.    
    */
   public Iterator rowIterator( int row ) {
      final int r = row;
      return new Iterator() {
                private int min_x = getMinX();
                private int max_x = getMaxX();
                private int x = min_x;
                public boolean hasNext() {
                   return x <= max_x;
                }
                public Object next() {
                   if ( hasNext() ) {
                      Object o = get( x, r );
                      x += 1;
                      return o;
                   }
                   throw new NoSuchElementException();
                }
                public void remove() {
                   throw new UnsupportedOperationException();
                }
             };
   }

   /**
    * Required by Map interface, stores an object in the table. <code>key</code>
    * must be a java.awt.Point or a ClassCastException will be thrown.
    * @param key must be a Point
    */
   public Object put( Object key, Object value ) {
      return put( ( ( Point ) key ).x, ( ( Point ) key ).y, value );
   }

   /**
    * Required by Map interface, dumps the given map into this table. Keys in
    * the map must be java.awt.Points or a ClassCastException will be thrown.
    */
   public void putAll( Map t ) {
      Iterator it = t.keySet().iterator();
      while ( it.hasNext() ) {
         Point key = ( Point ) it.next();
         Object value = t.get( key );
         put( key.x, key.y, value );
      }
   }

   /**
    * Required by Map interface, removes an object from the table, <code>key</code>
    * must be a java.awt.Point.
    * @param key must be a Point.
    * @return the removed object
    */
   public Object remove( Object key ) {
      return table.remove( key );
   }

   /**
    * Removes an item from the table.
    * @param row row index of item
    * @param col column index of item
    * @return the removed item
    */
   public Object remove( int row, int col ) {
      return remove( new Point( row, col ) );
   }

   /**
    * Removes a complete row from the table.

    * @param row the index of the row to remove

    */
   public void removeRow( int row ) {
      for ( int x = getMinX(); x < getMaxX(); x++ ) {
         remove( x, row );
      }
   }

   /**
    * Removes a complete column from the table.

    * @param col the index of the column to remove

    */
   public void removeColumn( int col ) {
      for ( int y = getMinY(); y < getMaxY(); y++ ) {
         remove( col, y );
      }
   }

   /**
    * Required by Map interface.
    * @return the number of objects stored in this array. Beware: this is not
    * necessarily the same value as getWidth() * getHeight(). For example,
    * size() may return 2, while getWidth() * getHeight() could be 100. This just
    * means there are 98 empty spots in the array.
    */
   public int size() {
      return table.size();
   }

   /**
    * Same as <code>size</code>.
    * @see #size   
    */
   public int getSize() {
      return size();
   }

   /**
    * Returns the number of cells in the table. As this table allows empty cells,
    * there may not be an object stored in all cells. This IS the same as 
    * getWidth() * getHeight().
    * @see #size
    * @return the number of cells in the table.
    */
   public int getCellCount() {
      return getWidth() * getHeight();
   }

   /**
    * Required by Map interface, retuns a collection of the values of this
    * table. This is a dense collection, no null values (i.e. empty table cells)
    * are included. If you need the sparse set (all values, regardless of
    * whether or not they are null), use the Iterator provided by the 
    * <code>valueIterator</code> method.
    * @return a collection of the objects stored in the table.
    */
   public Collection values() {
      return table.values();
   }

   /**
    * Returns a string showing the coordinates of the bottom left and top right
    * corners of the bounding rectangle, the width and height of the rectangle,
    * and the number of objects contained in the table. Note that the table may
    * have empty cells, so width times the height may not equal the number of
    * objects in the table.
    * @return a String showing getMinX, getMinY, getMaxX, getMaxY, getWidth,
    * getHeight, and getSize, in that order.
    */
   public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append( "TableArray [(" ).append( getMinX() ).append( "," ).append( getMinY() ).append( "), (" ).append( getMaxX() ).append( "," ).append( getMaxY() ).append( "), w=" ).append( getWidth() ).append( ", h=" ).append( getHeight() ).append( ", size=" ).append( size() ).append( "]" );
      if ( size() > 0 )
         sb.append( System.getProperty( "line.separator" ) ).append( dataToString() );
      else
         sb.append( "[empty]" );
      return sb.toString();
   }

   /**
    * Returns a string showing just the data from the table.

    * @return the table data suitable for display.

    */
   public String dataToString() {
      StringBuffer table_data = new StringBuffer();
      String ls = System.getProperty( "line.separator" );
      for ( int y = getMinY(); y <= getMaxY(); y++ ) {
         StringBuffer row = new StringBuffer();
         row.append( "[" );
         for ( int x = getMinX(); x <= getMaxX(); x++ ) {
            Object o = get( x, y );
            if ( o == null )
               o = "";
            row.append( o.toString() );
            if ( x < getMaxX() )
               row.append( "," );
         }
         row.append( "]" ).append( ls );
         table_data.append( row );
      }
      return table_data.toString();
   }

   /**
    * Clone this TableArray.   

    */
   public Object clone() {
      TableArray ta = new TableArray();
      Iterator it = keySet().iterator();
      while ( it.hasNext() ) {
         Point p = ( Point ) it.next();
         ta.put( p, get( p.x, p.y ) );
      }
      return ta;
   }

   /**
    * Compares two Points by comparing x and y coordinates.<br>
    * <ul>
    * <li>check for less than: "less than" is defined using the Cartesian
    * coordinate system such that p1 is less than p2 if and only if
    * p1.y < p2.y or in the case that p1.y == p2.y, p1 is less than p2 if
    * and only if p1.x < p2.x. Graphically, this can be pictured as p1 and
    * p2 being opposite corners of a rectangle, the point on the bottom of
    * rectangle is less than the point at the top of the rectangle. If it
    * happens that p1 and p2 form a horizontal line, then p1 is less than
    * p2 if p1 is to the left of p2.<br>
    * <li>check for equals: "equals" is defined as: Given points p1 and p2,
    * p1 == p2 if and only if p1.x == p2.x and p1.y == p2.y<br>
    * <li>check for greater than: "greater than" is simply not less than and not equals.
    * </ul>
    * This is used by TableArray.
    * @author Dale Anson, March 2000
    */
   class PointComparator implements Comparator, Serializable {
      /**
       * @param a must be a java.awt.Point
       * @param b must be a java.awt.Point
       * @return -1 if a < b, 0 if a == b, 1 if a > b
       */
      public int compare( Object a, Object b ) {
         // don't worry about ClassCastExceptions, if the objects aren't Points,
         // this is the wrong place to be anyway.
         Point p1 = ( Point ) a;
         Point p2 = ( Point ) b;

         // check for equality
         if ( p1.x == p2.x && p1.y == p2.y )
            return 0;

         // check for less than
         if ( p1.y < p2.y )
            return -1;
         else if ( p1.y == p2.y ) {
            if ( p1.x < p2.x )
               return -1;
         }

         // check for greater than -- nothing to check here, the points are not
         // equal and p1 is not less than p2, therefore p1 must be greater than
         // p2.
         return 1;
      }
   }
}

