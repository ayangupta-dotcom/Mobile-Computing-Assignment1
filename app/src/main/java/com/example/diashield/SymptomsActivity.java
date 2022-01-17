package com.example.diashield;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class SymptomsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ContentValues contentVal = new ContentValues();
    RatingBar ratingBar;
    Map<String, String> symptomsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);
        Spinner spinner = findViewById(R.id.Spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.symptoms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Intent intent = getIntent();
        int rowId = intent.getIntExtra("rowId", 0);
        String Name = intent.getStringExtra("Name");

        symptomsMap.put(getString(R.string.headache), com.example.diashield.DatabaseHelper.COL_HEADACHE);
        symptomsMap.put(getString(R.string.fever_or_chills), com.example.diashield.DatabaseHelper.COL_FEVER);
        symptomsMap.put(getString(R.string.nausea), com.example.diashield.DatabaseHelper.COL_NAUSEA);
        symptomsMap.put(getString(R.string.diarrhea), com.example.diashield.DatabaseHelper.COL_DIARRHEA);
        symptomsMap.put(getString(R.string.sore_throat), com.example.diashield.DatabaseHelper.COL_SORE_THROAT);
        symptomsMap.put(getString(R.string.muscle_pain), com.example.diashield.DatabaseHelper.COL_MUSCLE_PAIN);
        symptomsMap.put(getString(R.string.cough), com.example.diashield.DatabaseHelper.COL_COUGH);
        symptomsMap.put(getString(R.string.loss_of_smell_or_taste), com.example.diashield.DatabaseHelper.COL_LOSS_OF_SMELL_OR_TASTE);
        symptomsMap.put(getString(R.string.shortness_of_breath), com.example.diashield.DatabaseHelper.COL_SHORTNESS_OF_BREATH);
        symptomsMap.put(getString(R.string.fatigue), com.example.diashield.DatabaseHelper.COL_FATIGUE);

        // Click handler for rating bar
        ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String selectedSymptom = spinner.getSelectedItem().toString();
                String symptomsColumn = symptomsMap.get(selectedSymptom);
                contentVal.put(symptomsColumn, ratingBar.getRating());
            }
        });

        // Click handler for upload button
        Button uploadBtn =findViewById(R.id.btn_uploadSymp);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.example.diashield.DatabaseHelper databaseHelper = new com.example.diashield.DatabaseHelper(SymptomsActivity.this, Name);
                databaseHelper.updateRow(contentVal, rowId);
                Toast.makeText(getApplicationContext(),"Symptom ratings are uploaded successfully.",Toast.LENGTH_SHORT).show();
               // Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
              //  startActivity(mainActivity);
            }
        });

        // Click handler for Back button
        Button backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivity);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String symptom = parent.getItemAtPosition(position).toString();
        String symptomCol = symptomsMap.get(symptom);
        float rating = 0;

        if (contentVal.get(symptomCol) != null) {
            rating = (float) contentVal.get(symptomCol);
        }

        ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setRating(rating);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}