package ise.antelope.tasks;
import java.util.Enumeration;

import java.util.Vector;

import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Available;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.UpToDate;
import org.apache.tools.ant.taskdefs.condition.*;

/**
 * Extends ConditionBase so I can get access to the condition count and the
 * first condition. This is the class that the BooleanConditionTask is proxy
 * for.
 *
 * @author   danson
 */
public class BooleanConditionBase extends ProjectComponent {
    
    
   /**
    * Gets the conditionCount attribute of the BooleanConditionBase object
    *
    * @return   The conditionCount value
    */
   public int getConditionCount() {
      return countConditions();
   }


   /**
    * Gets the firstCondition attribute of the BooleanConditionBase object
    *
    * @return   The firstCondition value
    */
   public Condition getFirstCondition() {
      return (Condition)getConditions().nextElement();
   }
   
   /**
    * Adds a feature to the IsPropertyTrue attribute of the BooleanConditionBase
    * object
    *
    * @param i  The feature to be added to the IsPropertyTrue attribute
    */
   public void addIsPropertyTrue( IsPropertyTrue i ) {
      addIsTrue( i );
   }


   /**
    * Adds a feature to the IsPropertyFalse attribute of the
    * BooleanConditionBase object
    *
    * @param i  The feature to be added to the IsPropertyFalse attribute
    */
   public void addIsPropertyFalse( IsPropertyFalse i ) {
      addIsFalse( i );
   }
   
   public void addIsGreaterThan( IsGreaterThan i) {
      addEquals(i);  
   }
   
   public void addIsLessThan( IsLessThan i) {
      addEquals(i);  
   }
   
   public void addStartsWith(StartsWith i) {
      addIsTrue(i);   
   }
   public void addEndsWith(EndsWith i) {
       addIsTrue(i);   
   }
   
   
    private Vector conditions = new Vector();

    /**
     * Count the conditions.
     *
     * @return the number of conditions in the container
     * @since 1.1
     */
    protected int countConditions() {
        return conditions.size();
    }

    /**
     * Iterate through all conditions.
     *
     * @return an enumeration to use for iteration
     * @since 1.1
     */
    protected final Enumeration getConditions() {
        return conditions.elements();
    }

    /**
     * Add an &lt;available&gt; condition.
     * @param a an available condition
     * @since 1.1
     */
    public void addAvailable(Available a) {
        conditions.addElement(a);
    }

    /**
     * Add an &lt;checksum&gt; condition.
     *
     * @param c a Checksum condition
     * @since 1.4, Ant 1.5
     */
    public void addChecksum(Checksum c) {
        conditions.addElement(c);
    }

    /**
     * Add an &lt;uptodate&gt; condition.
     *
     * @param u an UpToDate condition
     * @since 1.1
     */
    public void addUptodate(UpToDate u) {
        conditions.addElement(u);
    }

    /**
     * Add an &lt;not&gt; condition "container".
     *
     * @param n a Not condition
     * @since 1.1
     */
    public void addNot(Not n) {
        conditions.addElement(n);
    }

    /**
     * Add an &lt;and&gt; condition "container".
     *
     * @param a an And condition
     * @since 1.1
     */
    public void addAnd(And a) {
        conditions.addElement(a);
    }

    /**
     * Add an &lt;or&gt; condition "container".
     *
     * @param o an Or condition
     * @since 1.1
     */
    public void addOr(Or o) {
        conditions.addElement(o);
    }

    /**
     * Add an &lt;equals&gt; condition.
     *
     * @param e an Equals condition
     * @since 1.1
     */
    public void addEquals(Equals e) {
        conditions.addElement(e);
    }

    /**
     * Add an &lt;os&gt; condition.
     *
     * @param o an Os condition
     * @since 1.1
     */
    public void addOs(Os o) {
        conditions.addElement(o);
    }

    /**
     * Add an &lt;isset&gt; condition.
     *
     * @param i an IsSet condition
     * @since Ant 1.5
     */
    public void addIsSet(IsSet i) {
        conditions.addElement(i);
    }

    /**
     * Add an &lt;http&gt; condition.
     *
     * @param h an Http condition
     * @since Ant 1.5
     */
    public void addHttp(Http h) {
        conditions.addElement(h);
    }

    /**
     * Add a &lt;socket&gt; condition.
     *
     * @param s a Socket condition
     * @since Ant 1.5
     */
    public void addSocket(Socket s) {
        conditions.addElement(s);
    }

    /**
     * Add a &lt;filesmatch&gt; condition.
     *
     * @param test a FilesMatch condition
     * @since Ant 1.5
     */
    public void addFilesMatch(FilesMatch test) {
        conditions.addElement(test);
    }

    /**
     * Add a &lt;contains&gt; condition.
     *
     * @param test a Contains condition
     * @since Ant 1.5
     */
    public void addContains(Contains test) {
        conditions.addElement(test);
    }

    /**
     * Add a &lt;istrue&gt; condition.
     *
     * @param test an IsTrue condition
     * @since Ant 1.5
     */
    public void addIsTrue(IsTrue test) {
        conditions.addElement(test);
    }

    /**
     * Add a &lt;isfalse&gt; condition.
     *
     * @param test an IsFalse condition
     * @since Ant 1.5
     */
    public void addIsFalse(IsFalse test) {
        conditions.addElement(test);
    }

    /**
     * Add an &lt;isreference&gt; condition.
     *
     * @param i an IsReference condition
     * @since Ant 1.6
     */
    public void addIsReference(IsReference i) {
        conditions.addElement(i);
    }

    /**
     * Add an arbitrary condition
     * @param c a  condition
     * @since Ant 1.6
     */
    public void add(Condition c) {
        conditions.addElement(c);
    }
    
}

