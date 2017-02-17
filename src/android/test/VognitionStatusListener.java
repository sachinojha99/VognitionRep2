package com.vognition.opensdk;

/**
 * Created by noahternullo on 11/21/14.
 */
public interface VognitionStatusListener {

    public void handleRequestStatus(RequestStatus requestStatus);
    public void handleServiceException(VognitionServiceException exception);
    public void handleHandlerException(VognitionHandlerException exception);


}
