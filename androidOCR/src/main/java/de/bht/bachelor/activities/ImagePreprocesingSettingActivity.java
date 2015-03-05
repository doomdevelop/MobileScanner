package de.bht.bachelor.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import de.bht.bachelor.R;
import de.bht.bachelor.setting.AppSetting;

public class ImagePreprocesingSettingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()..");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preprocesing);
		contrastCheckBox = (CheckBox) findViewById(R.id.contrastCheckBox);
		contrastCheckBox.setChecked(AppSetting.getInstance().isContrastEnhanceActive());
		contrastCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// is chkIos checked?
				if (((CheckBox) v).isChecked()) {
					AppSetting.getInstance().setContrastEnhanceStatus(AppSetting.CONTRAST_ENHANCE_ACTIVE);
				} else {
					AppSetting.getInstance().setContrastEnhanceStatus(AppSetting.CONTRAST_ENHANCE_INACTIVE);
				}

			}
		});
		contrastTextView = (TextView) findViewById(R.id.contrastTextView);
		// contrastCheckBox.s
	}

	private static final String TAG = ImagePreprocesingSettingActivity.class.getSimpleName();
	private TextView contrastTextView;
	private CheckBox contrastCheckBox;
}
