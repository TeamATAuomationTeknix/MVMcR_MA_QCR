package com.example.MVMcR_MA_QCR;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQR1 extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private String page;
    public static TextView qu;
    private String partname;
    ZXingScannerView scannerView;
    String qr_res="res";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(ScanQR1.this);
        setContentView(scannerView);
        qr_res=getIntent().getStringExtra("qr_result");
        qu = (TextView) findViewById(R.id.txtView);
        Log.e("tag", "scan qr activity started");
        //Getting data from ScanQR
        page = getIntent().getStringExtra("calling_page");
        partname = getIntent().getStringExtra("partname");
        // Add Back Arrow to Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            switch (page){
                case "Questions":
                    Intent qu = new Intent(ScanQR1.this, Battery.class);
                    qu.putExtra("partname",partname);
                    qu.putExtra("qrScanned","no");
                    qu.putExtra("qr_result",qr_res);
                    startActivity(qu);
                    finish();
                    break;
                case "Battery":
                    Intent inte = new Intent(ScanQR1.this, Battery.class);
                    inte.putExtra("qr_result",qr_res);
                    startActivity(inte);
                    finish();
                    break;
                default:
                    Toast.makeText(ScanQR1.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {

        Pattern p = Pattern.compile("\\d{3}:\\d{4}\\S{2}\\d{5}\\S{1}:\\S{2}\\d{3}:\\S{6}:\\d{2}:\\d{5}");
        //067:1401AA00762N:DA040:140421:02:06163
       // if (p.matcher(rawResult.getText()).matches()) {
        String txtQR = rawResult.getText();
        String[] arr=txtQR.split(":");
        if(arr.length==4){
            //Toast.makeText(ScanQR.this, "Done", Toast.LENGTH_SHORT).show();
            switch (page){
                case "Questions":
                    Intent qu = new Intent(ScanQR1.this, Battery.class);
                    qu.putExtra("partname",partname);
                    qu.putExtra("qrScanned","scanned");
                    qu.putExtra("qr_result",qr_res);
                    qu.putExtra("battery_qr", rawResult.getText());
                    startActivity(qu);
                    finish();
                    break;
                case "Battery":
                    Intent inte = new Intent(ScanQR1.this, Battery.class);
                    inte.putExtra("battery_qr", rawResult.getText());
                    inte.putExtra("qr_result",qr_res);
                    startActivity(inte);
                    finish();
                    break;
                default:
                    Toast.makeText(ScanQR1.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }


        } else {
            qr_res=getIntent().getStringExtra("qr_result");

            Toast.makeText(ScanQR1.this, "Invalid QR Code...!", Toast.LENGTH_LONG).show();
            switch (page){
                case "Questions":
                    Intent qu = new Intent(ScanQR1.this, Battery.class);
                    qu.putExtra("partname",partname);
                    qu.putExtra("qrScanned","o");
                    startActivity(qu);
                    finish();
                    break;
                case "Battery":
                    Intent inte = new Intent(ScanQR1.this, Battery.class);
                    inte.putExtra("battery_qr", rawResult.getText());
                    inte.putExtra("qr_result",qr_res);
                    startActivity(inte);
                    finish();
                    break;
                default:
                    Toast.makeText(ScanQR1.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        scannerView.stopCamera();

    }

    @Override
    protected void onResume() {
        super.onResume();

        scannerView.setResultHandler(this);
        scannerView.startCamera();

    }

    @Override
    public void onBackPressed() {
        qr_res=getIntent().getStringExtra("qr_result");

        switch (page) {
            case "Questions":
                Intent qu = new Intent(ScanQR1.this, Battery.class);
                qu.putExtra("partname", "partname");
                qu.putExtra("qrScanned", "no");
                startActivity(qu);
                finish();
                break;
            case "Battery":
                Intent inte = new Intent(ScanQR1.this, Battery.class);
                inte.putExtra("qr_result",qr_res);
                startActivity(inte);
                finish();
                break;
            default:
                Toast.makeText(ScanQR1.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String battery_no(){
        String  qr_code = qu.getText().toString().trim();
        String[] a = qr_code.split(":");
        String model_name = a[3].charAt(1)+""+a[3].charAt(2)+""+a[3].charAt(3)+""+a[3].charAt(4)+""+a[3].charAt(5)+""+a[3].charAt(6);
        return model_name.trim();
    }




}
