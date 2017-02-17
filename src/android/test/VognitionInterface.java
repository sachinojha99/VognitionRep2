package com.vognition.opensdk;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;

import org.apache.http.auth.InvalidCredentialsException;

/**
 * This defines the public interface for interacting with the Vognition cloud.
 */
public interface VognitionInterface {

    /**
	 * Interface ENUMERATIONS
	 */



    enum RoleType {
        owner,
        friendOrFamily,
        installer,
        other
    }

	enum SpeakerType {
		/**US English Female */
		usenglishfemale,
		/**US English Male	*/
		usenglishmale,		
		/**English Female*/
		ukenglishfemale,	
		/**UK English Male */
		ukenglishmale,		
		/**Australian English Female*/
		auenglishfemale,		
		/**US Spanish Female	*/
		usspanishfemale,	
		/**US Spanish Male*/
		usspanishmale,	
		/**Chinese Female*/
		chchinesefemale,	
		/**Chinese Male*/
		chchinesemale,			
		/**Hong Kong Cantonese Female*/
		hkchinesefemale,
		/**Taiwan Chinese Female	*/
		twchinesefemale,	
		/**Japanese Female*/
		jpjapanesefemale,	
		/**Japanese Male*/
		jpjapanesemale,	
		/**Korean Female	*/
		krkoreanfemale,		
		/**Korean Male	*/
		krkoreanmale,
		/**Canadian English Female */
		caenglishfemale,
		/**Hungarian Female*/
		huhungarianfemale,
		/**Brazilian Portuguese Female*/
		brportuguesefemale,
		/**European Portuguese Female*/
		eurportuguesefemale,
		/**European Portuguese Male*/
		eurportuguesemale,
		/**European Spanish Female*/
		eurspanishfemale,
		/**European Spanish Male*/
		eurspanishmale,
		/**European Catalan Female*/
		eurcatalanfemale,
		/**European Czech Female*/
		eurczechfemale,
		/**European Danish Female*/
		eurdanishfemale,
		/**European Finnish Female*/
		eurfinnishfemale,
		/**European French Female*/
		eurfrenchfemale,
		/**European French Male*/
		eurfrenchmale,
		/**European Norwegian Female*/
		eurnorwegianfemale,
		/**European Dutch Female*/
		eurdutchfemale,
		/**European Polish Female*/
		eurpolishfemale,
		/**European Italian Female*/
		euritalianfemale,
		/**European Italian Male*/
		euritalianmale,
		/**European Turkish Female*/
		eurturkishfemale,
		/**European Turkish Male*/
		eurturkishmale,
		/**European German Female*/
		eurgermanfemale,
		/**European German Male*/
		eurgermanmale,
		/**Russian Female*/
		rurussianfemale,
		/**Russian Male*/
		rurussianmale,
		/**Swedish Female*/
		swswedishfemale,
		/**Canadian French Female*/
		cafrenchfemale;
		
		public String text() {
			return toString();
		}
	};

    /**
     * Used for dictation
     */
	enum Locale {
		/**English (United States)	locale=*/
		EN_US("en-US"),
		/**English (Canada)	locale=*/
		EN_CA("en-CA"),
		/**English (United Kingdom)	locale=*/
		EN_GB("en-GB"),
		/**English (Australia)	locale=*/
		EN_AU("en-AU");
		
		private String locale_text = null;
		
		Locale(String text) {
			this.locale_text = text;
		}
		
		String text() {
			return locale_text;
		}
		
	};
	
	

	/***
	 * The app key given to identify vognition enterprise customer; provided by Vognition
	 */
	public void setAppKey(String appKey);
	
	/**
	 * The app secret key given to authenticate the identiy of the enterprise customer; provided by Vognition
	 */
	public void setSecretAppKey(String secretAppKey);
	
	/**
	 * The locale describes what kind of dialect the speech recognition library should choose from.
	 */
	public void setLocale(Locale locale);
	
