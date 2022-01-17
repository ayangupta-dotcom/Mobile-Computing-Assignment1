package com.example.diashield;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    User user = null;
    DatabaseHelper dbHelper = null;
    long rowId = 0;
    private static final int REQUEST_VIDEO_CAPTURE= 1;
    VideoView videoView;
    Uri videoUri;
    private static final String TAG = "DiaShield";
    public static double hr = 0.0;
    boolean measureBtnClicked = false;
    int CAMERA_PERMISSION_REQUEST_CODE = 0x01;
    EditText eText;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


//        this.dbHelper = new DatabaseHelper(MainActivity.this, user.Name);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//

        eText = (EditText) findViewById(R.id.enterLastName);
        btn = (Button) findViewById(R.id.enter);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = eText.getText().toString();
                user.Name=str;

                Toast msg = Toast.makeText(getBaseContext(),"Last Name is"+str,Toast.LENGTH_LONG);
                msg.show();
            }
        });
        user = new User();
        this.dbHelper = new DatabaseHelper(MainActivity.this, user.Name+".db");


        // Measure Respiratory Rate
        Button respBtn = (Button) findViewById(R.id.respRate);
        respBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IntentFilter filter = new IntentFilter();
//                filter.addAction("success");
//                registerReceiver(bReceiver, filter);
                Intent respiratoryService = new Intent(getApplicationContext(),RespiratoryService.class);
                startService(respiratoryService);

                TextView tv = (TextView) findViewById(R.id.respRateVal);
                tv.setText("Calculating the Respiratory Rate");
                if(user == null) {
                    user = new User();
                }
//
            }
        });

        // Heart Rate
        // Open Camera
        Button cameraBtn = (Button) findViewById(R.id.openCam);
        cameraBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    if(hasCamera()) {
                        if(user == null) {
                            user = new User();
                        }
                        startRecording();
                    }
                }
            }
        });

        // Measure Heart Rate
        Button heartBtn = (Button) findViewById(R.id.heartRate);
        heartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.heartRateVal);
//                if(user == null) {
//                    user = new User();
//                }

                tv.setText("Calculating the Heart Rate");
                measureBtnClicked = true;


            }
        });

        // Upload Heart Rate and Respiratory Rate
        Button uploadBtn = (Button) findViewById(R.id.upSymp);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowId = dbHelper.addRow(user);
                user.id = (int) rowId;
                Toast.makeText(MainActivity.this, "Heart Rate and Respiratory Rate are uploaded successfully with rowid " + rowId, Toast.LENGTH_LONG).show();
                Log.v(TAG, "Values in user are: " + String.valueOf(user.heartrate) + " " + String.valueOf(user.resprate));
            }
        });

        // Continue Button Handler
        Button continueBtn = (Button) findViewById(R.id.symp);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent symptomsActivity = new Intent(MainActivity.this, SymptomsActivity.class);
                symptomsActivity.putExtra("rowId", user.id);
                symptomsActivity.putExtra("Name", user.Name);
                MainActivity.this.startActivity(symptomsActivity);
            }

            private void startActivity(Intent symptomsActivity) {
            }
        });
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0x01:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(hasCamera()) {
                        if(user == null) {
                            user = new User();
                        }
                        startRecording();
                    }
                }
                break;
        }
    }

    BroadcastReceiver bReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.example.DiaShield.RESP_RATE")) {
                String data = intent.getStringExtra("data");
                user.resprate = Double.parseDouble(data);
                TextView tv = findViewById(R.id.respRateVal);
                Log.v(TAG, "Received intent with data: " + data);
                tv.setText(data);
            }
        }
    };

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("com.example.DiaShield.RESP_RATE"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    // Check if camera is available
    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY);
    }

    // STart Recording
    private void startRecording() {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,45);
