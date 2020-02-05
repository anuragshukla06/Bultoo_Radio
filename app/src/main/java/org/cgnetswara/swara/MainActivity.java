package org.cgnetswara.swara;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;


public class MainActivity extends AppCompatActivity {


    private int MY_PERMISSIONS_REQUESTS = 0;
    private static int CREDIT_AMOUNT = 2;
    private static int linesInFile = 0;
    private static int linesInStorySP = 0;
    String version_code = "0";
    SharedPreferences sp;
    boolean isRequestingRecharge = false;
    RequestQueue requestQueue;
    StringRequest stringRequest, stringRequest2;
    public static final String REQUESTTAG = "requesttag";
    public static final String REQUESTTAG2 = "requesttag2";
    static SharedPreferences spStoryShare;
    static SharedPreferences spWalletData;
    public static final String MyPREFERENCES = "MainActivityPrefs";
    public static final String StoryShareInfo = "StoryShareInfo1";
    private static final String WalletData = "WalletData";
    private FusedLocationProviderClient fusedLocationClient;
    ArrayList<Location> locations = new ArrayList<>();
    EditText phoneNumber;
    Spinner operator;
    Button op1, op2, op3, op4;
    static int INVALID_STRENGTH = -1000;
    IntentFilter mFilter;
    Boolean numberOk = false, operatorOk = false;
    Boolean onCreateFlag = true;
    public static final String BULTOO_FILE = "org.cgnetswara.swara.BULTOO_FILE";
    String[] opArray = {"-", "AC", "AR", "B", "ID", "JIO", "MT", "M", "DC", "DG", "RC", "RG", "UN", "VC"};//Caution! Make sure this array is congruent to R.array.operator_array
    private String rechargePhoneNumber = "", rechargeOperator = "", rechargeAmount = "", id = "";


