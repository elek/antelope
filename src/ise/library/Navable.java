
package ise.library;

/**
 * Objects implement this interface to be able to work with
 * Nav to add a navigation (web-browser-like back and forward) ability
 * to themselves.
 * @author Dale Anson, danson@germane-software.com
 */
public interface Navable {
   /**
    * Sets the position of the Navable object. Generally, if a Navable
    * object calls this method on itself, it should immediately call
    * <code>Nav.update(o)</code>.
    *
    * @param o  The new position value
    */
   public void setPosition( Object o );
}

