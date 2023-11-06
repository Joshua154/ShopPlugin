package de.joshua.commands;

import de.joshua.ShopPlugin;
import de.joshua.util.DataBaseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class RunSQLCommand implements CommandExecutor {
    ShopPlugin shopPlugin;

    public RunSQLCommand(ShopPlugin shopPlugin) {
        this.shopPlugin = shopPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!player.getUniqueId().equals(ShopPlugin.JOSHUA_UUID)) {
            ShopPlugin.sendMessage(Component.text("You are not allowed to use this command"), player);
            return false;
        }
        Pair<ResultSet, PreparedStatement> result = DataBaseUtil.executeQuery(shopPlugin.getDatabaseConnection(), String.join(" ", args));
        ResultSet resultSet = result.getLeft();
        PreparedStatement preparedStatement = result.getRight();

        List<String> rows = new ArrayList<>();

        try {
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
            if (preparedStatement != null)
                preparedStatement.close();
            else
                rows.add("SUCCESS");
        } catch (Exception e) {
            ShopPlugin.sendMessage(Component.text("ERROR: " + e.getCause()), player);
        }

        for (String row : rows) {
            ShopPlugin.sendMessage(MiniMessage.miniMessage().deserialize(row), player);
        }

        return true;
    }
}
