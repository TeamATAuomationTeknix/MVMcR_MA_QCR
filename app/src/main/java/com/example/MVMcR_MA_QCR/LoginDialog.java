package com.example.MVMcR_MA_QCR;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LoginDialog extends Dialog {
    Context context;
    EditText txt_user,txt_pass;
    Button btnLogin;
    public LoginDialog(@NonNull Context context) {
        super(context);
        this.context=context;
    }
    protected LoginDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
        txt_user=findViewById(R.id.loginUser);
        txt_pass=findViewById(R.id.loginPass);
        btnLogin=findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnLoginClickListner());
    }
    class OnLoginClickListner implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            if(txt_user.getText().toString().equals("teamat")&&txt_pass.getText().toString().equals("TeamAT@01")){
                Intent intent=new Intent(context,ChangeIP.class);
                context.startActivity(intent);
                LoginDialog.this.dismiss();
            }
            else
                Toast.makeText(context, "Incorrect Username Or Password", Toast.LENGTH_SHORT).show();
        }
    }
}
