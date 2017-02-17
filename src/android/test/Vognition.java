package test;


import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
//import android.app.Activity;
//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.ProgressDialog;
//import android.app.TaskStackBuilder;
import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

//import com.vognition.opensdk.homeautomation.Constants;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


/**
 * This class is the abstraction for the Vognition cloud, but it's the concrete implementation of the VognitionInterface.  It's two primary methods are recognize and transtext.  Recognize is used when you've recorded and opensdk file
 * and you want the Vognition cloud to do the speech to text translation.  On an Android platform, it is more efficient to simply utilize Googles Speech to Text facilities and submit 
 * the resulting text to Vognition.  To do that, call the transtext method with the text command as input.  Both methods return a VognitionResponse object containing the response from the server.
 * In order for this driver to function, it must have the URL for the Vognition cloud, and all of the consumer and app keys to authenticate and authorize itself.
 *
 * 
 * @author noahternullo
 *
 */
public class Vognition extends CordovaPugin implements VognitionInterface, ShakeEventManager.ShakeListener {

    private final static String TAG = "Vognition";
    private int recCount = 0;

    //this is used to ensure we aren't sending duplicate home profiles to the cloud
    private int previous_home_profile_hash = 0;

    public static String getVERSION() {
        return VERSION;
    }

    public static final String VERSION = BuildConfig.VERSION_NAME;
    //provides a filter for shake events.  onShake calls that happen before this time are ignored.
    private long shakeIgnoreBeforeTime;

    /**
     * Originally apeared at http://stackoverflow.com/questions/3773338/httpdelete-with-body
     */
	 
