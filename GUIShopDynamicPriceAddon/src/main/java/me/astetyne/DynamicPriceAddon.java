package me.astetyne;

import com.pablo67340.guishop.api.DynamicPriceProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DynamicPriceAddon extends JavaPlugin implements DynamicPriceProvider {

    final int maxStock = 10000;         // max items that can be stored in stock
    final double slope = 100;           // just slope of the function
    final double ratio = 0.5;           // ratio between buy/sell price

    final String fileName = "stocks.dat";
    final Map<String, Integer> map = new HashMap<>();

    @Override
    public void onLoad() {
        getServer().getServicesManager().register(DynamicPriceProvider.class, this, this, ServicePriority.Highest);
    }

    @Override
    public void onEnable() {
        loadStocks();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::saveStocks, 0L, 20L * 60); // every minute
    }

    public void onDisable() {
        saveStocks();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getLogger().info("[DynamicPricingAddon] Reloading started.");
        sender.sendMessage("Reloading started. Cached changes are lost!");
        loadStocks();
        Bukkit.getLogger().info("[DynamicPricingAddon] Reloading completed. Transactions from "+fileName+" were loaded.");
        sender.sendMessage("Reloading completed. Transactions from "+fileName+" were loaded.");
        return true;
    }

    public void loadStocks() {
        map.clear();
        File f = new File(getDataFolder(), fileName);
        if(!f.exists()) return;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            while(in.ready()) {
                String[] parts = in.readLine().split(" ");
                map.put(parts[0], Integer.parseInt(parts[1]));
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void saveStocks() {
        getDataFolder().mkdirs();
        File f = new File(getDataFolder(), fileName);
        try {
            f.createNewFile();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8));
            for(Map.Entry<String, Integer> entry : map.entrySet()) {
                out.write(entry.getKey() + ' ' + entry.getValue() + '\n');
            }
            out.flush();
            out.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BigDecimal calculateBuyPrice(String item, int quantity, BigDecimal staticBuyPrice, BigDecimal staticSellPrice) {
        double price = 0;
        int originalStock = map.getOrDefault(item, 0);
        for(int i = 0; i < quantity; i++) {
            price += fBuy(originalStock - i, staticSellPrice.doubleValue(), staticBuyPrice.doubleValue());
        }
        return BigDecimal.valueOf(((int) (price * 100)) / 100d);
    }

    @Override
    public BigDecimal calculateSellPrice(String item, int quantity, BigDecimal staticBuyPrice, BigDecimal staticSellPrice) {
        double price = 0;
        int stock = map.getOrDefault(item, 0);
        for(int i = 0; i < quantity; i++) {
            price += fSell(stock + i, staticSellPrice.doubleValue(), staticBuyPrice.doubleValue());
        }
        return BigDecimal.valueOf(((int) (price * 100)) / 100d);
    }

    private double fBuy(int x, double multiply, double base) {
        if(x < 0) {
            x = 0;
        }
        return base * (multiply * maxStock + (slope - multiply + 1) * x) / (maxStock + slope * x);
    }

    private double fSell(int x, double multiply, double base) {
        if(x > maxStock) {
            x = maxStock;
        }
        return ratio * ((maxStock - (double)x) / maxStock) * base *
                (multiply * maxStock + (slope - multiply + 1) * x) / (maxStock + slope * x);
    }

    @Override
    public void buyItem(String item, int amount) {
        int stock = map.getOrDefault(item, 0);
        map.put(item, Math.max(stock - amount, 0));
    }

    @Override
    public void sellItem(String item, int amount) {
        int stock = map.getOrDefault(item, 0);
        map.put(item, Math.min(stock + amount, maxStock));
    }
}
