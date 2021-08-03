package com.example.MVMcR_MA_QCR;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageRegistrationAdapter extends RecyclerView.Adapter<ImageRegistrationAdapter.ViewHolder>{

    private Context context;
    int row_index = -1;

    ArrayList<Integer> listId;
    ArrayList<Bitmap> listImages;

    public ImageRegistrationAdapter(Context context, ArrayList<Integer> listId, ArrayList<Bitmap> listImages) {

        this.context = context;
        this.listId = listId;
        this.listImages=listImages;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_image_registration_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        //holder.txtSrNo.setText(String.valueOf(listSrNo.get(position)));
        holder.txtSrNo.setText("Part Number - "+(position+1));
        holder.imgItem.setImageBitmap(listImages.get(position));
        //holder.imgItem.setRotation(90);
        //holder.linearLayout.setBackgroundResource(0);


        holder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {

                if (isLongClick) {
                    // Handle Long Click
                    row_index=position;
                    notifyDataSetChanged();

                    ImageRegistration.id = listId.get(position);
                    ImageRegistration.imagePreview.setVisibility(View.VISIBLE);
                    ImageRegistration.imagePreview.setImageBitmap(listImages.get(position));
                    //ImageRegistration.imagePreview.setRotation(90);

                    ImageRegistration.recamera.setVisibility(View.VISIBLE);
                    ImageRegistration.add.setVisibility(View.INVISIBLE);
                    ImageRegistration.update.setVisibility(View.INVISIBLE);
                    ImageRegistration.remove.setVisibility(View.VISIBLE);

                }
                else {
                    // Handle Single Click
                    holder.linearLayout.setBackgroundResource(0);
                    //ImageRegistration.remove.setVisibility(View.INVISIBLE);
                    ImageRegistration.clearImage();
                    Toast.makeText(context,"Long Press for Edit",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(row_index==position){
            holder.linearLayout.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.border));
            //ImageRegistration.remove.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.linearLayout.setBackgroundResource(0);
        }

    }

    @Override
    public int getItemCount() {
        return listImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public TextView txtSrNo;
        public ImageView imgItem;
        public LinearLayout linearLayout;

        private ItemClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);

            txtSrNo = itemView.findViewById(R.id.txt_srno);
            imgItem = itemView.findViewById(R.id.img_item);
            linearLayout = itemView.findViewById(R.id.linear_item);

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
