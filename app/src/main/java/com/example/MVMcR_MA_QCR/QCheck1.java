package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class QCheck1 extends AppCompatActivity {

    RecyclerView recyclerViewQCheck;
    RecyclerView.Adapter adapter;

    public static TextView qr, clip_no, clip_not_found;
    public static ImageView imagePreview, imageClip;
    public static ImageButton camera, recamera, check, rework, back, next;
    private ImageButton scanqr;
    public static RelativeLayout relativeLayout_img;

    Bitmap bitmapImage = null;

    ArrayList<Integer> listClipNo;
    ArrayList<Integer> listId;
    ArrayList<Bitmap> listImages;

    ArrayList<Integer> listOkClips;
    ArrayList<Integer> listNotOkClips;
    ArrayList<Integer> listUncheckedClips;

    ArrayList<Integer> listClip;
    ArrayList<String> listStatus;

    ArrayList<Integer> listUniqueClips;

    ArrayList<Integer> listShift;
    ArrayList<String> listStartTime;
    ArrayList<String> listEndTime;

    ArrayList<ArrayList> listDetectedObject;

    /*------------------ Server Related Declaration -------------------*/

    public static final String INSERT_URL = Main_page.IP_ADDRESS + "/CurrentDataInsertion.php";
    public static final String FETCH_URL = Main_page.IP_ADDRESS + "/CurrentDataFetch.php";

    //1 means data is synced and 0 means data is not synced
    public static final int CURRENT_DATA_SYNCED_WITH_SERVER = 1;
    public static final int CURRENT_DATA_NOT_SYNCED_WITH_SERVER = 0;

    public static final String KEY_QR_CODE = "QRCode";
    public static final String KEY_MODEL_CODE = "ModelCode";
    public static final String KEY_HOSE_CLIP_NO = "HoseClipNo";
    public static final String KEY_MASTER_IMAGE_ID = "MasterImageId";
    public static final String KEY_RESULT = "Result";
    public static final String KEY_WORK_TIME = "WorkTime";
    public static final String KEY_SHIFT = "Shift";
    public static final String KEY_IMAGE_DATA = "ImageData";
    public static final String KEY_IMAGE_PATH = "ImagePath";

    /*------------------------------------------------------------------*/

    private int current_clip=0;
    private EditText res;

    private Uri file;
    int TAKE_PHOTO_CODE = 100;
    Bitmap cropedBitmapImage = null;
    private  int camera_id;
    private static File mediaStorageDir;

    String modelFile= "detect_2hose_clips700_1.tflite";

    float DEFAULT_THRESHOLD = 0.85f;

    float xmin, ymin, xmax, ymax;
    Bitmap scaledImage;

    final int OK = 1;
    final int NOT_OK = 0;

    /*Remove*/public static String timeStamp1;

    /*-------------------------- Print QR-------------------------------*/
    ImageView qr_img;
    RecyclerView printRecyclerView;
    LinearLayout printLayout;
    TextView sub_qr1, sub_qr2, model_color, fule_type, print_time, print_hose_clip, print_result;
    RecyclerView.Adapter printAdapter;

    ArrayList<Integer> listPrintHoseClips;
    ArrayList<String> listPrintResults;
    /*------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qcheck);

        //Toast.makeText(getApplicationContext(),"Id : "+autoIncrementId(),Toast.LENGTH_SHORT).show();
        qr = (TextView) findViewById(R.id.txt_qr);

        scanqr = (ImageButton) findViewById(R.id.btn_scan_qr);
        imagePreview = (ImageView) findViewById(R.id.img_preview);
        camera = (ImageButton) findViewById(R.id.img_btn_camera);
        recamera = (ImageButton) findViewById(R.id.img_btn_recamera);

        ///////
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //////

        clip_no = (TextView) findViewById(R.id.txt_clip);
        next = (ImageButton) findViewById(R.id.img_btn_next);

        relativeLayout_img = (RelativeLayout) findViewById(R.id.relative_layout_img);

        recyclerViewQCheck=(RecyclerView)findViewById(R.id.recycler_view_qcheck);

        clip_not_found.setVisibility(View.GONE);

        /*-------------------------- Print QR-------------------------------*/

        listPrintHoseClips=new ArrayList<>();
        listPrintResults=new ArrayList<>();

        /*------------------------------------------------------------------*/

        //Toast.makeText(ImageRegistration.this,"mmm",Toast.LENGTH_SHORT).show();

        clearImage();
        camera.setVisibility(View.INVISIBLE);
        next.setEnabled(false);
        next.setImageResource(R.drawable.next_disable);
        imageClip.setVisibility(View.INVISIBLE);

        //recyclerViewQCheck.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));

        // Calling the RecyclerView
        recyclerViewQCheck.setHasFixedSize(true);

        // The number of Columns
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, true);
        recyclerViewQCheck.setLayoutManager(layoutManager);

        listClipNo=new ArrayList<>();
        listId=new ArrayList<>();
        listImages=new ArrayList<>();

        listOkClips=new ArrayList<>();
        listNotOkClips=new ArrayList<>();
        listUncheckedClips=new ArrayList<>();

        listStatus=new ArrayList<>();
        listClip=new ArrayList<>();

        listShift=new ArrayList<>();
        listStartTime=new ArrayList<>();
        listEndTime=new ArrayList<>();

        listDetectedObject=new ArrayList<>();

        //Getting data from ScanQR
        final String qr_res = getIntent().getStringExtra("qr_result");
        qr.setText(qr_res);

        // Check Model Exist Or Not
        isModelNameExist();

        // Scan  QR Code
        scanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), ScanQR.class));
                TimingLogger timingLogger =  new TimingLogger("YOUR_TAG", "methodB");;

                //registering the broadcast receiver to update sync status
                registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

                //fetchLast7DaysData();
                // ... do some work A ...
                timingLogger.addSplit("work A");
                Intent i = new Intent(QCheck1.this, ScanQR.class);
                i.putExtra("calling_page", "QCheck");
                startActivity(i);
                finish();
                // ... do some work D ...
                timingLogger.addSplit("work D");
                timingLogger.dumpToLog();
            }
        });

        qr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inputQRCode();
                return false;
            }
        });


        // Open Camera
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearImage();
                //isModelNameExist();
            /*    Intent i = new Intent(QCheck.this, CameraPreview.class);
                i.putExtra("calling_page", "QCheck_Camera");
                startActivity(i);
             */
                //takePicture(v);
                Intent i = new Intent(QCheck1.this, CustomCamera.class);
                startActivity(i);

            }
        });

        // Open Recamera
        recamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearImage();
                //isModelNameExist();
           /*     Intent i = new Intent(QCheck.this, CameraPreview.class);
                i.putExtra("calling_page", "QCheck_Recamera");
                startActivity(i);
            */
                //takePicture(v);
                Intent i = new Intent(QCheck1.this, CustomCamera.class);
                startActivity(i);

            }
        });

        // Check & Store Record
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord(v);
            }
        });

        // Rework
        rework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord(v);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backImage();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextImage();
            }
        });

        // Add Back Arrow to Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Handle Back Arrow operation Here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // handle arrow click here
        if (id == android.R.id.home) {
            String alertMsg = "Are you sure ? Do you want to LEAVE this Page ?";
            alertDialog("BACK", alertMsg);
        }
