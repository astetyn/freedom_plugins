package me.astetyne;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main extends JavaPlugin implements Listener {

    Map<String, String> sharedAccounts = new HashMap<>();
    Map<String, BigDecimal> lastBalance = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getDataFolder().mkdirs();
        loadAccounts();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        loadAccounts();
        return true;
    }

    void loadAccounts() {
        getLogger().info("Loading shared eco accounts.");
        sharedAccounts.clear();

        File f = new File(getDataFolder(), "shared_accounts.txt");
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(!f.exists()) {
            getLogger().info("Shared account folder does not exists.");
            return;
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
            while(in.ready()) {
                String[] parts = in.readLine().split(" ");
                sharedAccounts.put(parts[0], parts[1]);
                sharedAccounts.put(parts[1], parts[0]);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onBalanceChange(UserBalanceUpdateEvent event) {

        Player p = event.getPlayer();

        String pairedPlayer = sharedAccounts.get(p.getName());

        if(pairedPlayer == null) {
            return;
        }

        lastBalance.put(p.getName(), event.getNewBalance());

        try {
            BigDecimal balance = lastBalance.get(pairedPlayer);

            if(balance != null && event.getNewBalance().equals(balance)) {
                return;
            }

            Economy.setMoney(pairedPlayer, event.getNewBalance());

        } catch (NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e) {
            getLogger().info("Problem with shared account balance change.");
        }
    }
}
