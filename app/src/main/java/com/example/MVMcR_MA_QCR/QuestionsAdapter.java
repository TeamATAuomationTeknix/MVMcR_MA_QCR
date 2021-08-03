package com.example.MVMcR_MA_QCR;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;
import com.github.dhaval2404.imagepicker.ImagePicker;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.VHQuestions> {

        ArrayList<Questions_main> questionsList;
        int id=1;
        Context context;

    public QuestionsAdapter(ArrayList<Questions_main> questionsList, Context context) {
            this.questionsList = questionsList;
            this.context=context;
            Log.e("tag","adapter initialized");
            id=1;
            }

    @NonNull
    @Override
    public VHQuestions onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater inflater=LayoutInflater.from(parent.getContext());
            LinearLayout layout=(LinearLayout) inflater.inflate(R.layout.questions,null);
            Log.e("tag","on create view holder");
            return new VHQuestions(layout);
            }
    @Override
    public void onBindViewHolder(@NonNull VHQuestions holder, int position) {
            holder.qNo.setText((position+1) +". ");
            id++;
            if(questionsList.get(position).highlight.equals("EXTERNAL")){
            holder.question.setBackgroundColor(holder.question.getResources().getColor(R.color.highlighit));
            }
        if(questionsList.get(position).highlight.equals("INTERNAL")){
            holder.question.setBackgroundColor(holder.question.getResources().getColor(R.color.BlueColor));
        }
            if(questionsList.get(position).getAnswer()==null) {
                holder.increase=true;
            holder.btnOk.setBackgroundColor(holder.btnOk.getResources().getColor(R.color.white));
            holder.btnNotOk.setBackgroundColor(holder.btnNotOk.getResources().getColor(R.color.white));
            }else{
            switch (questionsList.get(position).getAnswer()){
            case Questions_main.OK:
            holder.btnOk.setBackgroundColor(holder.btnOk.getResources().getColor(R.color.color_ok));
            holder.btnNotOk.setBackgroundColor(holder.btnNotOk.getResources().getColor(R.color.white));
            break;
            case Questions_main.NOT_OK:
            holder.btnNotOk.setBackgroundColor(holder.btnNotOk.getResources().getColor(R.color.color_not_ok));
            holder.btnOk.setBackgroundColor(holder.btnOk.getResources().getColor(R.color.white));
            }
            }

            holder.question.setText(questionsList.get(position).getQuestion());
            Log.e("tag","On bind view");
            }

            @Override
            public int getItemCount() {
                    return questionsList.size();
            }

        class VHQuestions extends RecyclerView.ViewHolder {
            TextView qNo,question;
            Button btnOk,btnNotOk;
            boolean increase=true;
            public VHQuestions(@NonNull View itemView) {
                super(itemView);

                qNo=itemView.findViewById(R.id.txtQNo);
                question=itemView.findViewById(R.id.txtQuestion);
                btnOk=itemView.findViewById(R.id.btnOk);
                btnNotOk=itemView.findViewById(R.id.btnNotOk);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        questionsList.get(getAdapterPosition()).setAnswer(Questions_main.OK);
                        btnOk.setBackgroundColor(v.getResources().getColor(R.color.color_ok));
                        btnNotOk.setBackgroundColor(v.getResources().getColor(R.color.white));
                        if(increase){
                            increase=false;
                            Questions.count=Questions.count+Questions.partcount;
                            Questions.progressBar.setProgress((int) Questions.count);
                        }
                    }
                });

                btnNotOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Questions.notOKPosition=getAdapterPosition();
                        int qid=questionsList.get(getAdapterPosition()).getId();

                        questionsList.get(getAdapterPosition()).setAnswer(Questions_main.NOT_OK);
                        btnNotOk.setBackgroundColor(v.getResources().getColor(R.color.color_not_ok));
                        btnOk.setBackgroundColor(v.getResources().getColor(R.color.white));
                        if(increase){
                            increase=false;
                            Questions.count=Questions.count+Questions.partcount;
                            Questions.progressBar.setProgress((int) Questions.count);
                        }
                        ImagePicker.with((Activity) context).maxResultSize(512,512).cameraOnly()
                                .start(103);
                    }
                });
        }
    }

}
