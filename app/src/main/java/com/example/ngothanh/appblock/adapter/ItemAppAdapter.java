package com.example.ngothanh.appblock.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ngothanh.appblock.R;
import com.example.ngothanh.appblock.model.ItemApp;

import java.util.List;

/**
 * Created by ngoth on 2/16/2018.
 */

public class ItemAppAdapter extends RecyclerView.Adapter<ItemAppAdapter.ViewHolder> {
    private List<ItemApp> itemApps;
    private LayoutInflater inflater;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private  OnItemLongClickListener onItemLongClickListener;

    public ItemAppAdapter(Context context, List<ItemApp> apps) {
        this.context = context;
        this.itemApps = apps;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.icon_app_in_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ItemApp iconApp= itemApps.get(position);
        holder.txtAppName.setText(iconApp.getTxtAppName());
        BitmapDrawable drawableIcon = (BitmapDrawable) iconApp.getImgIconApp().getDrawable();
        Bitmap bitmapIcon = drawableIcon.getBitmap();
        holder.imgIconApp.setImageBitmap(bitmapIcon);

        BitmapDrawable drawableStatus = (BitmapDrawable) iconApp.getImgStatus().getDrawable();
        Bitmap bitmapStatus= drawableStatus.getBitmap();
        holder.imgStatus.setImageBitmap(bitmapStatus);

        holder.imgStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onImageStatusClicked(holder.getAdapterPosition(), iconApp.getPackageName());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClicked(holder.itemView, holder.getAdapterPosition(), iconApp.getPackageName());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    Log.d("Checkadapter", "onLongClick: " + holder.itemView + " " + holder.getAdapterPosition() + " " + iconApp.getPackageName());
                    onItemLongClickListener.onItemLongClick(holder.itemView, holder.getAdapterPosition(), iconApp.getPackageName());
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemApps.size();
    }

    public ItemApp getIconApp(int position){
        return itemApps.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgIconApp;
        private TextView txtAppName;
        private ImageView imgStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            txtAppName = itemView.findViewById(R.id.txt_app_name);
            imgIconApp= itemView.findViewById(R.id.img_icon_app);
            imgStatus= itemView.findViewById(R.id.img_status);
        }
    }

    public void remoItem(int position){
        itemApps.remove(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener  onItemLongClickListener){
        this.onItemLongClickListener= onItemLongClickListener;
    }

    public interface  OnItemLongClickListener{
        void onItemLongClick( View itemView, int position, String packageName);
    }

    public interface OnItemClickListener {
        void onItemClicked(View itemView, int position,String packageName);

        void onImageStatusClicked(int position, String packageName);
    }
}
