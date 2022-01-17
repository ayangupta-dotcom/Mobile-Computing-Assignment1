package com.example.diashield;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.w3c.dom.Text;

public class RespiratoryService extends Service implements SensorEventListener {

    float RespRate = 0f;
    public RespiratoryService() {
    }
    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[451];
    float accelValuesY[] = new float[451];
    float accelValuesZ[] = new float[451];
    int index = 0;
    @Override
    public int onStartCommand(Intent intent, int flags,int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel,SensorManager.SENSOR_DELAY_NORMAL);
        return START_NOT_STICKY;
    }

//    private void sendBroadcast (boolean success){
//        Intent intent = new Intent ("message"); //put the same message as in the filter you used in the activity when registering the receiver
//        intent.putExtra("success", RespRate);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if(mySensor.getType()== mySensor.TYPE_ACCELEROMETER) {

            accelValuesX[index] = event.values[0];
            accelValuesY[index] = event.values[1];
            accelValuesZ[index] = event.values[2];
            index++;

            if(index>=450) {
                index=0;
                accelManage.unregisterListener(this);
                findRespRate();
                //accelManage.registerListener(this, senseAccel,SensorManager.SENSOR_DELAY_NORMAL);
            }

        }
    }

    public void findRespRate() {
        float diffArray[] = new float[451];
        for(int i=0;i<450;i++) {
            diffArray[i] = Math.abs(accelValuesZ[i+1]- accelValuesZ[i]);
        }

        float epsilon;
        epsilon = (float)0.1;
        int newPeak = 0;
        for(int i=1;i<450;i++) {
            if(diffArray[i]<epsilon && diffArray[i-1]>epsilon) {
                newPeak = newPeak + 1;
            }
            Log.v("Dia", "Calculating for i: " + String.valueOf(i));
        }


        RespRate = ((newPeak * 45)/60)/2;
        Log.v("Dia", "Calculated resp rate");
//        Toast.makeText(this,"Respirator Rate = " + RespRate/2, Toast.LENGTH_LONG).show();
        Intent intent = new Intent("com.example.DiaShield.RESP_RATE");
        intent.putExtra("data", String.valueOf(RespRate/2));
//        intent.putExtra("data", RespRate);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}