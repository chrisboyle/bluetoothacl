/*
 * Copyright 2012 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package name.boyle.chris.locale.bluetoothacl.ui;

import java.util.NoSuchElementException;
import java.util.Vector;

import name.boyle.chris.locale.bluetoothacl.Constants;
import name.boyle.chris.locale.bluetoothacl.R;
import name.boyle.chris.locale.bluetoothacl.bundle.BundleScrubber;
import name.boyle.chris.locale.bluetoothacl.bundle.PluginBundleManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.twofortyfouram.locale.BreadCrumber;

/**
 * This is the "Edit" activity for a Locale Plug-in condition.
 */
public final class EditActivity extends Activity
{

    /**
     * Help URL, used for the {@link com.twofortyfouram.locale.platform.R.id#twofortyfouram_locale_menu_help} menu item.
     */
    private static final String HELP_URL = "http://chris.boyle.name/projects/bluetoothacl"; //$NON-NLS-1$

    /**
     * Flag boolean that can only be set to true via the "Don't Save"
     * {@link com.twofortyfouram.locale.platform.R.id#twofortyfouram_locale_menu_dontsave} menu item in
     * {@link #onMenuItemSelected(int, MenuItem)}.
     * <p>
     * If true, then this {@code Activity} should return {@link Activity#RESULT_CANCELED} in {@link #finish()}.
     * <p>
     * If false, then this {@code Activity} should generally return {@link Activity#RESULT_OK} with extras
     * {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} and {@link com.twofortyfouram.locale.Intent#EXTRA_STRING_BLURB}.
     * <p>
     * There is no need to save/restore this field's state when the {@code Activity} is paused.
     */
    private boolean mIsCancelled = false;

    /**
     * ListView shown in the Activity
     */
    private ListView mList = null;

    private Spinner deviceSpinner = null;

    Vector<String> deviceMacs = new Vector<String>();
    Vector<String> deviceNames = new Vector<String>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        BluetoothAdapter a = BluetoothAdapter.getDefaultAdapter();
        if (a != null) {
            for (BluetoothDevice d : a.getBondedDevices()) {
                deviceNames.add(d.getName());
                deviceMacs.add(d.getAddress());
            }
        }
        if(deviceNames.isEmpty()) {
            Toast.makeText(this, R.string.no_devices, Toast.LENGTH_LONG).show();
            mIsCancelled = true;
            finish();
            return;
        }

