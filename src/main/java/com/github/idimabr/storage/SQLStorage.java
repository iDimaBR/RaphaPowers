package com.github.idimabr.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.github.idimabr.RaphaPowers;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class SQLStorage {

	private final RaphaPowers plugin;
	private Connection connection;
	private PreparedStatement smt;

	public SQLStorage(RaphaPowers plugin) {
		this.plugin = plugin;
		FileConfiguration config = plugin.getConfig();
		try {
			String driverName = "com.mysql.jdbc.Driver";
			Class.forName(driverName);
			String host = config.getString("MySQL.Host"); 
			String database = config.getString("MySQL.Database");
			String url = "jdbc:mysql://" + host + "/" + database;
			String username = config.getString("MySQL.Username");
			String password = config.getString("MySQL.Password");
			connection = DriverManager.getConnection(url, username, password);
			plugin.getLogger().info("§aConexão com banco de dados foi estabelecida.");
		} catch (Exception e) {
			plugin.getLogger().info("§cOcorreu um erro no banco de dados");
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}
	
	public void createTable() {
		try {
			smt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players(`UUID` varchar(36) NOT NULL, `life` int(11) NOT NULL, `time` BIGINT NOT NULL, PRIMARY KEY (`UUID`))");
			smt.executeUpdate();
		} catch (SQLException e) {
			plugin.getLogger().info("Erro de criação na tabela do MYSQL");
		}
	}

	public Connection getConnectionMySQL() {
		return connection;
	}

	public boolean delete(UUID uuid){
		try {
			smt = connection.prepareStatement("DELETE FROM `players` WHERE `UUID` = ?");
			smt.setString(1, uuid.toString());
			int result = smt.executeUpdate();
			if(result == 1)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean contains(UUID uuid){
		try {
			smt = connection.prepareStatement("SELECT `UUID` FROM players WHERE `UUID` = ?");
			smt.setString(1, uuid.toString());
			ResultSet result = smt.executeQuery();
			if(result.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public ResultSet getString(String coluna, String valor) {
		try {
			String sql = "SELECT * FROM players WHERE ? = ?";
			smt = connection.prepareStatement(sql);
			smt.setString(1, coluna);
			smt.setString(2, valor);
			return smt.executeQuery();
		} catch (SQLException e) {
			Bukkit.getLogger().info("Método getString retornou nullo");
		}
		return null;
	}

	public void executeUpdateMySQL(String query) {
		try {
			smt = connection.prepareStatement(query);
			smt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean close() {
		try {
			if(getConnectionMySQL() != null) {
				getConnectionMySQL().close();
			}else {
				return false;
			}
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	public Connection restart() {
		close();
		return getConnectionMySQL();
	}
}