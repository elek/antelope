package ise.antelope.tasks;
import java.util.Enumeration;

import java.util.Vector;

import org.apache.tools.ant.taskdefs.ConditionTask;
import org.apache.tools.ant.taskdefs.condition.*;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * Extends ConditionBase so I can get access to the condition count and the
 * first condition. This is the class that the BooleanConditionTask is proxy
 * for.
 *
 * @author   danson
 */
public class BooleanConditionBase extends ConditionBase {
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
      super.addIsTrue( i );
   }


   /**
    * Adds a feature to the IsPropertyFalse attribute of the
    * BooleanConditionBase object
    *
    * @param i  The feature to be added to the IsPropertyFalse attribute
    */
   public void addIsPropertyFalse( IsPropertyFalse i ) {
      super.addIsFalse( i );
   }
   
   public void addIsGreaterThan( IsGreaterThan i) {
      super.addEquals(i);  
   }
   
   public void addIsLessThan( IsLessThan i) {
      super.addEquals(i);  
   }
}

