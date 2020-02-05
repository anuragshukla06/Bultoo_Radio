package org.cgnetswara.swara;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class StoryViewActivity extends AppCompatActivity {

    public static final String REQUESTTAG3 = "requesttag3";
    public static final String REQUESTTAG2 = "requesttag2";
    public static final String REQUESTTAG1 = "requesttag1";
    StringRequest stringRequest1, stringRequest2, stringRequest3;
    RequestQueue requestQueue;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<MessageModel> commentList = new ArrayList<>();
    Typeface Hindi;
    String username, problem_id, type, audioFile;
    MediaPlayer storyPlayer;
    ImageButton playStoryButton;
    String fileLocation;
    Button downloadButton;
    SeekBar seekBarProgress;
    int audioDuration;
    Handler mHandler;
    boolean playState = true;
    public static final String BULTOO_FILE = "org.cgnetswara.swara.BULTOO_FILE";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions, menu);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = (String) item.getTitleCondensed();
        if (title != null && title.equals("Share")) {
            shareStory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //TODO 1: Change wallet credit settings here
    public void shareStory(){
        /*-Anurag's code start-*/

        boolean STRONG_NETWORK = true;
        if (!MainActivity.isSimAvailable(this)) {
            Toast.makeText(this, "Please insert the sim first.", Toast.LENGTH_LONG).show();
            return;
        }
        else if(MainActivity.isAirplaneModeOn(this)) {
            Toast.makeText(this, "Please switch off the airplane mode.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (MainActivity.getSignalStrength(this) > -110) {
            Toast.makeText(this, "Sorry, money won't be credited since the network is strong.", Toast.LENGTH_LONG).show();
            STRONG_NETWORK = false;
        }

        /*-Anurag's code end-*/
        Intent sendName = new Intent();
        sendName.setAction(BULTOO_FILE);
        sendName.putExtra("problem_id", problem_id);
        sendName.putExtra("type",type);
        sendName.putExtra("STRONG_NETWORK", STRONG_NETWORK);
        sendBroadcast(sendName);

        Uri shareUri;
        Intent intent = new Intent(Intent.ACTION_SEND);
        File f= new File(fileLocation);
        if(f.exists()) {
            if (Build.VERSION.SDK_INT >= 24) {
                shareUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", f);
            } else {
                shareUri = Uri.fromFile(f);
            }
            Log.d("share: ", "" + shareUri);
            intent.putExtra(Intent.EXTRA_STREAM, shareUri);
            intent.setType("*/*");
            startActivity(Intent.createChooser(intent, "Share"));
        }
        else{
            Toast.makeText(getBaseContext(), "भेजने से पेहले सन्देश डाउनलोड करें", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);
        requestQueue= Volley.newRequestQueue(this);
        ActionBar a=getSupportActionBar();
        a.setDisplayHomeAsUpEnabled(true);

        Intent endpoint = getIntent();
        problem_id = endpoint.getStringExtra("story_id");
        type=endpoint.getStringExtra("type");
        username = endpoint.getStringExtra("accessing_user");
        audioFile=endpoint.getStringExtra("audio_file");
        final TextView problem_desc = (TextView) findViewById(R.id.apvDesc);
        final TextView problem_text = (TextView) findViewById(R.id.apvText);
        final TextView problem_dt = (TextView) findViewById(R.id.apvDatetime);
        final TextView problem_count = (TextView) findViewById(R.id.apvCount);
        try {

            problem_desc.setText(endpoint.getStringExtra("story_desc"));
            problem_text.setText(endpoint.getStringExtra("story_text"));
            Linkify.addLinks(problem_text,Linkify.ALL);
            problem_dt.setText(endpoint.getStringExtra("datetime"));
            problem_count.setText(endpoint.getStringExtra("count"));

        }
        catch(NullPointerException e){
            e.printStackTrace();
        }

        recyclerView = (RecyclerView) findViewById(R.id.rCommentView);
        commentAdapter = new CommentAdapter(this,commentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(commentAdapter);
        layoutManager();
        loadComments();
    }


    public void loadComments(){
        commentList.clear();
        String url = getString(R.string.base_url)+"fetchComments/"+problem_id;


        stringRequest3 = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);
                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {
                                //getting product object from json array
                                JSONObject jsonProblem = array.getJSONObject(i);
                                MessageModel comment=new MessageModel();
                                comment.setSender(jsonProblem.getString("username"));
                                comment.setMessage(jsonProblem.getString("comments"));
                                comment.setDatetime(jsonProblem.getString("datetime").substring(0,4)+"/"+
                                        jsonProblem.getString("datetime").substring(4,6)+"/"+
                                        jsonProblem.getString("datetime").substring(6,8));
                                commentList.add(comment);
                                commentAdapter.notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
                    }
                });
        stringRequest3.setTag(REQUESTTAG3);
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        stringRequest3.setShouldCache(false);
        requestQueue.add(stringRequest3);
        commentAdapter.notifyDataSetChanged();
    }

    public void unAdoptProblem(View view) {

        String url= getString(R.string.base_url)+"unAdoptProblem/"+username+"/"+problem_id;

        stringRequest2 = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("UnAdopted")){
                            onUnAdoption();
                        }
                        else {
                            onUnknownResponse();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
                    }
                });
        stringRequest2.setTag(REQUESTTAG2);
        requestQueue.add(stringRequest2);
    }

    private void onUnknownResponse() {
        Toast.makeText(getBaseContext(), "Unknown Response", Toast.LENGTH_LONG).show();
    }

    private void onUnAdoption() {
        Toast.makeText(getBaseContext(), "Problem Un-Adopted!", Toast.LENGTH_LONG).show();
        finish();
    }
    @Override
    protected void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUESTTAG2);
            requestQueue.cancelAll(REQUESTTAG1);
        }
    }

    public void submitComment(View view) {
        EditText inputComments=findViewById(R.id.inputComments);
        final String comment=inputComments.getText().toString();
        Log.d("comment",comment);
        String url = getString(R.string.base_url)+"registerComment";

        stringRequest1 = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("Done")){
                            onResponseDone();
                        }
                        else {
                            Log.d("response",response);
                            onUnknownResponse();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map <String,String> params = new HashMap<String,String>();
                params.put("username",username);
                params.put("problem_id",problem_id);
                params.put("comment",comment);
                return params;
            }
        };
        stringRequest1.setTag(REQUESTTAG1);
        requestQueue.add(stringRequest1);
    }

    private void onResponseDone() {
        Toast.makeText(getBaseContext(), "Done!", Toast.LENGTH_LONG).show();
        loadComments();
    }


    public void findPath(){
        String myDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        File f = new File(myDirectory);
        if (f.exists() && f.isDirectory()){
            final Pattern p = Pattern.compile("myfile_*\\_(^0*(1?\\d|%d)$).mp4"); // I know I really have a stupid mistake on the regex;

            File[] flists = f.listFiles(new FileFilter(){
                @Override
                public boolean accept(File file) {
                    return p.matcher(file.getName()).matches();
                }
            });

        }
    }


    public void layoutManager(){
        Log.d("P_id",problem_id);
        fileLocation=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/CGSwaraStory_"+audioFile;
        File f= new File(fileLocation);
        Log.d("P_Guessed_Location",fileLocation);
        if(!f.exists()){
            fileLocation="/storage/emulated/0/bluetooth/CGSwaraStory_"+audioFile;
            f= new File(fileLocation);
        }
        Log.d("P_Guessed_Location",fileLocation);
        playStoryButton=findViewById(R.id.imageButtonPlayStory);
        seekBarProgress=findViewById(R.id.seekBar);
        downloadButton=findViewById(R.id.buttonDownload);
        downloadButton.setEnabled(true);
        if(f.exists()){
            playStoryButton.setVisibility(View.VISIBLE);
            seekBarProgress.setVisibility(View.VISIBLE);
            downloadButton.setText("पुनः डाउनलोड करें ");
        }
        else{
            downloadButton.setVisibility(View.VISIBLE);
            playStoryButton.setVisibility(View.INVISIBLE);
            seekBarProgress.setVisibility(View.INVISIBLE);
        }
    }


    public void playStory(View view) {
        if(playState){
            playStoryButton.setImageResource(android.R.drawable.ic_media_pause);
            playState=false;
            try {
                storyPlayer=new MediaPlayer();
                storyPlayer.setDataSource("file://" + fileLocation);
                storyPlayer.prepare();
                audioDuration=storyPlayer.getDuration()/1000;
                seekBarProgress.setMax(((int)Math.ceil(audioDuration)));
                Log.d("Length is ",""+audioDuration);
                storyPlayer.start();
                storyPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        playStoryButton.setImageResource(android.R.drawable.ic_media_play);
                        playState=true;
                    }
                });
            }catch(Exception e){//audioplayer exceptions
                e.printStackTrace();
            }
        }
        else{
            playStoryButton.setImageResource(android.R.drawable.ic_media_play);
            playState=true;
            storyPlayer.stop();
            //stopPlaying();
        }

        mHandler = new Handler();
        //Make sure you update Seekbar on UI thread
        try {
            StoryViewActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (storyPlayer!= null && storyPlayer.isPlaying()) {
                        int mCurrentPosition = storyPlayer.getCurrentPosition()/1000;
                        //Log.d("position: ",""+mCurrentPosition);
                        seekBarProgress.setProgress(mCurrentPosition);
                    }
                    mHandler.postDelayed(this, 1000);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(storyPlayer != null && fromUser){
                    storyPlayer.seekTo(progress * 1000);
                }
            }
        });
    }

    public void releasePlaying(){
        //Repeated tasks on Clear, Play or Accept
        if(storyPlayer!=null) {
            storyPlayer.release();
        }
    }

    @Override
    protected void onDestroy() {
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
        releasePlaying();
        super.onDestroy();
    }



    public void downloadStory(View view) {
        String url;
        downloadButton.setEnabled(false);
        downloadButton.setBackgroundColor(Color.LTGRAY);
        if(type.equals("normal")) {
            url = "http://cgnetswara.org/audio/" + audioFile;
        }else{
            url="https://cgstories.s3.ap-south-1.amazonaws.com/"+audioFile;
        }
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
        String tempcheck=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/CGSwaraStory_"+audioFile;
        File temp_f= new File(tempcheck);
        if(temp_f.exists()){
            temp_f.delete();
        }
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CGSwaraStory_"+audioFile);
        r.allowScanningByMediaScanner();
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        r.setVisibleInDownloadsUi(true);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(r);
    }
}