//        else if (id == R.id.action_print) {
//            if (qr.getText().toString().equals("")){
//                Toast.makeText(getApplicationContext(),"Please Scan or Insert QR Code First...",Toast.LENGTH_LONG).show();
//            }else {
//                printPreview();
//            }
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }


    private String getModel(){
        String  qr_code = qr.getText().toString().trim();;
        String[] a = qr_code.split("_");
        String model_name = a[1].charAt(1)+""+a[1].charAt(2);
        return model_name.trim();
    }

    private void isModelNameExist(){
        String txtQR = qr.getText().toString().trim();
        //String model_nm = qr.getText().toString().replaceAll("\\s", "");
        //String model_nm = qr.getText().toString().replaceAll("([0-9])", "").trim();
        if (txtQR.equals("")){
            camera.setVisibility(View.INVISIBLE);
            next.setEnabled(false);
            next.setImageResource(R.drawable.next_disable);

        }
        else {
            String model_nm = getModel();
            //Toast.makeText(getApplicationContext(),"model = "+model_nm,Toast.LENGTH_SHORT).show();

            try {

                Database db = new Database(QCheck1.this);
                db.open();

                Cursor c1 = db.getAllModels();
                Log.d("func", "outside");

                if (c1 != null && c1.getCount() != 0) {
                    if (c1.moveToFirst()) {

                        do {

                            int id=c1.getInt(c1.getColumnIndex(db.ID));
                            String model = c1.getString(c1.getColumnIndex(db.MODEL_NM));
                            byte[] image=c1.getBlob(c1.getColumnIndex(db.IMG));

                            if (model_nm.equals(model)){
                                camera.setVisibility(View.VISIBLE);
                                fetchImages();
                                //fetchRequiredImages();

                                //next.setEnabled(true);
                                //next.setImageResource(R.drawable.next_shadow);
                   /*             Toast.makeText(QCheck.this,""+listOkClips,Toast.LENGTH_SHORT).show();
                                imageClip.setVisibility(View.VISIBLE);
                                imageClip.setRotation(90);
                                imageClip.setImageBitmap(listImages.get(listUniqueClips.get(current_clip)-1));
                                hose_clip_no =  listUniqueClips.get(current_clip);
                                clip_no.setText("Hose Clip - " + listUniqueClips.get(current_clip));

                                if (listNotOkClips.contains(listUniqueClips.get(current_clip))){

                                    recamera.setVisibility(View.VISIBLE);

                                }

                    */
                                break;
                            }

                        } while (c1.moveToNext());
                    }
                }
                c1.close();
                db.close();


            }
            catch (Exception e){

            }


        }
    }

    private void backImage(){
        clearImage();
        next.setEnabled(true);
        next.setImageResource(R.drawable.next_shadow);
        if (current_clip <= listUniqueClips.size() && current_clip > 0) {

            back.setEnabled(true);
            back.setImageResource(R.drawable.back_shadow);
            int prev_clip = current_clip - 1;

            int clip = listUniqueClips.get(prev_clip);
            //imageClip.setRotation(90);
            imageClip.setImageBitmap(listImages.get(clip-1));
            clip_no.setText("Hose Clip - " + clip);

            if (listNotOkClips.contains(clip)) {

                recamera.setVisibility(View.VISIBLE);

            }else {
                recamera.setVisibility(View.INVISIBLE);
            }

            current_clip = prev_clip;
            // Toast.makeText(QCheck.this,""+ current_clip,Toast.LENGTH_SHORT).show();

        }
        if(current_clip <= 0) {
            back.setEnabled(false);
            back.setImageResource(R.drawable.back_disable);
        }


    }

    private void nextImage(){
        clearImage();
        back.setEnabled(true);
        back.setImageResource(R.drawable.back_shadow);

        if (current_clip < listUniqueClips.size() && current_clip >= 0) {

            next.setEnabled(true);
            next.setImageResource(R.drawable.next_shadow);
            int next_clip = current_clip + 1;

            int clip = listUniqueClips.get(next_clip);
            //imageClip.setRotation(90);
            imageClip.setImageBitmap(listImages.get(clip-1));
            clip_no.setText("Hose Clip - " + clip);

            if (listNotOkClips.contains(clip)) {

                recamera.setVisibility(View.VISIBLE);

            }else {
                recamera.setVisibility(View.INVISIBLE);
            }

            current_clip = next_clip;
            // Toast.makeText(QCheck.this,""+ current_clip,Toast.LENGTH_SHORT).show();
        }
        if(current_clip >= listUniqueClips.size()-1) {
            next.setEnabled(false);
            next.setImageResource(R.drawable.next_disable);
        }

    }

    private Date stringToDate(String strDate){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm");
        Date dt=null;
        try {
            dt = dateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    private int getShift(){

        try {

            Database db = new Database(QCheck1.this);
            db.open();
            //Toast.makeText(ImageRegistration.this,listImages.size(),Toast.LENGTH_SHORT).show();
            Cursor c1 = db.getAllShifts();
            Log.d("func", "outside");

            if (c1 != null && c1.getCount() != 0) {
                if (c1.moveToFirst()) {
                    int count = 1;
                    do {

                        int id=c1.getInt(c1.getColumnIndex(db.ID));
                        String start=c1.getString(c1.getColumnIndex(db.START_TIME));
                        String end=c1.getString(c1.getColumnIndex(db.END_TIME));

                        listShift.add(count);
                        listStartTime.add(start);
                        listEndTime.add(end);

                        count++;

                    } while (c1.moveToNext());
                }
            }
            c1.close();
            db.close();

        }catch (Exception  e){

        }

        int shift = 0;
        for (int i = 0; i < listShift.size(); i++){
            String startTime = listStartTime.get(i);
            String endTime = listEndTime.get(i);
            int start = stringToDate(startTime).getHours();
            if (start < stringToDate(listStartTime.get(0)).getHours()){
                start = 24 + start;
            }
            int end = stringToDate(endTime).getHours();
            if (end < stringToDate(listStartTime.get(0)).getHours()){
                end = 24 + end;
            }
            int current = new Date().getHours();

            if (current < stringToDate(listStartTime.get(0)).getHours()){
                current = 24 + current;
            }

            if (current >= start && current <= end){
                shift = listShift.get(i);
            }

        }

        return shift;
    }


    private void addRecord(View view){

        Bitmap bitmapImage = ((BitmapDrawable)imagePreview.getDrawable()).getBitmap();

        Time dtNow = new Time();
        dtNow.setToNow();
        int hours = dtNow.hour;
        String lsNow = dtNow.format("%Y/%m/%d %H:%M");
        //String lsYMD = dtNow.toString();    // YYYYMMDDTHHMMSS
        //int r = Integer.parseInt(res.getText().toString().trim());

        final String qr_code = qr.getText().toString().trim();
        final String model_nm = getModel();
        final int hose_clip = Integer.parseInt(clip_no.getText().toString().replaceAll("[^0-9]",""));
        final int master_img_id = listId.get(hose_clip-1);


        /*Remove*/timeStamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

/*        CheckObjectPosition checkObjectPosition = new CheckObjectPosition(this);
        final int result = checkObjectPosition.detectDistance(scaledImage, Math.round(xmin), Math.round(ymin), Math.round(xmax), Math.round(ymax), timeStamp1);

        final String work_time = lsNow.trim();
        final int shift = getShift();
        //final String image_data = bitmapToBase64(scaledImage);


        if (result == CheckObjectPosition.PASS_FLAG){
            //Toast.makeText(getApplicationContext(),"HoseClip Is OK",Toast.LENGTH_LONG).show();
            Snackbar.make(view, "HoseClip Is OK", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }else if (result == CheckObjectPosition.FAIL_FLAG){
            //Toast.makeText(getApplicationContext(),"HoseClip Is NOT-OK",Toast.LENGTH_LONG).show();
            Snackbar.make(view, "HoseClip Is NOT-OK", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }else {
            check.setVisibility(View.INVISIBLE);
            rework.setVisibility(View.INVISIBLE);
            camera.setVisibility(View.VISIBLE);
            clip_not_found.setText("Image is NOT-PROPER. \nPlease Recapture Image...!");
            clip_not_found.setVisibility(View.VISIBLE);
//            Toast.makeText(getApplicationContext(),"Image NOT-PROPER. Please Recapture Image...!",Toast.LENGTH_LONG).show();
            Snackbar.make(view, "Image NOT-PROPER. Please Recapture Image...!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }
*/
        //Toast.makeText(this, "Distance : "+ CheckObjectPosition.distanceFinal, Toast.LENGTH_LONG).show();
        //Toast.makeText(this, "Distance : "+ CheckObjectPosition.distanceFinal, Toast.LENGTH_LONG).show();


//        if(result == NOT_OK){
//            current_clip = listUniqueClips.indexOf(hose_clip);

//        }

//        if (current_clip == listClip.size()-1 && result == OK){
//            current_clip = current_clip-1;
//        }


        OutputStream fOut = null;
        Uri outputFileUri;
        String imagePath = null;

        try {
            outputFileUri = Uri.fromFile(currentImagesDir(model_nm, hose_clip));
            imagePath = String.valueOf(currentImagesDir(model_nm, hose_clip));
            fOut = new FileOutputStream(currentImagesDir(model_nm, hose_clip));
        } catch (Exception e) {
            Toast.makeText(this, "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
        }
        try {
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }

        final String image_path_local_db = imagePath.trim();
        final String image_path_server = image_path_local_db.split("Download")[1];
        //Toast.makeText(getApplicationContext(),"Image Path === "+image_path_server,Toast.LENGTH_LONG).show();

        final TimingLogger timings = new TimingLogger("YOUR_TAG", "methodC");

        final String image_data = bitmapToBase64(bitmapImage);

        // ... do some work A ...
        timings.addSplit("work A");
        /*----------------------------------------------------------------------------------------------*/

        // Start Volley Work

/*        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        try {

                            // ... do some work A ...
                            timings.addSplit("work B");
                            timings.dumpToLog();
                            if(response.equals("Unsuccessfully"))
                            {
                                addRecordToLocalDB(autoIncrementId(), qr_code, model_nm, hose_clip, master_img_id, result, work_time, shift, image_path_local_db, CURRENT_DATA_NOT_SYNCED_WITH_SERVER);

                            }
                            else if(response.equals("Successfully"))
                            {
                                addRecordToLocalDB(autoIncrementId(), qr_code, model_nm, hose_clip, master_img_id, result, work_time, shift, image_path_local_db, CURRENT_DATA_SYNCED_WITH_SERVER);
                                Toast.makeText(getApplicationContext(),"Pending Data Saving "+response,Toast.LENGTH_LONG).show();
                            }


                        }
                        catch (Exception e){
                            //Toast.makeText(getApplicationContext(),"oooo"+e.toString(),Toast.LENGTH_LONG).show();
                        }

                        //Toast.makeText(getApplicationContext(),"Response--- "+response,Toast.LENGTH_LONG).show();

                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(),"Error--- "+error.toString(),Toast.LENGTH_LONG).show();
                        addRecordToLocalDB(autoIncrementId(), qr_code, model_nm, hose_clip, master_img_id, result, work_time, shift, image_path_local_db, CURRENT_DATA_NOT_SYNCED_WITH_SERVER);

                    }
                }){
            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = new HashMap<String, String>();

                params.put(KEY_QR_CODE, qr_code);
                params.put(KEY_MODEL_CODE, model_nm);
                params.put(KEY_HOSE_CLIP_NO, String.valueOf(hose_clip));
                params.put(KEY_MASTER_IMAGE_ID, String.valueOf(master_img_id));
                params.put(KEY_RESULT, String.valueOf(result));
                params.put(KEY_WORK_TIME, work_time);
                params.put(KEY_SHIFT, String.valueOf(shift));
                params.put(KEY_IMAGE_DATA, image_data);
                params.put(KEY_IMAGE_PATH, image_path_server);

                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


        /*----------------------------------------------------------------------------------------------*/


    }

    private void addRecordToLocalDB(int id, String qr_code, String model_nm, int hose_clip, int master_img_id, int result, String work_time, int shift, String image_path, int sync_status){
        try {
            Database db = new Database(QCheck1.this);
            db.open();
            db.addQCheckResult(id, qr_code, model_nm, hose_clip, master_img_id, result, work_time, shift, image_path, sync_status);
            db.close();
            fetchRequiredImages();
        }catch (Exception e){

        }
        //clearImage();
        imagePreview.setImageBitmap(null);
        imagePreview.destroyDrawingCache();
        imagePreview.setVisibility(View.INVISIBLE);
        check.setVisibility(View.INVISIBLE);
        rework.setVisibility(View.INVISIBLE);

    }

    private static File currentImagesDir(String model_nm, int hose_clip){

        //String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File mediaStorageCurrent = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "Q-Check : Hose Clips/"+date);

        if (!mediaStorageCurrent.exists()){
            if (!mediaStorageCurrent.mkdirs()){
                return null;
            }
        }

        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        /*Remove*/String timeStamp = timeStamp1;

        return new File(mediaStorageCurrent.getPath() + File.separator + model_nm+"_HoseClip"+hose_clip+"_"+ timeStamp + ".jpg");
    }

    public String bitmapToBase64(Bitmap bitmapImg){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;

    }

    private void fetchImages(){

        listClipNo.clear();
        listId.clear();
        listImages.clear();
        String model_nm = getModel();
        //String model_nm = qr.getText().toString().replaceAll("\\s", "");
        //String model_nm = qr.getText().toString().replaceAll("([0-9])", "").trim();

        try {

            Database db = new Database(QCheck1.this);
            db.open();
            //Toast.makeText(ImageRegistration.this,listImages.size(),Toast.LENGTH_SHORT).show();
            Cursor c1 = db.getAllImagesOfSpecificModel(model_nm);
            Log.d("func", "outside");

            if (c1 != null && c1.getCount() != 0) {
                if (c1.moveToFirst()) {
                    int count = 1;
                    do {

                        int id=c1.getInt(c1.getColumnIndex(db.ID));
                        byte[] image=c1.getBlob(c1.getColumnIndex(db.IMG));

                        listClipNo.add(count);
                        listId.add(id);
                        listImages.add(byteArrayToBitmap(image));

                        count++;

                    } while (c1.moveToNext());
                }
            }
            c1.close();
            db.close();

        }catch (Exception  e){

        }


        //Toast.makeText(QCheck.this,""+listImages.size(),Toast.LENGTH_SHORT).show();

        //adapter = new ImageRegistrationAdapter(ImageRegistration.this, listId, listImages);
        //recyclerViewImages.setAdapter(adapter);
        fetchRequiredImages();
    }

    private void fetchRequiredImages(){

        //fetchImages();

        listOkClips.clear();
        listNotOkClips.clear();

        String qr_code = qr.getText().toString().trim();

        try {

            Database db = new Database(QCheck1.this);
            db.open();
            //Toast.makeText(QCheck.this,""+db.getAllReworkResult(),Toast.LENGTH_SHORT).show();

            Cursor c1 = db.getSpecificQCheckResult(qr_code);
            //Cursor c1 = db.getAllQCheckResult();
            Log.d("func", "outside");

            if (c1 != null && c1.getCount() != 0) {
                if (c1.moveToFirst()) {

                    do {

                        int result = c1.getInt(c1.getColumnIndex(db.RESULT));
                        int clip = c1.getInt(c1.getColumnIndex(db.HOSE_CLIP_NO));

                        //listResult.add(result);
                        //listClip.add(clip);

                        if (result == OK){
                            listOkClips.add(clip);

                        } else if(result == NOT_OK){
                            listNotOkClips.add(clip);
                        }

                    } while (c1.moveToNext());
                }
            }
            c1.close();
            db.close();


        }
        catch (Exception e){
            //Toast.makeText(QCheck.this,"Null",Toast.LENGTH_SHORT).show();
        }

        // Toast.makeText(QCheck.this,"listNotOkClips befor"+listNotOkClips,Toast.LENGTH_SHORT).show();
  /*      for (int i = 0; i < listNotOkClips.size(); i++){
            int item = listNotOkClips.get(i);
            if (listOkClips.contains(item)){
                listNotOkClips.removeAll(listOkClips);
            }
        }
*/
        listNotOkClips.removeAll(listOkClips);

        // Toast.makeText(QCheck.this,"listNotOkClips after"+listNotOkClips,Toast.LENGTH_SHORT).show();



        listUncheckedClips.clear();

        for(int i = 1; i <= listImages.size(); i++){

            if(!(listOkClips.contains(i) || listNotOkClips.contains(i))){
                listUncheckedClips.add(i);
            }else {

            }
        }

        listClip.clear();
        listStatus.clear();
        //listUniqueClips.clear();

        if(!listNotOkClips.isEmpty()){
            //Toast.makeText(QCheck.this,"listNotOkClips not empty"+listNotOkClips,Toast.LENGTH_SHORT).show();
            for(int i = 0; i < listNotOkClips.size(); i++){
                listClip.add(listNotOkClips.get(i));
                listStatus.add("NotOk");
            }
        }

        if(!listUncheckedClips.isEmpty()){
            //Toast.makeText(QCheck.this,"listUncheckedClips not empty"+listUncheckedClips,Toast.LENGTH_SHORT).show();
            for(int i = 0; i < listUncheckedClips.size(); i++){
                listClip.add(listUncheckedClips.get(i));
                listStatus.add("Uncheck");
            }
        }

        if (!listClip.isEmpty()) {

            Set setClip = new HashSet<Integer>(listClip);
            listUniqueClips = new ArrayList<>(setClip);
            Collections.sort(listUniqueClips);
            //Toast.makeText(QCheck.this, "" + listUniqueClips, Toast.LENGTH_SHORT).show();

            //  Toast.makeText(QCheck.this,""+listOkClips,Toast.LENGTH_SHORT).show();
            //current_clip = 0;
            int clip = listUniqueClips.get(current_clip);
            imageClip.setVisibility(View.VISIBLE);
            //imageClip.setRotation(90);
            imageClip.setImageBitmap(listImages.get(clip - 1));
            clip_no.setText("Hose Clip - " + clip);

            if (listNotOkClips.contains(clip)) {

                recamera.setVisibility(View.VISIBLE);
                check.setVisibility(View.INVISIBLE);
                rework.setVisibility(View.INVISIBLE);

            }else {
                camera.setVisibility(View.VISIBLE);
                recamera.setVisibility(View.INVISIBLE);
                rework.setVisibility(View.INVISIBLE);
            }

            //Toast.makeText(QCheck.this, "size - " + listUniqueClips.size(), Toast.LENGTH_SHORT).show();
            if (listUniqueClips.isEmpty() || listUniqueClips.size() <= 1 || listUniqueClips.indexOf(clip)==(listUniqueClips.size()-1)) {
                next.setEnabled(false);
                next.setImageResource(R.drawable.next_disable);
            } else {
                next.setEnabled(true);
                next.setImageResource(R.drawable.next_shadow);
            }

            if (listUniqueClips.isEmpty() || listUniqueClips.size() <= 1 || listUniqueClips.indexOf(clip)==0){
                back.setEnabled(false);
                back.setImageResource(R.drawable.back_disable);
            }else {
                back.setEnabled(true);
                back.setImageResource(R.drawable.back_shadow);
            }

/*
            if (listUniqueClips.isEmpty()){
                Toast.makeText(QCheck.this,"Job Done Successfully...",Toast.LENGTH_SHORT).show();
                camera.setVisibility(View.INVISIBLE);
                next.setEnabled(false);
                next.setImageResource(R.drawable.next_disable);
                imageClip.setVisibility(View.INVISIBLE);
            }
*/
        }else {
            //Toast.makeText(QCheck.this,"Job Done Successfully...",Toast.LENGTH_SHORT).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Job Done Successfully...!");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //do things
                    qr.setText("");
                    clearImage();
                    camera.setVisibility(View.INVISIBLE);
                    imageClip.setVisibility(View.INVISIBLE);
                    clip_no.setText("");
                    recyclerViewQCheck.setAdapter(null);
                }
            });
            builder.setNegativeButton("PRINT",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            printPreview();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }


        //adapter = new QCheckAdapter(QCheck1.this, listClipNo, listOkClips, listNotOkClips, listUncheckedClips);
        recyclerViewQCheck.setAdapter(adapter);


    }

    private static Bitmap byteArrayToBitmap(byte[] byteimg){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteimg, 0, byteimg.length);
        return bitmap;
    }

    public static void clearImage(){
        imagePreview.setImageBitmap(null);
        imagePreview.destroyDrawingCache();
        clip_not_found.setVisibility(View.GONE);
        imagePreview.setVisibility(View.INVISIBLE);
        camera.setVisibility(View.VISIBLE);
        recamera.setVisibility(View.INVISIBLE);
        check.setVisibility(View.INVISIBLE);
        rework.setVisibility(View.INVISIBLE);
        back.setImageResource(R.drawable.back_disable);
        back.setEnabled(false);

    }

    private void backPage() {
        this.finish();
    }

    private void alertDialog(final String operation, String alertMsg) {

        // Build the AlartBox
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QCheck1.this);

        // Set the appropriate message into it.
        alertDialogBuilder.setMessage(alertMsg);

        // Add a positive button and it's action. In our case action would be deletion of the data
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {

                            switch (operation) {
                                case "BACK":
                                    backPage();
                                    break;
                                default:
                                    Toast.makeText(QCheck1.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                    break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Add a negative button and it's action. In our case, just hide the dialog box
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (operation) {
                            case "BACK":

                                break;
                            default:
                                Toast.makeText(QCheck1.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });

        // Now, create the Dialog and show it.
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void inputQRCode(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("QR Code.");
        builder.setMessage("Enter Correct QR Code.");

        // Set up the input
        final EditText inputQR = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputQR.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        int maxLength = 35;
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(maxLength);
        inputQR.setFilters(filters);
        builder.setView(inputQR);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //fetchLast7DaysData();
                String qr_code = inputQR.getText().toString().toUpperCase().trim().replaceAll("\\s+", "").replaceAll("\n","");
                Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}");
                if (!qr_code.equals("") && p.matcher(qr_code).matches()) {
                    qr_code = qr_code.substring(0, 17) + "_" + qr_code.substring(17, qr_code.length())+"_";
                    Intent i = new Intent(QCheck1.this, QCheck1.class);
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

    public void takePicture(View view) {
        camera_id = view.getId();
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            file = FileProvider.getUriForFile(QCheck1.this, "com.example.qcheckhoseclipgray.fileprovider", getOutputMediaFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }

        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {

            //Bitmap bitmapImage = null;
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            imagePreview.setImageBitmap(bitmapImage);
            imagePreview.setVisibility(View.VISIBLE);
            if (camera_id == camera.getId()){
                //Toast.makeText(this, "Camera = "+camera_id, Toast.LENGTH_SHORT).show();
                check.setVisibility(View.VISIBLE);
                recamera.setVisibility(View.INVISIBLE);
                imagePreview.setVisibility(View.VISIBLE);


            }else if (camera_id == recamera.getId()){
                //Toast.makeText(this, "Recamera = "+camera_id, Toast.LENGTH_SHORT).show();
                rework.setVisibility(View.VISIBLE);
                camera.setVisibility(View.INVISIBLE);
                imagePreview.setVisibility(View.VISIBLE);

            }
            //new ObjectDetectionThread().execute();

        }

    }

    private static File getOutputMediaFile(){

        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CurrentImagesTemp");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
    }

    public static boolean deleteFolderWithImages(File dir) {
        try {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i=0; i<children.length; i++) {
                    boolean success = deleteFolderWithImages(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
        }catch (Exception e){

        }
        return dir.delete();
    }

    private float fetchThreshold(){

        float threshold = 0;
        try {

            String threshold_val = null;

            Database db = new Database(QCheck1.this);
            db.open();
            //Toast.makeText(ImageRegistration.this,model_nm,Toast.LENGTH_SHORT).show();
            Cursor c1 = db.getThreshod();
            Log.d("func", "outside");

            if (c1 != null && c1.getCount() != 0) {
                if (c1.moveToFirst()) {

                    do {
                        int id = c1.getInt(c1.getColumnIndex(db.ID));
                        threshold_val = c1.getString(c1.getColumnIndex(db.THRESHOLD_VALUE));

                    } while (c1.moveToNext());
                }
            }
            c1.close();
            db.close();

            if(threshold_val == null || threshold_val.equals("")){
                //return DEFAULT_THRESHOLD;
                threshold = DEFAULT_THRESHOLD;
            }else {
                //return Float.parseFloat(threshold);
                threshold = Float.parseFloat(threshold_val);
            }

        }catch (Exception e){
            System.out.println("Error --- "+e);

        }
        return threshold;
    }

    @Override
    public void onBackPressed() {
        String alertMsg = "Are you sure ? Do you want to LEAVE this Page ?";
        alertDialog("BACK", alertMsg);
    }


    public class ObjectDetectionThread extends AsyncTask<String, String, String>{

        String title;
        float confidence;
        float threshold;
        Bitmap resultImage;

        private String hose_clip_1 = "Hose Clip - 1";
        private String hose_clip_2 = "Hose Clip - 2";
        private String hose_clip_3 = "Hose Clip - 3";
        private String hose_clip_4 = "Hose Clip - 4";
        private String hose_clip_5 = "Hose Clip - 5";
        private String hose_clip_6 = "Hose Clip - 6";

        private String hose_clip_no;

        private static final int IMAGE_WIDTH = 700;
        private static final int IMAGE_HEIGHT = 700;

        ProgressDialog dialog;
        TimingLogger timings;
        @Override
        protected void onPreExecute() {

            hose_clip_no = clip_no.getText().toString();
            timings = new TimingLogger("YOUR_TAG", "methodA");
            imagePreview.setVisibility(View.INVISIBLE);
            clip_not_found.setVisibility(View.GONE);
            dialog = ProgressDialog.show(QCheck1.this, "", "Object Recognising...", true);
            dialog.create();
            super.onPreExecute();

            // ... do some work A ...
            timings.addSplit("work A");

        }

        @Override
        protected String doInBackground(String... strings) {

            try {

                if (bitmapImage.getWidth() > bitmapImage.getHeight()){
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, bitmapImage.getWidth(), bitmapImage.getHeight(), true);
                    bitmapImage = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    System.out.println("Rotated");
                }

                int x, y, width, height;
                int center, start;
                int rotation;
                int w = bitmapImage.getWidth();
                int h = bitmapImage.getHeight();

                width = w;
                height = w;
                center = h/2;
                start = center - (w/2);
                x = 0;
                y = start;

                Bitmap cropedBitmapImage = Bitmap.createBitmap(bitmapImage, x, y, width, height);
                //ByteArrayOutputStream out = new ByteArrayOutputStream();
                //cropedBitmapImage.compress(Bitmap.CompressFormat.JPEG, 30, out);
                //Bitmap decodedImage = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                //System.out.println("Image Size : "+ cropedBitmapImage.getWidth()+" x "+cropedBitmapImage.getHeight());
                scaledImage = Bitmap.createScaledBitmap(cropedBitmapImage, IMAGE_WIDTH, IMAGE_HEIGHT, true);

// ... do some work B ...
/*                timings.addSplit("work B");


                DetectObject dtObj = new DetectObject(QCheck1.this, modelFile, true);

                listDetectedObject = dtObj.detectedObject(scaledImage);

                title = (String) listDetectedObject.get(1).get(0);

                confidence = (float) listDetectedObject.get(2).get(0);

                RectF coordinates = (RectF) listDetectedObject.get(3).get(0);

                xmin = coordinates.left;
                ymin = coordinates.top;
                xmax = coordinates.right;
                ymax = coordinates.bottom;
                Scalar strokeColor = new Scalar(0,255,0);
                int strokeThickness = 2;
                threshold = fetchThreshold();


                if (confidence >= threshold && title.equals("GrayClip") && (hose_clip_no.equals(hose_clip_1) || hose_clip_no.equals(hose_clip_2) || hose_clip_no.equals(hose_clip_3))){

                    resultImage = dtObj.drawRect(scaledImage, scaledImage.getWidth(), scaledImage.getHeight(), xmin, ymin, xmax, ymax, strokeColor, strokeThickness);

                } else if (confidence >= threshold && title.equals("SilverClip") && (hose_clip_no.equals(hose_clip_4) || hose_clip_no.equals(hose_clip_5))){

                    resultImage = dtObj.drawRect(scaledImage, scaledImage.getWidth(), scaledImage.getHeight(), xmin, ymin, xmax, ymax, strokeColor, strokeThickness);

                } else {
                    resultImage = scaledImage;
                }

//                if (confidence >= threshold){
//                    resultImage = dtObj.drawRect(scaledImage, scaledImage.getWidth(), scaledImage.getHeight(), xmin, ymin, xmax, ymax, strokeColor, strokeThickness);
//                }else {
//                    resultImage = scaledImage;
//                }*/

// ... do some work C ...
                timings.addSplit("work C");

            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Exception : "+ e, Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                imagePreview.setImageBitmap(resultImage);
                imagePreview.setVisibility(View.VISIBLE);
                if (camera_id == camera.getId()){
                    //Toast.makeText(this, "Camera = "+camera_id, Toast.LENGTH_SHORT).show();
                    check.setVisibility(View.VISIBLE);
                    recamera.setVisibility(View.INVISIBLE);
                    imagePreview.setVisibility(View.VISIBLE);


                }else if (camera_id == recamera.getId()){
                    //Toast.makeText(this, "Recamera = "+camera_id, Toast.LENGTH_SHORT).show();
                    rework.setVisibility(View.VISIBLE);
                    camera.setVisibility(View.INVISIBLE);
                    imagePreview.setVisibility(View.VISIBLE);

                }

                if(confidence < threshold || resultImage == scaledImage){
                    check.setVisibility(View.INVISIBLE);
                    rework.setVisibility(View.INVISIBLE);
                    clip_not_found.setText("Clip Not Found...! \nPlease Capture Proper Image...!");
                    clip_not_found.setVisibility(View.VISIBLE);
                }else {
                    clip_not_found.setVisibility(View.GONE);
                }

                dialog.dismiss();
                deleteFolderWithImages(mediaStorageDir);

// ... do some work D ...
                timings.addSplit("work D");
                timings.dumpToLog();

            }catch (Exception e){
                Toast.makeText(getApplicationContext(),"Exception : "+ e, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(getApplicationContext(),"Title : "+ title +" +++ Confidence : "+ confidence, Toast.LENGTH_SHORT).show();
            //super.onPostExecute(s);
        }

    }


    public void fetchLast7DaysData(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        try {

                            Database db = new Database(QCheck1.this);
                            db.open();
                            db.deleteAllQCheckResult();
                            db.close();

                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String qr_c = jsonObject.getString("QRCode");
                                int hose_clip_no = jsonObject.getInt("HoseClipNo");
                                int res = jsonObject.getInt("Result");

                                addRecordToLocalDB(autoIncrementId(), qr_c, null, hose_clip_no, 0, res, null, 0, null, CURRENT_DATA_SYNCED_WITH_SERVER);

                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Please Check Server Connection...",Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                Toast.makeText(getApplicationContext(),"Sorry"+error,Toast.LENGTH_LONG).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public int autoIncrementId(){
        int id = 1;
        try {

            Database db = new Database(QCheck1.this);
            db.open();
            Cursor c1 = db.getMaxIdQCheckResult();
            id = c1.getCount() + 1;
            c1.close();
            db.close();

        }catch (Exception  e){

        }

        return id;
    }

    /*---------------------- Print QR -----------------------------*/

    private void generateQRCode() {
        String barcode_content = qr.getText().toString().trim();//"MA1YU2WTUK6K15183_AAW2APEW7TU05TH0PC_";
        try {
            Bitmap bitmap = TextToImageEncode(barcode_content);
            qr_img.setImageBitmap(bitmap);
            printData();
        } catch (WriterException e) {

        }
    }

    int QR_SIZE = 200;

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QR_SIZE, QR_SIZE, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.BLACK):getResources().getColor(R.color.WHITE);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, QR_SIZE, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    public void printData(){

        listPrintHoseClips.clear();
        listPrintResults.clear();

        for(int i = 0; i < listClipNo.size(); i++) {

            listPrintHoseClips.add(listClipNo.get(i));

            if (listOkClips.contains(listClipNo.get(i))) {
                listPrintResults.add("Ok");
            }

            if (listNotOkClips.contains(listClipNo.get(i))) {
                listPrintResults.add("Not-Ok");
            }

            if (listUncheckedClips.contains(listClipNo.get(i))) {
                listPrintResults.add("NA");
            }
        }

        String hose_clip_no = String.valueOf(listPrintHoseClips).replaceAll("\\[|\\]", "").replace(" ","").replace(",", "\n");
        String result = String.valueOf(listPrintResults).replaceAll("\\[|\\]", "").replace(" ","").replace(",", "\n");

        print_hose_clip.setText(hose_clip_no);
        print_result.setText(result);

        sub_qr1.setText(qr.getText().toString().split("_")[0]);
        sub_qr2.setText(qr.getText().toString().split("_")[1]);

        SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
        String format = s.format(new Date());

        print_time.setText(format);

    }

    private void doQRPrint() {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = loadBitmapFromView(printLayout);
        photoPrinter.printBitmap("QR Print", bitmap);
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    public void printPreview(){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.print_item);
        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
        qr_img = (ImageView) dialog.findViewById(R.id.img_qr_code);
        printRecyclerView = (RecyclerView)dialog.findViewById(R.id.recycler_view_print);
        printLayout = (LinearLayout) dialog.findViewById(R.id.layout_print);
        sub_qr1 = (TextView) dialog.findViewById(R.id.txt_sub_qr1);
        sub_qr2 = (TextView) dialog.findViewById(R.id.txt_sub_qr2);
        model_color = (TextView) dialog.findViewById(R.id.txt_model_color);
        fule_type = (TextView) dialog.findViewById(R.id.txt_fuel_type);
        print_time = (TextView) dialog.findViewById(R.id.txt_print_time);

        print_hose_clip = (TextView) dialog.findViewById(R.id.txt_hose_clip_print);
        print_result = (TextView) dialog.findViewById(R.id.txt_result_print);

        generateQRCode();

        Button print = (Button) dialog.findViewById(R.id.btn_print);
        Button cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        // if button is clicked, close the custom dialog
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doQRPrint();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                fetchRequiredImages();
            }
        });

        dialog.show();

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(false);
    }

    /*--------------------------------------------------------------*/


}