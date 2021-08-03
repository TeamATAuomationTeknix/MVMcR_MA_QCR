package com.example.MVMcR_MA_QCR;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.MVMcR_MA_QCR.databinding.ActivityMainPageBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main_page extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static String IP_ADDRESS = "http://192.168.0.13/at/app";
    //public static final String IP_ADDRESS = "http://192.168.0.13/Test";
    public final String IP_ADDRESS_IMG = "http://192.168.137.1";
    public static TextView resultTextView;
    public static ImageView imagePreview, inputImageView1;
    private Button identifyButton;
    private SharedPreferences sharedPreferences;

    private ImageButton next;
    // **********************2 variables added
    ArrayList<String> pnames;
    String partname="";
    public static final String DATA_SAVED_BROADCAST = "datasaved";
    private AppBarConfiguration mAppBarConfiguration;

    private BroadcastReceiver broadcastReceiver;
    public static boolean partEnabled=true;
    public static int appNameSelection=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        SharedPreferences preferences=getSharedPreferences("userpref",MODE_PRIVATE);
        Log.e("user from main",preferences.getString("user","unknown"));

        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        pnames=new ArrayList<>();
        //***************** local db
        MyDbHelper dbHelper=new MyDbHelper(Main_page.this,MyDbHelper.DB_NAME,null,1);
        pnames= dbHelper.getPartnames();
        IP_ADDRESS="http://"+dbHelper.getIpAdress()+"/at/app";

        //**************** server db
        if(pnames.size()==0) {
            Log.e("MainPage getting pnames",pnames.size()+"");
            ServerJson serverJson = new ServerJson(Main_page.this, pnames);
            serverJson.getPartName();
        }
        Cursor cursor=dbHelper.getMaxImageRegResult();
        if(!cursor.moveToFirst()){
            ServerJson serverJson = new ServerJson(Main_page.this, pnames);
            ImageRegistration imageRegistration=new ImageRegistration();
            serverJson.fetchImagesFromServer();
        }
        sharedPreferences = getSharedPreferences("Picture Pref", Context.MODE_PRIVATE);

        if (checkPermissions()){
            //  permissions  granted
        }


        next = (ImageButton) findViewById(R.id.img_btn_next);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("tag","next button clicked");
                // pnames=serverJson.getPartnames();
                if(pnames!=null&&pnames.size()>0)
                    partname=pnames.get(0);
                //add partnames from serverjson if not exist

                Log.e("part in main page is: ",partname);
                Intent i=new Intent(Main_page.this, Questions.class);
                i.putExtra("partname",partname);
                startActivity(i);
            }
        });

    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_page);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/
    /*---------------------------------------------------------------------------------------------*/

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
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                } else {
                    //Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                }
                // permissions list of don't granted permission
            }
            return;
        }
    }


    private static File currentImagesDir(){

        File mediaStorageCurrent = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "QC Testing");

        if (!mediaStorageCurrent.exists()){
            if (!mediaStorageCurrent.mkdirs()){
                return null;
            }
        }

        return new File(mediaStorageCurrent.getPath());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_master) {
            Intent i=new Intent(Main_page.this,ImageRegistration.class);
            startActivity(i);

        }else if (id == R.id.nav_gallery) {
            Intent i=new Intent(Main_page.this,MasterImages.class);
            startActivity(i);

        } else if (id == R.id.nav_current_page) {
            Intent i=new Intent(Main_page.this,QCheck.class);
            startActivity(i);

        }
        else if (id == R.id.nav_battery_check) {
            Intent i = new Intent(Main_page.this, Battery.class);
            startActivity(i);
        }
        else if (id == R.id.nav_restart) {
            Log.e("main_activity","restarting app");
//            ServerJson serverJson=new ServerJson(getBaseContext());
//            serverJson.refreshData();
            MyDbHelper myDbHelper=new MyDbHelper(Main_page.this,MyDbHelper.DB_NAME,null,1);
            myDbHelper.deletePrimaryData();
            this.finish();
            startActivity(getIntent());
        }
        else if (id == R.id.nav_exit) {
            this.finish();
        }else if (id == R.id.nav_change_ip) {
            LoginDialog loginDialog=new LoginDialog(this);
            loginDialog.show();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
    public byte[] base64ToByteArray(String imageString) {
        byte[] imgBytes = Base64.decode(imageString, Base64.DEFAULT);
        return imgBytes;
    }

}