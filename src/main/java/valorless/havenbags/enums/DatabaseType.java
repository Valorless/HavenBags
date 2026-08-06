package valorless.havenbags.enums;

public enum DatabaseType {

	FILES, MYSQL, SQLITE, MYSQLPLUS;
	
	public static DatabaseType get(String type) {
        return switch (type.toUpperCase()) {
            case "FILES" -> FILES;
            case "MYSQL" -> MYSQL;
            case "MYSQLPLUS" -> MYSQLPLUS;
            case "SQLITE" -> SQLITE;
            default -> throw new IllegalArgumentException("Cannot resolve Database type: " + type);
        };
    }
}
