/**
 * 
 */
package org.openmrs.module.ncd.preprocessing;

/**
 * Interface for classes that will pre-process messages prior to parsing.
 * 
 * @author John Brown
 *
 */
public interface MessagePreProcessor {
	/**
	 * Implement this method to transform a message.
	 * For instance, you might transform segments with 4-letter segment names
	 * that would normally cause parsing errors to segments with 3 letter names.
	 * 
	 * @return The transformed message.
	 */
	public String preProcessMessage(String message);
}
