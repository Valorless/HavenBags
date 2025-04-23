package valorless.havenbags.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import valorless.havenbags.Main.ServerVersion;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.utils.FoodComponentFixer;
import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;

public class MySQL {
	private MySQL mysql;
	private String host, database, username, password;
	private int port;
	private Connection connection;

	public MySQL() {
		init();
	}

	void init() {
		mysql = this;
		host = Main.config.GetString("mysql.host");
		port = Main.config.GetInt("mysql.port");
		database = Main.config.GetString("mysql.name");
		username = Main.config.GetString("mysql.user");
		password = Main.config.GetString("mysql.password");

		try {
			connect();
			Log.Info(Main.plugin,"Connected to MySQL!");
			createTables();
		} catch (SQLException e) {
			Log.Error(Main.plugin,"Could not connect to MySQL!");
			Bukkit.getPluginManager().disablePlugin(Main.plugin);
			e.printStackTrace();
		}
	}

	// Connect to MySQL
	public void connect() throws SQLException {
		if (connection != null && !connection.isClosed()) return;
		String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
		connection = DriverManager.getConnection(url, username, password);
	}

	// Disconnect from MySQL
	public void disconnect() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}
	}

	// Get the active connection
	public Connection getConnection() {
		return connection;
	}

	// Create a table for player data
	private void createTables() {
		try (Connection conn = mysql.connection;
				Statement stmt = conn.createStatement()) {
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS bags (" +
					"uuid VARCHAR(36) PRIMARY KEY, " +
					"owner VARCHAR(36), " +
					"creator VARCHAR(36), " +
					"size TINYINT, " +
					"texture TEXT, " +
					"custommodeldata INT, " +
					"itemmodel TEXT, " +
					"trusted TEXT, " +
					"auto_pickup TEXT, " +
					"weight DOUBLE, " +
					"weight_max DOUBLE, " +
					"content LONGTEXT, " +
					"open BOOLEAN, " +
					"extra LONGTEXT" +
					")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public MySQL getDatabase() {
		return mysql;
	}

	public List<String> getAllBagUUIDs() {
		List<String> uuids = new ArrayList<>();
		String sql = "SELECT uuid FROM bags";
		
		try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				uuids.add(rs.getString("uuid"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return uuids;
	}
	
	public List<String> getBagOwners() {
        List<String> uuids = new ArrayList<>();
        String sql = "SELECT owner FROM bags";
        
        try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
            	if(!uuids.contains(rs.getString("owner"))) {
            		uuids.add(rs.getString("owner"));
            	}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uuids;
    }
    
    public List<String> getPlayerBags(String uuid) {
        List<String> owners = new ArrayList<>();
        String sql = "SELECT uuid FROM bags WHERE owner = ?;";
        
        try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid); // Set UUID parameter

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    owners.add(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return owners;
    }

	public void saveBag(Data data) {
		Log.Debug(Main.plugin, "[DI-233] " + "Attempting to write bag " + data.getOwner() + "/" + data.getUuid() + " onto database");
		String sql = "INSERT INTO bags (uuid, owner, creator, size, texture, custommodeldata, " +
				"itemmodel, trusted, auto_pickup, weight, weight_max, content, open, extra) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
				"ON DUPLICATE KEY UPDATE owner=?, creator=?, size=?, texture=?, " +
				"custommodeldata=?, itemmodel=?, trusted=?, auto_pickup=?, weight=?, " +
				"weight_max=?, content=?, open=?, extra=?";

		try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try (Connection conn = connection;
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, data.getUuid());
    		stmt.setString(2, data.getOwner());
    		stmt.setString(3, data.getCreator());
    		stmt.setInt(4, data.getSize());
    		stmt.setString(5, data.getTexture());
    		stmt.setInt(6, data.getModeldata());
    		stmt.setString(7, data.getItemmodel());
    		stmt.setString(8, JsonUtils.toJson(data.getTrusted()));
    		stmt.setString(9, data.getAutopickup());
    		stmt.setDouble(10, data.getWeight());
    		stmt.setDouble(11, data.getWeightMax());
    		stmt.setString(12, JsonUtils.toJson(data.getContent()));
    		stmt.setBoolean(13, data.isOpen());
    		stmt.setString(14, "null");

    		// Update values for conflict resolution
    		stmt.setString(15, data.getOwner());
    		stmt.setString(16, data.getCreator());
    		stmt.setInt(17, data.getSize());
    		stmt.setString(18, data.getTexture());
    		stmt.setInt(19, data.getModeldata());
    		stmt.setString(20, data.getItemmodel());
    		stmt.setString(21, JsonUtils.toJson(data.getTrusted()));
    		stmt.setString(22, data.getAutopickup());
    		stmt.setDouble(23, data.getWeight());
    		stmt.setDouble(24, data.getWeightMax());
    		stmt.setString(25, JsonUtils.toJson(data.getContent()));
    		stmt.setBoolean(26, data.isOpen());
    		stmt.setString(27, DatabaseUtils.Extra(data));

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public void saveBags(List<Data> bags) {
	    String sql = "INSERT INTO bags (uuid, owner, creator, size, texture, custommodeldata, " +
	                 "itemmodel, trusted, auto_pickup, weight, weight_max, content, open, extra) VALUES ";

	    try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
	    List<String> values = new ArrayList<>();
	    for (Data bag : bags) {
	        values.add("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
	    }

	    sql += String.join(", ", values) +
	            " ON DUPLICATE KEY UPDATE " +
	            "owner = VALUES(owner), " +
	            "creator = VALUES(creator), " +
	            "size = VALUES(size), " +
	            "texture = VALUES(texture), " +
	            "custommodeldata = VALUES(custommodeldata), " +
	            "itemmodel = VALUES(itemmodel), " +
	            "trusted = VALUES(trusted), " +
	            "auto_pickup = VALUES(auto_pickup), " +
	            "weight = VALUES(weight), " +
	            "weight_max = VALUES(weight_max), " +
	            "content = VALUES(content), " +
        		"open = VALUES(open), " +
        		"extra = VALUES(extra)";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        int index = 1;
	        for (Data bag : bags) {
	            stmt.setString(index++, bag.getUuid().toString());
	            stmt.setString(index++, bag.getOwner());
	            stmt.setString(index++, bag.getCreator());
	            stmt.setInt(index++, bag.getSize());
	            stmt.setString(index++, bag.getTexture());
	            stmt.setInt(index++, bag.getModeldata());
	            stmt.setString(index++, bag.getItemmodel());
	            stmt.setString(index++, JsonUtils.toJson(bag.getTrusted()));
	            stmt.setString(index++, bag.getAutopickup());
	            stmt.setDouble(index++, bag.getWeight());
	            stmt.setDouble(index++, bag.getWeightMax());
	            stmt.setString(index++, JsonUtils.toJson(bag.getContent()));
	            stmt.setBoolean(index++, bag.isOpen());
	            stmt.setString(index++, DatabaseUtils.Extra(bag));
	        }
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public Data loadBag(String uuid) {
		Log.Debug(Main.plugin, "[DI-234] " + "Attempting to load bag "  + uuid + ".");
		String sql = "SELECT * FROM bags WHERE uuid = ?";

		try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try (Connection conn = connection;
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, uuid.toString());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				Data data = new Data(
	                    rs.getString("uuid"),
	                    rs.getString("owner")
	                );
	                data.setCreator(rs.getString("creator"));
	                data.setSize(rs.getInt("size"));
	                data.setTexture(rs.getString("texture"));
	                data.setModeldata(rs.getInt("custommodeldata"));
	                data.setItemmodel(rs.getString("itemmodel"));
	                data.setTrusted(JsonUtils.fromJson(rs.getString("trusted")));
	                data.setAutopickup(rs.getString("auto_pickup"));
	                data.setWeight(rs.getDouble("weight"));
	                data.setWeightMax(rs.getDouble("weight_max"));
	                data.setContent(loadContent(rs.getString("content"), data.getUuid()));
	                data.setOpen(rs.getBoolean("open"));
	                
	                Map<String, Object> extra = DatabaseUtils.ParseExtra(rs.getString("extra"));
	                if(extra.containsKey("autosort")) data.setAutoSort((Boolean) extra.get("autosort"));
	                if(extra.containsKey("material")) data.setMaterial((String)extra.get("material"));
	                if(extra.containsKey("name")) data.setName((String) extra.get("name"));
	                if(extra.containsKey("blacklist")) data.setBlacklist(DatabaseUtils.parseBlacklist((String) extra.get("blacklist")));
	                if(extra.containsKey("whitelist")) data.setWhitelist((Boolean) extra.get("whitelist"));
	                if(extra.containsKey("ignoreglobalblacklist")) data.setIgnoreGlobalBlacklist((Boolean) extra.get("ignoreglobalblacklist"));
	                
	                return data;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Data> loadAllBags() {
	    List<Data> bags = new ArrayList<>();
	    String sql = "SELECT * FROM bags";
	    
	    try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	    try (PreparedStatement stmt = connection.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	        	Data data = new Data(
	                    rs.getString("uuid"),
	                    rs.getString("owner")
	                );
	                data.setCreator(rs.getString("creator"));
	                data.setSize(rs.getInt("size"));
	                data.setTexture(rs.getString("texture"));
	                data.setModeldata(rs.getInt("custommodeldata"));
	                data.setItemmodel(rs.getString("itemmodel"));
	                data.setTrusted(JsonUtils.fromJson(rs.getString("trusted")));
	                data.setAutopickup(rs.getString("auto_pickup"));
	                data.setWeight(rs.getDouble("weight"));
	                data.setWeightMax(rs.getDouble("weight_max"));
	                data.setContent(loadContent(rs.getString("content"), data.getUuid()));
	                data.setOpen(rs.getBoolean("open"));
	                
	                Map<String, Object> extra = DatabaseUtils.ParseExtra(rs.getString("extra"));
	                if(extra.containsKey("autosort")) data.setAutoSort((Boolean) extra.get("autosort"));
	                if(extra.containsKey("material")) data.setMaterial((String)extra.get("material"));
	                if(extra.containsKey("name")) data.setName((String) extra.get("name"));
	                if(extra.containsKey("blacklist")) data.setBlacklist(DatabaseUtils.parseBlacklist((String) extra.get("blacklist")));
	                if(extra.containsKey("whitelist")) data.setWhitelist((Boolean) extra.get("whitelist"));
	                if(extra.containsKey("ignoreglobalblacklist")) data.setIgnoreGlobalBlacklist((Boolean) extra.get("ignoreglobalblacklist"));
	                
	                bags.add(data);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return bags;
	}
	
	public void deleteBag(String uuid) {
	    String sql = "DELETE FROM bags WHERE uuid = ?";

	    try {
			if(connection.isClosed()) {
				connect();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	    
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, uuid);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public List<ItemStack> loadContent(String jsonString, String uuid) {
    	List<JsonObject> json = BagData.deserializeItemStackList(jsonString);
		
		List<ItemStack> items = new ArrayList<>();
		for(JsonObject e : json) {
			if(e == null) {
				items.add(null); 
				continue;
			}
			String entry = e.toString();
			//Log.Info(Main.plugin, entry + "");
			entry = entry.replace("◊","'");
			ItemStack item = null;
			if(entry.equalsIgnoreCase("null")) {
				items.add(null); 
				continue;
			}
			if(Main.VersionCompare(Main.server, ServerVersion.v1_21_4) >= 0) {
				try {
					item = JsonUtils.fromJson(
							FoodComponentFixer.fixFoodJson(entry)
							);
				}catch(Exception E) {
					Log.Error(Main.plugin, uuid);
					Log.Error(Main.plugin, entry);
					Log.Info(Main.plugin, FoodComponentFixer.fixFoodJson(entry));
					E.printStackTrace();
				}
			}else {
				item = JsonUtils.fromJson(entry);
			}
			items.add(item);
		}
		return items;
    }
}
