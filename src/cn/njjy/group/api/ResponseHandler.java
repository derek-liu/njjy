package cn.njjy.group.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ResponseHandler {
	public static final int STATUS_OK = 200;
	public static final int STATUS_PENDING = 202;
	public static final int STATUS_NO_ACCESS = 203;
	public static final int STATUS_NO_CONTENT = 204;
	public static final int STATUS_NO_AUTHORIZED= 401;
	
	public static final int STATUS_ERROR = 500;
	public static final int STATUS_SERVER_ERROR = 500;
	public static final int STATUS_NETWORK_ERROR = 600;
	
	private Handler mHandler;
	
	public ResponseHandler() {
		if(null != Looper.myLooper()) {
			mHandler = new Handler(new Handler.Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					if(msg.arg1 < STATUS_ERROR) {
						onSuccess(msg.arg1, (String)msg.obj);
					} else {
						onFailed(msg.arg1);
					}
					onFinished();
					
					return true;
				}
			});
		}
	}
	
	void sendResponseMessage(int status) {
		sendResponseMessage(status, null);
	}
	
	void sendResponseMessage(int status, String response) {
		Message msg = mHandler.obtainMessage();
		msg.arg1 = status;
		msg.obj = response;
		
		mHandler.sendMessage(msg);
	}
	
	public void onSuccess(int status, String content){}
	public void onFailed(int status){
		//
	}
	public void onFinished() {}
	
	public void onStart() {}
}