	/**
	 *
	 * The consumer key generated identifying the individual user; provided by Vognition
	 */
	public void setConKey(String conKey);
	
	/**
	 *
	 * The consumer secret authenticates the consumer key.; provided by Vognition
	 */
	public void setSecretConKey(String secretConKey);
	
	/**
	 * This is the audio voice dialect that comes back with the response message.
	 */
	public void setSpeakerType(SpeakerType speakerType);
	
	/**
	 * This allows setting of the entry URL to the vognition cloud
	 * @param url The location of the vognition web api
	 */
	public void setREST_URL(String url);
	
	/**
	 * Vognition response to all translation focused methods.
     * This captures whether the action was cancelled, successful, what the test back to the user should be
     * what the actual command response from Vognition was, and an AudioURL where the tts of the textToUser may be available.
     *
	 * @author noahternullo
	 *
	 */
	public class VognitionResponse {

        VognitionContext responseMap = null;

        public VognitionResponse() {}

        public VognitionResponse(VognitionContext responseMap) {
            this.responseMap = responseMap;
        }

        public void setResponseMap(VognitionContext responseMap) {
            this.responseMap = responseMap;
        }

		/**
		 * Indicates that the operation was cancelled before completion
		 */
		public boolean cancelled = false;
		
		/**
		 * Was the recognize command a success
		 */
		public boolean success = false;
		/**
		 * This is the ttsText OMMS returned to be displayed to the user
		 */
		public String textToUser = "";
		/**
		 * This is the actual response command message as a string
		 */
		public String response = "";
		/**
		 * This is the reponse audio to be played to the user
		 */
		public String audioResponseURL = null;

        /**
         * This is the session number of the transaction that created this response.  Useful in verifying requests
         * Can be null
         */
        public String session = null;

        public String getValue(String key) {
            return (responseMap!=null)?responseMap.get(key).toString():null;
        }
		
		/**
		 * 
		 * @return true if the recognize function was successful
		 */
		public boolean wasSuccessful() {
			return success;
		}
		
		public boolean wasCancelled() {
			return cancelled;
		}
	}
	
	/**
	 * This method is only used if you have an audio wave file, and you need Vognition to translate that audio wave file.  On the Android, it's
	 * more efficient to utilize Google's speech to text utilities on the platform, and submit the resulting text to Vognition via the transtext method.
	 * When using the recognize method, utilize the VoiceRecorder class to generate the sound file.  This class will generate the appropriate
	 * file format for processing on the back end.
	 * @param audioWavFile The audio bytes in wav form containing the user's command to be recognized and interpreted by Vognition.
	 * @return The Server response as a VognitionResponse Object
	 * @throws java.io.IOException If there is difficulty establishing a connection to the OMMS Server
	 * @throws IllegalArgumentException if the filename passed in doesn't map to an actual file.
	 * @throws org.apache.http.auth.InvalidCredentialsException if the keys provided aren't accepted as valid credentials by OMMS
	 * @throws VognitionServiceException If there is any difficulty in the service completing the request, the JSON parsing, or if there are file/network errors.  Frequently the getCause() method
	 * 							     will reveal the deeper issue
	 */
	public VognitionResponse recognize(String audioWavFile) throws VognitionServiceException, IllegalArgumentException, InvalidCredentialsException;

	/**
	 * This is the primary method of communicating with the Vognition service on an Android platform.
     * @param  recognized_text The text to translate into a VognitionResponse Object
	 * @param statusListener
     * @return The Server response as a VognitionResponse Object
	 * @throws java.io.IOException If there is difficulty establishing a connection to the OMMS Server
	 * @throws IllegalArgumentException if the recognized_text is null
	 * @throws org.apache.http.auth.InvalidCredentialsException if the keys provided aren't accepted as valid credentials by OMMS
	 * @throws VognitionServiceException If there is any difficulty in the service completing the request, the JSON parsing, or if there are file/network errors.  Frequently the getCause() method
	 * 							     will reveal the deeper issue 
	 */
	public VognitionResponse transText(String recognized_text, VognitionContext context, VognitionStatusListener statusListener) throws VognitionServiceException, IllegalArgumentException, InvalidCredentialsException;
	/**
	 * This cancels any in progress interaction with the server.  
	 * This will cause the recognize method to return a result indicating it was cancelled.
	 */
	public void cancel();


