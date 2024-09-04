package com.m.bin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MyJSONObject extends JSONObject {
    public MyJSONObject(String source) throws JSONException {
        super(source);
    }

    @Override
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) {
        try {
            return super.getEnum(clazz, key);
        }catch (JSONException e){return null;}
    }

    @Override
    public boolean getBoolean(String key){
        try {
            return super.getBoolean(key);

        }catch (JSONException e){return false;}
    }

    @Override
    public double getDouble(String key){
        try {        return super.getDouble(key);

        }catch (JSONException e){return 0;}
    }

    @Override
    public float getFloat(String key) {
        try {        return super.getFloat(key);

        }catch (JSONException e){return 0;}
    }

    @Override
    public Number getNumber(String key){
        try {        return super.getNumber(key);

        }catch (JSONException e){return null;}
    }

    @Override
    public int getInt(String key){
        try {        return super.getInt(key);

        }catch (JSONException e){return 0;}
    }

    @Override
    public JSONArray getJSONArray(String key){
        try {
            return super.getJSONArray(key);
        }catch (JSONException e){return null;}
    }

    @Override
    public JSONObject getJSONObject(String key){
        try {
            return super.getJSONObject(key);
        }catch (JSONException e){return null;}
    }

    @Override
    public long getLong(String key){
        try {        return super.getLong(key);
        }catch (JSONException e){return 0;}
    }

    @Override
    public String getString(String key){
        try {
            return super.getString(key);
        }catch (JSONException e){return null;}
    }
}
