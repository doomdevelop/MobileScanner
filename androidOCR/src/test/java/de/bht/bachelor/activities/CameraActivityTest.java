package de.bht.bachelor.activities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import de.bht.bachelor.BuildConfig;
import de.bht.bachelor.RobolectricGradleTestRunner;
import de.bht.bachelor.setting.AppSetting;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import android.test.suitebuilder.annotation.LargeTest;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class,emulateSdk = 21)
public class CameraActivityTest {
    private CameraActivity activity;


     @Before
    public void setup() {
        Assert.assertNotNull(RuntimeEnvironment.application);
        AppSetting.getInstance().initLanguageMenager(RuntimeEnvironment.application);
        activity = Robolectric.buildActivity(CameraActivity.class).get();

    }

    @Test
    public void testActivity(){
        Assert.assertNotNull(activity);
    }
}
