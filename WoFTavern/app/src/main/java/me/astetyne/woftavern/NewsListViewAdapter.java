package me.astetyne.woftavern;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class NewsListViewAdapter extends BaseAdapter {

    LayoutInflater inflater;
    ArrayList<NewsUnit> news;

    NewsListViewAdapter(Context context, ArrayList<NewsUnit> news) {
        inflater = LayoutInflater.from(context);
        this.news = news;
    }

    @Override
    public int getCount() {
        return news.size();
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
            view = inflater.inflate(R.layout.news_card_design, container, false);
        }

        NewsUnit nu = news.get(position);

        CardView cardView = view.findViewById(R.id.ncd_cv);

        ImageView thumbnailIV = cardView.findViewById(R.id.news_thumbnail_iv);

        switch(nu.thumbnail) {
            case INFORMATIVE: thumbnailIV.setImageResource(R.drawable.informative_icon); break;
            case WARNING: thumbnailIV.setImageResource(R.drawable.warning_icon); break;
        }

        TextView subjectTV = cardView.findViewById(R.id.ncd_subject_tv);
        subjectTV.setText(nu.subject);

        TextView textTV = cardView.findViewById(R.id.ncd_text_tv);
        textTV.setText(nu.text);

        TextView authorTV = cardView.findViewById(R.id.ncd_author_tv);
        authorTV.setText(nu.footer);

        return view;

    }
}