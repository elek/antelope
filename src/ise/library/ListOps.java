package ise.library;

import java.util.*;

/**
 * Set operations for Lists and Sets:
 * <ul>
 * <li>union: a combined set consisting of the unique elements of two sets</li>
 * <li>intersection: a set containing elements that are members of both sets</li>
 * <li>symmetric difference: a set containing elements that are in one or the other of, but not both, sets</li>
 * <li>difference: a set containing the elements of one set minus any elements in the second set</li>
 * <li>cartesian product: the set of all ordered pairs with the first element of each pair selected from one set and the second element selected from the other.
 * </ul>
 * and some utility functions:
 * <ul>
 * <li>isSubset: returns true if all elements of the second set are elements of the first set</li>
 * <li>equals: returns true if both sets contain the same elements</li>
 * <li>disjoint: returns true if no elements of either set are contained in the other</li>
 * <li>toSet: converts a list to a set</li>
 * <li>toList: converts a set to a list</li>
 * </ul>
 * There are some interesting side effects: Suppose you have an ArrayList, <code>a</code>, that
 * may contain duplicates. Calling <code>toList(toSet(a))</code> return a new ArrayList sorted
 * in the same order as <code>a</code> but containing no duplicates.
 * @author Dale Anson
 */
public class ListOps {

   /**
    * Performs a union of two sets. The elements of the returned set will be
    * ordered with elements of <code>a</code> first (in their original order),
    * followed by elements of <code>b</code>, in their original order.
    * @param a one set
    * @param b the other set
    * @return a List containing the elements of both lists with no duplicates.
    */
   public static Set union( Set a, Set b ) {
      LinkedHashSet union = new LinkedHashSet( a );
      union.addAll( b );
      return union;
   }

   /**
    * Performs a union of two lists. The elements of the returned list will be
    * ordered with elements of <code>a</code> first (in their original order),
    * followed by elements of <code>b</code>, in their original order.
    * @param a one list
    * @param b the other list
    * @return a List containing the elements of both lists with no duplicates.
    */
   public static List union( List a, List b ) {
      Set union = union( toSet( a ), toSet( b ) );
      return new ArrayList( union );
   }

   /**
    * Finds the intersection of two sets. Ordering is the order that the elements
    * are in in set <code>a</code>.
    * @param one set
    * @param the other set
    * @return the intersection of the two sets, may be empty, will not be null.
    */
   public static Set intersection( Set a, Set b ) {
      LinkedHashSet intersection = new LinkedHashSet();
      Iterator itr = a.iterator();
      while ( itr.hasNext() ) {
         Object o = itr.next();
         if ( b.contains( o ) )
            intersection.add( o );
      }
      return intersection;
   }

   /**
    * Finds the intersection of two Lists. Ordering is the order that the elements
    * are in in List <code>a</code>.
    * @param one List
    * @param the other List
    * @return the intersection of the two List, may be empty, will not be null.
    */
   public static List intersection( List a, List b ) {
      return toList( intersection( toSet( a ), toSet( b ) ) );
   }

   /**
    * Finds the difference of set <code>a</code> and set <code>b</code>.
    * @param a the first set
    * @param b the other set
    * @return a set containing the elements of set <code>a</code> that are NOT also
    * in set <code>b</code>.
    */
   public static Set difference( Set a, Set b ) {
      LinkedHashSet difference = new LinkedHashSet();
      Iterator itr = a.iterator();
      while ( itr.hasNext() ) {
         Object o = itr.next();
         if ( !b.contains( o ) )
            difference.add( o );
      }
      return difference;
   }

   /**
    * Finds the difference of list <code>a</code> and list <code>b</code>.
    * @param a the first list
    * @param b the other list
    * @return a set containing the elements of list <code>a</code> that are NOT also
    * in list <code>b</code>.
    */
   public static List difference( List a, List b ) {
      return toList( difference( toSet( a ), toSet( b ) ) );
   }

   /**
    * Finds the symmetric difference of set <code>a</code> and set <code>b</code>.
    * @param a the first set
    * @param b the other set
    * @return a set containing the elements of set <code>a</code> that are NOT also
    * in set <code>b</code> unioned with the elements of set <code>b</code> that
    * are NOT also in set <code>a</code>.
    */
   public static Set symmetricDifference( Set a, Set b ) {
      return union( difference( a, b ), difference( b, a ) );
   }

   /**
    * Finds the symmetric difference of list <code>a</code> and list <code>b</code>.
    * @param a the first list
    * @param b the other list
    * @return a list containing the elements of list <code>a</code> that are NOT also
    * in list <code>b</code> unioned with the elements of list <code>b</code> that
    * are NOT also in list <code>a</code>.
    */
   public static List symmetricDifference( List a, List b ) {
      return toList( symmetricDifference( toSet( a ), toSet( b ) ) );
   }

   /**
    * Constructs the Cartesian product of two sets. The values in the returned 
    * TableArray are <code>ListOps.Pair</code>s.
    * @param a one set
    * @param b the other set
    * @return the Cartesian product of the two sets.
    */
   public static TableArray product( Set a, Set b ) {
      TableArray product = new TableArray();
      Iterator itra = a.iterator();
      for ( int y = 0; itra.hasNext(); y++ ) {
         Object key = itra.next();
         Iterator itrb = b.iterator();
         for ( int x = 0; itrb.hasNext(); x++ ) {
            Object value = itrb.next();
            product.put( x, y, new Pair( key, value ) );
         }
      }
      return product;
   }

   /**
    * Constructs the Cartesian product of two Lists. The values in the returned 
    * TableArray are <code>ListOps.Pair</code>s.
    * @param a one set
    * @param b the other set
    * @return the Cartesian product of the two sets.
    */
   public static TableArray product( List a, List b ) {
      return product( toSet( a ), toSet( b ) );
   }

   /**
    * @return true if all elements of <code>b</code> are also in <code>a</code>.   
    */
   public static boolean isSubset( Set a, Set b ) {
      return a.containsAll( b );
   }

   /**
    * @return true if all elements of <code>b</code> are also in <code>a</code>.   
    */
   public static boolean isSubset( List a, List b ) {
      return isSubset( toSet( a ), toSet( b ) );
   }

   /**
    * @return true if both sets contain the same elements.   
    */
   public static boolean equals( Set a, Set b ) {
      return isSubset( a, b ) && isSubset( b, a );
   }

   /**
    * @return true if both Lists contain the same elements.   
    */
   public static boolean equals( List a, List b ) {
      return equals( toSet( a ), toSet( b ) );
   }

   /**
    * Converts a List to a Set.   
    */
   public static Set toSet( List a ) {
      return new LinkedHashSet( a );
   }

   /**
    * Converts a Set to a List.   
    */
   public static List toList( Set a ) {
      return new ArrayList( a );
   }


   /**
    * Used by <code>product</code>, represents an ordered pair.
    */
   public static class Pair {

      public Object x = null;
      public Object y = null;

      public Pair() {}

      public Pair( Object a, Object b ) {
         x = a;
         y = b;
      }
      
      public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("(");
         sb.append(x == null ? "null" : x.toString());
         sb.append(",");
         sb.append(y == null ? "null" : y.toString());
         sb.append(")");
         return sb.toString();  
      }
   }

}
