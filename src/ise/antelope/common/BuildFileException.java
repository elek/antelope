package ise.antelope.common;

/**
 * Throw this when there is a parsing or validation error in a build file.
 */
public class BuildFileException extends Exception {
   public BuildFileException() {
      super();
   }
   public BuildFileException( String msg ) {
      super( msg );
   }
   public BuildFileException( String msg, Throwable cause ) {
      super( msg, cause );
   }
   public BuildFileException( Throwable cause ) {
      super( cause );
   }
}
