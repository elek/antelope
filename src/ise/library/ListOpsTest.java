package ise.library;

import java.util.*;

/**
 * Cheapo unit testing, in the style of junit, but without junit. Add test cases
 * as needed, then call them from the main method. Be sure to run with -ea option
 * on command line so assert works: java -ea ise.library.ListOpsTest
 */
public class ListOpsTest {

   LinkedHashSet a, b;
   LinkedHashSet union, intersection, difference, sdifference;
   TableArray product;
   

   public void setup() {
      a = new LinkedHashSet();
      b = new LinkedHashSet();
      a.add("1");
      a.add("2");
      a.add("3");
      a.add("a");
      a.add("b");
      b.add("a");
      b.add("b");
      b.add("c");
      b.add("d");
      
      union = new LinkedHashSet();
      union.add("1");
      union.add("2");
      union.add("3");
      union.add("a");
      union.add("b");
      union.add("c");
      union.add("d");
      
      intersection = new LinkedHashSet();
      intersection.add("a");
      intersection.add("b");
      
      difference = new LinkedHashSet();
      difference.add("1");
      difference.add("2");
      difference.add("3");
      
      sdifference = new LinkedHashSet();
      sdifference.add("1");
      sdifference.add("2");
      sdifference.add("3");
      sdifference.add("c");
      sdifference.add("d");
      
      product = new TableArray();
      
     
   }

   public void testUnion() {
      assert(SetOps.equals(SetOps.union(a, b), union));
   }
   
   public void testIntersection() {
      assert(ListOps.equals(ListOps.intersection(a, b), intersection));
   }
   
   public void testDifference() {
      assert(ListOps.equals(ListOps.difference(a, b), difference));
   }
   
   public void testSymmetricDifference() {
      assert(ListOps.equals(ListOps.symmetricDifference(a, b), sdifference));
   }
   
   public void testProduct() {
      System.out.println(ListOps.product(a, b));  
   }
   
   public void testDuplicates() {
      ArrayList al = new ArrayList();
      al.add("a");
      al.add("b");
      al.add("a");
      al.add("b");
      ArrayList bl = new ArrayList();
      bl.add("a");
      bl.add("b");
      assert(ListOps.equals(ListOps.toList(ListOps.toSet(al)), bl));
   }


   public static void main( String[] args ) {
      ListOpsTest tat = new ListOpsTest();
      tat.setup();
      tat.testUnion();
      System.out.println("union test passed");
      tat.testIntersection();
      System.out.println("intersection test passed");
      tat.testDifference();
      System.out.println("difference test passed");
      tat.testSymmetricDifference();
      System.out.println("symmetric difference passed");
      tat.testProduct();
      tat.testDuplicates();
      System.out.println("duplicates test passed");
      System.exit( 0 );
   }
}
