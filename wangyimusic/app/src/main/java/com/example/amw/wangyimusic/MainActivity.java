package com.example.amw.wangyimusic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private MediaPlayer mMedia;
    private Button mPlay, mPause, mStop;
    private TextView mPlayTime, mAllTime;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        getPermission();
    }



    private void initView() {

        mPlay = (Button) findViewById(R.id.button_play);
        mPause = (Button) findViewById(R.id.button_pause);
        mStop= (Button) findViewById(R.id.button_stop);
        mPlay.setOnClickListener(this);
        mPause.setOnClickListener(this);
        mStop.setOnClickListener(this);

        mPlayTime = (TextView)findViewById(R.id.played_time);
        mAllTime = (TextView)findViewById(R.id.all_time);

        mSeekBar = (SeekBar)findViewById(R.id.seek_bar);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b == true) {
                        mMedia.seekTo(i);
                        //mPlayTime.setText(i/60000+":"+i/1000%60);
                    }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public void onClick(View view) {
       
        switch (view.getId()){
            case R.id.button_play:
                Log.d(TAG, "onClick: start");
                if (!mMedia.isPlaying()){
                    mMedia.start();
                    Message message = handle.obtainMessage();
                    message.what =1;
                    message.arg1 = mMedia.getDuration();
                    handle.sendMessage(message);
                    handle.post(updateThread);
                }

                break;
            case R.id.button_pause:
                if (mMedia.isPlaying()){
                    mMedia.pause();
                    handle.removeCallbacks(updateThread);
                }

                break;
            case R.id.button_stop:

                //if (mMedia.isPlaying()){
                    mMedia.stop();
                    mMedia.reset();
                    Message message = handle.obtainMessage();
                    message.what =3;
                    handle.sendMessage(message);
                    initMedia();
               // }

                break;
            default:
                break;
        }

    }

    private void getPermission() {

        if (Build.VERSION.SDK_INT >=23){
            int checkPremissoin = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            Log.d(TAG, "getPermission: ="+checkPremissoin);
            if (checkPremissoin != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},111);
                Log.d(TAG, "getPermission: return");
                return;
            }else{
                initMedia();
            }
        }else{
            initMedia();
        }
    }

    private void initMedia() {

        //mMedia = new MediaPlayer();
        try {
            mMedia = MediaPlayer.create(this,R.raw.alltimelow);
            //mMedia.prepare();
            mSeekBar.setMax(mMedia.getDuration());
            mAllTime.setText(mMedia.getDuration()/60000+":"+mMedia.getDuration()/1000%60);
            Log.d(TAG, "initMedia: getDuration="+mMedia.getDuration());

        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "initMedia: mMedia="+mMedia);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMedia != null){
            mMedia.stop();
            mMedia.release();
        }
    }


    private  Handler handle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    mAllTime.setText(msg.arg1/60000+":"+msg.arg1/1000%60);
                    break;
                case 3:
                    mPlayTime.setText("00:00");
                    break;
            }
        }
    };

    Runnable updateThread = new Runnable() {
        @Override
        public void run() {
            int  cur = mMedia.getCurrentPosition();
            mSeekBar.setProgress(cur);
            mPlayTime.setText(cur/60000+":"+cur/1000%60);
            handle.postDelayed(updateThread,100);
        }
    };
}
