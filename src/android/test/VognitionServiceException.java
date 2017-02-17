package com.vognition.opensdk;


public class VognitionServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VognitionServiceException() {
		
	}

	public VognitionServiceException(String detailMessage) {
		super(detailMessage);
		
	}

	public VognitionServiceException(Throwable throwable) {
		super(throwable);
		
	}

	public VognitionServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		
	}

}
