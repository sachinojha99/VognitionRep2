package test;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


/**
 * This class echoes a string called from JavaScript.
 */
public class TestAppPlugin extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callbackContext.success("Welcome to Sachin's Cordova Plugin!");
        return true;
    }

		
}