    //#########################--ANURAG'S CODE START--###########################
    public static boolean isSimAvailable(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager sManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

                SubscriptionInfo infoSim1 = sManager.getActiveSubscriptionInfoForSimSlotIndex(0);
                SubscriptionInfo infoSim2 = sManager.getActiveSubscriptionInfoForSimSlotIndex(1);
                if (infoSim1 != null || infoSim2 != null) {
                    return true;
                }
            } else {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager.getSimSerialNumber() != null) {
                    return true;
                }
            }
            return false;
        } else {
            // TODO: ASK FOR PERMISSION IF NOT AVAILABLE
            return false;
        }
    }

    /**
     * Gets the state of Airplane Mode.
     *
     * @return true if enabled.
     */
    public static boolean isAirplaneModeOn(Context context) {

        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

    }


    public static int getSignalStrength(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int strength = INVALID_STRENGTH;
            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
            if (cellInfos != null) {
                for (int i = 0; i < cellInfos.size(); i++) {
                    if (cellInfos.get(i).isRegistered()) {
                        if (cellInfos.get(i) instanceof CellInfoWcdma) {
                            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfos.get(i);
                            CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                            strength = cellSignalStrengthWcdma.getDbm();
                        } else if (cellInfos.get(i) instanceof CellInfoGsm) {
                            CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                            CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                            strength = cellSignalStrengthGsm.getDbm();
                        } else if (cellInfos.get(i) instanceof CellInfoLte) {
                            CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                            CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                            strength = cellSignalStrengthLte.getDbm();
                        } else if (cellInfos.get(i) instanceof CellInfoCdma) {
                            CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(i);
                            CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                            strength = cellSignalStrengthCdma.getDbm();
                        }
                    }
                }
            }
            return strength;
        } else {
            // TODO: ASK FOR PERMISSION IF NOT AVAILABLE
            return INVALID_STRENGTH;
        }
    }

    //#########################--ANURAG'S CODE END--#############################

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
    }

    private void givePermissions() {
        final List<String> permissionsList = new ArrayList<String>();
        addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
        addPermission(permissionsList, Manifest.permission.RECORD_AUDIO);
        addPermission(permissionsList, ACCESS_COARSE_LOCATION);
        addPermission(permissionsList, READ_PHONE_STATE);
        if (permissionsList.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        MY_PERMISSIONS_REQUESTS);
            }
            return;
        }
    }

    private void checkForOptions() {
        Log.d("number/op", "" + numberOk + "/" + operatorOk);
        if (numberOk) {
            switchOptions(true);
            handleHiddenFile();
            writeIdInFile();
            makeMP3FromRawResource(R.raw.cgnet_parichay_in_hindi, "CGSwaraStory_cgnet_parichay_in_hindi.mp3");
            makeMP3FromRawResource(R.raw.cgnet_parichay_in_gondi, "CGSwaraStory_cgnet_parichay_in_gondi.mp3");
        } else {
            switchOptions(false);
        }

    }

    private void makeMP3FromRawResource(int resId, String fileName) {
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DOWNLOADS;
        String path = exstPath + "/" + fileName;
        File file = new File(path);
        if (!file.exists()) {
            try {
                InputStream in;
                OutputStream out = new FileOutputStream(file);
                Resources resources = this.getResources();
                in = resources.openRawResource(resId);
                byte[] buf = new byte[1024];
                int len;
                try {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions, menu);
        menu.getItem(0).setVisible(false);
        spWalletData = getSharedPreferences(WalletData, Context.MODE_PRIVATE);
        if (version_code == "0") {
            SharedPreferences.Editor editor = spWalletData.edit();
            editor.putString("Cash", "0");
            editor.apply();
        }
        menu.getItem(2).setTitle("₹ " + spWalletData.getString("Cash", "0"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = (String) item.getTitleCondensed();
        if (title != null && title.equals("रिचार्ज करे") && !isRequestingRecharge) {
            buildDialogPhoneNumber();
            return true;
        } else if (title != null && title.equals("रिचार्ज करे") && isRequestingRecharge) {
            buildBlockerDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void buildDialogPhoneNumber() {
        readIdfromFile();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया 10 अंकों का फोन नंबर दर्ज करें");
        final EditText input = new EditText(this);
        builder.setView(input);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rechargePhoneNumber = input.getText().toString();
                if (rechargePhoneNumber.length() != 10) {
                    buildDialogPhoneNumber();
                } else {
                    buildDialogOperator();
                }
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildDialogOperator() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया ऑपरेटर का चयन करें");
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);
        input.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                rechargeOperator = opArray[position];
                Log.d("option selected", rechargeOperator);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d("option selected", ":");
            }
        });
        builder.setView(input);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (rechargeOperator.equals("-")) {
                    buildDialogOperator();
                } else {
                    buildDialogAmount();
                }
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildDialogAmount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया रिचार्ज राशि दर्ज करें");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rechargeAmount = input.getText().toString();
                buildDialogConfirm();
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildDialogConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("जाँच के बाद पुष्टि करें");
        final TextView info = new TextView(this);
        info.setGravity(Gravity.CENTER);
        info.setText("\nPhone: " + rechargePhoneNumber + "\n\tOperator: " + rechargeOperator + "\n\tAmount: " + rechargeAmount);
        builder.setView(info);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String walletAmount = spWalletData.getString("Cash", "0");
                Log.d("Finally: ", rechargePhoneNumber + rechargeOperator + rechargeAmount);
                if (Integer.parseInt(walletAmount) >= Integer.parseInt(rechargeAmount)) {
                    sendTopUpRequestToServer(walletAmount, rechargePhoneNumber, rechargeOperator, rechargeAmount);
                }
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildBlockerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया कुछ समय प्रतीक्षा करें");
        final TextView info = new TextView(this);
        info.setGravity(Gravity.CENTER);
        info.setText("कृपया कुछ समय प्रतीक्षा करें");
        builder.setView(info);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    void testLocation(final String rpn) {
        Toast.makeText(this, "inside test location", Toast.LENGTH_SHORT).show();
        String url = getString(R.string.base_url) + "testLocation";
        StringRequest myStringReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Toast.makeText(MainActivity.this, "Success Location!", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                isRequestingRecharge = false;
                Log.d("Some", "Network Error: ", error);
                Toast.makeText(getBaseContext(), "Network Error Location", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phone_number", rpn);
                params.put("latitude", Double.toString(22.667));
                params.put("longitude", Double.toString(42.668));
                return params;
            }
        };
        requestQueue.add(myStringReq);
    }

    public void sendTopUpRequestToServer(final String wa, final String rpn, final String ro, final String ra) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: ASK for permission here
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i("Location Error", "permission not granted");
            return;
        }
        Log.i("Location Permitted", "permission granted");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            final double latitude = location.getLatitude();
                            final double longitude = location.getLongitude();

                            isRequestingRecharge = true;
                            String url = getString(R.string.base_url) + "swaraRecharge";
                            stringRequest2 = new StringRequest(Request.Method.POST, url,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            isRequestingRecharge = false;
                                            String newWalletAmount = response;
                                            Log.d("Response is: ", response);
                                            if (newWalletAmount.equals(wa) || newWalletAmount.equals(Integer.toString((Integer.parseInt(wa) - Integer.parseInt(ra))))) {
                                                setToWallet(newWalletAmount);
                                            } else {
                                                Log.e("Error!!", "this should never happen");
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    isRequestingRecharge = false;
                                    Log.d("Some", "Network Error: ", error);
                                    Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("phone_number", rpn);
                                    params.put("amount", ra);
                                    params.put("carrier_code", ro);
                                    params.put("wallet_amount", wa);
                                    params.put("id", id);
                                    params.put("version", version_code);
                                    params.put("latitude", Double.toString(latitude));
                                    params.put("longitude", Double.toString(longitude));
                                    return params;
                                }
                            };
                            Log.i("Location_retreived", latitude + " " + longitude);
                            Log.i("Request_sent", rpn + " " + wa + " " + ra);
                            stringRequest2.setTag(REQUESTTAG);
                            stringRequest2.setShouldCache(false);
                            stringRequest2.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            requestQueue.add(stringRequest2);

                        }
                        else {
                            Toast.makeText(MainActivity.this, "Error Retreiving location! Try turning on location services.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        Log.i("Locations_size", locations.size() + "");






    }

    private void initialiseUI() {
        phoneNumber = findViewById(R.id.editText);
        operator = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operator.setAdapter(adapter);
        op1 = findViewById(R.id.button);
        op2 = findViewById(R.id.button2);
        op3 = findViewById(R.id.button3);
        op4 = findViewById(R.id.button4);//Phone Number and operator info

        if (sp.contains("phone_number")) {
            phoneNumber.setText(sp.getString("phone_number", "DNE"));
            operator.setSelection(Integer.parseInt(sp.getString("operator", "0")));
            numberOk = true;
            operatorOk = true;
        } else {
            phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
        }
        checkForOptions();
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 10) {
                    phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
                    numberOk = false;
                } else {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("phone_number", s.toString());
                    editor.apply();
                    numberOk = true;
                }
                checkForOptions();
            }
        });
        operator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!onCreateFlag) {
                    if (id != 0) {
                        Log.d("option selected", position + ":" + opArray[position]);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("operator", "" + position);
                        editor.apply();
                        operatorOk = true;
                    } else {
                        operatorOk = false;
                    }
                    checkForOptions();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Log.d("option selected", ":");
            }
        });

    }

    public void switchOptions(boolean val) {
        op1.setEnabled(val);
        op2.setEnabled(val);
        op3.setEnabled(val);
        op4.setEnabled(val);
    }

    /*Code Addition: Anurag Shukla*/
    public boolean isConnected() throws InterruptedException, IOException {
        final String command = "ping -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
    /*Code Addition: Anurag Shukla*/

    /* My Code*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
    }
    /* My Code*/

//    void getLastLocation() {
//
//        return;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //asking for permissions

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            givePermissions();

        }
        

        /* This is the test code */
