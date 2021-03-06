/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import java.io.OutputStream;
import java.io.PrintWriter;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SettingsRobolectricTestRunner.class)
@Config(manifest = TestConfig.MANIFEST_PATH, sdk = TestConfig.SDK_VERSION)
public class SettingsDumpServiceTest {
    private static final String PACKAGE_BROWSER = "com.android.test.browser";
    private static final String PACKAGE_NULL = "android";
    @Mock
    private PackageManager mPackageManager;
    @Mock
    private ResolveInfo mResolveInfo;
    private TestService mTestService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mPackageManager.resolveActivity(TestService.BROWSER_INTENT,
                PackageManager.MATCH_DEFAULT_ONLY)).thenReturn(mResolveInfo);
        mTestService = new TestService();
        mTestService.setPackageManager(mPackageManager);
    }

    @Test
    public void testDumpDefaultBrowser_DefaultBrowser_ReturnBrowserName() {
        mResolveInfo.activityInfo = new ActivityInfo();
        mResolveInfo.activityInfo.packageName = PACKAGE_BROWSER;

        assertThat(mTestService.dumpDefaultBrowser()).isEqualTo(PACKAGE_BROWSER);
    }

    @Test
    public void testDumpDefaultBrowser_NoDefault_ReturnNull() {
        mResolveInfo.activityInfo = new ActivityInfo();
        mResolveInfo.activityInfo.packageName = PACKAGE_NULL;

        assertThat(mTestService.dumpDefaultBrowser()).isEqualTo(null);
    }

    @Test
    public void testDump_ReturnJsonObject() throws JSONException {
        mResolveInfo.activityInfo = new ActivityInfo();
        mResolveInfo.activityInfo.packageName = PACKAGE_BROWSER;
        TestPrintWriter printWriter = new TestPrintWriter(System.out);

        mTestService.dump(null, printWriter, null);
        JSONObject object = (JSONObject)printWriter.getPrintObject();

        assertThat(object.get(TestService.KEY_SERVICE)).isNotNull();
    }

    /**
     * Test service used to pass in the mock {@link PackageManager}
     */
    private class TestService extends SettingsDumpService {
        private PackageManager mPm;

        public void setPackageManager(PackageManager pm) {
            mPm = pm;
        }

        @Override
        public PackageManager getPackageManager() {
            return mPm;
        }
    }

    /**
     * Test printWriter to store the object to be printed
     */
    private class TestPrintWriter extends PrintWriter {
        private Object mPrintObject;

        public TestPrintWriter(@NonNull OutputStream out) {
            super(out);
        }

        @Override
        public void println(Object object) {
            mPrintObject = object;
        }

        public Object getPrintObject() {
            return mPrintObject;
        }
    }
}
