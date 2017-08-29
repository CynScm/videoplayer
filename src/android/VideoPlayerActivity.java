package com.kf580.pluginvideoplayer;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by r on 2017/7/11.
 */

public class VideoPlayerActivity extends Activity {
    private static final String TAG = "VideoPlayerActivity";
    private static final int resultCode = 13;
    private NormalGSYVideoPlayer mStandardGSYVideoPlayer;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String url;
    Intent intent;

    private Resources resources;

    private String package_name;

    private int getResourceId (String typeAndName)
    {
        if(package_name == null) package_name = getApplication().getPackageName();
        if(resources == null) resources = getApplication().getResources();
        return resources.getIdentifier(typeAndName, null, package_name);
    }


    /**
     * 生成 base auth 字符串
     * @param username 用户名
     * @param password 密码
     * @return baseauth字符串
     */
    public String getBaseAuth(String username, String password) {
        String userpass = username + ":" + password;
        return Base64.encodeToString(userpass.getBytes(), Base64.DEFAULT);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        Log.i(TAG, "videoPlayerActivityOnCreate");

        // 获取只能被本应用程序读、写的SharedPreferences对象
        sp = getSharedPreferences("videoPlayer", MODE_PRIVATE);
        editor = sp.edit();


        intent = getIntent();
        this.intent = intent;
        String id = intent.getStringExtra("id");
        url = intent.getStringExtra("url");



//        setContentView(R.layout.activity_videoplayer);
        setContentView(getResourceId("layout/activity_videoplayer"));

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        initPlayer(id, url);

    }

    private void initPlayer(String id, String url) {

        String filename;
        File cacheFile;
        try {
            filename = URLEncoder.encode(url, "UTF-8");
            cacheFile = new File(this.getCacheDir().toString() + "/" + filename);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            this.finish();
            return;
        }

        Log.i(TAG, "id:" + id + ", url:" + url);


        mStandardGSYVideoPlayer = ((NormalGSYVideoPlayer) findViewById(getResourceId("id/gsyVideoPlayer")));
//        mStandardGSYVideoPlayer.setUp(url, true, cacheFile, "测试视频");

        HashMap<String, String> map = new HashMap<>();

        String authString = getBaseAuth("wangzhen","12345678");



        authString = authString.replace("\n","").replace("\r","");
        map.put("Authorization"," Basic "+authString);


        //调用可以设置头部信息的setup方法,这里第二个参数设置为false，因为目前缓存的方法还没有加上头，先设置不缓存
        mStandardGSYVideoPlayer.setUp(url,false,cacheFile,map,"测试视频");
//        mStandardGSYVideoPlayer.setUp(url,true,cacheFile,"测试视频");
        Log.i(TAG,url+" "+authString);


        // 调整样式
        mStandardGSYVideoPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        mStandardGSYVideoPlayer.getBackButton().setVisibility(View.VISIBLE);

        // 注册回调
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoPlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        VideoPlayerActivity.this.finish();
                    }
                });
            }
        };
        mStandardGSYVideoPlayer.getFullscreenButton().setOnClickListener(listener);
        mStandardGSYVideoPlayer.getBackButton().setOnClickListener(listener);

        mStandardGSYVideoPlayer.setThumbPlay(false);

        // 直接全屏
        mStandardGSYVideoPlayer.setRotateViewAuto(false);
        mStandardGSYVideoPlayer.setLockLand(true);
        mStandardGSYVideoPlayer.setShowFullAnimation(false);

        //设置快进的step，参数越大，step越小
        mStandardGSYVideoPlayer.setSeekRatio(10f);



        setVideoPlay(url);

    }


    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        //这一句离奇重要，activity生命周期的问题
        MediaSavePosition(url);
        this.setResult(13,intent);
        finish();
        super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaSavePosition(url);
        GSYVideoPlayer.releaseAllVideos();

    }

    //记录上次播放的时间
    public void MediaSavePosition(String url) {
        if ((sp != null) && (url != null)) {
            Long mCurrentPosition = GSYVideoManager.instance().getMediaPlayer().getCurrentPosition();
            Long duration = GSYVideoManager.instance().getMediaPlayer().getDuration();
            Log.e(TAG, "mCurrentPosition=" + mCurrentPosition + "duration=" + duration);
            //2s以内的误差认为已经播放完成
            if ((duration > 0) && (duration - mCurrentPosition < 2000)) {
                mCurrentPosition = 0L;
            }
            String position = String.valueOf(mCurrentPosition);
            editor.putString(url, position);
            editor.commit();
            intent.putExtra("lastTime",position);
            Log.e(TAG, "save url=" + url + "position" + position);
          }

    }

    //下次播放的时候从上次播放的时间开始
    public void setVideoPlay(String url) {
        if (url != null) {
            String pos = null;
            Long mCurrentPosition = 0L;
            if (sp != null) {
                pos = sp.getString(url, "");
                Log.e(TAG, "mCurrentPosition=" + mCurrentPosition);
            }
            if ((pos != null) && (pos.length() > 0)) {
                try {
                    mCurrentPosition = Long.parseLong(pos);
                } catch (Exception e) {
                    mCurrentPosition = 0L;
                }
            }
            Log.e(TAG, "url+" + url + "mCurrentPosition" + mCurrentPosition);

            mStandardGSYVideoPlayer.setSeekOnStart(mCurrentPosition);

//            mStandardGSYVideoPlayer.getStartButton().performClick();
            mStandardGSYVideoPlayer.startPlayLogic();

        }
    }

}
