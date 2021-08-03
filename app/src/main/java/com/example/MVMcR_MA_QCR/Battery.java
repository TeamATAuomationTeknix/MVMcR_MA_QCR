package com.example.MVMcR_MA_QCR;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Battery extends AppCompatActivity {

    public static TextView qr, battery_qr;
    private ImageButton scanqr, scanqr1;
    private String qr_res,qr_res2;
    String model_name;
    TextView textView;
    String status="NOT OK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        textView = findViewById(R.id.txtView);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyy");
        String dateTime = simpleDateFormat.format(calendar.getTime());

        qr = (TextView) findViewById(R.id.txt_qr);
        battery_qr=findViewById(R.id.battery_qr);
        scanqr = (ImageButton) findViewById(R.id.btn_scan_qr);
        scanqr1 = (ImageButton) findViewById(R.id.btn_scan_qr2);
        qr_res = getIntent().getStringExtra("qr_result");
        qr_res2=getIntent().getStringExtra("battery_qr");
        qr.setText(qr_res);
        battery_qr.setText(qr_res2);
        if(battery_qr!=null){
            try {
                checking();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        scanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), ScanQR.class));
                Intent i = new Intent(Battery.this, ScanQR.class);
                i.putExtra("calling_page", "Battery");
                startActivity(i);
                finish();
            }
        });

        scanqr1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), ScanQR.class));
                if(qr.getText().toString().equals("")){
                    Toast.makeText(Battery.this, "Please scan QR code of car", Toast.LENGTH_SHORT).show();
                }else {
                    Intent i = new Intent(Battery.this, ScanQR1.class);
                    i.putExtra("calling_page", "Battery");
                    String q=getIntent().getStringExtra("qr_result");
                    i.putExtra("qr_result", q);
                    startActivity(i);
                    finish();
                }
            }
        });


        qr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inputQRCode();
                return false;
            }
        });
        battery_qr.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(!qr.getText().toString().equals(""))
                        checkBatteryqr();
                        else
                            Toast.makeText(Battery.this, "Please insert main QR code", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
        );

    }

    private String getModel(){
        if(model_name==null) {
            String  qr_code = qr.getText().toString().trim();
            String[] a = qr_code.split("_");
            String model_name = a[1].charAt(1)+""+a[1].charAt(2);
        }
        return model_name;
    }

    private void inputQRCode(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR Code.");
        builder.setMessage("Enter Correct QR Code.");

        // Set up the input
        final EditText inputQR = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputQR.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        int maxLength = 36;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        inputQR.setFilters(filters);
        builder.setView(inputQR);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String qr_code = inputQR.getText().toString();//toUpperCase().trim().replaceAll("\\s+", "").replaceAll("\n","");
               // Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}");
               // if (!qr_code.equals("") && p.matcher(qr_code).matches()) {
                if(qr_code.length()==36){

                    Intent i = new Intent(Battery.this, Battery.class);
                    i.putExtra("qr_result", qr_code);
                    startActivity(i);
                    finish();
                    //qr.setText(qr_code);
                    //isModelNameExist();
                }else {
                    Toast.makeText(getApplicationContext(), "Invalid QR Code...!", Toast.LENGTH_LONG).show();
                    inputQRCode();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    private void checking() throws ParseException {
        String txtQR = battery_qr.getText().toString();
        String[] arr=txtQR.split(":");
        if(arr.length>=4) {
            Date date = new Date();
            long today = date.getTime();
            Date prev = new SimpleDateFormat("ddMMyy").parse(arr[3]);
            long pretime = prev.getTime();
            long milliseconds = today - pretime;
            int days = (int) (milliseconds / (60 * 60 * 24 * 1000));
            if (days <= 45) {
                status="\n"+days;
                textView.setText(status);
                textView.setBackgroundResource(R.drawable.green_circle);
            } else {
                status="\n"+days;
                textView.setText(status);
                textView.setBackgroundResource(R.drawable.red_circle);
            }
            ServerJson serverJson=new ServerJson(this);
            HashMap<String,String> hashMap=new HashMap<>();
            hashMap.put("mainqr",qr.getText().toString());
            hashMap.put("batteryqr",battery_qr.getText().toString());
            hashMap.put("status",textView.getText().toString());
            serverJson.insertBatteryStatus(hashMap);
        }
        else{
            if(txtQR!="")
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
        }
        
    }
    public void checkBatteryqr(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR Code.");
        builder.setMessage("Enter Battery QR Code.");

        // Set up the input
        final EditText inputQR = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputQR.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        int maxLength = 36;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        inputQR.setFilters(filters);
        builder.setView(inputQR);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String qr_code = inputQR.getText().toString();//toUpperCase().trim().replaceAll("\\s+", "").replaceAll("\n","");
                // Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}");
                // if (!qr_code.equals("") && p.matcher(qr_code).matches()) {
                String[] arr=qr_code.split(":");
                if(arr.length>=4){

                    Intent i = new Intent(Battery.this, Battery.class);
                    i.putExtra("battery_qr", qr_code);
                    i.putExtra("qr_result", qr.getText().toString());
                    startActivity(i);
                    finish();
                    //qr.setText(qr_code);
                    //isModelNameExist();
                }else {
                    Toast.makeText(getApplicationContext(), "Invalid QR Code...!", Toast.LENGTH_LONG).show();
                    inputQRCode();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    //todo add home icon to tolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}