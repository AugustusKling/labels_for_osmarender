package geometry;

import org.w3c.dom.DOMException;

/**
 * Signalizes that a label does not fit into available space
 */
public class FalloffException extends RuntimeException {

	/**
	 * Serialization
	 */
	private static final long serialVersionUID = 1L;

	public FalloffException(String message, DOMException reason) {
		super(message, reason);
	}

}
