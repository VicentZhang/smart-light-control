package edu.xidian.si.zhouwei;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LightCtrlActivity extends ListActivity {
	
    // Debugging
    private static final String TAG = "LightLisActivity";
    private static final boolean D = false;
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    // Layout Views
    private TextView mTitle;
	private static final int ITEM_MODIFY = 1;
	private static final int ITEM_DELETE = 2;
	private LightListViewAdapter mListViewAdapter = null;
	
    // Name of the connected device
    private String mConnectedDeviceName = null;
	
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    // 
    private SQLiteDatabase mSqLiteDatabase = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.text_view_title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.text_view_title_right_text);
        
        // 检查数据库是否已经安装，如是，打开；否则，提示后退出程序
        try{   
	        mSqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(
	        	LcConfig.DATABASE_PATH, null);  
	        if (D) {
	        	Toast.makeText(this, "SqLiteDatabase ok", Toast.LENGTH_LONG).show();
	        }
        } catch (SQLiteException sqLiteException){  
        	if (D) {
        		Toast.makeText(this, sqLiteException.toString(), Toast.LENGTH_LONG).show();
        	}
        } catch (Exception e) {
        	if (D) {
        		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        	}
        }
        if(mSqLiteDatabase == null){  
        	if (D) {
        		Toast.makeText(this, R.string.db_not_enabled_leaving, Toast.LENGTH_LONG).show();
        	}
            finish();
            return;
        } else {
        	if (D) {
        		Toast.makeText(this, "SqLiteDatabase not null ^-^", Toast.LENGTH_LONG).show();
        	}
        }
        
		// 初始化添加和退出按钮
		Button addButton = (Button) findViewById(R.id.button_add);
		addButton.setOnClickListener(
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.button_add:
						if (D) {
							showViaToast("I am button_add");
						}
						break;
					default:
						break;
					}

				}
			}
		);
//		Button exitButton = (Button) findViewById(R.id.button_exit);
//		exitButton.setOnClickListener(
//			new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					switch (v.getId()) {
//					case R.id.button_exit:
//						if (D) {
//							showViaToast("I am button_exit");
//						}
//						onDestroy();
//						break;
//					default:
//						break;
//					}
//
//				}
//			}
//		);

		// 关联Layout中的ListView
		ListView lightListView = (ListView) findViewById(android.R.id.list);

		// 设置ListView和适配器
		mListViewAdapter = new LightListViewAdapter(
			this, (LightCtrlActivity)this, mSqLiteDatabase);
		lightListView.setAdapter(mListViewAdapter);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
        	if (mSqLiteDatabase != null) {
        		mSqLiteDatabase.close();
        	}
            Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_LONG).show();
        }

	}

	@Override
	public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");

        if (mBluetoothAdapter != null) {
            // If BT is not on, request that it be enabled.
            // setupChat() will then be called during onActivityResult
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
            } else {
                if (mChatService == null)  {
                	setupChat();
                }
            }
        }

	}

	@Override
	public synchronized void onResume() {
        super.onResume();
        Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) {
        	mChatService.stop();
        }
        // Close the database connection
        if (mSqLiteDatabase != null) {
        	mSqLiteDatabase.close();
        }
        Log.e(TAG, "--- ON DESTROY ---");
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) {
        	Log.d(TAG, "onActivityResult " + resultCode);
        }
        
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
            	if (D) {
            		Log.d(TAG, "BT not enabled");
            	}
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

	// 长按时显示的菜单
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (D) {
			showViaToast("LightListActivity.onCreateContextMenu()");
		}
//		menu.setHeaderTitle(R.string.operation_choice);
//		menu.add(0, ITEM_MODIFY, 0, R.string.operation_edit);
//		menu.add(0, ITEM_DELETE, 1, R.string.operation_delete);
	}

	// 响应编辑和删除事件处理
	@Override
	public boolean onContextItemSelected(MenuItem item) {
//		if (D) {
//			showViaToast("LightListActivity.onContextItemSelected()");
//		}
		// switch (item.getItemId()) {
		// case ITEM_MODIFY:
		// break;
		// case ITEM_DELETE:
		// break;
		// default:
		// break;
		// }
		return false;
	}
	
	public static boolean isInDebugMode() {
		return D;
	}
	
	// 封装Toast,一方面调用简单,另一方面调整显示时间只要改此一个地方即可.
	public void showViaToast(String text) {
		Toast.makeText(this, text, 1000).show();
	}
	
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                mListViewAdapter.add(writeBuf);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                mListViewAdapter.add(readBuf);
                break;
            case MESSAGE_DEVICE_NAME:
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
    private void ensureDiscoverable() {
        if(D) {
        	Log.d(TAG, "ensure discoverable");
        }
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
    
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
	
    private void setupChat() {
        if (D) {
        	Log.d(TAG, "setupChat()");
        }

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        mListViewAdapter.setBluetoothChatService(mChatService);
    }

}
