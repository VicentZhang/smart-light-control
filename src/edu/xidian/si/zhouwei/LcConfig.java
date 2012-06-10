package edu.xidian.si.zhouwei;

public class LcConfig {
	public static final byte POWER_STATE_ON = (byte) 1;
	public static final byte POWER_STATE_OFF = (byte) 0;
	
	// for demo
	public static final byte[] DEMO_LIGHT_0_POWER_OFF_CODE 	= { (byte) 0x00 };
	public static final byte[] DEMO_LIGHT_0_POWER_ON_CODE 	= { (byte) 0x01 };
	public static final byte[] DEMO_LIGHT_1_POWER_OFF_CODE 	= { (byte) 0x10 };
	public static final byte[] DEMO_LIGHT_1_POWER_ON_CODE 	= { (byte) 0x11 };
	public static final byte[] DEMO_LIGHT_2_POWER_OFF_CODE 	= { (byte) 0x20 };
	public static final byte[] DEMO_LIGHT_2_POWER_ON_CODE 	= { (byte) 0x21 };
	public static final byte[] DEMO_LIGHT_3_POWER_OFF_CODE 	= { (byte) 0x30 };
	public static final byte[] DEMO_LIGHT_3_POWER_ON_CODE 	= { (byte) 0x31 };
	public static final String DATABASE_PATH = "/data/data/edu.xidian.si.zhouwei/light_control.db";
}
