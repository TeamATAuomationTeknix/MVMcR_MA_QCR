package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRScanner extends AppCompatActivity {

    SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private String page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        //Getting data from ScanQR
        page = getIntent().getStringExtra("calling_page");

        surfaceView = findViewById(R.id.surfaceViewQRScan);

        BoxQRScanner boxQRScanner = new BoxQRScanner(this);
        addContentView(boxQRScanner, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));


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


    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(QRScanner.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(QRScanner.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                //Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {

                    switch (page){
                        case "ImageRegistration":
                          /*  Intent i = new Intent(QRScanner.this, ImageRegistration.class);
                            i.putExtra("qr_result", barcodes.valueAt(0).displayValue);
                            startActivity(i);
                            finish();
                            */break;
                        case "QCheck":
                          /*  Intent in = new Intent(QRScanner.this, QCheck.class);
                            in.putExtra("qr_result", barcodes.valueAt(0).displayValue);
                            startActivity(in);
                            finish();
                            */break;
                        default:
                            Toast.makeText(QRScanner.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    /*
                    QrReport.qr.post(new Runnable() {

                        @Override
                        public void run() {

                            QrReport.qr.setText(barcodes.valueAt(0).displayValue);
                            finish();

                        }
                    });
                    */

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }

    @Override
    public void onBackPressed() {

        switch (page){
            case "ImageRegistration":
                Intent i = new Intent(QRScanner.this, ImageRegistration.class);
                startActivity(i);
                finish();
                break;
            case "QCheck":
                Intent in = new Intent(QRScanner.this, QCheck.class);
                startActivity(in);
                finish();
                break;
            default:
                Toast.makeText(QRScanner.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
