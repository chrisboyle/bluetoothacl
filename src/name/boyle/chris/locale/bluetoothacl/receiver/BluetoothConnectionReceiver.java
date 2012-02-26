package name.boyle.chris.locale.bluetoothacl.receiver;

import name.boyle.chris.locale.bluetoothacl.Constants;
import name.boyle.chris.locale.bluetoothacl.ui.EditActivity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BluetoothConnectionReceiver extends BroadcastReceiver
{
	private final Intent REQUEST_REQUERY = new Intent(com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY).putExtra(com.twofortyfouram.locale.Intent.EXTRA_ACTIVITY, EditActivity.class.getName());
	private static final String PREFS_NAME = "last_known_state";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (! BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())
				&& ! BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
			return;
		}
		boolean connected = BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction());

		BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		if (dev == null) return;
		String addr = dev.getAddress();
		Log.v(Constants.LOG_TAG, "device "+addr+" is "+(connected ? "connected" : "disconnected"));
		if (addr == null) return;
		SharedPreferences.Editor ed = context.getSharedPreferences(PREFS_NAME, 0).edit();
		ed.putBoolean(addr, connected);
		ed.commit();

		context.sendBroadcast(REQUEST_REQUERY);
	}

	public static boolean isConnected(Context context, String deviceMac)
	{
		Log.v(Constants.LOG_TAG, "asked about "+deviceMac);
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		boolean b = prefs.getBoolean(deviceMac, false);
		Log.v(Constants.LOG_TAG, "result "+b);
		return b;
	}

}