        /*
         * A hack to prevent a private serializable classloader attack
         */
        BundleScrubber.scrub(getIntent());
        BundleScrubber.scrub(getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));

        setContentView(R.layout.main);

        if (Build.VERSION.SDK_INT >= 11)
        {
            CharSequence callingApplicationLabel = null;
            try
            {
                callingApplicationLabel = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(getCallingPackage(), 0));
            }
            catch (final NameNotFoundException e)
            {
                if (Constants.IS_LOGGABLE)
                {
                    Log.e(Constants.LOG_TAG, "Calling package couldn't be found", e); //$NON-NLS-1$
                }
            }
            if (null != callingApplicationLabel)
            {
                setTitle(callingApplicationLabel);
            }
        }
        else
        {
            setTitle(BreadCrumber.generateBreadcrumb(getApplicationContext(), getIntent(), getString(R.string.plugin_name)));
        }

        mList = ((ListView) findViewById(android.R.id.list));
        mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, getResources().getStringArray(R.array.bt_states)));

        deviceSpinner = ((Spinner) findViewById(R.id.deviceSpinner));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1, deviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(adapter);

        /*
         * if savedInstanceState is null, then then this is a new Activity instance and a check for EXTRA_BUNDLE is needed
         */
        if (null == savedInstanceState)
        {
            final Bundle forwardedBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
            if (PluginBundleManager.isBundleValid(forwardedBundle))
            {
                String wantedMac = forwardedBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_BLUETOOTH_MAC);
                int i=0;
                for (String mac : deviceMacs) {
                    if (mac.equals(wantedMac)) {
                        deviceSpinner.setSelection(i);
                        break;
                    }
                    i++;
                }
                mList.setItemChecked(getPositionForIdInArray(getApplicationContext(), R.array.bt_states, forwardedBundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE) ? R.string.list_connected
                        : R.string.list_disconnected), true);
            } else {
                mList.setItemChecked(getPositionForIdInArray(getApplicationContext(), R.array.bt_states, R.string.list_connected), true);
            }
        }
        /*
         * if savedInstanceState isn't null, there is no need to restore any Activity state directly via onSaveInstanceState(), as
         * the ListView object handles that automatically
         */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish()
    {
        if (!mIsCancelled && mList != null && ListView.INVALID_POSITION != mList.getCheckedItemPosition())
        {
            final int selectedResourceId = getResourceIdForPositionInArray(getApplicationContext(), R.array.bt_states, mList.getCheckedItemPosition());

            /*
             * This is the result Intent to Locale
             */
            final Intent resultIntent = new Intent();

            /*
             * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note that anything placed in
             * this Bundle must be available to Locale's class loader. So storing String, int, and other standard objects will
             * work just fine. However Parcelable objects must also be Serializable. And Serializable objects must be standard
             * Java objects (e.g. a private subclass to this plug-in cannot be stored in the Bundle, as Locale's classloader will
             * not recognize it).
             */
            final Bundle resultBundle = new Bundle();

            resultBundle.putInt(PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE, Constants.getVersionCode(this));

            resultBundle.putString(PluginBundleManager.BUNDLE_EXTRA_STRING_BLUETOOTH_MAC,
                    deviceMacs.get(deviceSpinner.getSelectedItemPosition()));

            if (R.string.list_connected == selectedResourceId)
            {
                resultBundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, true);
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
                        getString(R.string.blurb_connected) + ": " + deviceNames.get(deviceSpinner.getSelectedItemPosition()));
            }
            else if (R.string.list_disconnected == selectedResourceId)
            {
                resultBundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, false);
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB,
                        getString(R.string.blurb_disconnected) + ": " + deviceNames.get(deviceSpinner.getSelectedItemPosition()));
            }
            else
            {
                // this should never happen
                throw new AssertionError();
            }

            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

            setResult(RESULT_OK, resultIntent);
        }

        super.finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        /*
         * inflate the default menu layout from XML
         */
        getMenuInflater().inflate(com.twofortyfouram.locale.platform.R.menu.twofortyfouram_locale_help_save_dontsave, menu);

        /*
         * Set up the breadcrumbs for the ActionBar
         */
        if (Build.VERSION.SDK_INT >= 11)
        {
            getActionBar().setSubtitle(BreadCrumber.generateBreadcrumb(getApplicationContext(), getIntent(), getString(R.string.plugin_name)));
        }
        /*
         * Dynamically load the home icon from the host package for Ice Cream Sandwich or later. Note that this leaves Honeycomb
         * devices without the host's icon in the ActionBar, but eventually all Honeycomb devices should receive an OTA to Ice
         * Cream Sandwich so this problem will go away.
         */
        if (Build.VERSION.SDK_INT >= 14)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            /*
             * Note: There is a small TOCTOU error here, in that the host could be uninstalled right after launching the plug-in.
             * That would cause getApplicationIcon() to return the default application icon. It won't fail, but it will return an
             * incorrect icon.
             *
             * In practice, the chances that the host will be uninstalled while the plug-in UI is running are very slim.
             */
            try
            {
                getActionBar().setIcon(getPackageManager().getApplicationIcon(getCallingPackage()));
            }
            catch (final NameNotFoundException e)
            {
                if (Constants.IS_LOGGABLE)
                {
                    Log.w(Constants.LOG_TAG, "An error occurred loading the host's icon", e); //$NON-NLS-1$
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item)
    {
        final int id = item.getItemId();

        if (Build.VERSION.SDK_INT >= 11)
        {
            if (id == android.R.id.home)
            {
                finish();
                return true;
            }
        }

        if (id == com.twofortyfouram.locale.platform.R.id.twofortyfouram_locale_menu_help)
        {
            try
            {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(HELP_URL)));
            }
            catch (final Exception e)
            {
                Toast.makeText(getApplicationContext(), com.twofortyfouram.locale.platform.R.string.twofortyfouram_locale_application_not_available, Toast.LENGTH_LONG).show();
            }

            return true;
        }
        else if (id == com.twofortyfouram.locale.platform.R.id.twofortyfouram_locale_menu_dontsave)
        {
            mIsCancelled = true;
            finish();
            return true;
        }
        else if (id == com.twofortyfouram.locale.platform.R.id.twofortyfouram_locale_menu_save)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the position of an element in a typed array
     *
     * @param context Application context. Cannot be null.
     * @param arrayId resource ID of the array
     * @param elementId resource ID of the element in the array
     * @return position of the {@code elementId} in the array
     * @throws IllegalArgumentException if {@code context} is null.
     * @throws NoSuchElementException if {@code elementId} is not in the array
     */
    /* package */static int getPositionForIdInArray(final Context context, final int arrayId, final int elementId)
    {
        if (Constants.IS_PARAMETER_CHECKING_ENABLED)
        {
            if (null == context)
            {
                throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
            }
        }

        TypedArray array = null;
        try
        {
            array = context.getResources().obtainTypedArray(arrayId);
            for (int x = 0; x < array.length(); x++)
            {
                if (array.getResourceId(x, 0) == elementId)
                {
                    return x;
                }
            }
        }
        finally
        {
            if (null != array)
            {
                array.recycle();
                array = null;
            }
        }

        throw new NoSuchElementException();
    }

    /**
     * Gets the position of an element in a typed array.
     *
     * @param context Application context. Cannot be null.
     * @param arrayId resource ID of the array
     * @param position position in the array to retrieve
     * @return resource id of element in {@code position}
     * @throws IllegalArgumentException if {@code context} is null.
     * @throws IndexOutOfBoundsException if {@code position} is not in the array
     */
    /* package */static int getResourceIdForPositionInArray(final Context context, final int arrayId, final int position)
    {
        if (Constants.IS_PARAMETER_CHECKING_ENABLED)
        {
            if (null == context)
            {
                throw new IllegalArgumentException("context cannot be null"); //$NON-NLS-1$
            }
        }

        TypedArray stateArray = null;
        try
        {
            stateArray = context.getResources().obtainTypedArray(arrayId);
            final int selectedResourceId = stateArray.getResourceId(position, 0);

            if (0 == selectedResourceId)
            {
                throw new IndexOutOfBoundsException();
            }

            return selectedResourceId;
        }
        finally
        {
            if (null != stateArray)
            {
                stateArray.recycle();
                stateArray = null;
            }
        }
    }
}
