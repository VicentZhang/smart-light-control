package edu.xidian.si.zhouwei;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author zhouwei
 * 
 */
public class LightListViewAdapter extends BaseAdapter {

	private static final boolean D = LightCtrlActivity.isInDebugMode();

	private static final int SUCCESS = 0;
	private static final int NO_CHAT_SERVICE = 1;
	private static final int NOT_CONNECTED = 2;

	private Context mContext;
	private ArrayList<Light> mLights = null;
	private LayoutInflater mInflater = null;
	private RowView mRowView = null;
	private byte[] mBuffer = null;

	// Member object for the chat services
	// 对 BluetoothChatService 的引用，
	// 若为 null，则表示软件还不能够使用蓝牙
	private BluetoothChatService mRefToChatService = null;

	private LightCtrlActivity mRefToightListActivity = null;

	private SQLiteDatabase mSqLiteDatabase = null;

	/**
	 * 
	 * 
	 * @param context
	 * @param lightListActivityRef
	 * @param sqLiteDatabase
	 */
	public LightListViewAdapter(Context context,
			LightCtrlActivity lightListActivityRef,
			SQLiteDatabase sqLiteDatabase) {

		mContext = context;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRefToightListActivity = lightListActivityRef;
		mSqLiteDatabase = sqLiteDatabase;
		setLightData();
	}

	public void setBluetoothChatService(BluetoothChatService chatService) {
		mRefToChatService = chatService;
	}

	public void clearBluetoothChatService() {
		mRefToChatService = null;
	}

	public void add(byte[] buffer) {
		mBuffer = new byte[buffer.length];
		System.arraycopy(buffer, 0, mBuffer, 0, buffer.length);
	}

	@Override
	public int getCount() {
		return mLights.size();
	}

	@Override
	public Object getItem(int position) {
		return mLights.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeItem(int position) {
		mLights.remove(position);
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView != null) {
			mRowView = (RowView) convertView.getTag();
		} else {
			convertView = mInflater.inflate(R.layout.light_list_view_row, null);
			mRowView = new RowView();
			mRowView.locationTextView = (TextView) convertView
					.findViewById(R.id.text_view_location);
			mRowView.powerStateImageView = (ImageView) convertView
					.findViewById(R.id.image_view_power_state);
			mRowView.powerButton = (Button) convertView
					.findViewById(R.id.button_power);
			convertView.setTag(mRowView);
		}

		Light aLight = mLights.get(position);
		mRowView.locationTextView.setText(aLight.getLocation());
		if (aLight.getPowerState() == LcConfig.POWER_STATE_ON) {
			mRowView.powerStateImageView.setImageResource(R.drawable.light_on);
			mRowView.powerButton.setText(R.string.button_power_text_off);
		} else {
			mRowView.powerStateImageView.setImageResource(R.drawable.light_off);
			mRowView.powerButton.setText(R.string.button_power_text_on);
		}
		mRowView.powerButton
				.setOnClickListener(new RowButtonListener(position));

		return convertView;
	}

	private final class RowView {
		public TextView locationTextView;
		public ImageView powerStateImageView;
		public Button powerButton;
	}

	private final class RowButtonListener implements OnClickListener {
		private int mmPosition;

		public RowButtonListener(int position) {
			mmPosition = position;
		}

		@Override
		public void onClick(View v) {
			int viewId = v.getId();
			if (viewId == mRowView.powerButton.getId()) {
				// 更换灯泡电源状态图片以及本行按钮的名称
				notifyPowerStateChanged(mmPosition);
			}
		}
	}