//        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
        try {
            startActivityForResult(takeVideoIntent,REQUEST_VIDEO_CAPTURE);
            Toast.makeText(this, "Started Activity", Toast.LENGTH_LONG).show();
        }catch (ActivityNotFoundException e) {
            Toast.makeText(this, "failed ", Toast.LENGTH_LONG).show();
        }
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = intent.getData();
            new heartRateTask().execute(videoUri);
            Toast.makeText(this, "Started ", Toast.LENGTH_LONG).show();
        }
    }

    private class heartRateTask extends AsyncTask<Uri, Long, Void> {
        @Override
        protected void onPreExecute(){
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected Void doInBackground(Uri... params) {

            try {
                publishProgress((Long) 0L);
                while(!measureBtnClicked);
                measureRate(params[0]);
            } catch (Exception e) {
                Log.e(TAG, "Measure rate exception");
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Long... value){
            Log.v(TAG,  "Progress Update");

        }
        @Override
        protected void onPostExecute(final Void unused){
            Log.v(TAG,  "Post");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void measureRate(Uri videoUri) {
        try {
            Log.v(TAG, "Something started");
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(this, videoUri);
//            Toast.makeText(this, "measureRATE ", Toast.LENGTH_LONG).show();
            //int Count = metaRetriever.METADATA_KEY_VIDEO_FRAME_COUNT;
            //int Count = Integer.parseInt(str_count);
            // ArrayList<Bitmap> rev=new ArrayList<Bitmap>();
            // String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FingertipVideo.mp4";
            //String path = "/sdcard/FingertipVideo.mp4";
            // Log.d("mani", path);
            //MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            //metaRetriever.setDataSource(path, new HashMap<String, String>());
            //String durString = metaRetriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_DURATION );
            //int millis = Integer.parseInt(durString);
            //Create a new Media Player
            Uri file_path_URI = Uri.parse(this.toString());
            MediaPlayer mp = MediaPlayer.create(getBaseContext(), videoUri);
            ArrayList<Float> arr = new ArrayList<Float>();
            int videoDuration = mp.getDuration();
            int processFramesPerSec = 12;
            int processtime = 100000;
            int indexFrame = 0;
            int totalframes = 0;
            int differenceThreashhold = 12;
            int outputHeartRate =0;
            totalframes = (int) Math.floor(videoDuration/1000) * processFramesPerSec;
            outputHeartRate = 1;
            indexFrame = 1;
            while(indexFrame < totalframes) {
                float currentColor = 0f;
                Bitmap currentFrameBitmap = metaRetriever.getFrameAtTime(processtime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                processtime = processtime + 100000;
                //Get an area of image
                int i = 450;
                while (i <= 550) {
                    int j = 900;
                    while (j < 1200) {
                        currentColor = currentColor + Color.red(currentFrameBitmap.getPixel(i, j));
                        j++;
                    }
                    i++;
                }

                float previousColor = 1f;
                boolean isArrayListEmpty = (arr.size()!=0);
                if(isArrayListEmpty!=false){
                    int currentSize = arr.size();
                    previousColor = arr.get(currentSize - 1); //-1 because index starts at 0
                }

                boolean isCountable = Math.abs(previousColor - currentColor) > differenceThreashhold;
                if(isCountable == true){
                    outputHeartRate++;
                }

                arr.add(currentColor);

                indexFrame++;
            }
//            int millis = mp.getDuration();
////            Toast.makeText(this, "Heart Rate calculating", Toast.LENGTH_LONG).show();
//
//            Log.v(TAG, "Before calculating frames");
//            for(int i=1000000;i<millis*1000;i+=100000)
//            {
//                Bitmap bitmap=metaRetriever.getFrameAtTime(i,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//                int tot_pixel = 0;
//                int red_val = 0;
//                for (int j = 0; j < bitmap.getHeight(); j++) {
//                    for (int k = 0; k < bitmap.getWidth(); k++) {
//                        int col = bitmap.getPixel(k, j);
//                        tot_pixel++;
//                        red_val += Color.red(col);
//                    }
//                }
////                Toast.makeText(this, "DDD ", Toast.LENGTH_LONG).show();
//                double res = (double)red_val / tot_pixel;
//                arr.add(res);
//                Toast.makeText(this, "RES "+arr, Toast.LENGTH_LONG).show();

            //}

          /*
          String time = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
          long Count = Long.parseLong(time);
          long i = 0;
          ArrayList<Double> arr = new ArrayList<Double>();
          while (i < Count) {
              Bitmap bitmap = metaRetriever.getFrameAtTime(i);
              int tot_pixel = 0;
              int red_val = 0;
              for (int j = 0; j < bitmap.getHeight(); j++) {
                  for (int k = 0; k < bitmap.getWidth(); k++) {
                      int col = bitmap.getPixel(k, j);
                      tot_pixel++;
                      red_val += Color.red(col);
                  }
              }
              double res = (double)red_val / tot_pixel;
              arr.add(res);
              i=i+1000;
          }
          */
            Log.v(TAG, "Before calculating peaks.");
            int lag = 5;
            float threshold = 1.0F;
            float influence = (float) 0.1;
            PeakDetection peakDetector = new PeakDetection();
            Log.v(TAG, "Returned from peak detection");
            HashMap<String, List> resultsMap = peakDetector.analyzeDataForSignals(arr, lag, (float) threshold, influence);
            Log.v(TAG, "Calculated HashMap");
            List<Integer> signalsList = resultsMap.get("signals");
            Log.v(TAG, String.valueOf(resultsMap));
            int totalPeaks = peakDetector.countPeaks(signalsList);
            totalPeaks = totalPeaks*60/45;
            //Long heartRate = String.valueOf(totalPeaks);
            int heartRate = totalPeaks;
//            Toast.makeText(this, "TotalPeaks "+ totalPeaks, Toast.LENGTH_LONG).show();

            if(user == null) {
                user = new User();
            }
            user.heartrate = heartRate;
            hr = heartRate*3;
            Log.v("red", resultsMap.toString());
            Log.v(TAG, "Measured Heartarte: " + user.heartrate);
//            Toast.makeText(this, "Heart Rate = " + totalPeaks, Toast.LENGTH_LONG).show();
//            calculating = false;
            TextView tv = (TextView) findViewById(R.id.heartRateVal);
//                if(user == null) {
//                    user = new User();
//                }

            tv.setText(Double.toString(hr));
        }   catch(ActivityNotFoundException e) {
            Log.v(TAG, "Something went wrong");
        }
    }

}