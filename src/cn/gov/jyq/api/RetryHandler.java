package cn.gov.jyq.api;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.NoHttpResponseException;

import android.os.SystemClock;

public class RetryHandler {
    private static final int RETRY_SLEEP_TIME_MILLIS = 1500;
    private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
    private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

    static {
        exceptionWhitelist.add(NoHttpResponseException.class);
        exceptionWhitelist.add(UnknownHostException.class);
        exceptionWhitelist.add(SocketException.class);

        exceptionBlacklist.add(InterruptedIOException.class);
        exceptionBlacklist.add(SSLHandshakeException.class);
    }

    private final int maxRetries;

    public RetryHandler(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
	public boolean retryRequest(IOException exception, int executionCount) {
		boolean retry;

	    if(executionCount > maxRetries) {
	    	retry = false;
	    } else if (exceptionBlacklist.contains(exception.getClass())) {
	    	retry = false;
	    } else if (exceptionWhitelist.contains(exception.getClass())) {
	    	retry = true;
	    } else {
	        retry = true;   
	    }

	    if(retry) {
	        SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
	    } else {
	    	
	    }
	    return retry;
	}
}