    /**
     * ===================== SHAKE METHODS =======================
     */
	/**
	 * This enables recordAndProcessVoiceCommand on Shake
	 * @param number_of_shakes The total number of movements in the shake gesture
	 * @param shake_threshold The threshold above which it's considered to be part of a gesture
	 */
    public void enableShake(int number_of_shakes, int shake_threshold);

	/**
	 * Uses the default number of shakes and shake threshold
	 */
	public void enableShake();
    public boolean shakeEnabled();

	/**
	 * ===================== NOTIFICATION METHODS =======================
	 */
	/**
	 * Then engages the ongoing notificiation that can be used to trigger recordAndProcessVoiceCommand
	 * @param enable Whether you are turning the notification on or off
	 * @param notificationSmallIcon The R.small icon reference
	 */
	public void enableActivateFromNotification(boolean enable, int notificationSmallIcon);
	public void enableActivateFromNotification(boolean enable);
	public boolean notificationEnabled();

    /**
     * ==================  LIFECYCLE METHODS  ====================
     */

    /**
     * This method must be called in an Android app's onStart() method.
     * @param parentActivity This is the activity we can attach framents and dialogs to.
     *
     */
    public void onAppCreation(final Context androidContext, Activity parentActivity);

    /**
     * This method must be called in an Android app's onResume() method
     * @param parentActivity This is the activity we can attach framents and dialogs to.
     */
    public void onAppResume(Activity parentActivity);

    /**
     * This method must be called in an Android app's onAppPause() method
     * @param appOptions TBD, null is acceptable
     */
    public void onAppPause(Object appOptions);
    /**
     * This method must be called in an Android app's onStop() method
     * @param appOptions TBD, null is acceptable
     */
    public void onAppTerminate(Object appOptions);

    /**
     * ============ One Click Button to record and process voice command ============
     */

    /**
     * This method programatically creates a gui element to handle the capture and processing of a voice command.
     * This button must be added to the view where the user is intended to activate Vogntion.
     *
     * @param listener A listener which will be provided feedback througout the Vogntion voice command process
     * @param context  The context object wich will be handed to any VognitionMessageHandlers given the VognitionResponse
     * @return A Button with the Vognition logo and tied to the Vognition subsystem
     *
     */
    public Button createSmartMicrophone(VognitionStatusListener listener, VognitionContext context);
    public void recordAndProcessVoiceCommand(VognitionStatusListener listener, VognitionContext context);

    /**
     * ============ Vognition Text To Speech Support =================
     */
    /**
     *
     * @return true if the text to speech subsystem is available and ready to use
     */
    public boolean ttsAvailable();

    /**
     * This method submits text to the text to speech subsystem for audio output.  Please note that
     * a Vognition.speakerType has no effect on this audio output.
     * @param speakMe The text to speak.
     */
    public void speak(String speakMe);

    /**
     * ============ Vognition Registration Process ==========
     */

    /**
     * This registration method creates a new user within the Vognition cloud.  Users are represented by conkeys, and authenticated by the possession of their consecret key.
     * Users are automatically associated to the appkey that they are created with, and are guaranteed to be unique to any other user associated with the same appkey.
     * It is assumed that an instance of Vognition is associated to a single user, and therefore conkey.  Calling this method automatically configured Vognition with the returned
     * user credentials.  There is no need to call setConkey, or setConSecret.  This configuration isn't persisted, however.  It is the responsibility of the client to store the passed back
     * credentials and reuse them for any future instance of the Vognition object.
     * @return The conkey and consecret key of the new user.  If the method is unsuccessful, null will be returned.
     */
    public UserCredential createUser();

