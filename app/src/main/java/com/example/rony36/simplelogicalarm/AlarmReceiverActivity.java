package com.example.rony36.simplelogicalarm;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by RonyLap on 8/30/2014.
 */
public class AlarmReceiverActivity extends Activity {
    private MediaPlayer mMediaPlayer;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Wake Log");
        mWakeLock.acquire();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,

                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.alarm);

        Button stopAlarm = (Button) findViewById(R.id.btnStopAlarm);
        Button stopMidAlarm = (Button) findViewById(R.id.midBtnStopAlarm);
        Button stopHighAlarm = (Button) findViewById(R.id.hiBtnStopAlarm);
//        String newString;
//        Bundle extras;
//        if (saveInstanceState == null) {
//            extras = getIntent().getExtras();
//            if(extras == null) {
//                newString= null;
//            } else {
//                newString= extras.getString("requestCode");
//            }
//        } else {
//            newString= (String) saveInstanceState.getSerializable("requestCode");
//        }
//        Serializable abc = getIntent().getExtras().getSerializable("requestCode");
        int rowId = getIntent().getIntExtra("requestCode", 1);
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        Alarm ringingAlarm = db.getAlarm(rowId);

        final LinearLayout mSimple = (LinearLayout) findViewById(R.id.simpleStop);
        final LinearLayout mMid = (LinearLayout) findViewById(R.id.midStop);
        final LinearLayout mHigh = (LinearLayout) findViewById(R.id.hiStop);

        final TextView mEquation = (TextView) findViewById(R.id.hiEquation);
        final EditText mEnterRes = (EditText) findViewById(R.id.enterRes);

        if(ringingAlarm.get_off_method() == 1){
           mSimple.setVisibility(View.GONE);
           mMid.setVisibility(View.GONE);
           mHigh.setVisibility(View.VISIBLE);

           mEquation.setText(generateMath(ringingAlarm.get_urgency()));
        }else if (ringingAlarm.get_off_method() == 0){
            mSimple.setVisibility(View.GONE);
            mMid.setVisibility(View.VISIBLE);
            mHigh.setVisibility(View.GONE);
        }

        db.close();
        playSound(this, getAlarmUri());

        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.stop();
                finish();
            }
        });
        stopMidAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.stop();
                finish();
            }
        });
        stopHighAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEnterRes.getText().toString().equals(Integer.toString(mResult))){
                    mMediaPlayer.stop();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Oh! Dear! It's not correct. Wake up and Try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String mEquation;
    public int mResult;

    public String generateMath(int difficulty){
        int min, max;
        if(difficulty == -1){
            min = 1;
            max = 10;
        }else if(difficulty == 0){
            min = 10;
            max = 99;
        }else{
            min = 100;
            max = 99;
        }

        Random r1 = new Random();
        int i1 = r1.nextInt(max - min) + min;
        Random r2 = new Random();
        int i2 = r2.nextInt(max - min) + min;
        Random r3 = new Random();
        int i3 = r3.nextInt(max - min) + min;

        String op1 = randOperator();
        String op2 = randOperator();

        mEquation = i1 + " " + op1 + " " + i2 + " " + op2 + " " + i3;

        mResult = 0;

        if(op1 == "+"){
            mResult = i1 + i2;
        }else{
            mResult = i1 - i2;
        }

        if(op2 == "+"){
            mResult += i3;
        }else{
            mResult -= i3;
        }
        return mEquation;
    }

    private String randOperator(){
        String[] operators = {"+","-"};
        int idx = new Random().nextInt(operators.length);
        return operators[idx];
    }

    private  void playSound(Context context, Uri alert){
        mMediaPlayer = new MediaPlayer();
        try{
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0){
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        }catch (IOException e){
            Log.i("AlarmReceiver", "No audio files are Found!");
        }
    }

    private Uri getAlarmUri(){
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null){
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null){
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return  alert;
    }

    protected  void onStop(){
        super.onStop();
        mWakeLock.release();
    }
}
