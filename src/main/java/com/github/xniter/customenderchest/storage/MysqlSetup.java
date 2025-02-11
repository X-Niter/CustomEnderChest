package com.github.xniter.customenderchest.storage;

import com.github.xniter.customenderchest.CustomEnderChest;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.Properties;

public class MysqlSetup {

    private Connection conn = null;
    private final CustomEnderChest enderchest;
    private boolean tablesChecked = false;

    public MysqlSetup(CustomEnderChest enderchest) {
        this.enderchest = enderchest;
        setupDatabase();
        updateTables();
    }

    public void setupDatabase() {
        connectToDatabase();
        databaseMaintenanceTask();
    }

    private void tableMaintenance(long inactiveTime, Connection conn, String tableName) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "DELETE FROM `" + tableName + "` WHERE `last_seen` < ?";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, String.valueOf(inactiveTime));
            preparedStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void databaseMaintenanceTask() {
        if (enderchest.getConfigHandler().getBoolean("database.mysql.removeOldUsers.enabled")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(enderchest, new Runnable() {

                @Override
                public void run() {
                    if (conn != null) {
                        long inactivityDays = Long.parseLong(enderchest.getConfigHandler().getString("database.mysql.removeOldUsers.inactive"));
                        long inactivityMils = inactivityDays * 24 * 60 * 60 * 1000;
                        long curentTime = System.currentTimeMillis();
                        long inactiveTime = curentTime - inactivityMils;
                        CustomEnderChest.log.info("Database maintenance task started...");
                        tableMaintenance(inactiveTime, getConnection(), enderchest.getConfigHandler().getString("database.mysql.tableName"));
                        CustomEnderChest.log.info("Database maintenance complete!");
                    }
                }

            }, 100 * 20L);
        }
    }

    private void connectToDatabase() {
        try {
            //Load Drivers
            Class.forName("com.mysql.jdbc.Driver");

            Properties properties = new Properties();
            properties.setProperty("user", enderchest.getConfigHandler().getString("database.mysql.user"));
            properties.setProperty("password", enderchest.getConfigHandler().getString("database.mysql.password"));
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", enderchest.getConfigHandler().getString("database.mysql.ssl"));
            properties.setProperty("requireSSL", enderchest.getConfigHandler().getString("database.mysql.ssl"));

            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + enderchest.getConfigHandler().getString("database.mysql.host") + ":" + enderchest.getConfigHandler().getString("database.mysql.port") + "/" + enderchest.getConfigHandler().getString("database.mysql.databaseName"), properties);
            CustomEnderChest.log.info("Database connection established!");
            if (!tablesChecked) {
                setupTables();
            }
        } catch (ClassNotFoundException e) {
            CustomEnderChest.log.severe("Could not locate drivers for mysql! Error: " + e.getMessage());
        } catch (SQLException e) {
            CustomEnderChest.log.severe("Could not connect to mysql database! Error: " + e.getMessage());
        } catch (Exception ex) {
            CustomEnderChest.log.severe("Could not connect to mysql database! Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setupTables() {
        if (conn != null) {
            PreparedStatement query1 = null;
            try {
                String data = "CREATE TABLE IF NOT EXISTS `" + enderchest.getConfigHandler().getString("database.mysql.tableName") + "` (id int(10) AUTO_INCREMENT, player_uuid varchar(50) NOT NULL UNIQUE, player_name varchar(50) NOT NULL, enderchest_data LONGTEXT NOT NULL, size int(3) NOT NULL, last_seen varchar(30) NOT NULL, PRIMARY KEY(id));";
                query1 = conn.prepareStatement(data);
                query1.execute();
                tablesChecked = true;
            } catch (Exception e) {
                CustomEnderChest.log.severe("Error creating tables! Error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (query1 != null) {
                        query1.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Connection getConnection() {
        checkConnection();
        return conn;
    }

    public void closeConnection() {
        try {
            CustomEnderChest.log.info("Closing database connection...");
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkConnection() {
        try {
            if (conn == null) {
                CustomEnderChest.log.warning("Database connection failed. Reconnecting...");
                conn = null;
                connectToDatabase();
            } else if (!conn.isValid(3)) {
                CustomEnderChest.log.warning("Database connection failed. Reconnecting...");
                conn = null;
                connectToDatabase();
            } else if (conn.isClosed()) {
                CustomEnderChest.log.warning("Database connection failed. Reconnecting...");
                conn = null;
                connectToDatabase();
            }
        } catch (Exception e) {
            CustomEnderChest.log.severe("Error re-connecting to the database! Error: " + e.getMessage());
        }
    }

    public boolean closeDatabase() {
        try {
            conn.close();
            conn = null;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateTables() {
        if (conn != null) {
            DatabaseMetaData md = null;
            ResultSet rs1 = null;
            ResultSet rs2 = null;
            PreparedStatement query1 = null;
            PreparedStatement query2 = null;
            try {
                md = conn.getMetaData();
                rs1 = md.getColumns(null, null, enderchest.getConfigHandler().getString("database.mysql.tableName"), "enderchest");
                if (rs1.next()) {
                    String data1 = "ALTER TABLE `" + enderchest.getConfigHandler().getString("database.mysql.tableName") + "` CHANGE COLUMN enderchest enderchest_data LONGTEXT NOT NULL;";
                    query1 = conn.prepareStatement(data1);
                    query1.execute();
                } else {
                    rs2 = md.getColumns(null, null, enderchest.getConfigHandler().getString("database.mysql.tableName"), "enderchest_data");
                    if (rs2.next()) {
                        if (rs2.getString("TYPE_NAME").matches("VARCHAR")) {
                            String data2 = "ALTER TABLE `" + enderchest.getConfigHandler().getString("database.mysql.tableName") + "` MODIFY enderchest_data LONGTEXT NOT NULL;";
                            query2 = conn.prepareStatement(data2);
                            query2.execute();
                        }
                    }
                }

            } catch (Exception e) {
                CustomEnderChest.log.warning("Error on table update! Error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (query1 != null) {
                        query1.close();
                    }
                    if (query2 != null) {
                        query2.close();
                    }
                    if (rs1 != null) {
                        rs1.close();
                    }
                    if (rs2 != null) {
                        rs2.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
