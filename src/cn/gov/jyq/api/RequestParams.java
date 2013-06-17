package cn.gov.jyq.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class RequestParams {
    private static String ENCODING = "UTF-8";
    protected ArrayList<BasicNameValuePair> mUrlParams;
    
    public RequestParams() {
    	mUrlParams = new ArrayList<BasicNameValuePair>();
    }

    public void put(String key, String value){
        if(key != null && value != null) {
        	mUrlParams.add(new BasicNameValuePair(key, value));
        }
    }
    
    public void put(String key, int value) {
    	put(key, String.valueOf(value));
    }
    
    public void put(String key, float value) {
    	put(key, String.valueOf(value));
    }
    
    public void put(String key, double value) {
    	put(key, String.valueOf(value));
    }
    
    public <T> void put(String key, List<T> list) {
    	for(T value : list) {
    		mUrlParams.add(new BasicNameValuePair(key, value.toString()));
    	}
    }
    
    public String[]	getKeyArray() {
    	String[] array = new String[mUrlParams.size()];
    	for(int i = 0; i < mUrlParams.size(); i++) {
    		BasicNameValuePair pair = mUrlParams.get(i);
    		array[i] = pair.getName();
    	}
    	return array;
    }
    
    public String getValue(String key) {
    	for(BasicNameValuePair pair : mUrlParams) {
    		if(pair.getName().equals(key)) {
    			return pair.getValue();
    		}
    	}
    	return null;
    }
    
    public void remove(String key){
    	mUrlParams.remove(key);
    }
    
    public byte[] getParamsBytes() {
    	return getParamsString().getBytes();
    }
 
    public String getParamsString() {
        return URLEncodedUtils.format(mUrlParams, ENCODING);
    }
}
