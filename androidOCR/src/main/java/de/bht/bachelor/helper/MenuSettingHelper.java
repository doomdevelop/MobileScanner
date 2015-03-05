package de.bht.bachelor.helper;

public class MenuSettingHelper {
	private MenuSettingHelper() {
	}

	private static class InstanceHolder {
		public static final MenuSettingHelper INSTANCE = new MenuSettingHelper();
	}

	public static final MenuSettingHelper getInstance() {
		return InstanceHolder.INSTANCE;
	}

	public CameraFlashState getCurrentFlashState() {
		return currentFlashState;
	}

	public void setCurrentFlashState(CameraFlashState currentFlashState) {
		this.currentFlashState = currentFlashState;
	}

	public int getSeekbarValue() {
		return seekbarValue;
	}

	public void setSeekbarValue(int seekbarValue) {
		this.seekbarValue = seekbarValue;
	}

	private CameraFlashState currentFlashState = CameraFlashState.FLASH_OFF;
	private int seekbarValue = 0;
}
