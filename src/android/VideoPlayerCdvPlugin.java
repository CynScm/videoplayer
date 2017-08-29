package com.kf580.pluginvideoplayer;

import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by r on 2017/7/11.
 */

public class VideoPlayerCdvPlugin extends CordovaPlugin {

	private final String TAG = "VideoPlayerCdvPlugin";

	CallbackContext context;
	String url;

	Intent intent;
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if(action.equals("play")) {
			String id = args.getString(0);
			String url = args.getString(1);
			Log.i(TAG, "id:"+ id + ",url:" + url);
			intent = new Intent();
			intent.putExtra("id", id);
			intent.putExtra("url", url);
			intent.setClass(this.cordova.getActivity(), VideoPlayerActivity.class);
			this.url = url;
			this.context = callbackContext;
			cordova.startActivityForResult(this, intent, 93);
		} else {
			return false;
		}
		return true;
	}

	//回调方法，从第二个页面回来的时候会执行这个方法
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent result) {
		super.onActivityResult(requestCode, resultCode, result);
		if(resultCode == 13){
			if(requestCode == 93){
					String lastTime = result.getStringExtra("lastTime");
					Log.i(TAG,"lastTime="+lastTime);
					context.success(lastTime);
			}
		}


	}
}