	  @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
     if (action.equals("createUser")) {
     createUser(callbackContext);
      return true;
    }
    return false;
  }
	 
	 
	 
	 
    @NotThreadSafe
    class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
        public static final String METHOD_NAME = "DELETE";
        public String getMethod() { return METHOD_NAME; }

        public HttpDeleteWithBody(final String uri) {
            super();
            setURI(URI.create(uri));
        }
        public HttpDeleteWithBody(final URI uri) {
            super();
            setURI(uri);
        }
        public HttpDeleteWithBody() { super(); }
    }


    /**
     * Constant representing the ID for the pull down and lock screen notification
     */
    private static final int NOTIFICATION_ID = 2112;
    /**
     * When true we've successfully registered a notification with the notificationManager and that notification is active.
     * When false there is no active notification.  Any previous notification will have been removed.
     */
    private boolean notificationActive = false;

    /** Standard mActivity result: operation canceled. */
    private static final int RESULT_CANCELED    = 0;
    /** Standard mActivity result: operation succeeded. */
    private static final int RESULT_OK           = 1;
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 20;
    private static final int CHECK_TTS_DATA = 12;
	public final static String DEFAULT_DEVELOPMENT_VOGNITION_SERVICE_URL =  "http://sample.whataremindsfor.com:46900";
	public final static String DEFAULT_DEVELOPMENT_APP_KEY = "59c000476cf2a00b9f5bc1585c177276d8a4d75f";
	public final static String DEFAULT_DEVELOPMENT_SECRET_APP_KEY = "01d3cb9455722c2f6760e11f14a05711796abc1a";
	public final static String DEFAULT_DEVELOPMENT_CON_KEY = "09326ebc1e2cad2ae9f4c0fd5d4c7b71db2b0e48";
	public final static String DEFAULT_DEVELOPMENT_SECRET_CON_KEY = "7092379b31cd621c2c1b574e47ec956733fabc1a";
	public final static String DEFAULT_VOGNITION_SERVICE_URL = "https://vrt.whataremindsfor.com:56900";
	public final static String DEFAULT_APP_KEY = "59c000476cf2a00b9f5bc1585c177276d8a4d75f";
	public final static String DEFAULT_SECRET_APP_KEY = "01d3cb9455722c2f6760e11f14a05711796abc1a";
	public final static String DEFAULT_CON_KEY = "09326ebc1e2cad2ae9f4c0fd5d4c7b71db2b0e48";
	public final static String DEFAULT_SECRET_CON_KEY = "7092379b31cd621c2c1b574e47ec956733fabc1a";
	public final static Locale DEFAULT_LOCALE = Locale.EN_US;
	public final static SpeakerType DEFAULT_SPEAKER_TYPE = SpeakerType.usenglishmale;
    public final static String TTS_ENGINE_ID = "com.google.android.tts";
    private final static long COMPLETE_SILENCE_MS = 1500;
	private SpeakerType speakerType = DEFAULT_SPEAKER_TYPE;
	private Locale locale = DEFAULT_LOCALE;
	private String url = DEFAULT_VOGNITION_SERVICE_URL;
	private String appKey = "rsacda87e0ee012345661ac86277dbade773fb";
	private String secretAppKey = "rssfd1bfbdb1a39fd1776c4085d03459625e59ff";
	private String conKey = "";
	private String secretConKey = "";
	private boolean cancelled = false;
	private HttpEntityEnclosingRequestBase httpRequest = null;
    private TextToSpeech tts;
    private SpeechRecognizer recognizer;
    private boolean tts_initialized = false;
    private Context clientContext;
    private Boolean recording = false;
    private VognitionStatusListener currentListener = null;
    private Thread uploadThread = null;
    private ProgressDialog progress = null;
    private VognitionContext currentVContext = null;
    enum ServerOperation {POST, PUT, GET, DELETE}
    private Handler mainThreadLooper = null;            //this allows us to perform functions on the main Thread
    private boolean shake_enabled = false;
    private ShakeEventManager sd = null;
    private VognitionContext persistentContext = new VognitionContext();
    private static boolean recognizer_already_ran_once = false;

    /**
     * Listeners by Composition
     */
    private TextToSpeech.OnInitListener ttsOnInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            Log.d("Vognition", "TextToSpeech: Status Returned was: ["+status+"]");
            if (status == TextToSpeech.SUCCESS) tts_initialized = true;
            else {
                Log.e("Vognition", "TextToSpeech: Initialization failed");
            }
        }
    };




    private RecognitionListener recognitionListener = new RecognitionListener() {
        private long last_called = 0;
        private int last_error = Integer.MAX_VALUE;
        private Boolean readyForSpeech = false;

        @Override
        public void onReadyForSpeech(Bundle params) {
            synchronized (readyForSpeech) {
                Log.d(TAG, "SpeachRecognizer READY_FOR_SPEECH");
                readyForSpeech = true;
            }
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            synchronized (readyForSpeech) {
                Log.d(TAG, "End Of Speech Recognition");
                readyForSpeech = false;
            }
        }



        @Override
        public void onError(int error) {
            Log.e(TAG, "IGO: onError: " + error);
            synchronized (readyForSpeech) {
                if (!readyForSpeech) {
                    //http://stackoverflow.com/questions/31071650/speechrecognizer-throws-onerror-on-the-first-listening
                    Log.d(TAG, "Error before we were ready for speech.");
                    //informListeners(RequestStatus.RequestState.ERROR);

                    //to avoid the situation where we get an error 7 and we weren't ready for speech but do nothing.
                    if (error == 7 && !recognizer_already_ran_once) {
                        recognizer_already_ran_once = true;
                        //restart recognizer
                        recognizer.stopListening();
                        startRecogniztion(currentListener);
                    }
                    return;
                }
            }

            String message = "";
            long current_time = System.currentTimeMillis();
            //only process a message if we're not cancelled, and we weren't just called
            //the latter helps handle being called multiple times for the same error code.
            //TODO determine why this gets called multiple times.
            if (!cancelled && (current_time - last_called > 1500  || last_error != error)) {
                last_called = current_time;
                last_error = error;

                //We've seen multiple error 7's that were eronious.  We now verify before throwing
                if (error != 7) synchronized (recording) {
                    recording = false;
                }

                switch (error) {

                    default:
                    case SpeechRecognizer.ERROR_AUDIO: //3
                    case SpeechRecognizer.ERROR_CLIENT: //5
                        message = "We're sorry.  Speech recognition isn't working on this smart device.";
                        break;

                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: //9
                        message = "We're sorry.  You currently don't have permission to use speech recognition on this device.  Please enable and try again.";
                        break;

                    case SpeechRecognizer.ERROR_NETWORK: //2
                        message = "We're sorry.  The cloud is having difficulties currently.  Please try again later when network quality is better.";
                        break;

                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: //1
                        message = "We're sorry.  Unable to connect to the cloud.  Please try again later when the connected to the network.";
                        break;

                    case SpeechRecognizer.ERROR_NO_MATCH: //7
                        message ="";
                        Log.d(TAG, "Waiting to verify 7");
                        new AsyncTask<Void, Void, Void>() {


                            @Override
                            protected Void doInBackground( final Void ... params ) {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                synchronized (recording) {
                                    if (recording) {
                                        Log.d(TAG, "Error 7 VERIFIED");
                                        informListeners(new VognitionServiceException("We're sorry.  We couldn't hear you properly.  Please try again."));
                                    }
                                    else Log.d(TAG, "Error 7 NOT verified");

                                }
                                return null;
                            }
                        }.execute();
                        break;

                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: //8
                        message = "We're Sorry.  There is currently high usage for speech recognition.  Please try again in a moment.";
                        break;

                    case SpeechRecognizer.ERROR_SERVER: //4
                        message = " We're sorry, the speech recognition in the cloud had a hiccup.  Would you mind saying that again?";
                        break;

                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: //6
                        break;


                }
                if (!message.isEmpty()) informListeners(new VognitionServiceException(message));
            }
            else {
                if (!cancelled) Log.e("Vognition", "RecognitionListener Called again with same code for same issue.  Ignoring.");
            }

        }

        @Override
        public void onResults(Bundle results) {
            readyForSpeech = false;
            int resultCode = RESULT_OK;
            Log.d("Vognition", "RecognizerListener.onResults() Called");
            ArrayList<String> stringResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            //if (stringResults.size() == 0)
            synchronized (recording) {
                recording = false;
            }
            processResults(stringResults, resultCode, currentListener);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            readyForSpeech = false;
            int resultCode = RESULT_OK;
            Log.d("Vognition", "RecognizerListener.onResults() Called");
            ArrayList<String> stringResults = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            //if (stringResults.size() == 0)
            recognizer.cancel();
            synchronized (recording) {
                recording = false;
            }
            processResults(stringResults, resultCode, currentListener);

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };


    private Activity mActivity = null;

    /** Message Handling Aparatus **/
    private HashMap<Integer, VognitionMessageHandler> messageHandlers = new HashMap<Integer, VognitionMessageHandler>();

	

	/***
	 * The app key given to identify this type of an app; provided by WRM4
	 */
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	
	/**
	 * Access method for app_key
	 * @return the currently configured app_key
	 */
	public String getAppKey() {
		return appKey;
	}
	
	/**
	 * The app secret key given to identify this type of an app; provided by WRM4
	 */
	public void setSecretAppKey(String secretAppKey) {
		this.secretAppKey = secretAppKey;
	}
	
	/**
	 * Accessor method for secrety app key
	 * @return the currently configured secret app key
	 */
	public String getSecretAppKey() {
		return secretAppKey;
	}
	
	/**
	 * The locale describes what kind of dialect the speech recognition library should choose from.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * Accessor method for locale
	 * @return the currently configured locale
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 *  
	 * The consumer key 
	 */
	public void setConKey(String conKey) {
		this.conKey = conKey;
	}
	
	/**
	 * Accessor method for con key
	 * @return the currently configure con key
	 */
	public String getConKey() {
		return conKey;
	}
	
	/**
	 * 
	 * Sets the consumer secret key
	 */
	public void setSecretConKey(String secretConKey) {
		this.secretConKey = secretConKey;
	}
	
	/**
	 * Accessor method for the secret con key
	 * @return the currently configure secret con key
	 */
	public String getSecretConKey(){
		return secretConKey;
	}
	
	/**
	 * This is the audio voice dialect that comes back with the response message.
	 */
	public void setSpeakerType(SpeakerType speakerType) {
		this.speakerType = speakerType;
	}
	
	/**
	 * Accessor method for the SpeakerType
	 * @return the current SpeakerType
	 */
	public SpeakerType getSpeakerType() {
		return speakerType;
	}
	
	/**
	 * This is the root RESTful URL against which process requests will be posted.
	 * @param url
	 */
	public void setREST_URL(String url){
        if (url.endsWith("/")) url = url.substring(0,url.lastIndexOf('/')-1);
        Log.d("Vognition", "Base URL = "+url);
		this.url = url;
	}
	
	/**
	 * This method is only used if you have an audio wave file, and you need Vognition to translate that audio wave file.  On the Android, it's
	 * more efficient to utilize googles speech to text utilities on the platform, and submit the resulting text to Vognition via the transtext method.
	 * When using the recognize method, utilize the VoiceRecorder class to generate the sound file.  This class will generate the appropriate
	 * file format for processing on the back end.
	 * @param audioWavFileName The audio bytes in wav form containing the user's command to be recognized and interpreted by Vognition.
	 * @return The Server response as a VognitionResponse Object
	 * @throws java.io.IOException If there is difficulty establishing a connection to the Vognition Server
	 * @throws IllegalArgumentException if the filename passed in doesn't map to an actual file.
	 * @throws org.apache.http.auth.InvalidCredentialsException if the keys provided aren't accepted as valid credentials by Vognition
	 * @throws VognitionServiceException If there is any difficulty in the service completing the request, the JSON parsing, or if there are file/network errors.  Frequently the getCause() method
	 * 							     will reveal the deeper issue
	 */
	public VognitionResponse recognize(String audioWavFileName) throws VognitionServiceException, IllegalArgumentException, InvalidCredentialsException {
		VognitionResponse retval = new VognitionResponse();
		JSONObject JSONresponse = null;
		String response = null;
		int statusCode = -1;

		//errorcheck on input
		File waveFile = new File(audioWavFileName);
		if (!waveFile.exists()) throw new IllegalArgumentException(audioWavFileName+" didn't exist.");

		//ToDo use AndroidDefaultHTTPClient()?
		HttpPost post = new HttpPost(url+"/apiv1/dictation");
        //needed for abort
        httpRequest = post;

		HttpClient client = new DefaultHttpClient();


		MultipartEntity entity = new MultipartEntity( /*HttpMultipartMode.BROWSER_COMPATIBLE */);
		try {
			// For File parameters
			entity.addPart( "appkey", new StringBody(appKey, "text/plain", Charset.forName("UTF-8"))); 	//could also add "text/plain" Charset.forName("UTF-8")
			//Log.d("Vognition", "application key"+appKey.toString());
			entity.addPart( "appsecret", new StringBody(secretAppKey, "text/plain", Charset.forName("UTF-8")));
			entity.addPart( "conkey", new StringBody(conKey, "text/plain", Charset.forName("UTF-8")));
			entity.addPart( "consecret", new StringBody(secretConKey, "text/plain", Charset.forName("UTF-8")));
			entity.addPart( "wav", new FileBody(waveFile)); //could add "application/zip"
			entity.addPart( "locale", new StringBody(locale.text(), "text/plain", Charset.forName("UTF-8")));
			entity.addPart( "ttsSpeakerType", new StringBody(speakerType.text(), "text/plain", Charset.forName("UTF-8")));

			post.setEntity( entity );

			HttpResponse serverResponse = client.execute(post);
			StatusLine sl = serverResponse.getStatusLine();
			statusCode = sl.getStatusCode();
			response = EntityUtils.toString(serverResponse.getEntity());	// Grab the response and put it in a string

	        if (statusCode == 200) {

	          JSONresponse = new JSONObject(response); // Parse the string into a JSON object

	          //download the audio file
	          if (JSONresponse.has("ttsResponsePath")) {
	        	  retval.audioResponseURL = JSONresponse.getString("ttsResponsePath");
	          }
	          retval.success = true;
	          retval.response = JSONresponse.getString("response");
	          retval.textToUser = JSONresponse.getString("ttsResponse");
              retval.session    = JSONresponse.getString("session");

	        }
	        else {
	        	switch (statusCode) {

	        		case 401:	//Unauthorized Authentication Credentials were missing or incorrect
	        				throw new InvalidCredentialsException("The Vognition credentials set, were invalid. ServerResponse: ["+response+"]");

                    case 509:
                            Log.d("Vognition","Received 509 from server.  Cancelling request.");
                            cancel();
                            break;

                    case 510:
                        Log.d("Vognition", "Received 510 from server [Maintenance Mode]");
                        throw new VognitionServiceException("Voice Recognition Servers are currently in maintenance mode.");

	        		default:
	        			String message = response;
	        			try {
	        				 JSONresponse = new JSONObject(response);
	        				 message = JSONresponse.getString("ttsResponse");
	        			} catch (JSONException jse) {
	        				message = response;
	        			}
	        			throw new VognitionServiceException(message);

	        	}
	        }
        } catch (UnsupportedEncodingException e) {
			throw new VognitionServiceException("Couldn't create and add a part of the MultiPartEntity to post", e);
		} catch (ClientProtocolException e) {
			throw new VognitionServiceException("Couldn't execute the POST to the Vognition service.", e);
		} catch (IOException e) {
			throw new VognitionServiceException("Net or file IO issue when trying to execute the POST", e);
		} catch (JSONException e) {
			throw new VognitionServiceException("Unable to create valid JSON from the Vognition service response", e);
		}

		 retval.cancelled = this.cancelled;
		 this.cancelled = false;
	     return retval;

	}

	/**
	 * This is the primary method of communicating with the Vognition service on an Android platform.
     * @param  recognized_text The text to translate into a VognitionResponse Object
	 * @param statusListener
     * @return The Server response as a VognitionResponse Object
	 * @throws java.io.IOException If there is difficulty establishing a connection to the Vognition Server
	 * @throws IllegalArgumentException if the recognized_text is null
	 * @throws org.apache.http.auth.InvalidCredentialsException if the keys provided aren't accepted as valid credentials by Vognition
	 * @throws VognitionServiceException If there is any difficulty in the service completing the request, the JSON parsing, or if there are file/network errors.  Frequently the getCause() method
	 * 							     will reveal the deeper issue 
	 */
	public VognitionResponse transText(String recognized_text, VognitionContext context, VognitionStatusListener statusListener) throws VognitionServiceException, IllegalArgumentException, InvalidCredentialsException {

        if (recognized_text == null) throw new IllegalArgumentException("NULL Text Passed into Transtext");

        if (statusListener!= null) currentListener = statusListener;

        context.put("locale", locale.text());
        context.put("requestType", "ASR");
        context.put("ttsSpeakerType", speakerType.text());
        context.put("sentence", recognized_text);

        return sendToServer("/apiv1/transtext", context, ServerOperation.POST);
	}
	
	
	
	@Override
	public void cancel() {
        Log.d(TAG, "Cancel() Called.");
		cancelled = true;
        //We run this on it's own thread to ensure we aren't on the GUI thread
        //This can easily be replaced with a sync task or other android centric solution.
        if (recording) {
           stopListening();
        }
        else {
            Log.d("CANCEL", "RECORDING WAS FALSE");
        }


		if (httpRequest != null) new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("CANCEL","Cancelling http request");
                httpRequest.abort();
            }
        }).start();

        if (tts != null && tts.isSpeaking()) {
            Log.d("CANCEL","Cancelling any tts");
            tts.stop();
        }
        uploadThread = null; // Kill the uploading thread

		
	}





    @Override
    public void onAppCreation(final Context androidContext, Activity parentActivity) {

        Log.d("Vognition",getVERSION());
        clientContext = androidContext;
        mActivity = parentActivity;
        //creates a handler for us to run actions on the main thread
        mainThreadLooper = new Handler(Looper.getMainLooper());

        //see if we have a tts subsystem
        Log.d("Vognition", "onAppCreations Starting Fragment for Result- TTS");
        //final TextToSpeech.OnInitListener onInitListener = ttsOnInitListener;
        ttsOnInitListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("Vognition", "TextToSpeech: Status Returned was: ["+status+"]");
                if (status == TextToSpeech.SUCCESS) tts_initialized = true;
                else {
                    Log.e("Vognition", "TextToSpeech: Initialization failed");
                }
            }
        };


       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "TTS Starting " + utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "TTS Finished with " + utteranceId);

                }

                /*@Override
                public void onError(String utteranceId, int errorCode) {
                    Log.d(TAG, "TTS Error [" + errorCode + "]: " + utteranceId);
                }


                @Override
                public void onError(String utteranceId) {
                    Log.d(TAG, "TTS Error " + utteranceId);
                }

                @Override
                public void onStop(String utteranceId, boolean interrupted) {
                    Log.d(TAG, "TTS Stopped.  Interupted [" + interrupted + "]: " + utteranceId);
                }
            })
        };*/


        try {
            // run this code in gui less fragment so we can pickup the
            // on mActivity result from inside the mygoogleplay class.
            //TODO pull out fragment as a seperate internal class so we don't duplicate code
            Fragment f = new Fragment() {       //BEGIN FRAGMENT
                @Override
                public void onAttach(Activity activity) {
                    super.onAttach(activity);
                    Log.d("Vognition", "TTS Fragment on Attach");
                    try {
                        Intent checkIntent = new Intent();
                        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                        this.startActivityForResult(checkIntent, CHECK_TTS_DATA);
                    } catch (Exception e) {
                        //There isn't any TTS infrastructure on the platform.
                        Log.e("Vognition", "Unable to check TTS Action "+e.getLocalizedMessage());
                        //point them to install text to speech
                        try {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + TTS_ENGINE_ID)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + TTS_ENGINE_ID)));
                            }
                        } catch (Exception ae) {
                            Log.e("Vognition", "Tried and failed to redirect the user to Google's TTL.  ["+e.getLocalizedMessage()+"]. Continuing.");
                        }

                        //Clean up the fragment because it won't be called again.
                        cleanOutFragment();

                    }


                }

                //TODO figure out if the mActivity is still getting this and disable
                @Override
                public void onActivityResult(int requestCode, int resultCode,
                                             Intent data) {

                    if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                        // success, create the TTS instance
                        tts = new TextToSpeech(androidContext, ttsOnInitListener);
                        //allow the user to set the audio of the TTS subsystem
                        Log.d("Vognition","Setting Control of Volume to User");
                        mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
                        Log.d("Vognition", "TextToSpeech Successfully created tts");
                    } else {
                        Log.d("Vognition", "TextToSpeech Unable to initialize ");
                        // missing data, install it
                        Intent installIntent = new Intent();
                        installIntent.setAction(
                                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(installIntent);
                    }

                    cleanOutFragment();

                }

                //This just cleans up after the fragment
                private void cleanOutFragment() {
                    try {
                        // get an instance of FragmentTransaction from your Activity
                        FragmentManager fragmentManager = mActivity.getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        //remove this fragment
                        fragmentTransaction.remove(this);
                        fragmentTransaction.commit();

                    } catch (Exception e) {
                        Log.e("Vognition", "Unable to remove fragment from Fragment Manager:" + e.getLocalizedMessage());
                    }
                }


                @Override
                public void onDetach() {
                    super.onDetach();
                    Log.d("Vognition", "TTS Fragment detached");
                }

                @Override
                public void onActivityCreated(Bundle bundle) {
                    super.onActivityCreated(bundle);
                    Log.d("Vognition", "TTS Fragment detects activity created");
                }

                //this is to verify the fragment has been removed.
                //you can log or put a breakpoint to verify
                @Override
                public void onDestroy() {
                    super.onDestroy();
                    Log.d("Vognition", "TTS Fragment destroyed");
                }
             }; //END FRAGMENT


            // get an instance of FragmentTransaction from your Activity
            FragmentManager fragmentManager = mActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            //add a fragment
            fragmentTransaction.add(f, "VognitionTTSFragment");
            fragmentTransaction.commit();
        } catch (Exception e) {
            Log.e("Vognition", "TTS Fragment failed to create/launch: "+e.getLocalizedMessage());
            Log.d("Vognition", "Attempting to create TTS manually");
            try {
                // success, create the TTS instance
                tts = new TextToSpeech(androidContext, ttsOnInitListener);
                Log.d("Vognition", "TextToSpeech Successfully created tts");
            } catch (Exception e2) {
                Log.e("Vognition", "TTS Fragment failed, attempt to create tts manually failed. ["+e2.getLocalizedMessage()+"]");

            }
        }


        //setup the recognition subsystem
        recognizer = SpeechRecognizer.createSpeechRecognizer(androidContext);
        recognizer.setRecognitionListener(recognitionListener);


    }

    @Override
    public void onAppResume(Activity parentActivity) {

        //if we've destroyed or lost the tts but it has been initialized before, initialize it again.
        if (tts_initialized && tts == null) tts = new TextToSpeech(clientContext, ttsOnInitListener);
        //if shake is enabled, register it
        if (shake_enabled && sd != null) sd.register();
        if (parentActivity != null) {
            mActivity=parentActivity;
            //creates a handler for us to run actions on the main thread
            mainThreadLooper = new Handler(Looper.getMainLooper());

        }
        else {
            Log.e("Vognition", "onAppResume:  Null passed in for parentActivity");
        }
    }

    @Override
    public void onAppPause(Object appOptions) {

        if (tts != null && tts.isSpeaking()) tts.stop();
        if (shake_enabled && sd != null) sd.deregister();
    }

    @Override
    public void onAppTerminate(Object appOptions) {
        if (tts != null && tts.isSpeaking()) tts.stop();

        //shutdown the tts engine
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }

        //shutdown the recognition engine
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }


        //remove the notification mechanism if we're exiting
        if (notificationActive) {
            enableActivateFromNotification(false, -1);
        }

        Log.d("Vognition", "onAppTerminate() finished");
    }

    @Override
    public Button createSmartMicrophone(VognitionStatusListener listener, VognitionContext context) {
        return null;
    }

    @Override
    synchronized public void recordAndProcessVoiceCommand(final VognitionStatusListener listener, VognitionContext context) {
        try {
            synchronized(recording) {
                if (recording) {
                    Log.e(TAG, "IGO: Nobody should call this method while we're already recording.");
                    return;
                }

                //TODO remove this if it turns out we don't need it
                /*if (recognizer != null) recognizer.destroy();
                //setup the recognition subsystem
                recognizer = SpeechRecognizer.createSpeechRecognizer(clientContext);
                recognizer.setRecognitionListener(recognitionListener);*/

                //trigger recording
                if (SpeechRecognizer.isRecognitionAvailable(mActivity) && !recording /* TODO make sure we aren't processing */) {
                    Log.d("Vognition", "Starting Speech Recognition");
                    if (listener != null) currentListener = listener;


                    //start recording
                    currentVContext = rectifyContextWithPersistentContext(context);
                    Intent recognizerIntent = null;

                    //this handles starting the recognizer, regardless of SDK version.
                    startRecogniztion(listener);
                } else {
                    //indicate that voice recognition isn't available
                    if (!recording) {
                        informListeners(RequestStatus.RequestState.ERROR, "Voice recognition not currently available");
                        Log.d("Vognition", "Cancel, SR NOT AVAILABLE.  recognizer.isAvaliable() [" + SpeechRecognizer.isRecognitionAvailable(mActivity) + "] Recording: [" + recording + "]");
                    } else {
                        Log.d("Vognition", "Speach Recognizer was called but we are already recording");
                    }

                }
            }
        } catch (Exception e) {
            //we do this so that if there is any error in our code it doesn't take out the application."
            //indicate that voice recognition isn't available
            Log.d("Vognition", "Unexpected Exception: "+e.getMessage());
            informListeners(RequestStatus.RequestState.ERROR, "Voice recognition not currently functional.");
        }
    }

    private void startRecogniztion(final VognitionStatusListener listener) {
        Intent recognizerIntent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Log.d("Vognition", "Platform is < Jelly Bean (16)");
            informListeners(RequestStatus.RequestState.RECORDING);
            //ACTION_VOICE_SEARCH_HANDS_FREE is only valid in APK Level 16 and higher
            //customToast("You are on SDK Version: "+Build.VERSION.SDK_INT);
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, COMPLETE_SILENCE_MS);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Recording:  Speak your command.");
            // run this code in gui less fragment so we can pickup the
            // on mActivity result from inside the mygoogleplay class.

            final Intent recIntent = recognizerIntent;

            // run this code in gui less fragment so we can pickup the
            // on mActivity result from inside the mygoogleplay class.
            Fragment f = new Fragment() {
                @Override
                public void onAttach(Activity activity) {
                    super.onAttach(activity);
                    Log.d("Vognition", "VR Fragment on Attach");
                    synchronized (recording) {
                        recording = true;
                    }
                    //if we are speaking, stop.
                    if (tts.isSpeaking()) tts.stop();
                    startActivityForResult(recIntent, VOICE_RECOGNITION_REQUEST_CODE);

                }

                @Override
                public void onActivityResult(int requestCode, int resultCode,
                                             Intent data) {

                    if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
                        Log.d("Vognition", "VR Fragment onActivityResult() VOICE_RECOGNITION_REQUEST_CODE called");
                        ArrayList<String> results = (resultCode == RESULT_OK) ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) : new ArrayList<String>();
                        //if (resultCode == RESULT_CANCELED)

                        processResults(results, resultCode, listener);

                    } else {
                        Log.e("Vognition", ".onActivityResult() Unknown request code received from recording mActivity: [" + requestCode + "]");

                    }

                    // get an instance of FragmentTransaction from your Activity
                    FragmentManager fragmentManager = mActivity.getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    //remove this fragment
                    fragmentTransaction.remove(this);
                    fragmentTransaction.commit();
                }

                @Override
                public void onDetach() {
                    super.onDetach();
                    Log.d("Vognition", "VR Fragment detached");
                }

                @Override
                public void onActivityCreated(Bundle bundle) {
                    super.onActivityCreated(bundle);
                    Log.d("Vognition", "VR Fragment detects activity created");
                }

                //this is to verify the fragment has been removed.
                //you can log or put a breakpoint to verify
                @Override
                public void onDestroy() {
                    super.onDestroy();
                    Log.d("Vognition", "VR Fragment destroyed");
                }
            };
            // get an instance of FragmentTransaction from your Activity
            FragmentManager fragmentManager = mActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            //add a fragment
            fragmentTransaction.add(f, "VognitionVRFragment");
            fragmentTransaction.commit();


        } else {

            Log.d("Vognition", "This is supposed to be for higher than Jelly Bean");

            recognizerIntent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);/* new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);//*/
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, COMPLETE_SILENCE_MS);
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault());
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, (long) 0);
            //recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);

            recording = true;

            //if we are speaking, stop.
            if (tts.isSpeaking()) {
                Log.d(TAG, "Stopping speaking because we are about to record");
                tts.stop();
            }
            recCount++;
            informListeners(RequestStatus.RequestState.RECORDING);
            recognizer.startListening(recognizerIntent);

            Log.d("Vognition", "Done triggering recognizer Recording Count: " + recCount);
        }
    }

    public VognitionResponse verify(String session, VognitionContext context, VognitionStatusListener listener) {
        //wait one second to ensure that the command was successful.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("Vognition", "verifying response - start session:["+session+"]");
        currentListener = listener;
        informListeners(RequestStatus.RequestState.VERIFYING);
        context.put("session", session);
        VognitionResponse response = performRestfulCommand("/apiv1/verify", context, ServerOperation.POST);
        if (response.wasSuccessful()) {
            Log.d("Vognition", "Able to verify the command for session ["+session+"] succeeded.\nMessage: "+response.response);
            informListeners(RequestStatus.RequestState.VERIFIED, response.response);
        }
        else {
            if (response.cancelled) {
                Log.d("Vognition", "Not reporting verified status back because request CANCELLED");
            }
            else {
                Log.d("Vogntion","VERIFIED FAILED for session ["+session+"].\nMessage: "+response.response);
                informListeners(RequestStatus.RequestState.ERROR, response.response);
            }

        }

        return response;

    }

    //======================= Vognition User, Role, HomeProfile Registration & Administration Methods =====================
	
	 
	 
	 
 
	
    @Override
    public UserCredential createUser() {
    callbackContext.success("This Plugin is working Perfectly...Please Have a Look...!!!");
	 return true;
    }



    @Override
    public boolean deleteUser() {
        throw new UnsupportedOperationException("Deleting a user isn't currently allowed from the client");
        /*if (!(getConKey() == null) && !(getSecretConKey() == null)) {
            if (performRestfulCommand("/apiv1/user", new VognitionContext(), ServerOperation.DELETE).success) {
                setConKey("");
                setSecretConKey("");
            }
        }else {
            informListeners(RequestStatus.RequestState.ERROR, "Unable to delete user because no user has been set..");
        }*/
    }

    @Override
    public String createHomeProfile() {
        String homeProfileID = "";
        if (!(getConKey()==null) && !(getSecretConKey() == null)) {
            VognitionContext context = new VognitionContext();
                VognitionResponse response = performRestfulCommand("/apiv1/homeprofile", context, ServerOperation.POST);
                if (response.wasSuccessful()) {
                    homeProfileID = response.getValue(Constants.HOME_PROFILE_ID);
                }
        }
        else {
            informListeners(RequestStatus.RequestState.ERROR, "Unable to create home profile, because the user wasn't provided.");
        }
        return homeProfileID;
    }

    @Override
    public boolean updateHomeProfile(String HPDL_ID, String homeProfile) {
        boolean success = false;
        VognitionContext context = new VognitionContext();
        if (homeProfile.hashCode() != previous_home_profile_hash) {
            context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
            persistentContext.put(Constants.HOME_PROFILE_ID, HPDL_ID);
            context.put("content", homeProfile);
            success = performRestfulCommand("/apiv1/homeprofile", context, ServerOperation.PUT, false).wasSuccessful();
            //only save the home profile if it was successfully uploaded
            Log.d(TAG, "Updating Home Profile");
            if (success) previous_home_profile_hash = homeProfile.hashCode();
        }
        else {
            Log.d(TAG, "updateHomeProfile: Duplicate Home Profile given.  IGNORED");
        }

        return success;

    }

    @Override
    public boolean deleteHomeProfile(String HPDL_ID) {
        VognitionContext context = new VognitionContext();
        context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
        return(performRestfulCommand("/apiv1/homeprofile", context, ServerOperation.DELETE).wasSuccessful());

    }


    @Override
    public boolean createRole(String HPDL_ID, String userConKey, RoleType newRole) {
        String role = "";
        VognitionContext context = new VognitionContext();
        context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
        context.put(VognitionConstants.ROLE, newRole.toString());
        context.put(VognitionConstants.ROLEE_KEY, userConKey);
        return(performRestfulCommand("/apiv1/role", context, ServerOperation.POST).wasSuccessful());
    }

    @Override
    public boolean editRole(String HPDL_ID, RoleType newRole, String roleeID) {
        VognitionContext context = new VognitionContext();
        context.put(VognitionConstants.ROLE, newRole.toString());
        context.put(VognitionConstants.ROLEE_KEY, roleeID);
        context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
        return(performRestfulCommand("/apiv1/role", context, ServerOperation.POST).wasSuccessful());
    }

    @Override
    public boolean deleteRole(String HPDL_ID, String roleeKey) {
        VognitionContext context = new VognitionContext();
        context.put(VognitionConstants.ROLEE_KEY, roleeKey);
        context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
        return(performRestfulCommand("/apiv1/role", context, ServerOperation.DELETE).wasSuccessful());
    }

    @Override
    public boolean acceptRole(String HPDL_ID, boolean acceptRole) {
       VognitionContext context = new VognitionContext();
       context.put("accept", acceptRole?"1":"0");
       context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
       context.put("roleeKey", getConKey());
       return(performRestfulCommand("/apiv1/assignedrolee", context, ServerOperation.POST).wasSuccessful());
    }

    @Override
    public boolean unassignRole(String HPDL_ID, String roleeID) {
        VognitionContext context = new VognitionContext();
        context.put(VognitionConstants.ROLEE_KEY, roleeID);
        context.put(Constants.HOME_PROFILE_ID, HPDL_ID);
        return(performRestfulCommand("/apiv1/assignedrolee", context, ServerOperation.DELETE).wasSuccessful());
    }

    private VognitionResponse performRestfulCommand(String endPoint, VognitionContext context, ServerOperation operation) {
        return performRestfulCommand( endPoint,  context,  operation, false);
    }
    //TODO either all performRestfulCommands pass back information or none of the should
    private VognitionResponse performRestfulCommand(String endPoint, VognitionContext context, ServerOperation operation, boolean requiresMultipart) {
        VognitionResponse response = new VognitionResponse();
        response.success = false;


        try {
            response = sendToServer(endPoint, context, operation, requiresMultipart);
            if (response.wasSuccessful()) {
                informListeners(RequestStatus.RequestState.COMPLETE, response.textToUser);
            }
            else {
                if (response.cancelled) {
                    informListeners(RequestStatus.RequestState.CANCELLED, response.textToUser);
                }
                else {
                    informListeners(RequestStatus.RequestState.ERROR, response.textToUser);
                }
            }
        } catch (InvalidCredentialsException e) {
            //informListeners(RequestStatus.RequestState.ERROR, e.getMessage());
            //TODO in the future fix this on client's side of the fence
            Log.e("Vognition", e.getLocalizedMessage(), e);

        } catch (VognitionServiceException e) {
            //informListeners(RequestStatus.RequestState.ERROR, e.getMessage());
            Log.e("Vognition", e.getLocalizedMessage(), e);
        }

        return response;

    }

    //============================  Vognition Alert Methods   ========================


    public void enableActivateFromNotification(boolean enable) {
        enableActivateFromNotification(enable, Integer.MIN_VALUE);
    }
    public void enableActivateFromNotification(boolean enable, int notificationSmallIcon) {
        //they want to enable the notification and it isn't already active
        if (enable && !notificationActive) {
            Log.d("Vognition", "Enabling Notification");
            NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(mActivity)
                    .setContentTitle("Click For Voice Control")
                    .setContentText("Click here to activate voice control");
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(mActivity, mActivity.getClass());// new Intent(this, ResultActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mActivity);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(mActivity.getClass());
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setOngoing(true);
            mBuilder.setSmallIcon(notificationSmallIcon); if (notificationSmallIcon != Integer.MIN_VALUE)
            // mId allows you to update the notification later on.
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            notificationActive = true;
        }
        else {
            //they want to disable the notification
            if (!enable && notificationActive) {
                //the notification is active
                NotificationManager mNotificationManager =
                        (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(NOTIFICATION_ID);
                notificationActive = false;
            }
            else Log.d("Vognition", "enableActivateFromNotification() called, but was already active.");
        }

    }

    public boolean notificationEnabled() {
        return notificationActive;
    }


    //============================  Vognition Shake Methods   ========================

    /**
     * Enables Shake
     */
    public void enableShake() {
        if (!shake_enabled) {
            initShake(mActivity, Integer.MIN_VALUE, Integer.MIN_VALUE);
        }

    }

    /**
     * Enables a ShakeEventManager with a given number of movements and a set threshold amount.
     * @param movementThreshold The threshold above which an acceleration counts as a movement
     * @param movementCounts The number of movements
     */
    public void enableShake(int movementThreshold, int movementCounts) {

        if (!shake_enabled) {
            initShake(mActivity, movementThreshold, movementCounts);
        }
    }

    /**
     * Initializes a ShakeEventManager with a given number of movements and a set threshold amount.
     * @param movementThreshold The threshold above which an acceleration counts as a movement
     * @param movementCounts The number of movements
     * @param ctx  The activity relating the shake
     */
    private void initShake(Context ctx, int movementThreshold, int movementCounts) {

        if (movementCounts > 0 && movementCounts > 0)
            sd = new ShakeEventManager(movementThreshold, movementCounts);
        else sd = new ShakeEventManager();
        sd.setListener(this);
        sd.init(ctx);
        shakeIgnoreBeforeTime = System.currentTimeMillis()+2000;
        sd.register();
        shake_enabled = true;
    }

    /**
     *
     * @return true if shake is enabled.
     */
    public boolean shakeEnabled() {
        return shake_enabled;
    }


    @Override
    public void onShake() {
        long current_time = System.currentTimeMillis();
        if (current_time > shakeIgnoreBeforeTime) {
            Log.d("Vognition", "OnShake called");
            VognitionContext context = new VognitionContext();
            for (String key : persistentContext.getKeys()) {
                context.put(key, persistentContext.get(key));
            }
            context.put("uiCallTrigger", "shake");
            recordAndProcessVoiceCommand(currentListener, context);
            //prevent it from triggering again too soon. (1.5 s delay)
            shakeIgnoreBeforeTime = current_time + 1500;
        }
    }

    //======================= Vognition Class Management Methods =====================


    /**
     *
     * @param messageHandler
     * @param <T>
     */
    @Override
    public <T extends VognitionMessageHandler> void addMessageHandler(T messageHandler) {
        messageHandlers.put(messageHandler.getMessageNumberHandled(), messageHandler);
    }

    @Override
    public <T extends VognitionMessageHandler> void removeMessageHandler(T messageHandler) {
         messageHandlers.remove(messageHandler.getMessageNumberHandled());
    }

    @Override
    public void addToContext(String key, Object addMe) {
        persistentContext.put(key, addMe);
    }

    @Override
    public void removeFromContext(String key) {
        persistentContext.remove(key);
    }

    public void setListener(VognitionStatusListener listener) {
        this.currentListener = listener;
    }


    /**
     * This handles dealing with a cancelled voice command, and makes the code agnostic to whether it was return via RecognitionListener, or from onActivityResults
     * @param results An ArrayList<String> containing 0 or more results.
     */
    private void  processResults(final ArrayList<String> results, int resultCode, final VognitionStatusListener listener) {
        synchronized (recording) {
            recording = false;
        }
        Log.d("Vognition", "Start");
        final VognitionInterface vognition = this;

        
        try {
            if (resultCode != RESULT_CANCELED) {
                //TODO let the listener know that we are now processing the request
                //we only are interested in the most probable.
                Log.d("Vognition","being called");

                //define spinner and start
                progress = ProgressDialog.show(mActivity, "Please Wait", "Processing your request", true, true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Log.d(TAG, "Cancell being called because DialogInterface was cancelled");
                        vognition.cancel();
                    }
                });
                //necessary to ensure that the user is informed that they cancelled it- must use back button
                progress.setCanceledOnTouchOutside(false);
                //progress = new ProgressDialog(clientContext);
                //progress.setOwnerActivity(mActivity);
                //progress.setTitle("Please Wait");
                //progress.setMessage("Processing your request");
                //progress.setCancelable(false);
                //progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                /*progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vognition.cancel();
                    }
                });
                progress.show();*/

                uploadThread = new Thread(new Runnable() {
                    public void run() {

                        try {
                            //show spinner
                            progress.show();
                            Log.d("Vognition","contacting vognition server");
                            //TODO take action if HPDL is missing
                            String HPDL_ID = (String) currentVContext.get(Constants.HOME_PROFILE_ID);

                            VognitionInterface.VognitionResponse response = transText(results.get(0), currentVContext, listener);



                            Log.d("Vognition", "Returned from Vognition");
                            //check to see if the action has been cancelled
                            if (!response.cancelled) {
                                informListeners(RequestStatus.RequestState.PROCESSING);
                                if (response.wasSuccessful()) {
                                    int messageNumber = VognitionMessageHandler.getMessageNumber(response);
                                    Log.d("Vognition", "I am handling the response");
                                    //ensure that the gui is in there

                                    if (messageHandlers.containsKey(messageNumber)) {
                                        VognitionMessageHandler messageHandler = messageHandlers.get(messageNumber);
                                        Log.d("Vognition", "Calling Handler: " + messageHandler.getClass().getName());
                                        if (messageHandler.handleMessage(response, currentVContext)) {


                                            //TODO Update Listener that the handler handled the message

                                        }
                                        //Assumption that if the message wasn't handled the Handler informed the listener
                                    } else {

                                        Log.d("Vognition", "No Handler for Message");


                                    }

                                    //say what happened.
                                    informListeners(RequestStatus.RequestState.COMPLETE, response.textToUser);

                                    //This will contact the server and wait up to 8 seconds to get confirmation of command success.
                                    //this also handle feedback to the listeners
                                    response = verify(response.session, currentVContext, listener);
                                    //TODO we could probably make this a state change without feedback.
                                    //informListeners(RequestStatus.RequestState.COMPLETE, response.textToUser);
                                }
                                else {
                                    Log.d("Vognition", "Vognition request was returned unsuccessful");

                                    informListeners(RequestStatus.RequestState.ERROR, response.textToUser);
                                }


                            }
                            else {
                                Log.d("Vognition", "Vognition Processing was cancelled, ignoring response");
                                //if they cancel, we don't need to tell them that.
                                informListeners(RequestStatus.RequestState.CANCELLED, "");

                            }
                        } catch (InvalidCredentialsException e) {
                           informListeners(new VognitionServiceException("Ensure the right credentials are supplied.", e));
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            //TODO inform the listener that Vognition returned an unexpected response.  Unable to process.  An update may be necessary");
                            informListeners(RequestStatus.RequestState.ERROR,
                                    "Vognition returned an unexpected response.  You may want to update your software");
                            e.printStackTrace();
                        } catch (VognitionServiceException e) {
                            informListeners(e);
                            e.printStackTrace();
                        } catch (VognitionHandlerException e) {
                            informListeners(e);
                            e.printStackTrace();
                        } catch (Exception e) {
                            informListeners(new VognitionServiceException("Unexpected Exception: ",e));
                            e.printStackTrace();
                        }
                        finally{
                            //take spinner down
                            progress.dismiss();
                        }
                    }
                });

                uploadThread.start();
            }
            else {

                Log.d("Vognition", "RecognitionListener on ResultsCall ignored because RESULTS_CANCELLED");
                informListeners(RequestStatus.RequestState.CANCELLED, "Processing Cancelled by User");

            }

        } catch (IllegalArgumentException e) {

            //TODO inform the listener for the exception customToast("Error: VognitionServiceException");
            Log.e("Vognition","IllegalArgumentException", e);
            e.printStackTrace();
        }
        finally {
            //we're done processing so ensure that the cancel has been reset
            cancelled = false;
        }
    }

    private void informListeners(VognitionServiceException vse) {
        Log.d("Vognition", "informListeners(VognitionServiceException: "+vse.getMessage()+")");
        if (currentListener != null) {
            //TODO:  Merge these functions
            //currentListener.handleServiceException(vse);
            currentListener.handleRequestStatus(new RequestStatus(RequestStatus.RequestState.ERROR, ""));
        }
        else {
            Log.d("Vognition", "No current VognitionStatusListener");
        }

    }

    private void informListeners(VognitionHandlerException vhe) {
        Log.d("Vognition", "informListeners(VognitionHandlerException"+vhe.getMessage()+")");
        if (currentListener != null) {
            //TODO:  Merge these functions
            //currentListener.handleHandlerException(vhe);
            currentListener.handleRequestStatus(new RequestStatus(RequestStatus.RequestState.ERROR, ""));
        }
        else {
            Log.d("Vognition", "No current VognitionStatusListener");
        }
    }

    private void informListeners(RequestStatus.RequestState update) {
        Log.d("Vognition", "IGO: informListeners(StatusChange:"+update.toString()+") " + recCount);
        if (currentListener != null) {
            currentListener.handleRequestStatus(new RequestStatus(update));
        }
        else {
            Log.d("Vognition", "No current VognitionStatusListener");
        }
    }

    private void informListeners(RequestStatus.RequestState update, String details) {
        Log.d("Vognition", "IGO: informListeners(StatusChange:"+update.toString()+", "+details+") " + recCount);
        if (currentListener != null) {

            currentListener.handleRequestStatus(new RequestStatus(update, details));
        }
        else {
            Log.d("Vognition", "No current VognitionStatusListener");
        }
    }


    public boolean ttsAvailable() {
        return (tts!=null && tts_initialized);
    }

    /**
     * This will utilize the platforms TTS engine to speak the string provided.
     * If we are already speaking, it will wait for up to 5 seconds before interrupting ourselves.
     * If we are currently listening to the user at the moment we go to speak, this will return.
     * @param speakMe The text to speak.
     */
    public void speak(String speakMe) {
       //be polite to ourselves.  If we are speaking wait for up to 5 seconds before interrupting
       int num_seconds = 0;

       while (tts.isSpeaking() && num_seconds++ < 5) {
           try {
               Log.d(TAG, "Waiting to speak");
               Thread.sleep(1000);
           } catch (InterruptedException e) {
               //don't care
           }
       }
       //if we are listening at the moment we are going to speak, forget about what we were going to
       //say.
      if  (ttsAvailable() && !recording && speakMe != null  && !speakMe.isEmpty()) {


           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
               String ID = UUID.randomUUID().toString();
               Log.d(TAG, "TTS already speaking: "+tts.isSpeaking()+ " Speaking > Lollipop: "+speakMe+" ID: "+ID);
               int success = tts.speak(speakMe, TextToSpeech.QUEUE_ADD, null, ID);
               Log.d(TAG, "TTS speaking after stimulous: "+tts.isSpeaking()+" return value: "+success);
           } else {
               Log.d(TAG, "TTS already speaking: "+tts.isSpeaking()+ " Speaking: "+speakMe);
               int success = tts.speak(speakMe,TextToSpeech.QUEUE_ADD, null);
               Log.d(TAG, "TTS speaking after stimulous: "+tts.isSpeaking()+" return value: "+success);
           }

       }
    }

    /**
     * Stop listening for user input, but allow tts to continue
     */
    public void stopListening() {
        Log.d(TAG,"stopListening Called");
            if (recording) {

                //this is required because the recognizer's methods can only be called from the
                //main thread
                mainThreadLooper.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,"cancelling the recognizer");
                        recognizer.cancel();
                        synchronized (recording) {
                            recording = false;
                        }
                        //hide the progress spinner
                        try {

                            if (progress!=null && progress.isShowing()) {
                                Log.d(TAG, "Dismissing the progress dialog");
                                progress.hide();
                                //progress.hide();
                            }
                        } catch (Exception e) {
                            Log.d("Vognition","Got exception on progress dialog cancel: " + e.getLocalizedMessage());
                        }
                    }
                });
            }
        else {
                Log.d(TAG,"Recognizer not recording.  No action taken");
            }
    }

    /**
     * This ensures that any information in the persistentContext not also in the rectifyMe context is included.
     * Any information already in the rectifyMe context isn't altered.  In this way it "overwrites" the persistentContext.
     * @param rectifyMe  The context to rectify with the peristentContext
     * @return A Union of the persistentContext and rectifyMe, where rectifyMe wins in a tie.
     */
    protected VognitionContext rectifyContextWithPersistentContext(VognitionContext rectifyMe) {
        for (String key : persistentContext.getKeys()) {
            if (!rectifyMe.contains(key)) rectifyMe.put(key, persistentContext.get(key));
        }

        return rectifyMe;

    }

    protected VognitionResponse sendToServer(String endpoint, ServerOperation operation) throws InvalidCredentialsException, VognitionServiceException {
        return sendToServer(endpoint, new VognitionContext(), operation);
    }

    protected VognitionResponse sendToServer(String endpoint, VognitionContext context, ServerOperation operation) throws VognitionServiceException, InvalidCredentialsException {
        return sendToServer(endpoint, context, operation, false);
    }

    protected VognitionResponse sendToServer(String endpoint, VognitionContext context, ServerOperation operation, boolean multipartRequired) throws VognitionServiceException, InvalidCredentialsException {
        VognitionResponse response = new VognitionResponse();
        JSONObject JSONresponse = null;
        String responseString = null;
        int statusCode = -1;
        HttpDelete httpDelete = null;
        System.out.println("--------Createuser4------");
        //setup the request parameters
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("appkey", appKey));
        System.out.println("--------Createuser5------");
        nameValuePairs.add(new BasicNameValuePair("appsecret", secretAppKey));
        System.out.println("--------Createuser6------");
        if (conKey != null && !conKey.isEmpty()) nameValuePairs.add(new BasicNameValuePair("conkey", conKey));
        if (secretConKey != null && !secretConKey.isEmpty()) nameValuePairs.add(new BasicNameValuePair("consecret", secretConKey));
        System.out.println("--------Createuser7------");

        //Ensure any key within the context is included in the post
        for (String key : context.getKeys()) {
            nameValuePairs.add(new BasicNameValuePair(key, context.get(key).toString()));
            Log.d("Vognition","Context Key: "+key+"\t\tValue: "+context.get(key));
        }
        System.out.println("--------Createuser8------");
        //error check on input
        if (context == null || endpoint == null) throw new IllegalArgumentException("Neither the endpoint nor the context may be null");
        //assemble URL
        String completeURL = url + endpoint;
        //create operation
        switch (operation) {
            case POST:
                httpRequest = new HttpPost(completeURL);
                break;
            case PUT:
                httpRequest = new HttpPut(completeURL);
                break;
            case GET:
                throw new UnsupportedOperationException("The GET HTTP mechanism isn't currently supported within Vognition");
                //httpRequest = new HttpGet(completeURL);
                //break;
            case DELETE:
                    //UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
                    httpRequest = new HttpDeleteWithBody(completeURL);


                break;
            default:
                //TODO error
                break;
        }

        Log.d("Vognition", "Hitting the server at: "+completeURL);
        //TODO use AndroidDefaultHTTPClient()?

        // set the connection timeout value to 30 seconds (10000 milliseconds)
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);
        HttpClient client = new DefaultHttpClient(httpParams);

        try {
            informListeners(RequestStatus.RequestState.SUBMITTING);
            //set the http post parameters into the post
            HttpEntity entity = (multipartRequired) ? convertToMultiPartEntity(nameValuePairs) : new UrlEncodedFormEntity(nameValuePairs);
            httpRequest.setEntity(entity);
            Log.d("Vognition", "URI: " + httpRequest.getURI());

            //actually send the post and receive the response
            HttpResponse serverResponse = client.execute(httpRequest);
            System.out.println("--------Createuser9------//"+serverResponse);
            if (!httpRequest.isAborted()) {
                System.out.println("--------Createuser10------//");
                StatusLine sl = serverResponse.getStatusLine();
                statusCode = sl.getStatusCode();
                System.out.println("--------Createuser10------//");
                //get the JSON in string form (needed to build the actual json)
                responseString = EntityUtils.toString(serverResponse.getEntity());    // Grab the response and put it in a string
                Log.d("Vognition", "Response String was [" + responseString + "]");

                if (statusCode == 200) {
                    //HANDLE BUILDING THE RESPONSE

                    parseVognitionReponse(response, responseString);

                } else {
                    switch (statusCode) {

                        case 401:    //Unauthorized Authentication Credentials were missing or incorrect
                            throw new InvalidCredentialsException("The Vognition credentials set was invalid. ServerResponse: [" + responseString + "]");


                        case 509:
                            Log.d("Vognition","Received 509 from server.  Cancelling request.");
                            cancel();
                            break;

                        default:
                            String message = responseString;
                            try {
                                Log.d("Vognition", "Response from server was: " + responseString);
                                JSONresponse = new JSONObject(responseString);
                                message = JSONresponse.getString("ttsResponse");
                            } catch (JSONException jse) {
                                message = responseString;
                            }
                            throw new VognitionServiceException(message);

                    }
                }
            }
            else {
                //HTTP REQUEST WAS ABORTED
                Log.d("Vognition","sendToServer HttpRequest was Aborted");
                response.cancelled = true;
            }
        } catch (UnsupportedEncodingException e) {
            VognitionServiceException vse =   new VognitionServiceException("Couldn't create and add a part of the key value pairs to the post", e);
            throw vse;
        } catch (ClientProtocolException e) {
            VognitionServiceException vse =  new VognitionServiceException("Couldn't execute the ["+operation+"] to the Vognition service.", e);
            throw vse;

        } catch (IOException e) {
            //sometimes a cancellation can loog like
            if (!cancelled) {
                //VognitionServiceException vse = new VognitionServiceException("Sorry, the vognition service is currently unavailable.  Please try again.", e);
                VognitionServiceException vse = new VognitionServiceException("");
                Log.d("Vognition", "Transtext IOException "+e.getMessage()+" "+e.getStackTrace());
                throw vse;
            }
            else {
                Log.d("Vognition","sendToServer IOException Recieved ["+e.getMessage()+"] but operation was Cancelled.");
            }
        } catch (JSONException e) {
            VognitionServiceException vse =  new VognitionServiceException("Unable to create valid JSON from the Vognition service response: "+e.getMessage(), e);
            throw vse;
        }finally {
            //TODO Refactor all the similar methods into a single method. Transtext, SendToServer
            if (this.cancelled) {
                response.cancelled = this.cancelled;
                this.cancelled = false;
                Log.d("Vognition", "Vognition.CANCELLED == TRUE.  Setting sendToServer VognitionResponse.cacelled = TRUE, resetting Vognition.cancelled to FALSE");
            }
        }

        return response;
    }

    /**
     * This method takes the string from the JSON returned from the server and creates a VognitionResponse out of it.
     * @param response
     * @param responseString
     * @throws JSONException
     */
    private void parseVognitionReponse(VognitionResponse response, String responseString) throws JSONException {
        JSONObject JSONresponse;
        JSONresponse = new JSONObject(responseString); // Parse the string into a JSON object

        //provide the URL to the audio item if it exists
        if (JSONresponse.has("ttsResponsePath"))
            response.audioResponseURL = JSONresponse.getString("ttsResponsePath");
        if (JSONresponse.has("response")) {
            response.response = "";
            //detect the condition where Vognition is sending back an array of responses.
            //if it is, keep the responses, but append message type 9999 to identify them.
            //TODO standardize this approach
            if ((JSONresponse.optJSONArray("response")!=null)) response.response = "9999:";
            response.response += JSONresponse.getString("response");

        }
        if (JSONresponse.has("ttsResponse"))
            response.textToUser = JSONresponse.getString("ttsResponse");
        if (JSONresponse.has("session"))
            response.session = JSONresponse.getString("session");

        //m and 0 are considered a success
        if (JSONresponse.has("response_code")) {
            switch (JSONresponse.getString("response_code").toLowerCase().charAt(0)) {
                case 'm':
                case '0':
                    response.success = true;
                    break;

                default:
                    Log.e("Vognition","Unsuccessful response code from server "+JSONresponse.getString("response_code"));
                    response.success = false;
            }
        }
        else response.success = false;

        Log.d("Vognition", "response.success = " + response.success + " & response.wasSuccessful=" + response.wasSuccessful() + " JSONResponse.has(response_code)=" + JSONresponse.has("response_code"));
        //iterate through JSON Response and put all key value pairs as string
        VognitionContext responseMap = new VognitionContext();
        Iterator<String> keyIterator = JSONresponse.keys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            Log.d("Vognition", "Found " + key);
            responseMap.put(key, JSONresponse.getString(key));
        }
        response.setResponseMap(responseMap);
    }

    //NOTE THIS IS NOT CURRENTLY USED
    private HttpEntity convertToMultiPartEntity(List<NameValuePair> nameValuePairs) throws IOException {
        MultipartEntity entity = new MultipartEntity( /*HttpMultipartMode.BROWSER_COMPATIBLE */);

        for (NameValuePair pair : nameValuePairs) {
            if (pair.getName().equalsIgnoreCase("content")) {
                Log.d("Vognition", "Creating tempfile to send home profile");
                final File tempFile = File.createTempFile("tempvognitiondata", "dat");
                tempFile.deleteOnExit();
                FileUtils.writeStringToFile(tempFile, pair.getValue());
                entity.addPart(pair.getName(), new FileBody(tempFile));
            } else {
                entity.addPart(pair.getName(), new StringBody(pair.getValue(), "text/plain", Charset.forName("UTF-8")));
            }

        }

        return entity;
    }

    public boolean getIsRecording() {
        synchronized (recording) {
            return recording;
        }
    }
}
