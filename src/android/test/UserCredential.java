package com.vognition.opensdk;

/**
 * Created by noahternullo on 11/21/14.
 * This captures the credentials that uniquely identify and authorize a specific user.  This object is passed back from the VognitionInterface.createUser() method.
 */
public class UserCredential {
    public String conKey;
    public String conKeySecret;
}