//        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
//        int type = networkInfo.getType();
//        String typeName = networkInfo.getTypeName();
//        boolean connected = networkInfo.isConnected();

        /*
        This is a test code
        */

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


//        getLastLocation();


        /*
        This is a test code
        */


        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);


        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
            Log.i("DEEBUUUG", isConnected() + "");
//            Toast.makeText(this, ""+mobileDataEnabled, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }

        // for example value of first element
        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{READ_PHONE_STATE},
                    0);
            return;
        }
//   ################CODE FOR CHECKING SIM FROM WHICH INTERNET IS USED ##########################
//        int sim = -1;
//        if (android.os.Build.VERSION.SDK_INT >= 22) {
//            SubscriptionManager sm = (SubscriptionManager) this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
//            if (sm != null) {
//                try {
//                    int id = Settings.Global.getInt(this.getContentResolver(), "multi_sim_data_call");
//                    SubscriptionInfo si = sm.getActiveSubscriptionInfo(id);
//                    if (si != null)
//                        sim = si.getSimSlotIndex();
//                } catch (Settings.SettingNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//   ################CODE FOR CHECKING SIM FROM WHICH INTERNET IS USED ##########################


        int simState = telephonyManager.getSimState();
        Log.i("DDDDDDDBBBMM", "sim: ");
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                Log.i("DDDDDDDBBBMM", "absent");
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_READY:
                Log.i("DDDDDDDBBBMM", "ready");
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                Log.i("DDDDDDDBBBMM", "unknown");
                break;
        }

        String strength = null;
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();   //This will give info of all sims present inside your mobile
        if (cellInfos != null) {
            for (int i = 0; i < cellInfos.size(); i++) {
                if (cellInfos.get(i).isRegistered()) {
                    if (cellInfos.get(i) instanceof CellInfoGsm) {
                        CellInfoGsm cellInfogsm = (CellInfoGsm) cellInfos.get(i);
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthGsm.getDbm());
                        Log.i("DDDDDDBBBBBMMMMMMMM gsm", strength + " ");
                    } else if (cellInfos.get(i) instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfos.get(i);
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        strength = String.valueOf(cellSignalStrengthLte.getDbm()) + "yoohooo";
                        Log.i("DDDDDDBBBBBMMMM lte " + i, strength + " ");
                    }

                    Log.i("DDDDDDBBBBBBBMMMMMMMM: " + i, strength + " none");
                }

            }
        }
