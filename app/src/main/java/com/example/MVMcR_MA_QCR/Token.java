package com.example.MVMcR_MA_QCR;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Token extends AppCompatActivity {
    EditText token;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        setupVariables();
    }

    private void setupVariables() {
        token = (EditText) findViewById(R.id.token_number);
        token.setOnKeyListener(new View.OnKeyListener()
        {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    Log.e("key code",keyCode+"");
                    switch (keyCode)
                    {

                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            Login();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        btn = (Button) findViewById(R.id.submit_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

        btn = (Button) findViewById(R.id.submit_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });

    }

    public void Login() {
        String tk=token.getText().toString();
        MyDbHelper myDbHelper=new MyDbHelper(Token.this,MyDbHelper.DB_NAME,null,1);
        String empname=myDbHelper.getEmployee(tk);
        if(tk.equals("")){
            Toast.makeText(this, "Please Enter Token Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if(tk.equals("1")){
            SharedPreferences preferences=getSharedPreferences("userpref",MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString("user","user1");
            Intent i = new Intent(getApplicationContext(), Main_page.class);
            startActivity(i);
            Toast.makeText(Token.this, "Successfully login", Toast.LENGTH_SHORT).show();
        }
        if (!empname.equals("")) {
            Log.e("emp is",empname);
            SharedPreferences preferences=getSharedPreferences("userpref",MODE_PRIVATE);
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString("user",empname);
            editor.apply();
            Intent i = new Intent(getApplicationContext(), Main_page.class);
            startActivity(i);
            Toast.makeText(Token.this, "Successfully login", Toast.LENGTH_SHORT).show();
        } else {
            ServerJson serverJson=new ServerJson(this);
            serverJson.getEmpInfo(token.getText().toString());
        }
    }
}