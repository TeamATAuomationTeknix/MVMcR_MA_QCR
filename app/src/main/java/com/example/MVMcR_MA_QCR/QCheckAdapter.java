package com.example.MVMcR_MA_QCR;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class QCheckAdapter extends RecyclerView.Adapter<QCheckAdapter.VhQuestions>{

    private Context context;
    ArrayList<Questions_main> list;
    public QCheckAdapter(Context context, ArrayList<Questions_main> list) {
        this.list=list;
        this.context = context;
    }
    @NonNull
    @NotNull
    @Override
    public VhQuestions onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View v=inflater.inflate(R.layout.qc_check_layout,null);
        return new VhQuestions(v);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull VhQuestions holder, int position) {
        holder.question.setText(position+1+" "+list.get(position).getQuestion());
        String ans=list.get(position).getAnswer();
        holder.btnOk.setText(ans);
        if(ans.equals(Questions_main.OK))
            holder.btnOk.setBackgroundColor(context.getResources().getColor(R.color.color_ok));
        else
            holder.btnOk.setBackgroundColor(context.getResources().getColor(R.color.color_not_ok));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class VhQuestions extends RecyclerView.ViewHolder {
        TextView question;
        Button btnOk;
        public VhQuestions(@NonNull View itemView) {
            super(itemView);
            question=itemView.findViewById(R.id.qcCheckQuestion);
            btnOk=itemView.findViewById(R.id.qcCheckBtn);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getAdapterPosition();
                    // TODO: 15-07-2021 show remark
//                   AlertDialog.Builder builder=new AlertDialog.Builder(context);
//                   builder.setTitle("Remark");
//                   builder.setMessage(list.get(getAdapterPosition()).getRemark());
//                   AlertDialog alertDialog=builder.create();
//                   alertDialog.show();
                    if(list.get(position).getAnswer().equals(Questions_main.NOT_OK)&& list.get(position).getRemarkImage()!=null){
                        AlertDialog.Builder builder=new AlertDialog.Builder(context);
                        //builder.setTitle("Image For Referance");
                        byte[] arr=list.get(position).getRemarkImage();
                        ImageView imageView=new ImageView(context);
                        imageView.setImageBitmap(byteArrayToBitmap(arr));
                        imageView.setPadding(0,15,0,20);
                        builder.setView(imageView);
                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();
                    }
                }
            });

        }
    }
    private static Bitmap byteArrayToBitmap(byte[] byteimg){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteimg, 0, byteimg.length);
        return bitmap;
    }


}