//        Toast.makeText(this, "Strngth: " + strength+ " "+cellInfos.size(), Toast.LENGTH_SHORT).show();
        /* This is the test code */

//        Toast.makeText(this, "k "+telephonyManager.getSignalStrength().getGsmSignalStrength(), Toast.LENGTH_SHORT).show();


        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        spStoryShare = getSharedPreferences(StoryShareInfo, Context.MODE_PRIVATE);
        spWalletData = getSharedPreferences(WalletData, Context.MODE_PRIVATE);
        version_code = sp.getString("version_code", "0");
        if (version_code.equals("0")) {
            SharedPreferences.Editor walletEditor = spWalletData.edit();
            SharedPreferences.Editor editor = sp.edit();

            walletEditor.putString("Cash", "0");
            editor.putString("version_code", "1");
            version_code = "1";
            editor.apply();
        }

        Log.i("spWalletData---", "" + (spWalletData == null));
        requestQueue = Volley.newRequestQueue(this);
        initialiseUI();
        onCreateFlag = false;

        mFilter = new IntentFilter();
        mFilter.addAction(BULTOO_FILE);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bultooReceiver, mFilter);
        //encash(-100);


        //Runnable
        final Handler mHandler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Email asyncTask = new Email(getApplicationContext());
                asyncTask.execute();
                mHandler.postDelayed(this, 5000);
                //Log.d("Noting instance ","of runnable");
                handleSync();
            }
        };
        mHandler.post(runnable);

