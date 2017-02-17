package com.vognition.opensdk;

import java.util.HashMap;
import java.util.Set;

/**
 * This class holds the environmental context for a VognitionMessageHandler for use during it's handleMessage() operation.
 * Any objects a messageHandler needs to deal with in order to perform it's function should be inserted into this context, and available via a "well known" key.
 * It is OK for applications to change what's in the context, or internally update the referenced objects anytime before or after a handler working with it, but not during.
 * One example usage would be the development of a ChangeTemperature VognitionMessageHandler the obtains the thermostat to modify through the context.
 *
 * Created by noahternullo on 11/21/14.
 */
public class VognitionContext {

    private HashMap<String, Object> context = new HashMap<String, Object>();

    /**
     * Allows inserting a key value pair into the context
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        context.put(key, value);
    }

    /**
     * Retrieves the object associated with the Key or null if nothing
     * @param key
     * @return the object associated with the key or null if nothing
     */
    public Object get(String key) {
        return context.get(key);
    }

    /**
     * Removes a key and it's reference object if it exists in the context.
     * @param key
     */
    public void remove(String key) {
        context.remove(key);
    }

    /**
     * inidicates whehter the given key exists in the context
     * @param key
     * @return true if the key exists in the context
     */
    public boolean contains(String key) {
        return context.containsKey(key);
    }

    /**
     *
     * @return All the keys in the context
     */
    public Set<String> getKeys() { return context.keySet();}

}
