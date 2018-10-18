package com.solux.bletest.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.solux.bletest.R;
import com.solux.bletest.constants.Constants;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    public static void startAct(Context context,String s){
        Intent intent = new Intent();
        intent.putExtra(Constants.HISTORY_DATA, s);
        intent.setClass(context,DetailActivity.class);
        context.startActivity(intent);
    }
}
