package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

public class QCheck extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    RecyclerView recyclerViewQCheck;
    RecyclerView.Adapter adapter;

    public static TextView qr, clip_no, clip_not_found, resultTextView;
    public static ImageView imagePreview, imageClip;

    private ImageButton scanqr;
    public static RelativeLayout relativeLayout_img;
    private Button identifyButton;
    private SharedPreferences sharedPreferences;
    private Bitmap bitmap;
    private Bitmap processedBitmap;
    private String currentPhotoPath;
    private static final String PREF_USE_CAMERA_KEY = "USE_CAMERA";

    public static final int SELECT_PHOTO_CODE = 1;
    public static final int CAPTURE_PHOTO_CODE = 2;
    private static File mediaStorageDir;
    private Uri imageUri;
    private static final int IMAGE_WIDTH = 700;
    private static final int IMAGE_HEIGHT = 700;

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
    ArrayList<String> partnamelist;

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
    Spinner spinner;
    /*------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qcheck);
        spinner=findViewById(R.id.spinner);
        //Toast.makeText(getApplicationContext(),"Id : "+autoIncrementId(),Toast.LENGTH_SHORT).show();
        qr = (TextView) findViewById(R.id.txt_qr);

        scanqr = (ImageButton) findViewById(R.id.btn_scan_qr);
        imagePreview = (ImageView) findViewById(R.id.img_preview);


       // sharedPreferences = getSharedPreferences("Picture Pref", Context.MODE_PRIVATE);

        ///////
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initializeSpinner();

        String qr_res = getIntent().getStringExtra("qr_result");
        qr.setText(qr_res);
        recyclerViewQCheck=(RecyclerView)findViewById(R.id.recycler_view_qcheck);
        recyclerViewQCheck.setLayoutManager(new LinearLayoutManager(QCheck.this));
        recyclerViewQCheck.setHasFixedSize(true);
        String qrcode=qr.getText().toString();
        if(qr_res!=null) {
            if (!qr_res.equals("")) {
                getTablleData();
                loadImages();
            }
        }
        //recyclerViewQCheck.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));

        // Calling the RecyclerView



        //isModelNameExist();

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
                Intent i = new Intent(QCheck.this, ScanQR.class);
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

        // Add Back Arrow to Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
    }

    private void loadImages() {
        String partname=spinner.getSelectedItem().toString();
        MyDbHelper myDbHelper=new MyDbHelper(getBaseContext(),MyDbHelper.DB_NAME,null,1);
        Cursor c1=myDbHelper.getAllImagesOfSpecificModel("no model",partname);

        if(c1.moveToFirst()){
            byte[] arrImg= c1.getBlob(1);
            // is= arrImg;
            Bitmap bmImg=BitmapFactory.decodeByteArray(arrImg,0,arrImg.length);

            imagePreview.setImageBitmap(bmImg);
        }
        else{
            Toast.makeText(getBaseContext(), "Please add image for: "+partname, Toast.LENGTH_SHORT).show();
        }
    }

    private void getTablleData() {
        String qr_res=qr.getText().toString();
        if(!qr_res.equals("")) {
            String partname = spinner.getSelectedItem().toString();
           MyDbHelper myDbHelper = new MyDbHelper(QCheck.this, MyDbHelper.DB_NAME, null, 1);
            // ArrayList<Questions_main> list = myDbHelper.getAllAnswers();
           ArrayList<Questions_main> list = myDbHelper.getAnswersBydata(qr_res, spinner.getSelectedItem().toString());
            if(list.size()==0&&!qr.getText().toString().equals("")){
            ServerJson serverJson = new ServerJson(QCheck.this, partnamelist);
          serverJson.setPartname(partname);
            //list=serverJson.getAnswers(qr_res);
            list= serverJson.getAnswers(qr_res, recyclerViewQCheck);
             }
            if(list.size()>0&&list!=null) {
                adapter = new QCheckAdapter(QCheck.this, list);
                recyclerViewQCheck.setVisibility(View.VISIBLE);
                recyclerViewQCheck.setAdapter(adapter);
            }
            else{
                recyclerViewQCheck.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void initializeSpinner() {
        spinner.setOnItemSelectedListener(this);
        MyDbHelper myDbHelper=new MyDbHelper(getBaseContext(),MyDbHelper.DB_NAME,null,1);

        //List<String> partnamelist = new ArrayList<String>();
        partnamelist=myDbHelper.getPartnames();
        if(partnamelist==null){
            ServerJson serverJson=new ServerJson(getBaseContext(), partnamelist);
            serverJson.getPartnames();
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, partnamelist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    // Handle Back Arrow operation Here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // handle arrow click here
        if (id == R.id.home) {
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

        }
        else {
            String model_nm = getModel();
            //Toast.makeText(getApplicationContext(),"model = "+model_nm,Toast.LENGTH_SHORT).show();

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






    //use for referance

    //db.addQCheckResult(id, qr_code, model_nm, hose_clip, master_img_id, result, work_time, shift, image_path, sync_status);






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

//****************************************adapter assigned***************************************



    }

    private static Bitmap byteArrayToBitmap(byte[] byteimg){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteimg, 0, byteimg.length);
        return bitmap;
    }

    public static void clearImage(){
        imagePreview.setImageBitmap(null);
        imagePreview.destroyDrawingCache();
//        clip_not_found.setVisibility(View.GONE);
        imagePreview.setVisibility(View.INVISIBLE);

    }

    private void backPage() {
        this.finish();
    }

    private void alertDialog(final String operation, String alertMsg) {

        // Build the AlartBox
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QCheck.this);

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
                                    Toast.makeText(QCheck.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(QCheck.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
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
        int maxLength = 36;
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
               // Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}");
               // if (!qr_code.equals("") && p.matcher(qr_code).matches()) {
                if(qr_code.length()==36){
                   // qr_code = qr_code.substring(0, 17) + "_" + qr_code.substring(17, qr_code.length())+"_";
                    Intent i = new Intent(QCheck.this, QCheck.class);
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.e("current data", "kkk");
            //Bitmap bitmapImage = null;
            try {
               // bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);

            } catch (Exception e) {
                e.printStackTrace();
            }



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



    @Override
    public void onBackPressed() {
        String alertMsg = "Are you sure ? Do you want to LEAVE this Page ?";
        alertDialog("BACK", alertMsg);
    }

    public void fetchLast7DaysData(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

//                        try {
//
//                            Database db = new Database(QCheck.this);
//                            db.open();
//                            db.deleteAllQCheckResult();
//                            db.close();
//
//                            JSONArray jsonArray = new JSONArray(response);
//
//                            for (int i = 0; i < jsonArray.length(); i++) {
//
//                                JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                                String qr_c = jsonObject.getString("QRCode");
//                                int hose_clip_no = jsonObject.getInt("HoseClipNo");
//                                int res = jsonObject.getInt("Result");
//
//                                addRecordToLocalDB(autoIncrementId(), qr_c, null, hose_clip_no, 0, res, null, 0, null, CURRENT_DATA_SYNCED_WITH_SERVER);
//
//                            }
//                        }
//                        catch (JSONException e) {
//                            e.printStackTrace();
//                            Toast.makeText(getApplicationContext(),"Please Check Server Connection...",Toast.LENGTH_LONG).show();
//                        }

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

//    public int autoIncrementId(){
//        int id = 1;
//        try {
//
//            Database db = new Database(QCheck.this);
//            db.open();
//            Cursor c1 = db.getMaxIdQCheckResult();
//            id = c1.getCount() + 1;
//            c1.close();
//            db.close();
//
//        }catch (Exception  e){
//
//        }
//
//        return id;
//    }

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

    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(),p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permissions granted.
                } else {
                    //Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                }
                // permissions list of don't granted permission
            }
            return;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(qr.getText()!=null) {
            if (!qr.getText().equals("")) {
                getTablleData();
                loadImages();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
       if(qr.getText()!=null) {
            if (!qr.getText().equals("")) {
                getTablleData();
                loadImages();
            }
        }
    }

    /*--------------------------------------------------------------*/


}