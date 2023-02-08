package me.astetyne.woftavern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class PlayerListViewAdapter extends BaseAdapter {

    LayoutInflater inflater;
    String[] names;
    ArrayList<Drawable> images;

    PlayerListViewAdapter(Context context, String[] names, ArrayList<Drawable> images) {
        inflater = LayoutInflater.from(context);
        this.names = names;
        this.images = images;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup container) {

        if(view == null) {
            view = inflater.inflate(R.layout.player_card_design, container, false);
        }

        CardView cardView = view.findViewById(R.id.pcd_cv);
        TextView textView = cardView.findViewById(R.id.pcd_tv);
        textView.setText(names[position]);

        ImageView iv = cardView.findViewById(R.id.pcd_iv);
        iv.setImageDrawable(images.get(position));

        return view;
    }
}
