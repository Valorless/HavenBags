package valorless.havenbags.datamodels;

public class Placeholder {
	String key;
	String value;
	
	public Placeholder(String key, Object value) {
		this.key = key;
		this.value = String.valueOf(value);
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
