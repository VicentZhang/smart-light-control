package edu.xidian.si.zhouwei;

public class LcDefine {
	public static final String LOCATION = "location";
	public static final String POWER_STATE = "power_state";
	public static final String POWER_STATE_ON = Byte.toString(LcConfig.POWER_STATE_ON);
	public static final String POWER_STATE_OFF = Byte.toString(LcConfig.POWER_STATE_OFF);
	public static final String POWER_ON_CODE = "power_on";
	public static final String POWER_OFF_CODE = "power_off";
	public static final String NOT_CONNECTED = "您还没有连接到任何蓝牙设备";
	public static final String NO_CHAT_SERVICE = "您还没有开启蓝牙";
	public static final String BLUETOOTH_NOT_AVAILABLE = "蓝牙不可用";
	public static final String UNABLE_TO_CONNECT_DEVICE = "无法连接设备";
	public static final String DEVICE_CONNECTION_LOST = "失去设备连接";
	
	// for demo
	public static final String DEMO_LIGHT_0_LOCATION = "01H on, 00H off";
	public static final String DEMO_LIGHT_1_LOCATION = "11H on, 10H off";
	public static final String DEMO_LIGHT_2_LOCATION = "21H on, 20H off";
	public static final String DEMO_LIGHT_3_LOCATION = "31H on, 30H off";
}
