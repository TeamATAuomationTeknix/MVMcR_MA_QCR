package com.example.MVMcR_MA_QCR;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MVMcR_MA_QCR.Fragments.PartFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;

public class Questions extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    ProgressDialog p;
    ArrayList<Questions_main> list;
    ArrayList<String> pnames;
    PartFragment partFragment;
    Button btnNext;
    private ImageButton scanqr;
    String partname;
    public TextView qr;
    private String qr_res;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    String qrScanned="no";
    Chronometer timer;
    Chronometer fullTimer;
    public static Boolean noRemaining=true;
    LinearLayout partTimeLayout,fullTimeLayout;
    String firstPart;
    public static ProgressBar progressBar;
    public static float count=0;
    public static float partcount=0;
    public static float devidedparts=0;
    public static boolean isConnected=true;
    Runnable questionTimer;
    Handler handler;
    boolean active=true;
    Spinner parts;
    public static String appName="LHS";
    boolean submitted=true;
    private QuestionViewModel viewModel;
    public static int notOKPosition=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        savedInstanceState=null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        viewModel = new ViewModelProvider(this).get(QuestionViewModel.class);
        handler=new Handler();
        questionTimer=new Runnable() {
            @Override
            public void run() {
                active=false;
                finish();
                Toast.makeText(Questions.this, "user is inactive", Toast.LENGTH_SHORT).show();
            }
        };
        startHandler();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        scanqr = (ImageButton) findViewById(R.id.btn_scan_qr);
        qr = (TextView) findViewById(R.id.txt_qr);
        timer=findViewById(R.id.timer);
        fullTimer=findViewById(R.id.fullTimer);
        partTimeLayout=findViewById(R.id.parttimeLayout);
        fullTimeLayout=findViewById(R.id.fulltimelayout);
        //Getting data from ScanQR
        parts=findViewById(R.id.parts);
        if(Main_page.partEnabled)
        parts.setVisibility(View.VISIBLE);
        else
            parts.setVisibility(View.INVISIBLE);
        qr_res = getIntent().getStringExtra("qr_result");
        qr.setText(qr_res);
        qr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                inputQRCode();
                return false;
            }
        });


        scanqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), ScanQR.class));
                Intent i = new Intent(Questions.this, ScanQR.class);
                i.putExtra("calling_page", "Questions");
                i.putExtra("partname",partname);
                startActivity(i);
                finish();
            }
        });

        init();
        ArrayList<String> appNames=new ArrayList<>();
        MyDbHelper myDbHelper=new MyDbHelper(this,MyDbHelper.DB_NAME,null,1);
        appNames=myDbHelper.getAppNames();
        if(appNames.size()==0) {
            ServerJson serverJson = new ServerJson(Questions.this);
            serverJson.getAppName(Questions.this,appNames);
            appNames=myDbHelper.getAppNames();
        }
