package com.example.MVMcR_MA_QCR;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeIP extends AppCompatActivity {

    EditText txt_ip;
    Button btn_ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ip);
        txt_ip=findViewById(R.id.edttxt_ip);
        btn_ip=findViewById(R.id.btn_change_ip);
        btn_ip.setOnClickListener(new OnIpBtnClickListner());
        // TODO: 23-06-2021 get current ip adress from db
        MyDbHelper myDbHelper=new MyDbHelper(ChangeIP.this,MyDbHelper.DB_NAME,null,1);
        String ip=myDbHelper.getIpAdress();
        txt_ip.setText(ip);
    }
    class OnIpBtnClickListner implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            String ip_adress=txt_ip.getText().toString();
            Pattern pattern=Pattern.compile("[0-9]{3}[.][0-9]{3}[.][0-9.]+");
            Matcher m=pattern.matcher(ip_adress);
            if (m.find()) {
                MyDbHelper myDbHelper=new MyDbHelper(ChangeIP.this,MyDbHelper.DB_NAME,null,1);
                myDbHelper.changeIp(ip_adress);
                Main_page.IP_ADDRESS="http://"+ip_adress+"/at/app";
                Toast.makeText(ChangeIP.this, "IP Address Changed Successfully...", Toast.LENGTH_SHORT).show();
                finish();
            }
           else{
                Toast.makeText(ChangeIP.this, "Please Enter Valid IP Address", Toast.LENGTH_SHORT).show();
            }

        }
    }
}