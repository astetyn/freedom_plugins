package me.astetyne;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class Main extends Plugin {

    private static final String validKey = "klucik";

    @Override
    public void onEnable() {
        String path = "/vip_callbacks";
        int port = 4001;

        System.out.println("Callback HTTP listener server started on: " + path + " " + port);

        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext(path, new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {

            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());

            Map<String, String> map = queryToMap(t.getRequestURI().getQuery());

            // manual at: https://github.com/martingalovic/crew.sk-url
            String key = map.get("key");
            String action = map.get("action");
            String method = map.get("method");
            String price = map.get("price");
            String days = map.get("days");
            String nick = map.get("variables[nick]");
            String sms_text = map.get("sms_text");
            String code = map.get("code");

            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();

            if(!key.equals(validKey)) {
                return;
            }

            // U suffix is for payU methods
            String group = switch (code) {
                case "evip", "evipU" -> "extravip";
                case "evipka", "evipkaU" -> "extravipka";
                case "premium", "premiumU" -> "premium";
                case "premiumka", "premiumkaU" -> "premiumka";
                default -> null;
            };

            if(group == null) {
                return;
            }

            CommandSender console = ProxyServer.getInstance().getConsole();
            String command = "lpb user " + nick +" parent addtemp " + group + " " + days + "d";
            ProxyServer.getInstance().getPluginManager().dispatchCommand(console, command);
            ProxyServer.getInstance().broadcast("Hrac " + nick + " si kupil/a VIP, dakujeme za podporu servera!");
        }
    }

    public static Map<String, String> queryToMap(String query) {
        if(query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }
}
