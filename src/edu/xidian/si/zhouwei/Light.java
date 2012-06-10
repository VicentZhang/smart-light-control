package edu.xidian.si.zhouwei;

public class Light {

	public static final String TABLE = "light";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_ON_CODE = "on_code";
	public static final String COLUMN_OFF_CODE = "off_code";
	public static final String COLUMN_POWER_STATE = "power_state";
	public static final String[] COLUMNS = { COLUMN_ID, COLUMN_LOCATION,
			COLUMN_ON_CODE, COLUMN_OFF_CODE, COLUMN_POWER_STATE };

	private int id;
	private String location = null;
	private byte onCode;
	private byte offCode;
	private byte powerState;

	public Light(int id, String location, byte onCode, byte offCode,
			byte powerState) {
		this.id = id;
		this.location = new String(location);
		this.onCode = onCode;
		this.offCode = offCode;
		this.powerState = powerState;
	}

	public Light(String id, String location, String onCode, String offCode,
			String powerState) {
		this.id = Integer.parseInt(id);
		this.location = new String(location);
		this.onCode = Byte.parseByte(onCode);
		this.offCode = Byte.parseByte(offCode);
		this.powerState = Byte.parseByte(powerState);
	}

	public int getId() {
		return id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public byte getOnCode() {
		return onCode;
	}

	public void setOnCode(byte onCode) {
		this.onCode = onCode;
	}

	public void setOnCode(String onCode) {
		this.onCode = Byte.parseByte(onCode);
	}

	public byte getOffCode() {
		return offCode;
	}

	public void setOffCode(String offCode) {
		this.offCode = Byte.parseByte(offCode);
	}

	public void setOffCode(byte offCode) {
		this.offCode = offCode;
	}

	public byte getPowerState() {
		return powerState;
	}

	public void setPowerState(byte powerState) {
		this.powerState = powerState;
	}

	public void setPowerState(String powerState) {
		this.powerState = Byte.parseByte(powerState);
	}

	public byte[] getOffCodeAsByteArray() {
		byte[] ret = { offCode };
		return ret;
	}

	public byte[] getOnCodeAsByteArray() {
		byte[] ret = { onCode };
		return ret;
	}

}
