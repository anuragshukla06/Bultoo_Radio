package org.cgnetswara.swara;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class Email extends AsyncTask<Void, Void, Boolean> {

    Mail mail;
    String audioLocation,phoneNumber,photoLocation;
    SharedPreferences sp;
    Context context;
    int audioDuration;
    String subject;
    String body;
    public static final String RecordingScreenPrefs = "RecordingScreenPrefs" ;

    public Email(Context context){
        this.context=context;
        sp=context.getSharedPreferences(RecordingScreenPrefs,Context.MODE_PRIVATE);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {


        SharedPreferences.Editor editor = sp.edit();
        Map<String, ?> paths = sp.getAll();
        for (Map.Entry<String, ?> row : paths.entrySet()) {
            //Iterating over each sp entry and emailing
            String values = row.getKey() + "," + row.getValue().toString();
            audioLocation = values.split(",")[0];
            phoneNumber = values.split(",")[1];
            if(values.split(",").length>2) {
                photoLocation = values.split(",")[2];
            }
            else{
                photoLocation="";
            }
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(audioLocation);
                metaRetriever.setDataSource(inputStream.getFD());
                inputStream.close();

                Long durationms = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                audioDuration = (int)(durationms / 1000);

            } catch (Exception e) {
                Log.e("Metadata error", e.toString());
                audioDuration = 0;
            }
            String pattern = "yyyy-MM-dd hh:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            String[] toArr = {"cgnetmail2019@gmail.com","cgnetswaratest@gmail.com"}; // multiple email addresses can be added here
            subject = "Swara-Main|app|" + audioDuration + "|DRAFT|" + phoneNumber + "|" + "unk" + "|" + date + "|PUBLIC";
            body=getBody(phoneNumber,date,audioDuration,"unk");

//            mail = new Mail("apikey", Safe.getPassword());
            mail = new Mail("apikey", "hello");
            mail.setTo(toArr);
            mail.setFrom("cgnetmail2019@gmail.com");
            mail.setSubject(subject);
            mail.setBody(body);


            //Attachment section
            try {
                mail.addAttachment(audioLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if(!photoLocation.equals("") && !photoLocation.equals("0")){
                    mail.addAttachment(photoLocation);
                    Log.d("Location: ",photoLocation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sending Mail section
            try {
                if (mail.send()) {
                    Log.d("Email.java", "Mail has been sent");
                    editor.remove(audioLocation);
                    editor.apply();
                }
            } catch (AuthenticationFailedException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return false;
    }
    private String getBody(String phoneNumber, String time, int length, String location) {
        String body;
        body =  "******************************************************************************\n" +
                "SERVER/सर्वर                        : Swara-Main\n" +
                "******************************************************************************\n" +
                "POST ID/पोस्ट क्र                       : unk" + "\n" +
                "******************************************************************************\n" +
                "CALLER/नंबर                         : " + phoneNumber + "\n" +
                "******************************************************************************\n" +
                "TIME STAMP/समय                  : " + time + "\n" +
                "******************************************************************************\n" +
                "NAME OF CALLER/फ़ोन करने वाले का नाम     :\n" +
                "******************************************************************************\n" +
                "CALL LOCATION/कॉल कहाँ से आई        :\n" +
                "******************************************************************************\n" +
                "TEL CIRC/ टेलिकॉम सर्किल                : "+ location + "\n" +
                "******************************************************************************\n" +
                "LNGTH/अवधी                              : " + length + "\n" +
                "******************************************************************************\n" +
                "STATUS/स्थिति                                           : DRAFT\n" +
                "******************************************************************************\n" +
                "TEXT SUMMARY/   सन्देश                  :";

        return body;
    }
}

    /*

            //Code to retrieve Metadata of audio
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(path);
                metaRetriever.setDataSource(inputStream.getFD());
                inputStream.close();

                Long durationms = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                audioDuration = durationms / 1000;

            } catch (Exception e) {
                Log.e("Metadata error", e.toString());
                audioDuration = 0;
            }
            */

    //********************************************************************************
    /*code to iterate over sp entries
    Map<String,?> paths = sp.getAll();

        for(Map.Entry<String,?> row : paths.entrySet()){
            Log.d("map values",row.getKey() + ": " + row.getValue().toString());
            //*****************************************
            //Code to remove sp entries
            editor.remove(row.getKey());
            editor.commit();
            //*****************************************
        }
    */
    //********************************************************************************

