package com.example.MVMcR_MA_QCR.Fragments;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.example.MVMcR_MA_QCR.MyDbHelper;
import com.example.MVMcR_MA_QCR.Questions;
import com.example.MVMcR_MA_QCR.Questions_main;
import com.example.MVMcR_MA_QCR.QuestionsAdapter;
import com.example.MVMcR_MA_QCR.R;
import com.example.MVMcR_MA_QCR.ServerJson;

import static android.content.Context.MODE_MULTI_PROCESS;


public class PartFragment extends Fragment {
    //changes made from ganesh r
    ArrayList<Questions_main> list;
    TextView fragmentName;
    RecyclerView recyclerView;
    ImageView masterImage;
    String partname="";
    URL ImageUrl=null;
    InputStream is=null;
    Bitmap bmImg=null;
    ProgressDialog p=null;
    MyDbHelper myDbHelper;
    EditText qr;
    String model_name="";
    ArrayList<String> pnames;
    //    public PartFragment(ArrayList<Questions_main> list, ArrayList<String> pnames) {
//        // Required empty public constructorthis
//        this.list=list;
//        this.pnames=pnames;
//
//
//    }
    public PartFragment (ArrayList<Questions_main> list,String partname,String model){
        this.list=list;
        this.partname=partname;
        model_name=model;
    }

    public ArrayList<Questions_main> getList() {
        return list;
    }

    public void setList(ArrayList<Questions_main> list) {
        this.list = list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment=inflater.inflate(R.layout.fragment_part, container, false);

        masterImage =fragment.findViewById(R.id.masterImage);
        fragmentName=fragment.findViewById(R.id.partName);
        SharedPreferences preferences=container.getContext().getSharedPreferences("appnameselection",MODE_MULTI_PROCESS);
        String appname=preferences.getString("appname","");
        fragmentName.setText(appname+"-"+partname);
        //ImageTask task=new ImageTask();
        // task.execute("https://img.etb2bimg.com/imgv2/width-368,height-311,resize_mode-1/retail_files/sumo-1503999478-prod-var.jpg");
        myDbHelper=new MyDbHelper(getContext(),MyDbHelper.DB_NAME,null,1);

        Cursor c1=myDbHelper.getAllImagesOfSpecificModel("no model",partname);
        if(c1.moveToFirst()){
            byte[] arrImg= c1.getBlob(1);
            // is= arrImg;
            bmImg=BitmapFactory.decodeByteArray(arrImg,0,arrImg.length);
            masterImage.setImageBitmap(bmImg);
        }
        else{
            Toast.makeText(getContext(), "Please add image for: "+partname, Toast.LENGTH_SHORT).show();
        }
        recyclerView=fragment.findViewById(R.id.recycle_questions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Check local questions**********************

        MyDbHelper myDbHelper=new MyDbHelper(getContext(),MyDbHelper.DB_NAME,null,1);
        if(list.size()==0)
        list=myDbHelper.getQuestions(partname);
        ServerJson serverJson;

        //************get questions from server
        if(list.size()==0) {
            serverJson = new ServerJson(getContext(), list, recyclerView, partname);
            Log.e("tag", "execute volley request");
            serverJson.volleyRequest();
        }
        if(list.size()>0)
        Questions.partcount=Questions.devidedparts/list.size();
        QuestionsAdapter adapter=new QuestionsAdapter(list,getContext());
        recyclerView.setAdapter(adapter);
        return fragment;
    }
    private class ImageTask extends AsyncTask<String,String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p=new ProgressDialog(getContext());
            p.setMessage("please wait.... image is downloading");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                ImageUrl=new URL(strings[0]);
                HttpURLConnection conn=(HttpURLConnection) ImageUrl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is= conn.getInputStream();
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inPreferredConfig=Bitmap.Config.RGB_565;
                bmImg=BitmapFactory.decodeStream(is,null,options);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmImg;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(masterImage!=null){
                p.hide();
                masterImage.setImageBitmap(bitmap);
            }
            else{
                p.show();
            }
        }
    }

}