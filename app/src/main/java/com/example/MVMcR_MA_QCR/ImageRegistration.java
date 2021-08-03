package com.example.MVMcR_MA_QCR;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ImageRegistration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    RecyclerView recyclerViewImages;
    RecyclerView.Adapter adapter;

    public static TextView qr;
    public static ImageView imagePreview;
    public static ImageButton camera, recamera, add, update, broom, remove;
    private ImageButton scanqr;
    public static RelativeLayout relativeLayout_img;
    Bitmap bitmapImage;
    Spinner spinner;
    String model_name;
    ArrayList<Integer> listId;
    ArrayList<Bitmap> listImages;
    //ArrayList byteArrayImages = new ArrayList();
    public static int id;
    private String qr_res;
    private Uri file;
    int TAKE_PHOTO_CODE = 100;
    //public static Bitmap decodedImage = null;
    private  int camera_id;
    private static File mediaStorageDir;

    /*------------------ Server Related Declaration -------------------*/

   // public static final String INSERT_URL = Main_page.IP_ADDRESS + "/MasterDataInsertion.php";
   public static String INSERT_URL = Main_page.IP_ADDRESS + "/SetMasterImageByModel.php";
    public static String UPDATE_URL = Main_page.IP_ADDRESS + "/MasterDataUpdation.php";
    public static String DELETE_URL = Main_page.IP_ADDRESS + "/MasterDataDeletion.php";
    //public static final String FETCH_URL = Main_page.IP_ADDRESS + "/MasterDataFetch.php";
    public static String FETCH_URL = Main_page.IP_ADDRESS + "/GetMasterImageByModel.php";
    public static String KEY_ID = "id";
    public static String KEY_PART_NAME = "prt_name";
    public static String KEY_IMAGE_DATA = "mst_img";
    String msg;
    /*------------------------------------------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_registration);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        qr = (TextView) findViewById(R.id.txt_qr);
        spinner=findViewById(R.id.spinner);
        scanqr = (ImageButton) findViewById(R.id.btn_scan_qr);
        imagePreview = (ImageView) findViewById(R.id.img_preview);
        camera = (ImageButton) findViewById(R.id.img_btn_camera);
        recamera = (ImageButton) findViewById(R.id.img_btn_recamera);
        add = (ImageButton) findViewById(R.id.img_btn_add);
        update = (ImageButton) findViewById(R.id.img_btn_update);
        broom = (ImageButton) findViewById(R.id.img_btn_broom);
        remove = (ImageButton) findViewById(R.id.img_btn_remove);
        relativeLayout_img = (RelativeLayout) findViewById(R.id.relative_layout_img);
        initializeSpinner();

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //qr.clearFocus();
        //Toast.makeText(ImageRegistration.this,"mmm",Toast.LENGTH_SHORT).show();

        clearImage();
        camera.setVisibility(View.INVISIBLE);

        recyclerViewImages=(RecyclerView)findViewById(R.id.recycler_view);

        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true));

        listId=new ArrayList<>();
        listImages=new ArrayList<>();

        // Calling the RecyclerView
        recyclerViewImages.setHasFixedSize(true);

        // The number of Columns
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, true);
        recyclerViewImages.setLayoutManager(layoutManager);
        //Getting data from ScanQR
        qr_res = getIntent().getStringExtra("qr_result");
        qr.setText(qr_res);

        // Check Model Exist Or Not
        isModelNameExist();

        // Scan  QR Code
        scanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), ScanQR.class));
                Intent i = new Intent(ImageRegistration.this, ScanQR.class);
                i.putExtra("calling_page", "ImageRegistration");
                startActivity(i);
                finish();
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

//                Intent i = new Intent(ImageRegistration.this, CameraPreview.class);
//                i.putExtra("calling_page", "ImageRegistration");
//                startActivity(i);

                takePicture(v);
//                AlertDialog.Builder builder=new AlertDialog.Builder(ImageRegistration.this);
//                builder.setTitle("Alert");
//                builder.setMessage("This feature is not available yet.");

//                builder.show();
            }

        });

        // Open Recamera
        recamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(ImageRegistration.this, CameraPreview.class);
//                i.putExtra("calling_page", "ImageRegistration");
//                startActivity(i);
                takePicture(v);
            }
        });

        // Add Captured Image
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addImage();
                sendDataToServer(v);
            }
        });
        // Update Captured Image
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String alertMsg = "Are you sure ? Do you want to UPDATE this Image ?";
                alertDialog("UPDATE", alertMsg);

            }
        });

        // Clear Captured Image
        broom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearImage();
                fetchImages();
            }
        });

        // Delete Single Image
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String alertMsg = "Are you sure ? Do you want to DELETE this Image ?";
                alertDialog("DELETE", alertMsg);

            }
        });


        // Add Back Arrow to Toolbar
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initializeSpinner() {
        spinner.setOnItemSelectedListener(this);
        MyDbHelper myDbHelper=new MyDbHelper(getBaseContext(),MyDbHelper.DB_NAME,null,1);

        //List<String> partnamelist = new ArrayList<String>();
        ArrayList<String> partnamelist=myDbHelper.getPartnames();
       if(partnamelist==null){
           ServerJson serverJson=new ServerJson(getBaseContext(), partnamelist);
          serverJson.getPartnames();
       }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, partnamelist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }

    // Handle Back Arrow operation Here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == R.id.home) {
            String alertMsg = "Are you sure ? Do you want to LEAVE this Page ?";
            alertDialog("BACK", alertMsg);
        }



        return super.onOptionsItemSelected(item);
    }


    private String getModel(){

//        String  qr_code = qr.getText().toString().trim();
//        String[] a = qr_code.split("_");
//        model_name = a[1].charAt(0)+""+a[1].charAt(1)+""+a[1].charAt(2);
//        return model_name;
        return "no model";
    }
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        model_name=item;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        model_name=parent.getSelectedItem().toString();
    }

    private void isModelNameExist(){
        String txtQR = qr.getText().toString().trim();

        if (txtQR.equals("")){
            camera.setVisibility(View.INVISIBLE);
            add.setVisibility(View.INVISIBLE);
            remove.setVisibility(View.INVISIBLE);

        }
        else {
            String model_nm = getModel();
            //Toast.makeText(ImageRegistration.this,"model = "+model_nm,Toast.LENGTH_SHORT).show();
            camera.setVisibility(View.VISIBLE);
            try {
                Database db = new Database(ImageRegistration.this);
                db.open();

                Cursor c1 = db.getAllModels();
                Log.d("func", "outside");

                if (c1 != null && c1.getCount() != 0) {
                    if (c1.moveToFirst()) {

                        do {

                            int id = c1.getInt(c1.getColumnIndex(db.ID));
                            String model = c1.getString(c1.getColumnIndex(db.MODEL_NM));
                            byte[] image = c1.getBlob(c1.getColumnIndex(db.IMG));

                            if (model_nm.equals(model)) {
                                fetchImages();
                                fetchDataFromServer();
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


    public int autoIncrementId(){
        int id = 1;
        try {

            MyDbHelper db = new MyDbHelper(ImageRegistration.this,MyDbHelper.DB_NAME,null,1);

            Cursor c1 = db.getMaxImageRegResult();
            id = c1.getCount() + 1;
            c1.close();
           // db.close();

        }catch (Exception  e){

        }

        return id;
    }

    private void addImage(){

        bitmapImage = ((BitmapDrawable)imagePreview.getDrawable()).getBitmap();
        if (bitmapImage != null){

            //listImages.add(bitmapImage);

                        final String part_name = spinner.getSelectedItem().toString();
                              String model_name=getModel();
            //Toast.makeText(ImageRegistration.this,"Model - "+ model_nm,Toast.LENGTH_SHORT).show();

            //String model_nm = qr.getText().toString().replaceAll("\\s", "");
            //String model_nm = qr.getText().toString().replaceAll("([0-9])", "").trim();

                  MyDbHelper db = new MyDbHelper(ImageRegistration.this,MyDbHelper.DB_NAME,null,1);

                  db.addImage( part_name,model_name, bitmapToByteArray(bitmapImage));
                  db.close();

            //byteArrayImages.add(bitmapToByteArray(bitmapImage));
            //Toast.makeText(ImageRegistration.this,"Hello "+ bitmapToByteArray(cropedBitmapImage),Toast.LENGTH_SHORT).show();
            //clearImage();

            fetchImages();

        }
        else {
            Toast.makeText(ImageRegistration.this,"There is No Image",Toast.LENGTH_SHORT).show();
        }

    }


    private void updateImage(){

        Bitmap bitmapImage = ((BitmapDrawable)imagePreview.getDrawable()).getBitmap();
        if (bitmapImage != null){

            //listImages.add(bitmapImage);

            final String model_nm = getModel();

            Database db = new Database(ImageRegistration.this);
            db.open();
            db.updateImage(model_nm ,bitmapToByteArray(bitmapImage), id);
            db.close();

            //clearImage();

            fetchImages();

        }
        else {
            Toast.makeText(ImageRegistration.this,"There is No Image",Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchImages(){

        listId.clear();
        listImages.clear();
        String part_name = spinner.getSelectedItem().toString();
        String model_nm=getModel();
        //String model_nm = qr.getText().toString().replaceAll("\\s", "");
        //String model_nm = qr.getText().toString().replaceAll("([0-9])", "").trim();

        MyDbHelper db = new MyDbHelper(ImageRegistration.this,MyDbHelper.DB_NAME,null,1);

        //Toast.makeText(ImageRegistration.this,model_nm,Toast.LENGTH_SHORT).show();
        Cursor c1 = db.getAllImagesOfSpecificModel(model_nm,part_name);

        if (c1 != null && c1.getCount() != 0) {
            if (c1.moveToFirst()) {

                do {

                    int id=c1.getInt(c1.getColumnIndex("id"));
                    byte[] image=c1.getBlob(c1.getColumnIndex("image"));
                    //Toast.makeText(ImageRegistration.this,"Id - "+id,Toast.LENGTH_SHORT).show();
                    Log.d("id = "+id, "inside");
                    listId.add(id);
                    listImages.add(byteArrayToBitmap(image));

                } while (c1.moveToNext());
            }
        }
        c1.close();
       // db.close();

        adapter = new ImageRegistrationAdapter(ImageRegistration.this, listId, listImages);
        recyclerViewImages.setAdapter(adapter);

    }

    private  void removeImage(){

        Database db = new Database(ImageRegistration.this);
        db.open();
        db.deleteSingleImage(id);
        db.close();

        //clearImage();
        fetchImages();

    }

    private byte[] bitmapToByteArray(Bitmap bmp){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private static Bitmap byteArrayToBitmap(byte[] byteimg){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteimg, 0, byteimg.length);
        return bitmap;
    }

    public static void clearImage(){
        imagePreview.setImageBitmap(null);
        imagePreview.destroyDrawingCache();
        //cropedBitmapImage = null;
        camera.setVisibility(View.VISIBLE);
        imagePreview.setVisibility(View.INVISIBLE);
        recamera.setVisibility(View.INVISIBLE);
        add.setVisibility(View.INVISIBLE);
        update.setVisibility(View.INVISIBLE);
        broom.setVisibility(View.INVISIBLE);
        remove.setVisibility(View.INVISIBLE);
    }

    private void backPage() {
        this.finish();
    }

    private void alertDialog(final String operation, String alertMsg){

        // Build the AlartBox
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImageRegistration.this);

        // Set the appropriate message into it.
        alertDialogBuilder.setMessage(alertMsg);

        // Add a positive button and it's action. In our case action would be deletion of the data
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {

                            switch (operation){
                                case "UPDATE":
                                    updateImage();
                                    sendDataToServer(update);
                                    break;
                                case "DELETE":
                                    removeImage();
                                    sendDataToServer(remove);
                                    break;

                                case "BACK":
                                    backPage();
                                    break;

                                default:
                                    Toast.makeText(ImageRegistration.this,"Something went wrong...",Toast.LENGTH_SHORT).show();
                                    break;
                            }

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        // Add a negative button and it's action. In our case, just hide the dialog box
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (operation){
                            case "UPDATE":
                                clearImage();
                                fetchImages();
                                break;
                            case "DELETE":
                                clearImage();
                                fetchImages();
                                break;

                            case "BACK":
                                break;

                            default:
                                Toast.makeText(ImageRegistration.this,"Something went wrong...",Toast.LENGTH_SHORT).show();
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
                String qr_code = inputQR.getText().toString();//toUpperCase().trim().replaceAll("\\s+", "").replaceAll("\n","");
               // Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}");
                //if (!qr_code.equals("") && p.matcher(qr_code).matches()) {
                if(qr_code.length()==36){
                    //qr_code = qr_code.substring(0, 17) + "_" + qr_code.substring(17, qr_code.length())+"_";
                    Intent i = new Intent(ImageRegistration.this, ImageRegistration.class);
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
//        Toast.makeText(getApplicationContext(),"Hello Javatpoint",Toast.LENGTH_SHORT).show();
        try {
            System.out.println("## mmmmm");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            System.out.println("## mmmmm1");
            file = FileProvider.getUriForFile(ImageRegistration.this, "com.example.MVMcR_MA_QCR.fileprovider", getOutputMediaFile());
            System.out.println("## mmmmm2");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
            System.out.println("## mmmmm3");
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }

        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support capturing images!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }


    private void sendDataToServer(View v){

        Bitmap bitmapImage = ((BitmapDrawable)imagePreview.getDrawable()).getBitmap();
        if (bitmapImage == null){
            Toast.makeText(getApplicationContext(),"Please Capture Image First...",Toast.LENGTH_LONG).show();
            return;
        }
         String part_name = spinner.getSelectedItem().toString();
         String image = bitmapToBase64(bitmapImage);
        String DATA_URL = null;
        if (v == add){
            DATA_URL = INSERT_URL;
        }
        else if(v == update){
            DATA_URL = UPDATE_URL;
        }
        else if(v == remove){
            DATA_URL = DELETE_URL;
        }

// Start Volley Work


        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.show();
        String url=Main_page.IP_ADDRESS+"/MasterDataInsertion.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        try {

                            Log.e("response: ",response);

                            if(response.equals("Unsuccessfully"))
                            {

                                Toast.makeText(getApplicationContext(),"Operation Failed...! Please Try Again...!",Toast.LENGTH_LONG).show();
                            }
                            else if(response.equals("Successfully"))
                            {

                                Toast.makeText(getApplicationContext(),"Operation Done Successfully...!",Toast.LENGTH_LONG).show();
                                fetchDataFromServer();
                                clearImage();
                            }
                            dialog.dismiss();
                        }
                        catch (Exception e){
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Operation Failed...! Please Try Again...! \n\n("+e+")",Toast.LENGTH_LONG).show();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Operation Failed...! Please Check Server Connection & Try Again...! \n\n("+error+")",Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){

                Map<String,String> params = new HashMap<String, String>();

                params.put(KEY_ID, String.valueOf(id));
                params.put(KEY_PART_NAME,part_name);
                params.put(KEY_IMAGE_DATA, "data:image\\/jpeg;base64,"+image);
                params.put("model_name",getModel());
                Log.e("model name: ",getModel());
                return params;
            }

        };
        Log.e("uploading image to: ",url);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        clearImage();
    }

    public String bitmapToBase64(Bitmap bitmapImg){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;

    }

    public void fetchDataFromServer(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Main_page.IP_ADDRESS+"/MasterDataFetch.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        try {
                            MyDbHelper db = new MyDbHelper(ImageRegistration.this,MyDbHelper.DB_NAME,null,1);
                            db.deleteAllImages();
                            Log.e("image response",response);
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                int id = jsonObject.getInt("id");
                                String part_name = jsonObject.getString("img_name");
                                String image = jsonObject.getString("img");
                               // String model=jsonObject.getString("model_name");
                               // db.addImage( part_name,"no model", base64ToByteArray(image));
                                db.addImage(part_name, "no model", Base64.decode(image.substring(23), Base64.DEFAULT));


                            }
                            fetchImages();
                            progressDialog.dismiss();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                //Toast.makeText(getApplicationContext(),"Sorry"+error,Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    public byte[] base64ToByteArray(String imageString){
        byte[] imgBytes = Base64.decode(imageString, Base64.DEFAULT);
        return imgBytes;
    }

    private void addDataToLocalDB(int id, String model_nm, String image){

        Database db = new Database(ImageRegistration.this);
        db.open();
        db.addImage(id, model_nm, base64ToByteArray(image));
        db.close();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {

            Bitmap bitmapImage = null;
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
            } catch (Exception e) {
                e.printStackTrace();
            }


            //Bitmap processedBitmap = null;
  /*          int x, y, width, height;
            int center, start;
            int rotation;
            int w = bitmapImage.getWidth();
            int h = bitmapImage.getHeight();

            if (bitmapImage.getWidth() > bitmapImage.getHeight()){
            /*    Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapImage, bitmapImage.getWidth(), bitmapImage.getHeight(), true);
                processedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
             */
    /*            width = h;
                height = h;
                center = w/2;
                start = center - (h/2);
                x = start;
                y = 0;
                rotation = 90;

            }else {
                width = w;
                height = w;
                center = h/2;
                start = center - (w/2);
                x = 0;
                y = start;
                rotation = 0;
            }


            Bitmap cropedBitmapImage = Bitmap.createBitmap(bitmapImage, x, y, width, height);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            cropedBitmapImage.compress(Bitmap.CompressFormat.JPEG, 30, out);
            Bitmap decodedImage = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));


            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(decodedImage, decodedImage.getWidth(), decodedImage.getHeight(), true);
            Bitmap processedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageBitmap(decodedImage);
            imagePreview.setRotation(rotation);
            //imagePreview.setImageURI(file);
*/

            if (bitmapImage.getWidth() > bitmapImage.getHeight()) {
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
            center = h / 2;
            start = center - (w / 2);
            x = 0;
            y = start;

            Bitmap cropedBitmapImage = Bitmap.createBitmap(bitmapImage, x, y, width, height);
            //ByteArrayOutputStream out = new ByteArrayOutputStream();
            //cropedBitmapImage.compress(Bitmap.CompressFormat.JPEG, 30, out);
            //Bitmap decodedImage = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
            Bitmap scaledImage = Bitmap.createScaledBitmap(cropedBitmapImage, 512, 512, true);
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageBitmap(scaledImage);
            if (camera_id == camera.getId()) {
                //Toast.makeText(this, "Camera = "+camera_id, Toast.LENGTH_SHORT).show();
                add.setVisibility(View.VISIBLE);
                broom.setVisibility(View.VISIBLE);
                camera.setVisibility(View.INVISIBLE);


            } else if (camera_id == recamera.getId()) {
                //Toast.makeText(this, "Recamera = "+camera_id, Toast.LENGTH_SHORT).show();
                update.setVisibility(View.VISIBLE);
                remove.setVisibility(View.INVISIBLE);
                broom.setVisibility(View.VISIBLE);
            }

            deleteFolderWithImages(mediaStorageDir);
        }

    }

    private static File getOutputMediaFile(){
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MasterImages");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
    }

    public static boolean deleteFolderWithImages(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteFolderWithImages(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    @Override
    public void onBackPressed() {
        String alertMsg = "Are you sure ? Do you want to LEAVE this Page ?";
        alertDialog("BACK", alertMsg);
    }
    //todo add home icon to tolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }

}
