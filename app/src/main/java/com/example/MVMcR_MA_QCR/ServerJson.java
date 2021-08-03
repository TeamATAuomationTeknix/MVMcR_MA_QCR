package com.example.MVMcR_MA_QCR;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class ServerJson {
    ArrayList<String> partnames;
    ArrayList<MyDbHelper.Parts> partnames1;
    Context context;
    ArrayList<Questions_main> qlist;
    RecyclerView recyclerView;
    ProgressDialog p;
    ProgressDialog p1;
    String partname;
    MyDbHelper myDbHelper;
    AlertDialog.Builder builder;

    public ServerJson(Context context, ArrayList<Questions_main> qlist, RecyclerView recyclerView, String partname) {
        this.context=context;
        this.qlist=qlist;
        this.recyclerView= recyclerView;
        this.partname=partname;
        Log.e("getting from partname: ",partname);
    }

    public ServerJson(Context context, ArrayList<String> pnames) {
        this.context = context;
        partnames=pnames;
    }
    public ServerJson(Context context) {
        this.context = context;
    }

    public String getPartname() {
        return partname;
    }

    public void setPartname(String partname) {
        this.partname = partname;
    }

    public ArrayList<String> getPartnames() {
        return partnames;
    }

    public void setPartnames(ArrayList<String> partnames) {
        this.partnames = partnames;
    }

    //********************todo get answers**********************
    public ArrayList<Questions_main> getAnswers(String qr, RecyclerView recyclerViewQCheck) {
        final ProgressDialog dialog=new ProgressDialog(context);
        ArrayList<Questions_main> qqlist=new ArrayList<Questions_main>();
        partname=partname.replace(" ","%20");

        dialog.setMessage("Please wait... Answers checking from server");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        Log.e("tag","showing progress dialog of answers");
        dialog.show();
        MySingleton mySingleton=MySingleton.getInstance(context);
        RequestQueue requestQueue= mySingleton.getRequestQueue();
        String url=Main_page.IP_ADDRESS+"/GetAnswers.php/?partname="+partname+"&qr="+qr;
        Log.e("url",url);
        StringRequest stringRequest=new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("response",response);
                        try {
                            JSONArray jsonArray=new JSONArray(response);
                            // (id Integer , question text,answer Integer, Highlight Integer, partname varchar, qr varchar, user varcha
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject=jsonArray.getJSONObject(i);
                                Questions_main q= new Questions_main(jsonObject.getInt("id"),jsonObject.getString("question"),jsonObject.getString("answer"),
                                        "NOHIGHLIGHT",qr);
                                String remarkImg=jsonObject.getString("remarkImage");
                                if(remarkImg.length()>3)
                                    q.setRemarkImage(Base64.decode(remarkImg, Base64.DEFAULT));
                                else
                                    q.setRemarkImage(new byte[]{1});
                                Log.e("answer from server",q.toString());
                                qqlist.add(q);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finally {
                            if (qqlist.size() > 0) {
                                MyDbHelper myDbHelper = new MyDbHelper(dialog.getContext(), MyDbHelper.DB_NAME, null, 1);
                                myDbHelper.insert_data(qqlist, ServerJson.this.getPartname().replace("%20"," "), qr, "user1");

                                QCheckAdapter qCheckAdapter = new QCheckAdapter(dialog.getContext(), qqlist);
                                recyclerViewQCheck.setAdapter(qCheckAdapter);
                            }
                            dialog.hide();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.hide();
                        Log.e("temp saving error:",error.toString());
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<String,String>();
                params.put("partname",partname);
                params.put("qr",qr);
                return params;
            }
        };
        // MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);
        requestQueue.add(stringRequest);
        return qqlist;
    }

    //*********************************toddo access questions**************************************
    public void volleyRequest(){
        partname=partname.replace(" ","%20");
        p=new ProgressDialog(context);
        p.setMessage("Please wait... Questions are downloading");
        p.setIndeterminate(false);
        p.setCancelable(false);
        Log.e("tag","Showing progress dialog");
        p.show();
        MySingleton m=MySingleton.getInstance(context);
        RequestQueue requestQueue= m.getRequestQueue();
        Log.e("getting from partname: ",partname);
        String url=Main_page.IP_ADDRESS + "/PhpMySql.php?partname="+partname;
        Log.e("url",url);
        StringRequest jsonObjectRequest=new StringRequest(
                Request.Method.POST,
                url,
                new JSONObjectResponseListener(),
                new ErrorListner()
        );
//        {
//            @Nullable
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                HashMap<String,String> hm=new HashMap<>();
//                hm.put("partname","Ext(Wheel Rim type)");
//                return hm;
//            }
//        };
        requestQueue.add(jsonObjectRequest);
    }


    class JSONObjectResponseListener implements Response.Listener<String>{

        @Override
        public void onResponse(String response) {
            p.hide();
            Log.e("length of ressponse",response.length()+"");
            try {
                JSONArray response1=new JSONArray(response);
                for(int i=0;i<response.length();i++) {
                    JSONObject obj = response1.getJSONObject(i);
                    Log.e("json tag", obj.getInt("id") + " " + obj.getString("question") + " " + obj.getString("Highlight"));
                    String highlight = obj.getString("Highlight");
                    qlist.add(new Questions_main(obj.getInt("id"), obj.getString("question"), highlight));
                    //qlist.add(new Questions_main(obj.getString("question"), flag));
                }

            } catch (JSONException e) {
                Log.e("respose is",response);
                e.printStackTrace();
            }
            finally {
                if(partname!=null) {
                    String pn = partname.replace("%20", " ");
                    myDbHelper = new MyDbHelper(context, MyDbHelper.DB_NAME, null, 1);
                    if (qlist != null && pn != null)
                        myDbHelper.addQuestions(qlist, pn);
                    QuestionsAdapter adapter = new QuestionsAdapter(qlist,context);
                    if (recyclerView != null) {
                        Questions.partcount=Questions.devidedparts/qlist.size();
                        recyclerView.setAdapter(adapter);
                    }
                }
            }
        }
    }
    class ErrorListner implements Response.ErrorListener{

        @Override
        public void onErrorResponse(VolleyError error) {
            p.hide();

            Log.e("json ERROR",error.toString());
        }
    }

    //**************************************************get partname*************************************
    public void getPartName(){

        p1=new ProgressDialog(context);
        p1.setMessage("Please wait... getting parts");
        p1.setIndeterminate(false);
        p1.setCancelable(false);
        Log.e("tag","showing progress dialog");
        p1.show();
        MySingleton m=MySingleton.getInstance(context);
        RequestQueue requestQueue= m.getRequestQueue();

        JsonArrayRequest jsonObjectRequest=new JsonArrayRequest(
                Request.Method.GET,
                Main_page.IP_ADDRESS + "/GetPartNames.php",
                null,
                new GetPartnameListner(),
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        p1.hide();
                        Toast.makeText(context, "Not connected to network", Toast.LENGTH_SHORT).show();
                        Log.e("json ERROR",error.toString());
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);


    }

    class GetPartnameListner implements Response.Listener<JSONArray>{
        @Override
        public void onResponse(JSONArray response) {
            p1.hide();
        partnames1=new ArrayList<>();
            try {
                for(int i=0;i<response.length();i++) {
                    JSONObject obj = response.getJSONObject(i);
                    //partnames.add(obj.getString("partname"));
                    partnames1.add(new MyDbHelper.Parts(obj.getString("partname"),obj.getString("appname")));
                    Log.e("json tag", obj.getString("partname"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            finally {
                MyDbHelper myDbHelper = new MyDbHelper(context, MyDbHelper.DB_NAME, null, 1);
                myDbHelper.addPartNames(partnames1);

            }


        }
    }


    //****************************************todo Submit answers*********************************
    public void submitAnswer(ArrayList<Questions_main> answerList, String partname, String partTime, String fullTime, String qr_res,String user){
        builder=new AlertDialog.Builder(context);
        String url=Main_page.IP_ADDRESS+"/InsertAnswer.php";
        Log.e("url",url);
        StringRequest stringRequest=new StringRequest(Request.Method.POST,
                url,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        builder.setTitle("Done");
                         //builder.setMessage("message: "+response);
                         Log.e("submit answer",response);
                        builder.setMessage("Records submitted successfully");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Connection problem", Toast.LENGTH_SHORT).show();
                        Log.e("submit answer",error.toString());
                        Toast.makeText(context, "Data is stored locally", Toast.LENGTH_SHORT).show();
                        myDbHelper=new MyDbHelper(context,MyDbHelper.DB_NAME,null,1 );
                        for(Questions_main q:answerList) {
                            int ans=q.getAnswer()=="OK"?1:0;
                            //( int qid, String partname, String qrcode, String operator,String answer,String partTime, Date TimeStamp)
                            SimpleDateFormat formatter=new SimpleDateFormat("yy-MM-dd hh:mm:ss");
                            myDbHelper.submitTempAnswers(q.getId(),partname,qr_res,"sukrut",ans,partTime,formatter.format(new Date()),q.getRemarkImage());
                        }
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<String,String>();
                JSONArray jsonArray=new JSONArray();
                JSONObject jsonObjet ;
                String part=partname.replace("%20"," ");
                for(Questions_main q:answerList) {

                    try {
                        jsonObjet= new JSONObject();
                        jsonObjet.put("partname", part);
                        jsonObjet.put("id_fk_lhs_all_prt_que_tbl", q.getId());
                        if(qr_res!=null)
                            jsonObjet.put("qr_code", qr_res);
                        else
                            jsonObjet.put("qr_code", "100");
                        jsonObjet.put("operator", user);
                        int ans=q.getAnswer()=="OK"?1:0;
                        jsonObjet.put("answer", ans);
                        jsonObjet.put("partTime",partTime);
                        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        jsonObjet.put("currentTime",formatter.format(new Date()));
                        if(ans==0)
                            jsonObjet.put("remarkImage",
                                    bitmapToBase64(byteArrayToBitmap(q.getRemarkImage()))
                            );
                        else
                            jsonObjet.put("remarkImage",
                                    1
                            );
                        jsonArray.put(jsonObjet);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                String op=jsonArray.toString();
                Log.e("json array",op);
                params.put("value",op);
                return params;
            }

        };
        // MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
    public void submitTempAnswer(JSONArray jsonArray){
        builder=new AlertDialog.Builder(context);
        StringRequest stringRequest=new StringRequest(Request.Method.POST,
                Main_page.IP_ADDRESS+"/InsertAnswer.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        MyDbHelper myDbHelper=new MyDbHelper(context,MyDbHelper.DB_NAME,null,1);
                        myDbHelper.deleteTempAnswers();
                        Log.e("temp answers: " ,"submitted and deleted");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("temp saving error:",error.toString());
                    }
                }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<String,String>();

                String op=jsonArray.toString();
                Log.e("json array temp: ",op);
                params.put("value",op);

                return params;
            }

        };
        // MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
// add images**************************************

    public void fetchImagesFromServer(){

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String url=Main_page.IP_ADDRESS+"/MasterDataFetch.php";
        Log.e("image url",url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {

                        try {
                            MyDbHelper db = new MyDbHelper(context,MyDbHelper.DB_NAME,null,1);
                            db.deleteAllImages();

                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Log.e("tag","adding image");
                                Log.e("response",jsonObject.toString());
                                int id = jsonObject.getInt("id");
                                String part_name = jsonObject.getString("img_name");
                                String image = jsonObject.getString("img");
                                //String model=jsonObject.getString("model_name");
                                 //db.addImage(id, model_nm, image.getBytes());
                                db.addImage(part_name, "no model", Base64.decode(image.substring(23), Base64.DEFAULT));
                                //db.addImage( part_name,model, Base64.decode(image, Base64.DEFAULT));

                            }

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

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

    }


    public void insertTotalTime(Context context,String qr,String operator,String time){
        MySingleton mySingleton=MySingleton.getInstance(context);
        RequestQueue requestQueue= mySingleton.getRequestQueue();
       // String url= Main_page.IP_ADDRESS + "/InsertTotalTime.php";
        String url= Main_page.IP_ADDRESS + "/InsertTotalTime.php";
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("insertTotalTime: ",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error: ",qr+" "+operator+" "+time+"//"+error.toString());
            }
        }){
            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> hashMap=new HashMap<>();
                hashMap.put("qr",qr);
                hashMap.put("operator",operator);
                hashMap.put("total_time",time);
                return hashMap;
            }
        };
        requestQueue.add(stringRequest);
    }
//****************** Get app name****************************
    public void getAppName(Questions context, ArrayList<String> appNames){
        //ArrayList<String> appNames=appnames;
        p1=new ProgressDialog(context);
        p1.setMessage("Please wait...");
        p1.setIndeterminate(false);
        p1.setCancelable(false);
        Log.e("tag","showing progress dialog");
        p1.show();
        MySingleton m=MySingleton.getInstance(context);
        RequestQueue requestQueue= m.getRequestQueue();

        JsonArrayRequest jsonObjectRequest=new JsonArrayRequest(

                Request.Method.GET,
                Main_page.IP_ADDRESS + "/GetAppName.php",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            for(int i=0;i<response.length();i++) {
                                JSONObject obj = response.getJSONObject(i);
                                appNames.add(obj.getString("appname"));
                                Log.e("appname", obj.getString("appname"));
                            }

                        } catch (JSONException e) {
                            Log.e("getting appname",e.toString());
                            e.printStackTrace();
                        }
                        finally{
                            MyDbHelper myDbHelper = new MyDbHelper(context, MyDbHelper.DB_NAME, null, 1);
                            myDbHelper.addAppNames(appNames);
                            context.initparts(appNames);
                            p1.dismiss();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        p1.dismiss();
                        Toast.makeText(context, "Not connected to network", Toast.LENGTH_SHORT).show();
                        Log.e("json ERROR", error.toString());
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }
    public void getEmpInfo(String token){
        String tk="";
        ProgressDialog pg=new ProgressDialog(context);
        pg.setMessage("Please wait checking token...");
        pg.setTitle("Wait");
        pg.setIndeterminate(false);
        pg.setCancelable(false);
        pg.show();
        StringRequest stringRequest=new StringRequest(Request.Method.GET, Main_page.IP_ADDRESS+"/GetToken.php?token="+token, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                pg.dismiss();
                try {
                    JSONArray jsonArray=new JSONArray(response);

                        JSONObject jsonObject=  jsonArray.getJSONObject(0);
                        Log.e("emp token",jsonObject.getString("token"));
                        Log.e("emp name",jsonObject.getString("emp_name"));

                        if(token.equals(jsonObject.getString("token"))){
                            String empname=jsonObject.getString("emp_name");
                            SharedPreferences preferences=context.getSharedPreferences("userpref",MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString("user",empname);
                            editor.apply();
                            MyDbHelper myDbHelper=new MyDbHelper(context,MyDbHelper.DB_NAME,null,1);
                            myDbHelper.addEmployee(jsonObject.getInt("token"),jsonObject.getString("emp_name"));
                            Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(context, Main_page.class);
                            context.startActivity(i);
                        }
                        else{
                            Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                } catch (JSONException e) {
                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pg.dismiss();
                Log.e("emp name error",error.toString());
            }
        });
//        {
//            @Nullable
//            @org.jetbrains.annotations.Nullable
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                HashMap<String,String> hashMap=new HashMap<>();
//                hashMap.put("token",token);
//                return hashMap;
//            }
//        };
        MySingleton singleton=MySingleton.getInstance(context);
        singleton.addToRequestQue(stringRequest);
    }
    public void insertBatteryStatus(HashMap<String,String> batteryInfo){
        builder=new AlertDialog.Builder(context);
        StringRequest stringRequest=new StringRequest(Request.Method.POST,
                Main_page.IP_ADDRESS+"/InsertBatteryInfo.php",

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        builder.setTitle("Done");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        // builder.setMessage("message: "+response);
                        if(response.equals("success"))
                        builder.setMessage("Records submitted successfully");
                        else {
                           // builder.setMessage("Server problem:" + response);
                            Log.e("error in battery",response);
                        }
                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Connection problem", Toast.LENGTH_SHORT).show();
                        Log.e("submit answer",error.toString());
                        Toast.makeText(context, "Data is stored locally", Toast.LENGTH_SHORT).show();
                        myDbHelper=new MyDbHelper(context,MyDbHelper.DB_NAME,null,1 );
                            myDbHelper.batteryStatusTemp(batteryInfo);
                    }
                }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return batteryInfo;
            }

        };
        // MySingleton.getInstance(MainActivity.this).addToRequestQue(stringRequest);
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);

    }
    private static Bitmap byteArrayToBitmap(byte[] byteimg){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteimg, 0, byteimg.length);
        return bitmap;
    }
    public String bitmapToBase64(Bitmap bitmapImg){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImg.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;

    }
}