	// 更换灯泡电源状态图片以及本行按钮的名称
	private void notifyPowerStateChanged(int position) {
		// 根据 position 获得对应的灯的数据
		Light aLight = mLights.get(position);
		byte currentPowerState = aLight.getPowerState();
		byte[] message = null;
		if (currentPowerState == LcConfig.POWER_STATE_ON) {
			message = aLight.getOffCodeAsByteArray();
		} else {
			message = aLight.getOnCodeAsByteArray();
			if (D) {
				mRefToightListActivity
						.showViaToast("notifyPowerStateChanged(): "
								+ Byte.toString(aLight.getOnCode())
								+ " and message is "
								+ Byte.toString(message[0]));
			}
		}

		int result = sendMessage(message);
		switch (result) {
		case SUCCESS:
			if (currentPowerState == LcConfig.POWER_STATE_ON) {
				aLight.setPowerState(LcConfig.POWER_STATE_OFF);
			} else {
				aLight.setPowerState(LcConfig.POWER_STATE_ON);
			}
			this.notifyDataSetChanged();
			break;

		case NO_CHAT_SERVICE:
			if (D) {
				if (currentPowerState == LcConfig.POWER_STATE_ON) {
					aLight.setPowerState(LcConfig.POWER_STATE_OFF);
				} else {
					aLight.setPowerState(LcConfig.POWER_STATE_ON);
				}
				this.notifyDataSetChanged();
				mRefToightListActivity.showViaToast(LcDefine.NO_CHAT_SERVICE);
			}
			break;

		case NOT_CONNECTED:
			mRefToightListActivity.showViaToast(LcDefine.NOT_CONNECTED);
			break;

		default:
			// no operations needed
			break;
		}

	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A byte array of command to send.
	 */
	private int sendMessage(byte[] message) {
		if (mRefToChatService == null) {
			return NO_CHAT_SERVICE;
		}

		// Check that we're actually connected before trying anything
		if (mRefToChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			return NOT_CONNECTED;
		}

		// Check that there's actually something to send
		if (message.length > 0) {
			mRefToChatService.write(message);
		}

		return SUCCESS;
	}

	private void setLightData() {
		mLights = new ArrayList<Light>();

		try {
			// query for all the data in the light table of the database
			Cursor cursor = null;
			cursor = mSqLiteDatabase.query(Light.TABLE, Light.COLUMNS, null,
					null, null, null, null);
			if (cursor != null) {
				// 获取各列的索引
				int idIndex = cursor.getColumnIndex(Light.COLUMN_ID);
				int locationIndex = cursor
						.getColumnIndex(Light.COLUMN_LOCATION);
				int offCodeIndex = cursor.getColumnIndex(Light.COLUMN_OFF_CODE);
				int onCodeIndex = cursor.getColumnIndex(Light.COLUMN_ON_CODE);
				int powerStateIndex = cursor
						.getColumnIndex(Light.COLUMN_POWER_STATE);

				Light light = null;
				for (cursor.moveToFirst(); !(cursor.isAfterLast()); cursor
						.moveToNext()) {
					String idString = cursor.getString(idIndex);
					String location = cursor.getString(locationIndex);
					String onCodeString = cursor.getString(onCodeIndex);
					String offCodeString = cursor.getString(offCodeIndex);
					String powerStateString = cursor.getString(powerStateIndex);
					light = new Light(idString, location, onCodeString,
							offCodeString, powerStateString);
					mLights.add(light);
				}
				// 关闭结果集
				cursor.close();

			} else {

			}
		} catch (Exception e) {
			if (D) {
				mRefToightListActivity.showViaToast(e.getMessage());
			}

		}

		// map = new HashMap<String, Object>();
		// map.put(new String(LcDefine.LOCATION),
		// LcDefine.DEMO_LIGHT_0_LOCATION);
		// map.put(new String(LcDefine.POWER_STATE), LcConfig.POWER_STATE_OFF);
		// map.put(new String(LcDefine.POWER_ON_CODE),
		// LcConfig.DEMO_LIGHT_0_POWER_ON_CODE);
		// map.put(new String(LcDefine.POWER_OFF_CODE),
		// LcConfig.DEMO_LIGHT_0_POWER_OFF_CODE);
		// mLightData.add(map);
		//
		// map = new HashMap<String, Object>();
		// map.put(new String(LcDefine.LOCATION),
		// LcDefine.DEMO_LIGHT_1_LOCATION);
		// map.put(new String(LcDefine.POWER_STATE), LcConfig.POWER_STATE_OFF);
		// map.put(new String(LcDefine.POWER_ON_CODE),
		// LcConfig.DEMO_LIGHT_1_POWER_ON_CODE);
		// map.put(new String(LcDefine.POWER_OFF_CODE),
		// LcConfig.DEMO_LIGHT_1_POWER_OFF_CODE);
		// mLightData.add(map);
		//
		// map = new HashMap<String, Object>();
		// map.put(new String(LcDefine.LOCATION),
		// LcDefine.DEMO_LIGHT_2_LOCATION);
		// map.put(new String(LcDefine.POWER_STATE), LcConfig.POWER_STATE_OFF);
		// map.put(new String(LcDefine.POWER_ON_CODE),
		// LcConfig.DEMO_LIGHT_2_POWER_ON_CODE);
		// map.put(new String(LcDefine.POWER_OFF_CODE),
		// LcConfig.DEMO_LIGHT_2_POWER_OFF_CODE);
		// mLightData.add(map);
		//
		// map = new HashMap<String, Object>();
		// map.put(new String(LcDefine.LOCATION),
		// LcDefine.DEMO_LIGHT_3_LOCATION);
		// map.put(new String(LcDefine.POWER_STATE), LcConfig.POWER_STATE_OFF);
		// map.put(new String(LcDefine.POWER_ON_CODE),
		// LcConfig.DEMO_LIGHT_3_POWER_ON_CODE);
		// map.put(new String(LcDefine.POWER_OFF_CODE),
		// LcConfig.DEMO_LIGHT_3_POWER_OFF_CODE);
	}

}