//        appNames.add("LHS");
//        appNames.add("RHS");
//        appNames.add("Engine Compart");
        initparts(appNames);
    }
    public void initparts(ArrayList<String> partsList){
        parts.setOnItemSelectedListener(this);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, partsList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        parts.setAdapter(dataAdapter);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        appName=parent.getItemAtPosition(position).toString();
        SharedPreferences preferences=getSharedPreferences("appnameselection",MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putInt("appnameselection",position);
        editor.putString("appname",appName);
        editor.apply();
        Log.e("position",position+"");
        Main_page.appNameSelection=position;
        if(noRemaining) {
            MyDbHelper myDbHelper = new MyDbHelper(this, MyDbHelper.DB_NAME, null, 1);
            pnames = myDbHelper.getPartnamesByApp(parts.getSelectedItem().toString());
            viewModel.pnames=pnames;
            partname = pnames.get(0);

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        SharedPreferences preferences=getSharedPreferences("appnameselection",MODE_MULTI_PROCESS);
        preferences.getInt("appnameselection",1);
        parts.setSelection( preferences.getInt("appnameselection",1));
    }
    private void init(){
        list = new ArrayList<>();
        pnames = new ArrayList<>();
        progressBar=findViewById(R.id.progress);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new OnBtnNextClickListner());
        //visibility**************
        if(qr.getText().toString().equals("")) {
            btnNext.setVisibility(View.INVISIBLE);
            partTimeLayout.setVisibility(View.INVISIBLE);
            fullTimeLayout.setVisibility(View.INVISIBLE);
        }
    }
    protected void onStart() {
        super.onStart();
        Questions_main.qNo = 1;
        MyDbHelper myDbHelper=new MyDbHelper(this,MyDbHelper.DB_NAME,null,1);
        ArrayList<String> appnames=myDbHelper.getAppNames();
        if(appnames.size()==0) {
            ServerJson serverJson = new ServerJson(Questions.this);
            serverJson.getAppName(Questions.this,appnames);
            appnames=myDbHelper.getAppNames();

        }
        for(String appp:appnames)
            Log.e("appnames from questios",appp);
       // parts.setEnabled(Main_page.partEnabled);
        SharedPreferences preferences=getSharedPreferences("appnameselection",MODE_MULTI_PROCESS);
        Log.e("preferance for appname",preferences.getInt("appnameselection",1)+"");
        parts.setSelection( preferences.getInt("appnameselection",1));
        firstPart = getIntent().getStringExtra("partname");
        partname = getIntent().getStringExtra("partname");
        qrScanned = getIntent().getStringExtra("qrScanned");
       myDbHelper = new MyDbHelper(this, MyDbHelper.DB_NAME, null, 1);
        // Cursor remainingParts=new ArrayList<>();
        Cursor remainingParts = myDbHelper.getRemainingParts();
        if (remainingParts != null) {
            if (remainingParts.moveToFirst()) {
                noRemaining = false;

                long t;
                do {
                    //"part_name","fullTime","qr_code"
                    pnames.add(remainingParts.getString(0));
                    Log.e("pnames size",pnames.size()+"");
                    t = remainingParts.getLong(1);
                    Log.e("time remaing part: ", t + "");

                    qr_res = remainingParts.getString(2);
                    Log.e("qr res remaing part: ", qr_res);
                } while (remainingParts.moveToNext());
                myDbHelper.deleteRemainingParts();
                //  pnames = remainingParts;
                fullTimer.setBase(t);

                getIntent().putExtra("qr_result", qr_res);
                //qr.setText(qr_res);
                partname = pnames.get(0);
                getIntent().putExtra("partname",partname);
                qrScanned = "scanned";

            }
            else{
                noRemaining = true;

            }
        }
        else{
            noRemaining = true;

        }

        if (qrScanned != null) {
            Log.e("tag", qrScanned);
            ServerJson serverJson;
            if (qrScanned.equals("scanned")) {

               serverJson = new ServerJson(this);



                qr.setText(getIntent().getStringExtra("qr_result"));

                if (noRemaining) {
                    MyDbHelper dbHelper = new MyDbHelper(this, MyDbHelper.DB_NAME, null, 1);
                    pnames = dbHelper.getPartnamesByApp(parts.getSelectedItem().toString());
                    if(pnames!=null) {
                        if (pnames.size() == 0) {

                            serverJson = new ServerJson(this, pnames);
                            serverJson.getPartName();
                            pnames = serverJson.getPartnames();
                        }
                    }
                }//no remaining
                if(partname!=null)
                    addFragment();
            }
        }
    }
    public void addFragment(){
        if(partname!=null&&pnames.size()>0){
       // if(pnames.size()>0) {
            devidedparts=100/pnames.size();
            Log.e("count",count+"");
            btnNext.setVisibility(View.VISIBLE);
            partTimeLayout.setVisibility(View.VISIBLE);
            fullTimeLayout.setVisibility(View.VISIBLE);
            parts.setVisibility(View.INVISIBLE);
            timer.start();
            fullTimer.start();
        }
        partFragment = new PartFragment(list, partname,getModel());
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layoutQuestions, partFragment).commit();

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


//                String qr_code = inputQR.getText().toString().toUpperCase().trim().replaceAll("\\s+", "").replaceAll("\n","");
//                Pattern p = Pattern.compile("\\S{2}\\d{1}\\S{2}\\d{1}\\S{4}\\d{1}\\S{1}\\d{5}\\S{3}\\d{1}\\S{4}\\d{1}\\S{2}\\d{2}\\S{2}\\d{1}\\S{2}");
//                if (!qr_code.equals("") && p.matcher(qr_code).matches()) {
                String qr_code=inputQR.getText().toString();


                if(qr_code.length()==36){
                    //parts.setEnabled(false);
                    Main_page.partEnabled=false;
                    // qr_code = qr_code.substring(0, 17) + "_" + qr_code.substring(17, qr_code.length())+"_";
                    Intent i = new Intent(Questions.this, Questions.class);
                    i.putExtra("qr_result", qr_code);
                    i.putExtra("qrScanned","scanned");
                    i.putExtra("partname",partname);
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

    class OnBtnNextClickListner implements View.OnClickListener{

        MyDbHelper helper;
        @Override
        public void onClick(View v) {
            ServerJson serverJson=new ServerJson(Questions.this);
            if(count<100) {
//                count=count+partcount;
//                progressBar.setProgress(count);
            }
            int i=1,ok=0,not_ok=0;
            list=partFragment.getList();
            for(Questions_main q:list){
                if(q.getAnswer()==null) {
                    Toast.makeText(Questions.this, "Please complete check point: " + i, Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    Log.e("tag",q.getId()+" "+q.getQuestion());
                    Log.e("tag",q.getAnswer());
                    if(q.getAnswer().equals(Questions_main.OK))
                        ok++;
                    else
                        not_ok++;
                    i++;
                }

            }
            if(i-1==list.size()&&i!=0) {
                //todo one activity completed
                SharedPreferences preferences=getSharedPreferences("userpref",MODE_PRIVATE);
                String user=preferences.getString("user","unknown");
                helper = new MyDbHelper(Questions.this, MyDbHelper.DB_NAME, null, 1);
                helper.insert_data(list,partname,qr.getText().toString(),user);
                serverJson=new ServerJson(Questions.this);
                serverJson.submitAnswer(list,partname,timer.getText().toString(),fullTimer.getText().toString(),qr.getText().toString(),user);
                // Toast.makeText(Questions.this, "Records submitted successfully", Toast.LENGTH_SHORT).show();
                Toast.makeText(Questions.this, "Ok: " + ok + " times & not ok: " + not_ok + "times", Toast.LENGTH_SHORT).show();
                Log.e("part time",fullTimer.getText().toString());
                helper.getAllAnswers();
                timer.setBase(SystemClock.elapsedRealtime());
            }
//            if(pnames.size()>0&&pnames.get(0).equals(partname)){
//                pnames.remove(0);
//            }
            //add fragments
            list=new ArrayList<>();
            if(pnames.size()>0) {
                pnames.remove(0);
                if(pnames.size()>0) {
                    partname = pnames.get(0);
                    viewModel.partname = pnames.get(0);
                    viewModel.pnames=pnames;
                    Questions_main.qNo = 1;
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(partFragment);
                    partFragment = new PartFragment(list, partname, getModel());
                    fragmentTransaction.add(R.id.layoutQuestions, partFragment).commit();
                }
            }
            else{
                Toast.makeText(Questions.this, "All check points completed", Toast.LENGTH_SHORT).show();
            }
            if(pnames.size()==0){
                //todo all part questios are completed
                count=0;
                qrScanned="";
                submitted=false;
                timer.stop();
                fullTimer.stop();
                SharedPreferences preferences=getSharedPreferences("userpref",MODE_PRIVATE);
                String user=preferences.getString("user","unknown");
                serverJson=new ServerJson(Questions.this);
                serverJson.insertTotalTime(Questions.this,qr_res,user,fullTimer.getText().toString());
                fragmentTransaction=fragmentManager.beginTransaction();
                fragmentTransaction.remove(partFragment);
                fragmentTransaction.commit();
                TextView t=new TextView(Questions.this);
                CompleteDialog completeDialog=new CompleteDialog(Questions.this);
                completeDialog.setCancelable(false);
                completeDialog.show();
                Log.e("Submit time",fullTimer.getText().toString());
                if(qr.getText().toString().equals("")) {
                    btnNext.setVisibility(View.INVISIBLE);
                    partTimeLayout.setVisibility(View.INVISIBLE);
                    fullTimeLayout.setVisibility(View.INVISIBLE);
                }
                qr.setText("");
                return;
            }



        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        stopHandler();//stop first and then start
        startHandler();
    }
    public void stopHandler() {
        handler.removeCallbacks(questionTimer);
    }
    public void startHandler() {
        handler.postDelayed(questionTimer, 15*60*1000); //for 15 minutes
    }

    private String getModel(){

        String  qr_code = qr.getText().toString().trim();

        String[] a = qr_code.split("_");
        String model_name = a[1].charAt(0)+""+a[1].charAt(1)+""+a[1].charAt(2);
        return model_name;
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        count=viewModel.count;
        devidedparts=viewModel.devidedparts;
        partcount=viewModel.partcount;
        list=viewModel.list;
        pnames=viewModel.pnames;

        if(requestCode==103){
            Uri uri=data.getData();
            InputStream iStream = null;
            try {
                iStream = getContentResolver().openInputStream(uri);
                byte[] inputData = getBytes(iStream);
                list.get(notOKPosition).setRemarkImage(inputData);
            } catch (Exception e){
                e.printStackTrace();
            }
            partFragment.setList(list);

           uri=data.getData();
            Log.e("list size",list.size()+"");
            partname=viewModel.partname;
            partFragment.setList(list);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentTransaction=fragmentManager.beginTransaction();
            partFragment = new PartFragment(list,partname ,getModel());
            fragmentTransaction.replace(R.id.layoutQuestions, partFragment).commit();
            LinkedHashSet<String> linkedHashSet=new LinkedHashSet<>(pnames);
            pnames=new ArrayList<>(linkedHashSet);
        }
        Log.e("size",list.size()+"");

    }
    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
    @Override
    protected void onPause() {
        super.onPause();

        viewModel.count=count;
        viewModel.devidedparts=devidedparts;
        viewModel.partcount=partcount;
        viewModel.pnames=pnames;
        viewModel.partname=partname;
        count=0;
        if(partFragment!=null)
            viewModel.list=partFragment.getList();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HashSet<String> ppnames=new HashSet<>(pnames);
        for(String s:pnames)
            Log.e("remaining parts:",s);
        if(noRemaining){
            ppnames.add(partname);
        }
        long fullTime= fullTimer.getBase();
        fullTimer.stop();
        timer.stop();
        String qr_code=qr.getText().toString();

        if(ppnames.size()>0 &&!qr_code.equals("")){
            if(partname!=firstPart)
                ppnames.add(partname);
//            if(ppnames.size()>1) {
//                if (ppnames.ge(0).equals(ppnames.get(1)))
//                    ppnames.remove(0);
//            }
            LinkedList<String> ll=new LinkedList<>(ppnames);
            MyDbHelper myDbHelper=new MyDbHelper(this,MyDbHelper.DB_NAME,null,1);
            myDbHelper.setRemainingParts(ll,fullTime,qr_code);
        }
    }
}