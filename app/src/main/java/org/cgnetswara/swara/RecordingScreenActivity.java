package org.cgnetswara.swara;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

public class RecordingScreenActivity extends AppCompatActivity{
    boolean recordState = true;
    boolean playState = true;
    ImageButton recordButton, deleteBitmap, playButton, clearButton, acceptButton;
    Chronometer chronometer;
    RecMicToMp3 mRecMicToMp3;
    MediaPlayer audioPlayer;
    String path;
    long audioDuration;
    SharedPreferences sp;
    public static final String MyPREFERENCES = "RecordingScreenPrefs" ;
    public static final int PICK_IMAGE = 1;
    private Bitmap bitmap = null;
    ImageView photoAttach;
    Uri selectedImageUri;
    String imageFilePath="";
    EditText phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Declarations and instantiations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_screen);
        initialiseUI();
    }

    public void initialiseUI(){
        recordButton=findViewById(R.id.imageButtonRecording);
        playButton=findViewById(R.id.imageButtonPlay);
        clearButton=findViewById(R.id.imageButtonClear);
        acceptButton=findViewById(R.id.imageButtonAccept);
        chronometer=findViewById(R.id.chronometer);
        photoAttach=findViewById(R.id.imageView2);
        phoneNumber=findViewById(R.id.editTextPhoneInputRecording);
        deleteBitmap=findViewById(R.id.imageButtonResetBitmap);

        recordButton.setVisibility(View.VISIBLE);
        deleteBitmap.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        chronometer.setVisibility(View.INVISIBLE);
        Intent data=getIntent();
        //phoneNumber.setText(data.getStringExtra("phone_number"));
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(NullPointerException e){
            e.printStackTrace();
        }

    }

    public void toggleRecording(View view) {
        //first step in recording
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        if(recordState){
            //Recording Started
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            chronometer.setVisibility(View.VISIBLE);
            recordButton.setImageResource(R.drawable.stopbtn);
            recordState = false;
            String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File folder = new File(exstPath+"/swararecordings");
            folder.mkdirs();
            Date dt=new Date();
            path=folder+"/"+dt+".mp3";
            Log.d("path",path);
            mRecMicToMp3 = new RecMicToMp3(path, 8000);
            mRecMicToMp3.start();
        }
        else{
            //Recording stop procedure
            chronometer.stop();
            recordButton.setVisibility(View.INVISIBLE);
            recordButton.setImageResource(R.drawable.recordbtn);
            playButton.setVisibility(View.VISIBLE);
            playButton.setImageResource(R.drawable.play);
            playState=true;
            clearButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.VISIBLE);
            mRecMicToMp3.stop();
        }
    }

    public void onClear(View view) {
        //Just reset things
        vibrateAndSetViews();
    }

    public void onPlay(View view) {
        //Re-play functionality
        if(playState){
            playButton.setImageResource(R.drawable.pause);
            playState=false;
            audioPlayer = new MediaPlayer();
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            try {
                audioPlayer.setDataSource("file://"+path);
                audioPlayer.prepare();
                audioPlayer.start();

                audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        playButton.setImageResource(R.drawable.play);
                        playState=true;
                        stopPlaying();
                    }
                });

            } catch (IOException e) {//audioplayer exceptions
                e.printStackTrace();
            }
              catch(Exception e){//audioplayer exceptions
                e.printStackTrace();
            }
        }
        else{
            audioPlayer.stop();
            playButton.setImageResource(R.drawable.play);
            playState=true;
            stopPlaying();
        }
    }

    public void stopPlaying(){
        //Repeated tasks on Clear, Play or Accept
        if(audioPlayer!=null) {
            audioPlayer.release();
        }
        chronometer.stop();
        chronometer.setBase(SystemClock.elapsedRealtime());
    }

    public void vibrateAndSetViews(){
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(100);
        recordButton.setVisibility(View.VISIBLE);
        playButton.setVisibility(View.INVISIBLE);
        clearButton.setVisibility(View.INVISIBLE);
        acceptButton.setVisibility(View.INVISIBLE);
        recordState=true;
        stopPlaying();
        resetBitmap();
    }

    public void onAccept(View view) {
        if(phoneNumber.getText().toString().length()==10) {
            //saving details in shared prefs for audio and/or photo to be mailed
            sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(path, ""+ phoneNumber.getText().toString()+ "," + imageFilePath+",");
            Log.d(path, ""+ phoneNumber.getText().toString()+ "," + imageFilePath+",");
            editor.apply();
            //Resetting things Now
            vibrateAndSetViews();
            Toast.makeText(getBaseContext(), "आपका संदेश भेज दिया जाएगा", Toast.LENGTH_LONG).show();
        }
        else{
            phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
        }
    }

    public void addPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //imageFilePath is stored to transfer and selectedImageUri used to display on UI
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data.getData();
            try {
                imageFilePath=PathUtil.getPath(getApplicationContext(),selectedImageUri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(bitmap != null) {
                while(bitmap.getHeight() > 1000 || bitmap.getWidth() > 1000) {
                    bitmap = halfSize(bitmap);
                }
            }
            photoAttach.setImageBitmap(bitmap);
            deleteBitmap.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap halfSize(Bitmap input) {
        int height = input.getHeight();
        int width = input.getWidth();
        return Bitmap.createScaledBitmap(input,  width/2, height/2, false);
    }

    public void resetBitmap(View view) {
        resetBitmap();
    }
    public void resetBitmap(){
        bitmap = null;
        photoAttach.setImageBitmap(bitmap);
        deleteBitmap.setVisibility(View.INVISIBLE);
        imageFilePath="";
    }
}
