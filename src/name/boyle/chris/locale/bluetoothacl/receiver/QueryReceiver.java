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

package name.boyle.chris.locale.bluetoothacl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import name.boyle.chris.locale.bluetoothacl.Constants;
import name.boyle.chris.locale.bluetoothacl.bundle.BundleScrubber;
import name.boyle.chris.locale.bluetoothacl.bundle.PluginBundleManager;
import name.boyle.chris.locale.bluetoothacl.ui.EditActivity;

/**
 * This is the "query" BroadcastReceiver for a Locale Plug-in condition.
 */
public final class QueryReceiver extends BroadcastReceiver
{

    /**
     * @param context {@inheritDoc}.
     * @param intent the incoming {@link com.twofortyfouram.locale.Intent#ACTION_QUERY_CONDITION} Intent. This should always
     *            contain the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by {@link EditActivity} and
     *            later broadcast by Locale.
     */
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        /*
         * Always be sure to be strict on input parameters! A malicious third-party app could always send an empty or otherwise
         * malformed Intent. And since Locale applies settings in the background, the plug-in definitely shouldn't crash in the
         * background
         */

        if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION.equals(intent.getAction()))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG, String.format("Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            }
            return;
        }

        /*
         * A hack to prevent a private serializable classloader attack
         */
        BundleScrubber.scrub(intent);
        BundleScrubber.scrub(intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE));

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

        /*
         * Verify the Bundle is correct
         */
        if (!PluginBundleManager.isBundleValid(bundle))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG, "Received an invalid bundle"); //$NON-NLS-1$
            }

            return;
        }

        /*
         * Handle the query
         */
        final String deviceMac = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_BLUETOOTH_MAC);
        final boolean conditionState = bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE);

        final boolean isDeviceConnected = BluetoothConnectionReceiver.isConnected(context, deviceMac);

        if (Constants.IS_LOGGABLE)
        {
            Log.v(Constants.LOG_TAG, String.format("Screen state is %b and condition state is %b", Boolean.valueOf(isDeviceConnected), Boolean.valueOf(conditionState))); //$NON-NLS-1$
        }

        if (isDeviceConnected == conditionState)
        {
           setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_SATISFIED);
        }
        else
        {
           setResultCode(com.twofortyfouram.locale.Intent.RESULT_CONDITION_UNSATISFIED);
        }
    }
}