//        testLocation("8085678704");
        
    }

    private static void handleHiddenFile() {
        linesInFile = 0;
        linesInStorySP = 0;
        readFromFile();
        Map<String, ?> Keys = spStoryShare.getAll();
        for (Map.Entry<String, ?> row : Keys.entrySet()) {
            linesInStorySP++;
        }
        Log.d("Lines in SP=", "" + linesInStorySP);
        Log.d("Lines in File=", "" + linesInFile);
        if (linesInStorySP > linesInFile) {
            saveToFile();
        }
    }

    public void handleSync() {
        String pn = "", rbtmac = "", fn = "", cc = "";
        pn = phoneNumber.getText().toString();
        cc = opArray[Integer.parseInt(sp.getString("operator", "0"))];
        if (pn.length() == 10) {
            Map<String, ?> Keys = spStoryShare.getAll();

            for (Map.Entry<String, ?> row : Keys.entrySet()) {
                try {
                    rbtmac = row.getKey().split(",")[0];
                    fn = row.getKey().split(",")[1];
                    //Log.d("Separately: ",rbtmac+fn+cc);
                    if (row.getValue().toString().equals("0")) {
                        syncToServer(row.getKey(), pn, rbtmac, fn, cc);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void syncToServer(final String key1, final String pn, final String rbtmac, final String fn, final String cc) {
        String url = getString(R.string.base_url) + "newswaratoken";
        final SharedPreferences.Editor editor = spStoryShare.edit();
        Log.d("Syncing File:", key1);

        stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("Done!")) {
                            Log.d("Synced File:", key1);
                            editor.putString(key1, "1");
                            editor.commit();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Synced File:", "Failed");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("senderBTMAC", pn);
                params.put("receiverBTMAC", rbtmac);
                params.put("filename", fn);
                params.put("appName", "Surajpur Bultoo Radio");
                params.put("phoneNumber", pn);
                params.put("carrierCode", cc);
                return params;
            }
        };
        stringRequest.setTag(REQUESTTAG);
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void option1Recording(View view) {
        Intent goToRecordingScreen = new Intent(this, RecordingScreenActivity.class);
        goToRecordingScreen.putExtra("phone_number", phoneNumber.getText().toString());
        startActivity(goToRecordingScreen);
    }

    public void option4ShareApp(View view) {
        Intent sendName = new Intent();
        sendName.setAction(BULTOO_FILE);
        sendName.putExtra("problem_id", "Apk");
        sendName.putExtra("type", "apk");
        sendBroadcast(sendName);
        ApplicationInfo app = getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;
        Intent intent = new Intent(Intent.ACTION_SEND);
        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");
        // Append file and send Intent
        File originalApk = new File(filePath);
        //Make new directory in new location
        File tempFile = new File(getExternalCacheDir() + "/ExtractedApk");
        //If directory doesn't exists create new
        if (!tempFile.isDirectory())
            if (!tempFile.mkdirs())
                return;
        //Get application's name and convert to lowercase
        tempFile = new File(tempFile.getPath() + "/" + getString(app.labelRes).replace(" ", "").toLowerCase() + ".apk");
        //Copy file to new location
        InputStream in;
        OutputStream out;
        try {
            in = new FileInputStream(originalApk);
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            return;
        }

        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
        }

        Uri shareUri;
        if (Build.VERSION.SDK_INT >= 24) {
            shareUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile);
        } else {
            shareUri = Uri.fromFile(tempFile);
        }
        intent.setClassName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        startActivity(intent);

    }

    public void option2ListenSpecific(View view) {
        Intent storyList = new Intent(this, StoryListViewActivity.class);
        storyList.putExtra("phone_number", phoneNumber.getText().toString());
        storyList.putExtra("option", "2");
        startActivity(storyList);
    }

    public void option3ListenStories(View view) {
        Intent storyList = new Intent(this, StoryListViewActivity.class);
        storyList.putExtra("option", "3");
        startActivity(storyList);
    }

    public static final BroadcastReceiver bultooReceiver = new BroadcastReceiver() {
        private Long timeStartWhenConnected = 0L, timeWhenDisconnected = 0L;
        private String mDeviceAddress = "";
        private String problemId = "";
        private String type = "";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BULTOO_FILE)) {
                problemId = intent.getStringExtra("problem_id");
                type = intent.getStringExtra("type");
                Log.d("Type: ", type);
            }
            if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
                timeStartWhenConnected = System.currentTimeMillis() / 1000;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceAddress = device.getAddress(); // MAC address

            } else if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                timeWhenDisconnected = System.currentTimeMillis() / 1000;
                Log.d("Time: ", "" + (timeWhenDisconnected - timeStartWhenConnected));
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("Disconnected: ", "" + device.getAddress());

                if (device.getAddress().equals(mDeviceAddress) &&
                        (timeWhenDisconnected - timeStartWhenConnected) > 10 &&
                        (type.equals("apk") || type.equals("normal") || type.equals("bultoo"))
                        && !intent.getBooleanExtra("STRONG_NETWORK", false)) {
                    Toast.makeText(context, "Money should be added.", Toast.LENGTH_SHORT).show();
                    String key = mDeviceAddress + "," + problemId;
                    Log.d("Key: ", key);
                    switch (spStoryShare.getString(key, "-1")) {
                        case "-1":
                            SharedPreferences.Editor editor = spStoryShare.edit();
                            editor.putString(key, "0");
                            editor.apply();
                            Log.d("Case -1", "New Unique File Transfer");
                            if (type.equals("bultoo")) {
                                addInWallet();
                            }
                            break;
                        case "0":
                            Log.d("Case 0", "Already Shared But not Synced");
                            break;
                        case "1":
                            Log.d("Case 1", "Shared and Synced");
                            break;
                    }
                }

            }
        }
    };

    private static void addInWallet() {
        SharedPreferences.Editor editor = spWalletData.edit();
        editor.putString("Cash", (Integer.parseInt(spWalletData.getString("Cash", "0")) + CREDIT_AMOUNT) + "");
        editor.apply();
        if (linesInFile == 0) {
            editor.putString("Cash", 10 + "");
            editor.apply();
        }
        handleHiddenFile();
        Log.d("Cash", spWalletData.getString("Cash", "Error"));
    }

    private void writeIdInFile() {
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(exstPath + "/swararecordings");
        folder.mkdirs();
        String path = folder + "/id.prf";
        File file = new File(path);
        if (!file.exists()) {
            try {
                FileWriter f = new FileWriter(path);
                BufferedWriter bw = new BufferedWriter(f);
                bw.write("id:" + phoneNumber.getText().toString());
                bw.newLine();
                bw.flush();
                bw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readIdfromFile() {
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(exstPath + "/swararecordings");
        folder.mkdirs();
        String path = folder + "/id.prf";
        try {
            FileReader f = new FileReader(path);
            BufferedReader br = new BufferedReader(f);
            String line;
            while ((line = br.readLine()) != null) {
                id = line.split(":")[1];
                Log.d("Deciphered id as:", id);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("NumLine=", "" + linesInFile);
    }

    private static void encash(int amount) {
        SharedPreferences.Editor editor = spWalletData.edit();
        editor.putString("Cash", (Integer.parseInt(spWalletData.getString("Cash", "0")) - amount) + "");
        editor.apply();
        Log.d("Cash", spWalletData.getString("Cash", "Error"));
    }

    private static void setToWallet(String amount) {
        SharedPreferences.Editor editor = spWalletData.edit();
        editor.putString("Cash", amount);
        editor.apply();
        Log.d("Cash", spWalletData.getString("Cash", "Error"));
    }

    public static void saveToFile() {
        Map<String, ?> map = spStoryShare.getAll();
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(exstPath + "/swararecordings");
        folder.mkdirs();
        String path = folder + "/pref123.prf";
        try {
            FileWriter f = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(f);
            for (Map.Entry<String, ?> row : map.entrySet()) {
                bw.write(row.getKey() + "," + row.getValue());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile() {
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(exstPath + "/swararecordings");
        folder.mkdirs();
        String path = folder + "/pref123.prf";
        try {
            FileReader f = new FileReader(path);
            BufferedReader br = new BufferedReader(f);
            String line;
            SharedPreferences.Editor editor = spStoryShare.edit();
            while ((line = br.readLine()) != null) {
                linesInFile++;
                try {
                    if (spStoryShare.contains((line.split(",")[0] + "," + line.split(",")[1]))) {
                        Log.d("Preventing ", "Overwriting sync info");
                    } else {
                        editor.putString((line.split(",")[0] + "," + line.split(",")[1]), line.split(",")[2]);
                        editor.apply();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                Log.d("Key,Value", line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("NumLine=", "" + linesInFile);
    }

    @Override
    protected void onDestroy() {
        MainActivity.this.unregisterReceiver(bultooReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivity.this.invalidateOptionsMenu();
    }
}
