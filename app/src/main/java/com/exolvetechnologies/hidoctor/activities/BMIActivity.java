package com.exolvetechnologies.hidoctor.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.views.GaugeView;

import java.text.DecimalFormat;

public class BMIActivity extends AppCompatActivity {

    private TextView hFeet, hInch, hKg, desc, result;
    private EditText editTextFt, editTextInch, editTextKg;
    private GaugeView mGaugeView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        hFeet = (TextView) findViewById(R.id.bmiParamsFeet);
        hInch = (TextView) findViewById(R.id.bmiParamsInch);
        hKg = (TextView) findViewById(R.id.bmiParamsKg);
        desc = (TextView) findViewById(R.id.bmiResultDesc);
        result = (TextView) findViewById(R.id.bmiResultTitle);

        editTextFt = (EditText) findViewById(R.id.editTextFt);
        editTextInch = (EditText) findViewById(R.id.editTextInch);
        editTextKg = (EditText) findViewById(R.id.editTextKg);


        mGaugeView1 = (GaugeView) findViewById(R.id.gauge_view1);

        mGaugeView1.setTargetValue(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                calculateBMI();
            }
        });

        editTextFt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hFeet.setText(s.toString()+"ft ");
                if (s.length() > 0){
                    editTextInch.requestFocus();
                }
            }
        });

        editTextInch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hInch.setText(s.toString()+"\" ");
                if (s.length() == 2){
                    editTextKg.requestFocus();
                }
            }
        });

        editTextKg.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hKg.setText(s.toString()+"kg");
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("SetTextI18n")
    private void calculateBMI(){
        String feet = editTextFt.getText().toString();
        String inch = editTextInch.getText().toString();
        String kg = editTextKg.getText().toString();
        DecimalFormat df = new DecimalFormat("#.##");

        if (feet.length() < 1){
            editTextFt.setError("Please enter height (ft)");
            editTextFt.requestFocus();
        }
        else if (inch.length() < 1){
            editTextInch.setError("Please enter height (inch)");
            editTextInch.requestFocus();
        }
        else if (kg.length() < 1){
            editTextKg.setError("Please enter weight (kg)");
            editTextKg.requestFocus();
        }else {
            double dFeet = Double.parseDouble(feet);
            double dInch = Double.parseDouble(inch);
            double dKg = Double.parseDouble(kg);
            double inchToFeet = dInch / 12;
            double height = (dFeet + inchToFeet) * 0.3048;

            double   bmi = dKg / (height * height);

            if(bmi < 18.5) {
                result.setText("Underweight");
                desc.setText(String.valueOf(df.format(bmi)));
                result.setTextColor(Color.parseColor("#e86f21"));
                setBmiValue(bmi);
            }
            if((bmi > 18.5) && (bmi < 24.9)) {
                result.setText("Normal");
                desc.setText(String.valueOf(df.format(bmi)));
                result.setTextColor(Color.parseColor("#1bca21"));
                //bmiImage.setImageResource(R.mipmap.normal);
                setBmiValue(bmi);
            }
            if((bmi > 25.0) && (bmi < 29.9)) {
                result.setText("Overweight");
                desc.setText(String.valueOf(df.format(bmi)));
                result.setTextColor(Color.parseColor("#e8e721"));
                //bmiImage.setImageResource(R.mipmap.overweight);
                setBmiValue(bmi);
            }
            if((bmi > 30) && (bmi < 34.9)) {
                result.setText("Obese");
                desc.setText(String.valueOf(df.format(bmi)));
                result.setTextColor(Color.parseColor("#e86f21"));
                //bmiImage.setImageResource(R.mipmap.obese);
                setBmiValue(bmi);
            }
            if(bmi >= 35) {
                result.setText("Extremely Obese");
                desc.setText(String.valueOf(df.format(bmi)));
                result.setTextColor(Color.parseColor("#e7202b"));
                //bmiImage.setImageResource(R.mipmap.extremely_obese);
                setBmiValue(bmi);
            }
            }
    }

    private void setBmiValue(double value){
        //Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this, R.anim.animator);
        //v.startAnimation(hyperspaceJumpAnimation);
        int target = (int) Math.round(value);
        mGaugeView1.setTargetValue(target);
    }

}
