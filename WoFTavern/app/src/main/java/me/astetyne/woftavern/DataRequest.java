package me.astetyne.woftavern;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class DataRequest implements Runnable {

    String address = "mc9.crew.sk";
    int port = 1414;
    String[] players;
    ArrayList<NewsUnit> news;
    ArrayList<Drawable> playerImages;
    boolean success = false;
    private Activity activity;
    private DataPullFinishable dataPullFinishable;

    static NewsUnit errorNewsUnit = new NewsUnit(0, "error", "sprava je zle formatovana", "---");

    void start(final Activity activity, final DataPullFinishable dataPullFinishable) {

        this.activity = activity;
        this.dataPullFinishable = dataPullFinishable;
        success = false;

        new Thread(this).start();
    }

    @Override
    public void run() {

        try {
            pullData();
        }catch(IOException | JSONException e) {
            success = false;
        }

        final DataRequest dr = this;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataPullFinishable.onFinish(dr);
            }
        });
    }

    void pullData() throws IOException, JSONException {

        Socket socket = new Socket(address, port);
        socket.setSoTimeout(10000); // 10 sec timeout

        DataInputStream dis = new DataInputStream(socket.getInputStream());

        int dataLen = dis.readInt(); // header
        byte[] data = new byte[dataLen];

        int alreadyRead = 0;

        while(alreadyRead < dataLen) {

            int read = dis.read(data, alreadyRead, dataLen - alreadyRead);

            if(read == -1) {
                break;
            }

            alreadyRead += read;
        }
        dis.close();
        socket.close();

        JSONObject JO = new JSONObject(new String(data, StandardCharsets.UTF_8));
        JSONObject playersJO = (JSONObject) JO.get("online");

        JSONArray jsa = (JSONArray) playersJO.get("players");
        players = new String[jsa.length()];
        for(int i = 0; i < jsa.length(); i++) {
            players[i] = jsa.getString(i);
        }

        playerImages = new ArrayList<>(jsa.length());
        for(String p : players) {
            playerImages.add(loadImageFromWeb("https://mc-heads.net/avatar/"+p));
        }

        JSONArray newsJSA = (JSONArray) JO.get("news");
        news = new ArrayList<>(newsJSA.length());

        for(int i = 0; i < newsJSA.length(); i++) {
            news.add(parseNewsString(newsJSA.getString(i)));
        }

        success = true;

    }

    Drawable loadImageFromWeb(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            return Drawable.createFromStream(is, "src name");
        } catch (Exception e) {
            return null;
        }
    }

    NewsUnit parseNewsString(String s) {

        String[] parts = s.split("##");

        if(parts.length != 4) {
            return errorNewsUnit;
        }

        int thumbnail = 0;

        try {
            thumbnail = Integer.parseInt(parts[0]);
        }catch(NumberFormatException ignored) {}

        return new NewsUnit(thumbnail, parts[1], parts[2], parts[3]);
    }
}
