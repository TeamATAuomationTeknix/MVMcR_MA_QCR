package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScan extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private String page;
    private ZXingScannerView scannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);

        scannerView = (ZXingScannerView) findViewById(R.id.scaner_view);

        //Getting data from ScanQR
        page = getIntent().getStringExtra("calling_page");


        // Add Back Arrow to Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle Back Arrow operation Here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {

        switch (page){
            case "ImageRegistration":
//                Intent i = new Intent(QRScan.this, ImageRegistration.class);
//                i.putExtra("qr_result", rawResult.getText());
//                startActivity(i);
//                finish();
                ServerJson serverJson=new ServerJson(QRScan.this);

                if(rawResult.getText().length()==36){
                    // qr_code = qr_code.substring(0, 17) + "_" + qr_code.substring(17, qr_code.length())+"_";
                    Intent i = new Intent(QRScan.this, Questions.class);
                    i.putExtra("qr_result", rawResult.getText());
                    i.putExtra("qrScanned","scanned");
                    i.putExtra("partname",getIntent().getStringExtra("partname"));
                    startActivity(i);
                    finish();
                    //qr.setText(qr_code);
                    //isModelNameExist();
                }else {
                    Toast.makeText(getApplicationContext(), "Invalid QR Code...!", Toast.LENGTH_LONG).show();
                    finish();
                }

                break;
            case "QCheck":
                Intent in = new Intent(QRScan.this, QCheck.class);
                in.putExtra("qr_result", rawResult.getText());
                startActivity(in);
                finish();
                break;
            default:
                Toast.makeText(QRScan.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                break;
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
            case "ImageRegistration":
                Intent i = new Intent(QRScan.this, ImageRegistration.class);
                startActivity(i);
                finish();
                break;
            case "QCheck":
                Intent in = new Intent(QRScan.this, QCheck.class);
                startActivity(in);
                finish();
                break;
            default:
                Toast.makeText(QRScan.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
