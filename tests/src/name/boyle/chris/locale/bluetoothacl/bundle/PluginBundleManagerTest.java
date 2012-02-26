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

package name.boyle.chris.locale.bluetoothacl.bundle;

import android.os.Bundle;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

/**
 * Tests {@link PluginBundleManager}.
 */
public final class PluginBundleManagerTest extends TestCase
{

    /**
     * Tests the extra constants haven't been accidentally changed.
     */
    @SmallTest
    public void testExtraConstants()
    {
        /*
         * NOTE: This test is expected to fail initially when you are adapting this example to your own plug-in. Once you've
         * settled on constant names for your Intent extras, go ahead and update this test case.
         *
         * The goal of this test case is to prevent accidental renaming of the Intent extras. If the extra is intentionally
         * changed, then this unit test needs to be intentionally updated.
         */
        assertEquals("name.boyle.chris.locale.bluetoothacl.extra.BOOLEAN_STATE", PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE); //$NON-NLS-1$
        assertEquals("name.boyle.chris.locale.bluetoothacl.extra.INT_VERSION_CODE", PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE); //$NON-NLS-1$
    }

    /**
     * Tests the {@link PluginBundleManager#isBundleValid(Bundle)} method with a correct bundle
     */
    @SmallTest
    public void testVerifyBundleCorrect()
    {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, true);
        bundle.putInt(PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE, 0);
        assertTrue(PluginBundleManager.isBundleValid(bundle));

        bundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, false);
        bundle.putInt(PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE, 1);
        assertTrue(PluginBundleManager.isBundleValid(bundle));
    }

    /**
     * Tests the {@link PluginBundleManager#isBundleValid(Bundle)} method with a null bundle
     */
    @SmallTest
    public void testVerifyBundleNull()
    {
        assertFalse(PluginBundleManager.isBundleValid(null));
    }

    /**
     * Tests the {@link PluginBundleManager#isBundleValid(Bundle)} method with bundle that is missing the required extra
     */
    @SmallTest
    public void testVerifyBundleMissingExtra()
    {
        assertFalse(PluginBundleManager.isBundleValid(new Bundle()));
    }

    /**
     * Tests the {@link PluginBundleManager#isBundleValid(Bundle)} method with bundle that contains extra items
     */
    @SmallTest
    public void testVerifyBundleExtraItems()
    {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, true);
        bundle.putInt(PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE, 0);
        bundle.putString("test", "test"); //$NON-NLS-1$//$NON-NLS-2$
        assertFalse(PluginBundleManager.isBundleValid(bundle));
    }

    /**
     * Tests the {@link PluginBundleManager#isBundleValid(Bundle)} method with bundle that contains a mapping to the wrong type
     */
    @SmallTest
    public void testVerifyBundleWrongType()
    {
        {
            final Bundle bundle = new Bundle();
            bundle.putString(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, "test"); //$NON-NLS-1$
            bundle.putInt(PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE, 0);
            assertFalse(PluginBundleManager.isBundleValid(bundle));
        }

        {
            final Bundle bundle = new Bundle();
            bundle.putBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOLEAN_STATE, true);
            bundle.putString(PluginBundleManager.BUNDLE_EXTRA_INT_VERSION_CODE, "test"); //$NON-NLS-1$
            assertFalse(PluginBundleManager.isBundleValid(bundle));
        }
    }
}
