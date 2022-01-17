package com.example.diashield;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

public class sensorHandlerClass extends Service implements SensorEventListener {
    public sensorHandlerClass() {
    }

    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[128];
    float accelValuesY[] = new float[128];
    float accelValuesZ[] = new float[128];
    int index = 0;

    @Override
    public void onCreate(){
        Toast.makeText(this, "Sensor Service Started", Toast.LENGTH_LONG).show();
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];
            if(index >= 450){
                index = 0;
                accelManage.unregisterListener(this);
                callRespRate();
                accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    public void callRespRate() {
        float diffArray[] = new float[450];
        for (int i = 0; i < 450; i++) {
            diffArray[i] = accelValuesZ[i+1] - accelValuesZ[i];
        }

        float epsilon;
        epsilon = (float) 0.05;
        int newPeak = 0;
        for (int i = 0; i < 450; i++) {
            if (diffArray[i] < epsilon && diffArray[i-1] > epsilon) {
                newPeak = newPeak + 1;
            }
        }

        float respRate = 60 * newPeak /45;
        Toast.makeText(this, "Respiratory Rate = " + respRate, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}