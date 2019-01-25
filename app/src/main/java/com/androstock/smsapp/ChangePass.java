package com.androstock.smsapp;

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

public class ChangePass extends AppCompatActivity {

    String pass;
    String confirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        final PatternLockView patternLockView = findViewById(R.id.patternView);

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

                if(TextUtils.isEmpty(pass)){
                    pass = PatternLockUtils.patternToString(patternLockView, pattern);
                    Toast.makeText(ChangePass.this, "Masukan pattern sekali lagi", Toast.LENGTH_SHORT).show();

                    patternLockView.clearPattern();
                }else if(TextUtils.isEmpty(confirmPass)){
                    confirmPass = PatternLockUtils.patternToString(patternLockView, pattern);

                    if (pass.equalsIgnoreCase(confirmPass)) {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                        Toast.makeText(ChangePass.this, "Password berhasil dibuat!", Toast.LENGTH_LONG).show();

                        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                                "lock_pattern", getApplicationContext().MODE_PRIVATE);
                        String keyPattern = "key_pattern";

                        prefs.edit().putString(keyPattern, pass).apply();

//                        Intent mainScreen = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(mainScreen);
                        finish();

                    } else {
                        patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        Toast.makeText(ChangePass.this, "Pattern tidak sama! Silahkan ulangi kembali.", Toast.LENGTH_LONG).show();
                    }

                }else {
                    pass = "";
                    pass = "";
                }


            }

            @Override
            public void onCleared() {

            }
        });
    }
}
