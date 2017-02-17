package com.vognition.opensdk;

/**
 * Created by noahternullo on 12/2/14.
 * Utility to make creating a Listener easy.  Only override the methods of interest
 */
public class StatusListenerStub implements VognitionStatusListener {

    @Override
    public void handleRequestStatus(RequestStatus requestStatus) {

    }

    @Override
    public void handleServiceException(VognitionServiceException exception) {

    }

    @Override
    public void handleHandlerException(VognitionHandlerException exception) {

    }
}
