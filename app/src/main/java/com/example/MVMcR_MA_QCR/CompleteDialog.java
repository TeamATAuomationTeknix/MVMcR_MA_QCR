package com.example.MVMcR_MA_QCR;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CompleteDialog extends Dialog {
    Button btnCoutinue,btnBack;
    Activity context;

    public CompleteDialog(@NonNull Activity context) {

        super(context);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complete_dialog);
        btnCoutinue=findViewById(R.id.btn_dialog_continue);
        btnCoutinue.setOnClickListener(new OnContinueListner());
        btnBack=findViewById(R.id.btn_dialog_back);
        btnBack.setOnClickListener(new OnBackListner());
    }

   class OnContinueListner implements View.OnClickListener{
       @Override
       public void onClick(View v) {
           Main_page.partEnabled=false;
           ArrayList<String> pnames=new ArrayList<>();
           MyDbHelper dbHelper=new MyDbHelper(context,MyDbHelper.DB_NAME,null,1);
           pnames= dbHelper.getPartnames();
           String part=pnames.get(0);
           Intent i=new Intent(context,Questions.class);
           i.putExtra("partname",part);
           context.finish();
           context.startActivity(i);
       }
   }
    class OnBackListner implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent i=new Intent(context,Main_page.class);
            Main_page.partEnabled=true;
            context.finish();
            context.startActivity(i);
        }
    }
}
