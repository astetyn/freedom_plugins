import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class WoFAppGateway extends Plugin {

    ServerSocket server;
    String prefix = "[WofAppGateway] ";
    int port = 1414;
    boolean running = false;

    @Override
    public void onEnable() {

        running = true;

        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        new Thread(() -> {

            try {
                server = new ServerSocket(1414);

                getLogger().info(prefix + "Listening on port: " + port);

                while(running) {

                    Socket client = server.accept();

                    byte[] data = generateJSON().toString().getBytes(StandardCharsets.UTF_8);

                    DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                    dos.writeInt(data.length); // header
                    dos.write(data); // data
                    dos.close();
                    client.close();

                }

            }catch(IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private JSONObject generateJSON() {

        int playersCount = getProxy().getPlayers().size();
        ArrayList<String> names = new ArrayList<>(playersCount);
        for(ProxiedPlayer p : getProxy().getPlayers()) {
            names.add(p.getName());
        }

        JSONObject jo = new JSONObject();

        JSONObject playersMap = new JSONObject();
        playersMap.put("count: ", playersCount);
        playersMap.put("players", names);

        jo.put("online", playersMap);

        jo.put("news", getNews());

        return jo;
    }


    private ArrayList<String> getNews() {

        ArrayList<String> news = new ArrayList<>();

        File[] files = getDataFolder().listFiles();

        if(files == null) {
            return news;
        }

        Arrays.sort(files, Collections.reverseOrder());

        for(File f : files) {
            if(f.isDirectory() || !f.getName().endsWith(".txt")) {
                continue;
            }
            try {
                news.add(new String(Files.readAllBytes(Paths.get(f.getPath()))));
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
        return  news;
    }

    public void onDisable() {

        running = false;

        try {
            server.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

}
