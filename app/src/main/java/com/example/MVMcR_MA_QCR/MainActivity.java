package com.example.MVMcR_MA_QCR;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    BiometricPrompt biometricPrompt;
    Executor executor;
    BiometricPrompt.PromptInfo promptInfo;
    Button btnCheck;

    @RequiresApi(api = Build.VERSION_CODES.P)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkStateChecker(),intentFilter);
        btnCheck=findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(new OnBtnCheckListner());
    }

    class OnBtnCheckListner implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            executor= ContextCompat.getMainExecutor(MainActivity.this);
            biometricPrompt=new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                   // Toast.makeText(MainActivity.this, "authentication error", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                    openActivity();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(MainActivity.this, "authentication failed", Toast.LENGTH_SHORT).show();
                }
            });
            promptInfo=new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric login from my app")
                    .setSubtitle("Login using biometric credential")
                    .setNegativeButtonText("use account password")
                    .build();
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void openActivity() {
        Intent intent = new Intent(this, Token.class);
        startActivity(intent);
    }
}