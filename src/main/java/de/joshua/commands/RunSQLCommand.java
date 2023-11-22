package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.util.database.DataBaseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunSQLCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public RunSQLCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!Arrays.asList(ShopPlugin.SQL_UUIDS).contains(player.getUniqueId())){
            ShopPlugin.sendMessage(Component.text(ShopPlugin.getConfigString("shop.error.noPermission")), player);
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.contains("https://gist.github.com")) {
                try {
                    args[i] = getContent(arg);
                } catch (IOException e) {
                    player.sendMessage(Component.text("Error while getting gist:" + e));
                }
            }
        }

        CompletableFuture<ResultSet> future = shopPlugin.getSQLQueue().enqueueOperation(String.join(" ", args));

        future.thenAcceptAsync(resultSet -> {
            List<String> rows = new ArrayList<>();

            try {
                if (resultSet == null)
                    rows.add("SUCCESS");
                else {
                    ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                    final int columnCount = resultSetMetaData.getColumnCount();

                    while (resultSet.next()) {
                        try {
                            StringBuilder row = new StringBuilder();
                            //Object[] values = new Object[columnCount];
                            for (int i = 1; i <= columnCount; i++) {
                                if (i > 1) row.append(",  ");
                                String columnValue = resultSet.getString(i);
                                row
                                        .append(resultSetMetaData.getColumnName(i))
                                        .append(": ")
                                        .append("<click:copy_to_clipboard:").append(columnValue).append(">")
                                        .append(columnValue)
                                        .append("</click>");
                                //values[i - 1] = resultSet.getObject(i);
                            }


                            rows.add(row.toString());
                        } catch (Exception e) {
                            ShopPlugin.sendMessage(Component.text("WARN: " + e.getCause()), player);
                        }
                    }
                }
            } catch (Exception e) {
                ShopPlugin.sendMessage(Component.text("ERROR: " + e.getCause()), player);
            }

            for (String row : rows) {
                ShopPlugin.sendMessage(MiniMessage.miniMessage().deserialize(row), player);
            }
        });
        return true;
    }

    private static String getContent(String arg) throws IOException {
        String id = arg.split("/")[arg.split("/").length - 1];
        URL url = new URL("https://api.github.com/gists/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        Matcher matcher = Pattern.compile("\"content\":\"(.*?)\"").matcher(content.toString());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