    /**
     * This method is currently unsupported within the Vognition client.  Please use the administrative interface to delete a user.
     */
    public boolean deleteUser();

    /**
     * This method must be called when a new HomeProfile is to be created.  This method simply obtains a unique ID for the new HomeProfile.  To actually define the home profile itself
     * updateHomeProfile must be called.
     * @return The unique ID for the home profile, or an empty string if unsuccessful.
     */
    public String createHomeProfile();

    /**
     * This actually defines, or updates a home profile.  The home profile ID must already exists.  This method is usually called after createHomeProfile.
     * @param HPDL_ID The unique ID of the home profile to define or update.
     * @param HomeProfile A String which is the definition of the HomeProfile (either in JSON or XML)
     * @return true is no error returned by the Vognition cloud, false otherwise
     */
    public boolean updateHomeProfile(String HPDL_ID, String HomeProfile);

    /**
     * This method deletes an existing home profile from the Vognition cloud.
     * @param HPDL_ID the unique ID of the vognition cloud.
     * @return true if no error returned from the Vognition cloud, false otherwise.
     */
    public boolean deleteHomeProfile(String HPDL_ID);

    /**
     * This method creates a role and associates that role with a particular User, and home profile.  This method must be called by the user who owns the home profile
     * to which the role is being associated.
     * @param HPDL_ID The unique ID associated with the home profile to add the role to.
     * @param roleeKey The conkey for the user fulfilling the new role
     * @param newRole The role to be fulfilled.
     * @return true if no error returned by the Vognition cloud, false otherwise
     */
    public boolean createRole(String HPDL_ID, String roleeKey, RoleType newRole);

    /**
     * This method allows changing the role type for a given user on a specific home profile.  The use must already have a role assigned to them and be associated with the specific home profile.
     * @param HPDL_ID  The ID of the home profile the role to alter is associated with
     * @param newType  The new role
     * @param roleeID  The conkey of the user currently fulfilling the role to be edited
     * @return true if no error returned by the Vognition cloud, false otherwise
     */
    public boolean editRole(String HPDL_ID, RoleType newType, String roleeID);

    /**
     * This removes an existing role associated with a specific home profile.
     * @param HPDL_ID The home profile ID to which the role is associated
     * @param roleeID The conkey of the user currently fulfilling the role to be deleted
     * @return true if no error returned by the Vognition cloud, false otherwise
     */
    public boolean deleteRole(String HPDL_ID, String roleeID);

    /**
     * When the owner of a home profile wishes to grant priveledges on that home profile to a second user, they create a role.  The second user is called the rolee.
     * The user who is the rolee must accept this role before they can fulfill it.  A rolee may also refuse the role as well.
     * @param HPDL_ID The home profile ID to which the role is associated
     * @param acceptRole True is the rolee is accepting, false otherwise
     * @return true if no error returned by the Vognition cloud, false otherwise.
     */
    public boolean acceptRole(String HPDL_ID, boolean acceptRole);

    /**
     * The owner of a home profile may remove a rolee form having access to that home profile.  This method allows the owner of a home profile to
     * remove the existing relationship between that home profile and another user they have granted a role to (rolee).
     * @param HPDL_ID The home profile ID to which the role is associated
     * @param roleeID The conkey of the user currently fulfilling the role to be unassigned
     * @return true if no error returned by the Vognition cloud, false otherwise
     */
    public boolean unassignRole(String HPDL_ID, String roleeID);


    /**
     * ============ Message Handler Methods ===========
     */
    public <T extends VognitionMessageHandler> void addMessageHandler(T messageHandler);
    public <T extends VognitionMessageHandler> void removeMessageHandler(T messageHandler);
    public void addToContext(String key, Object addMe);
    public void removeFromContext(String key);

    /**
     * This method allows a client to set a single listener once for all method calls, including registration.
     * @param listener The listener to receive status throughout all methods.
     */
    public void setListener(VognitionStatusListener listener);


	public boolean getIsRecording();


}
