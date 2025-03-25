package valorless.havenbags.database;

public enum DatabaseType {

	FILES, MYSQL, SQLITE, MYSQLPLUS;
	
	public static DatabaseType get(String type) {
        switch (type.toUpperCase()) {
            case "FILES":
                return FILES;
            case "MYSQL":
                return MYSQL;
            case "MYSQLPLUS":
                return MYSQLPLUS;
            case "SQLITE":
                return SQLITE;
            default:
                throw new IllegalArgumentException("Cannot resolve Database type: " + type);
        }
    }
}
