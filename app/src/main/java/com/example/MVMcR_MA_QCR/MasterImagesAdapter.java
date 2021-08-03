package com.example.MVMcR_MA_QCR;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MasterImagesAdapter extends RecyclerView.Adapter<MasterImagesAdapter.ViewHolder>{

    private Context context;
    int row_index = -1;

    ArrayList<Bitmap> listImages;
    ArrayList<String> listModel;
    ArrayList<Integer> listClipNo;

    public MasterImagesAdapter(Context context, ArrayList<Bitmap> listImages, ArrayList<String> listModel, ArrayList<Integer> listClipNo) {

        this.context = context;
        this.listImages = listImages;
        this.listModel = listModel;
        this.listClipNo = listClipNo;

    }

    @Override
    public MasterImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_master_images_item, parent, false);
        return new MasterImagesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MasterImagesAdapter.ViewHolder holder, int position) {

        holder.imgPreview.setImageBitmap(listImages.get(position));
        //holder.imgPreview.setRotation(90);
        holder.txtModel.setText("Part Number : "+listModel.get(position));
//        holder.txtClipNo.setText("Hose Clip : "+String.valueOf(listClipNo.get(position)));

        //holder.txtClipNo.setText(listEnd.get(position));
        //holder.linearLayout.setBackgroundResource(0);


        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {

                if (isLongClick) {
                    // Handle Long Click
                    row_index=position;
                    notifyDataSetChanged();


                }
                else {
                    // Handle Single Click
                    //Toast.makeText(context,"Long Press for Edit",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(row_index==position){
            //holder.linearLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border));
            //ImageRegistration.remove.setVisibility(View.VISIBLE);
        }
        else
        {
            //holder.linearLayout.setBackgroundResource(0);
        }

    }

    @Override
    public int getItemCount() {
        return listImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public ImageView imgPreview;
        public TextView txtModel, txtClipNo;

        private ItemClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);

            imgPreview = itemView.findViewById(R.id.img_master_imgs);
            txtModel = itemView.findViewById(R.id.txt_model);
            txtClipNo = itemView.findViewById(R.id.txt_clip_no);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getPosition(), true);
            return true;
        }

    }



}
