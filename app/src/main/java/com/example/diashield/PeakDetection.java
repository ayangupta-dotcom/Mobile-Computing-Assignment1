package com.example.diashield;


import android.util.Log;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PeakDetection {
    private String TAG;

    public int countPeaks(List<Integer> list){
        int count = 0;
        for(int i=1; i<list.size(); i++){
            if(list.get(i-1) < list.get(i)){
                count+=1;
            }
        }
        return count;

    }


    public HashMap<String, List> analyzeDataForSignals(List<Float> data, int lag, float threshold, float influence) {

        // init stats instance
        SummaryStatistics stats = new SummaryStatistics();

        // the results (peaks, 1 or -1) of our algorithm
        List<Integer> signals = new ArrayList<Integer>(Collections.nCopies(data.size(), 0));

        // filter out the signals (peaks) from our original list (using influence arg)
        List<Float> filteredData = new ArrayList<Float>(data);

        // the current average of the rolling window
        List<Float> avgFilter = new ArrayList<Float>(Collections.nCopies(data.size(), 0.0f));

        // the current standard deviation of the rolling window
        List<Float> stdFilter = new ArrayList<Float>(Collections.nCopies(data.size(), 0.0f));


        // init avgFilter and stdFilter
        for (int i = 0; i < lag; i++) {
            stats.addValue(data.get(i));
        }
        avgFilter.set(lag - 1, (float) stats.getMean());
        stdFilter.set(lag - 1, (float) Math.sqrt(stats.getPopulationVariance())); // getStandardDeviation() uses sample variance
        stats.clear();
        Log.v(TAG,  "avgFilter");


        // loop input starting at end of rolling window
        for (int i = lag; i < data.size(); i++) {
            // if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs((data.get(i) - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {
                // this is a signal (i.e. peak), determine if it is a positive or negative signal
                if (data.get(i) > avgFilter.get(i - 1)) {
                    signals.set(i, 1);
                } else {
                    signals.set(i, -1);
                }

                // filter this signal out using influence
                filteredData.set(i, (influence * data.get(i)) + ((1 - influence) * filteredData.get(i - 1)));
            } else {
                // ensure this signal remains a zero
                signals.set(i, 0);
                // ensure this value is not filtered
                filteredData.set(i, data.get(i));
            }

            // update rolling average and deviation
            for (int j = i - lag; j < i; j++) {
                stats.addValue(filteredData.get(j));
            }
            avgFilter.set(i, (float) stats.getMean());
            stdFilter.set(i, (float) Math.sqrt(stats.getPopulationVariance()));
            stats.clear();
        }

        HashMap<String, List> returnMap = new HashMap<String, List>();
        returnMap.put("signals", signals);
        returnMap.put("filteredData", filteredData);
        returnMap.put("avgFilter", avgFilter);
        returnMap.put("stdFilter", stdFilter);

        Log.v(TAG,  "returnMap");


        return returnMap;





    } // end
}
