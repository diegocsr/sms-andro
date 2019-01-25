package com.androstock.smsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

public class LockActivity extends AppCompatActivity {

  String keyPatternSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        final PatternLockView patternLockView = findViewById(R.id.patternView);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                "lock_pattern", getApplicationContext().MODE_PRIVATE);
        String keyPattern = "key_pattern";

        String key = prefs.getString(keyPattern, "");

        if(TextUtils.isEmpty(key)){
            Toast.makeText(this, "Silahkan buat kunci pattern", Toast.LENGTH_SHORT).show();

            Intent changePass = new Intent(getApplicationContext(), ChangePass.class);
            startActivity(changePass);
            finish();
        }else {
            keyPatternSaved = key;
        }

        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List progressPattern) {

            }

            @Override
            public void onComplete(List pattern) {

                Log.d(getClass().getName(), "Pattern complete: " +
                        PatternLockUtils.patternToString(patternLockView, pattern));
                if (PatternLockUtils.patternToString(patternLockView, pattern).equalsIgnoreCase(keyPatternSaved)) {
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    Toast.makeText(LockActivity.this, "Correct!", Toast.LENGTH_LONG).show();

                    Intent mainScreen = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainScreen);
                    finish();

                } else {
                    patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(LockActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();

                    patternLockView.clearPattern();
                }
            }

            @Override
            public void onCleared() {
            }
        });

    }
}
