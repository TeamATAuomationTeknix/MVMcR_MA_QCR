package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScanQR extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private String page;
    private String partname;
    ZXingScannerView scannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(ScanQR.this);
        setContentView(scannerView);
        Log.e("tag","scan qr activity started");
        //Getting data from ScanQR
        page = getIntent().getStringExtra("calling_page");
        partname=getIntent().getStringExtra("partname");
        // Add Back Arrow to Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle Back Arrow operation Here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            switch (page){
                case "Battery":
                    Intent u = new Intent(ScanQR.this, Battery.class);
                    u.putExtra("partname",partname);
                    u.putExtra("qrScanned","no");
                    startActivity(u);
                    finish();
                    break;
                case "Questions":
                    Intent qu = new Intent(ScanQR.this, Questions.class);
                    qu.putExtra("partname",partname);
                    qu.putExtra("qrScanned","no");
                    startActivity(qu);
                    finish();
                    break;

                case "ImageRegistration":
                    Intent i = new Intent(ScanQR.this, ImageRegistration.class);
                    startActivity(i);
                    finish();
                    break;
                case "QCheck":
                    Intent in = new Intent(ScanQR.this, QCheck.class);
                    startActivity(in);
                    finish();
                    break;
                default:
                    Toast.makeText(ScanQR.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {
        //Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}_\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}_");
        Pattern p=Pattern.compile("MA1YU2WTUK6K14913_AAW2XPEW7TU05TH0NP");
       // if (p.matcher(rawResult.getText()).matches()) {
            //Toast.makeText(ScanQR.this, "Done", Toast.LENGTH_SHORT).show();
        if(rawResult.getText().length()==36){
            switch (page){
                case "Battery":
                    Intent u = new Intent(ScanQR.this, Battery.class);
                    u.putExtra("partname",partname);
                    u.putExtra("qrScanned","scanned");
                    u.putExtra("qr_result", rawResult.getText());
                    startActivity(u);
                    finish();
                    break;
                case "Questions":
                    Intent qu = new Intent(ScanQR.this, Questions.class);
                    qu.putExtra("partname",partname);
                    qu.putExtra("qrScanned","scanned");
                    qu.putExtra("qr_result", rawResult.getText());
                    startActivity(qu);
                    finish();
                    break;
                case "ImageRegistration":
                    Intent i = new Intent(ScanQR.this, ImageRegistration.class);
                    i.putExtra("qr_result", rawResult.getText());
                    startActivity(i);
                    finish();
                    break;
                case "QCheck":
                    Intent in = new Intent(ScanQR.this, QCheck.class);
                    in.putExtra("qr_result", rawResult.getText());
                    startActivity(in);
                    finish();
                    break;
                default:
                    Toast.makeText(ScanQR.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    break;
            }


        } else {
            Toast.makeText(ScanQR.this, "Invalid QR Code...!", Toast.LENGTH_LONG).show();
            switch (page){
                case "Battery":
                    Intent l = new Intent(ScanQR.this, Battery.class);
                    l.putExtra("partname",partname);
                    l.putExtra("qrScanned","scanned");
                    l.putExtra("qr_result", rawResult.getText());
                    startActivity(l);
                    finish();
                    break;
                case "Questions":
                    Intent qu = new Intent(ScanQR.this, Questions.class);
                    qu.putExtra("partname",partname);
                    qu.putExtra("qrScanned","o");
                    startActivity(qu);
                    finish();
                    break;
                case "ImageRegistration":
                    Intent i = new Intent(ScanQR.this, ImageRegistration.class);
                    startActivity(i);
                    finish();
                    break;
                case "QCheck":
                    Intent in = new Intent(ScanQR.this, QCheck.class);
                    startActivity(in);
                    finish();
                    break;
                default:
                    Toast.makeText(ScanQR.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
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
        switch (page){
            case "Battery":
                Intent u = new Intent(ScanQR.this, Battery.class);
                u.putExtra("partname","partname");
                u.putExtra("qrScanned","no");
                startActivity(u);
                finish();
                break;
            case "Questions":
                Intent qu = new Intent(ScanQR.this, Questions.class);
                qu.putExtra("partname","partname");
                qu.putExtra("qrScanned","no");
                startActivity(qu);
                finish();
                break;
            case "ImageRegistration":
                Intent i = new Intent(ScanQR.this, ImageRegistration.class);
                startActivity(i);
                finish();
                break;
            case "QCheck":
                Intent in = new Intent(ScanQR.this, QCheck.class);
                startActivity(in);
                finish();
                break;
            default:
                Toast.makeText(ScanQR.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
