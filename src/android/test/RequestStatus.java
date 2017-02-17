package com.vognition.opensdk;

/**
 * Created by noahternullo on 12/2/14.
 */

/**
 * This class is used in conjunction with VognitionStatusListeners to identify which state the Vognition subsystem is in, and provide any details the user may need.
 */
public class RequestStatus {
    /**
     * NONE = The Request State hasn't been set yet
     * RECORDING = Vognition is in the process of recording the voice command
     * CONVERTING = Vognition is in the process of converting the voice to text
     * SUBMITTING = Vognition has submitted the request to the cloud
     * PROCESSING = Vognition has received the response from the cloud and is now processing it
     * COMPLETE = The Vognition request has completed
     * ERROR = There was an error and the Request couldn't be completed
     * VERIFYING = We are currently trying to verify the success of a command with the Vognition Cloud - will transition to VERIFIED or ERROR
     * VERIFIED  = The command associated with the session ID passed in to verify has completed successfully
     */
    public static enum RequestState { NONE, RECORDING, CONVERTING, SUBMITTING, PROCESSING, COMPLETE, CANCELLED, ERROR, VERIFYING, VERIFIED}

    /**
     * The current State of the Vognition Request
     */
    public RequestState requestState = RequestState.NONE;
    /**
     * Any textual details appropriate for a user to see
     * May be empty or null
     */
    public String detail = "";

    public RequestStatus() {};

    public RequestStatus(RequestState state) {
        this(state, null);
    }

    public RequestStatus(RequestState state, String message) {
        requestState = state;
        detail = message;
    }

}
