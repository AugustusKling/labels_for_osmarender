package geometry;

/**
 * Signalizes a bend label is bend too much to be still readable
 */
public class TightBendException extends RuntimeException {

	/**
	 * Serialization
	 */
	private static final long serialVersionUID = 1L;

	public TightBendException(String message) {
		super(message);
	}

}
