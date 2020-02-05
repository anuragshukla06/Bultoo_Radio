package org.cgnetswara.swara;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoryListViewActivity extends AppCompatActivity {
    private List<StoryModel> storyList=new ArrayList<>();
    private RecyclerView recyclerView;
    private StoryAdapter storyAdapter;
    public static final String REQUESTTAG = "requesttag";
    RequestQueue requestQueue;
    StringRequest stringRequest;
    EditText searchInput;
    ImageButton searchButton,loadNewerButton,refershButton,loadOlderButton;
    LinearLayout navButtons;
    private String phoneNumber="";
    private String option="";
    private int start;
    private int end;
    String url;
    public static final String StoryListPrefs = "StoryListPrefs" ;
    public static final String Stories = "Stories" ;
    SharedPreferences sp;
    SharedPreferences sp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list_view);
        start=0;
        end=start+10;
        intentManager();
        //creating adapter object and setting it to recyclerview
        storyAdapter = new StoryAdapter(this,storyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(storyAdapter);
        sp2=getSharedPreferences(Stories,Context.MODE_PRIVATE);
        loadStorys();
    }

    public void intentManager(){
        sp = getSharedPreferences(StoryListPrefs,Context.MODE_PRIVATE);
        try {
            Intent data = getIntent();
            option = data.getStringExtra("option");
            switch (option) {
                case "3":
                    break;
                case "2":
                    SharedPreferences.Editor editor = sp.edit();
                    phoneNumber = data.getStringExtra("phone_number");
                    editor.putString("phone_number", phoneNumber);
                    editor.apply();
                    break;
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("option_chosen", option);
            editor.apply();
        }catch (NullPointerException e){
            Log.d("Looks like:", "The intent is missing");
            option = sp.getString("option_chosen", "3");
            phoneNumber = sp.getString("phone_number", "9");
        }
        layoutManager();
    }

    public void layoutManager(){
        recyclerView = (RecyclerView) findViewById(R.id.rView);
        searchInput = findViewById(R.id.editTextSearchInput);
        searchButton = findViewById(R.id.imageButtonSearch);
        loadNewerButton=findViewById(R.id.loadNewerButton);
        loadOlderButton=findViewById(R.id.loadOlderButton);
        refershButton=findViewById(R.id.refreshButton);
        navButtons=findViewById(R.id.linearLayout2);
        switch (option) {
            case "3":
                searchButton.setVisibility(View.GONE);
                searchInput.setVisibility(View.GONE);
                loadOlderButton.setVisibility(View.GONE);
                loadNewerButton.setVisibility(View.GONE);
                refershButton.setVisibility(View.GONE);
                navButtons.setVisibility(View.GONE);
                url = getString(R.string.base_url)+"pblockswara/BULTOO/0/20";
                break;
            case "2":
                searchInput.setText(phoneNumber);
                url = getString(R.string.base_url)+"pblockswara2/"+phoneNumber+"/"+start+"/"+end;
                break;
        }
    }

    public void loadStorys(){
        Log.d("Fetching from URL:",url);
        stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {
                                //getting product object from json array
                                JSONObject jsonStory = array.getJSONObject(i);
                                StoryModel story=new StoryModel();
                                story.setId(jsonStory.getString("problem_id"));
                                story.setDesc(jsonStory.getString("problem_desc"));
                                story.setText(jsonStory.getString("problem_text"));
                                story.setCount(jsonStory.getString("duration"));
                                story.setDatetime(jsonStory.getString("datetime"));
                                story.setAudioFile(jsonStory.getString("audio_file"));
                                if(url.equals("http://flask-aws-dev.ap-south-1.elasticbeanstalk.com/pblockswara/BULTOO/0/20")){
                                    story.setType("bultoo");
                                }
                                else{
                                    story.setType("normal");
                                }
                                story.setAccessingUser(phoneNumber);
                                storyList.add(story);
                                storyAdapter.notifyDataSetChanged();
                            }
                            if(url.equals("http://flask-aws-dev.ap-south-1.elasticbeanstalk.com/pblockswara/BULTOO/0/20")){
                                storeStoriesOffline(array);
                                addDefaultStories();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Story fetch volley", error.toString());
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
                        if(url.equals("http://flask-aws-dev.ap-south-1.elasticbeanstalk.com/pblockswara/BULTOO/0/20")){
                            getStoriesOffline();
                            addDefaultStories();
                        }
                    }
                });
        stringRequest.setTag(REQUESTTAG);
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        storyAdapter.notifyDataSetChanged();
    }

    private void addDefaultStories() {
        StoryModel story1=new StoryModel();
        story1.setId("def_story_hindi");
        story1.setDesc("CGNet का परिचय हिंदी में");
        story1.setText(getString(R.string.hindi_desc));
        story1.setCount("600");
        story1.setDatetime("01 January");
        story1.setAudioFile("cgnet_parichay_in_hindi.mp3");
        story1.setType("bultoo");
        StoryModel story2=new StoryModel();
        story2.setId("def_story_gondi");
        story2.setDesc("CGNet का परिचय गोंडी में");
        story2.setText(getString(R.string.gondi_desc));
        story2.setCount("600");
        story2.setDatetime("01 January");
        story2.setAudioFile("cgnet_parichay_in_gondi.mp3");
        story2.setType("bultoo");
        storyList.add(story1);
        storyList.add(story2);
        storyAdapter.notifyDataSetChanged();
    }

    public void storeStoriesOffline(JSONArray array){
        for (int i = 0; i < array.length(); i++) {
            try {
                //getting product object from json array
                JSONObject jsonStory = array.getJSONObject(i);
                SharedPreferences.Editor editor = sp2.edit();
                editor.putString("problem_id_"+i,jsonStory.getString("problem_id"));
                editor.putString("problem_desc_"+i,jsonStory.getString("problem_desc"));
                editor.putString("problem_text_"+i,jsonStory.getString("problem_text"));
                editor.putString("duration_"+i,jsonStory.getString("duration"));
                editor.putString("datetime_"+i,jsonStory.getString("datetime"));
                editor.putString("audio_file_"+i,jsonStory.getString("audio_file"));
                editor.apply();
                //Note to add this line for story objects while fetching online --> story.setAccessingUser(phoneNumber);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void getStoriesOffline(){
        storyList.clear();
        for(int i=0; i<sp2.getAll().size()/6; i++){
            StoryModel story=new StoryModel();
            story.setId(sp2.getString("problem_id_"+i,""));
            story.setDesc(sp2.getString("problem_desc_"+i,""));
            story.setText(sp2.getString("problem_text_"+i,""));
            story.setCount(sp2.getString("duration_"+i,""));
            story.setDatetime(sp2.getString("datetime_"+i,""));
            story.setAudioFile(sp2.getString("audio_file_"+i,""));
            story.setAccessingUser(phoneNumber);
            story.setType("bultoo");
            storyList.add(story);
            storyAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUESTTAG);
        }
    }


    public void showNewer(View view) {
        if (start-10 >= 0){
            start=start-10;
            end=start+10;
            storyList.clear();
            layoutManager();
            loadStorys();
        }
    }

    public void showOlder(View view) {
        start=start+10;
        end=start+10;
        storyList.clear();
        layoutManager();
        loadStorys();
    }

    public void reload(View view) {
        storyList.clear();
        loadStorys();
    }

    public void search(View view) {
        if(searchInput.length()>0) {
            phoneNumber = searchInput.getText().toString();
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("phone_number", phoneNumber);
            editor.apply();
            storyList.clear();
            layoutManager();
            loadStorys();
        }else{
            searchInput.setError("कृपया सही फ़ोन नंबर दर्ज करें !");
        }
    }

}