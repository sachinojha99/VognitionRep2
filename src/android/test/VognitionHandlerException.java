/**
 * 
 */
package com.vognition.opensdk;

/**
 * @author noahternullo
 *
 */
public class VognitionHandlerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public VognitionHandlerException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 */
	public VognitionHandlerException(String detailMessage) {
		super(detailMessage);
		
	}

	/**
	 * @param throwable
	 */
	public VognitionHandlerException(Throwable throwable) {
		super(throwable);
		
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public VognitionHandlerException(String detailMessage,
                                     Throwable throwable) {
		super(detailMessage, throwable);
		
	}

}
