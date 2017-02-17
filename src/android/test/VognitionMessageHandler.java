package com.vognition.opensdk;

import com.vognition.opensdk.VognitionInterface.VognitionResponse;


public abstract class VognitionMessageHandler {

    /** This message always returns the Vognition message number, or throws a VognitionHandlerException */
	public static int getMessageNumber(VognitionResponse vResponse) throws VognitionHandlerException {
		int message_num = -1;
		String response = null;
		try {
			response = vResponse.response;
			message_num = Integer.parseInt(response.substring(0,4));
		} catch (NumberFormatException e) {
			throw new VognitionHandlerException("Unable to parse the number into an integer from ["+response+"] ["+vResponse.wasSuccessful()+"]", e);
		}
		
		return message_num;
	}
	
	/**
	 * This method returns the int number of the Vognition message handled by the VognitionMessageHandler
	 * @return int number corresponding the Vognition Message number returned in the JSON response element handled by this handler.
	 */
	public abstract int getMessageNumberHandled();
	
	/**
	 * This method performs the actual work of processing the response message.  It may take a while so the user's expectations should be managed.
	 * @param message The valid JSON response from the Vognition.
	 * @return true if this MessageHandler handled the message, false otherwise.
	 * @throws VognitionHandlerException
	 */
	public abstract boolean handleMessage(VognitionResponse message, VognitionContext context, VognitionStatusListener listener) throws VognitionHandlerException;

    /**
     * This is a convenience method.  It uses handleMessage(message, context, NULL)
     * @param message The valid JSON response from the Vognition.
     * @return true if this MessageHandler handled the message, false otherwise.
     * @throws VognitionHandlerException
     */
    public boolean handleMessage(VognitionResponse message, VognitionContext context) throws VognitionHandlerException {
        return handleMessage(message, context, null);
    }
}
