package me.astetyne.woftavern;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MenuActivity extends AppCompatActivity implements DataPullFinishable {

    DataRequest dataRequest;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu_layout);
    }

    @Override
    public void onResume(){
        super.onResume();
        dataRequest = new DataRequest();
        dataRequest.start(this, this);
    }

    @Override
    public void onFinish(DataRequest dr) {

        TextView loading = findViewById(R.id.ml_loading_textview);
        Button playersButton = findViewById(R.id.ml_players_button);
        ListView lv = findViewById(R.id.menu_news_list);

        if(!dr.success) {
            loading.setText("Chyba v pripojení.");
            playersButton.setText("---");
            lv.setVisibility(View.INVISIBLE);
            return;
        }

        loading.setVisibility(View.INVISIBLE);

        playersButton.setText(getOnlinePlayersFormatted());
        lv.setVisibility(View.VISIBLE);
        NewsListViewAdapter adapter = new NewsListViewAdapter(this, dr.news);
        lv.setAdapter(adapter);

    }

    public void onPlayersClick(View v) {

        if(!dataRequest.success) {
            toastShow("Údaje neboli načítané");
            return;
        }

        showPopUpPlayers();
    }

    public void onRefresh(View v) {

        TextView loading = findViewById(R.id.ml_loading_textview);
        loading.setText("Načítavam údaje...");
        loading.setVisibility(View.VISIBLE);
        dataRequest.start(this, this);
    }

    private void showPopUpPlayers() {

        final View parentRoot = findViewById(R.id.menu_cl);

        View popupView = LayoutInflater.from(this).inflate(R.layout.players_layout, null);

        final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        popupWindow.setElevation(5.0f);
        popupWindow.showAtLocation(parentRoot, Gravity.CENTER, 0, 0);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });

        ListView lv = popupView.findViewById(R.id.players_list);
        PlayerListViewAdapter adapter = new PlayerListViewAdapter(this, dataRequest.players, dataRequest.playerImages);
        lv.setAdapter(adapter);

        TextView tv = popupView.findViewById(R.id.pl_online_players_tv);
        tv.setText(getOnlinePlayersFormatted());

        dimBehind(popupWindow);
    }

    public void dimBehind(PopupWindow popupWindow) {
        View container = (View) popupWindow.getContentView().getParent();
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.7f;
        wm.updateViewLayout(container, p);
    }

    String getOnlinePlayersFormatted() {
        return "Online hráči: " + dataRequest.players.length + "/50";
    }

    public void toastShow(String text) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }
}