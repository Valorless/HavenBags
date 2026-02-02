package valorless.havenbags.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonObject;

import valorless.havenbags.BagData;
import valorless.havenbags.Main;
import valorless.havenbags.datamodels.Data;
import valorless.havenbags.utils.FoodComponentFixer;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.Server.Version;
import valorless.valorlessutils.ValorlessUtils.Log;
import valorless.valorlessutils.json.JsonUtils;

public class SQLite {
    @SuppressWarnings("unused")
	private SQLite database;
    private Connection connection;
    private final String databaseName = "database.db";

    public SQLite() {
    	init();
    }
    
    public void init() {
        database = this;
        try {
            connect();
			Log.Info(Main.plugin,"Connected to SQLite!");
            setupTables();
        } catch (SQLException e) {
			Log.Error(Main.plugin,"Could not connect to SQLite!");
            e.printStackTrace();
        }
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) return;

        File dbFile = new File(Main.plugin.getDataFolder(), databaseName);
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        connection = DriverManager.getConnection(url);
        //Log.Info(Main.plugin, "SQLite connected: " + dbFile.getAbsolutePath());
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            Log.Info(Main.plugin, "Disconnected from SQLite!");
        }
    }

    private void setupTables() {
    	try (Connection conn = connection) {

    		conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS bags (" +
    			"uuid TEXT PRIMARY KEY, " +          // UUID (MySQL VARCHAR(36) → SQLite TEXT)
    			"owner TEXT, " +                      // Owner UUID
    			"creator TEXT, " +                    // Creator UUID
    			"size INTEGER, " +                    // TINYINT → INTEGER
    			"texture TEXT, " +                     // Texture
    			"custommodeldata INTEGER, " +         // INT → INTEGER
    			"itemmodel TEXT, " +                   // Item model
    			"trusted TEXT, " +                     // List of trusted players (Stored as JSON?)
    			"auto_pickup TEXT, " +                 // Renamed (no `-` in column names)
    			"weight REAL, " +                      // DOUBLE → REAL (SQLite equivalent)
    			"weight_max REAL, " +                  // DOUBLE → REAL
    			"content TEXT, " +                      // LONGTEXT → TEXT
    			"extra TEXT " +                      // LONGTEXT → TEXT
    			");"
    		);
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    //addColumnIfNotExists("columnName", "\"TEXT DEFAULT 'value'"\");
    //addColumnIfNotExists("columnName", "\"INTEGER DEFAULT 0"\");
    
    // Use for later, if additional bag data gets added.
    @SuppressWarnings("unused")
	private void addColumnIfNotExists(String columnName, String columnDefinition) {
        String checkColumnExists = "PRAGMA table_info(bags);";
        boolean columnExists = false;

        try {
            if (connection.isClosed()) {
                connect(); // Ensure connection is open
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkColumnExists)) {

            while (rs.next()) {
                String existingColumn = rs.getString("name");
                if (existingColumn.equalsIgnoreCase(columnName)) {
                    columnExists = true;
                    break;
                }
            }

            if (!columnExists) {
                String alterTableSQL = "ALTER TABLE bags ADD COLUMN " + columnName + " " + columnDefinition + ";";
                stmt.executeUpdate(alterTableSQL);
                System.out.println("Column '" + columnName + "' added successfully.");
            } else {
                System.out.println("Column '" + columnName + "' already exists.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveBag(Data data) {
        String sql = "INSERT INTO bags (uuid, owner, creator, size, texture, custommodeldata, " +
                     "itemmodel, trusted, auto_pickup, weight, weight_max, content, extra) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT(uuid) DO UPDATE SET " +
                     "owner = excluded.owner, " +
                     "creator = excluded.creator, " +
                     "size = excluded.size, " +
                     "texture = excluded.texture, " +
                     "custommodeldata = excluded.custommodeldata, " +
                     "itemmodel = excluded.itemmodel, " +
                     "trusted = excluded.trusted, " +
                     "auto_pickup = excluded.auto_pickup, " +
                     "weight = excluded.weight, " +
                     "weight_max = excluded.weight_max, " +
                     "content = excluded.content, " +
        			 "extra = excluded.extra";

        try {
            if (connection.isClosed()) {
                connect(); // Ensure connection is open
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // INSERT values
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
            stmt.setString(13, DatabaseUtils.Extra(data));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    
    public Data loadBag(String uuid) {
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

            stmt.setString(1, uuid);
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
                
                DatabaseUtils.ApplyExtra(data, rs.getString("extra"));
                
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
			if(Server.VersionHigherOrEqualTo(Version.v1_21_4)) {
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
