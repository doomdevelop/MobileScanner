package de.bht.bachelor.helper;

import de.bht.bachelor.beans.OnLanguageChangeBean;
import de.bht.bachelor.camera.OrientationMode;

public class ChangeActivityHelper {

    private OrientationMode orientationMode;
    private byte[] rawPicture;
    public static final String RUN_CHECK_SETUP = "RUN_CHECK_SETUP";

    /**
     * Calling from MenuLanguageActivity
     */
    public static final String RUN_LANGUAGE_SETUP = "RUN_LANGUAGE_SETUP";

    private OnLanguageChangeBean onLanguageChangeBean;

    private ChangeActivityHelper() {
    }

    private static class InstanceHolder {
        public static final ChangeActivityHelper INSTANCE = new ChangeActivityHelper();
    }

    public static final ChangeActivityHelper getInstance() {
        return InstanceHolder.INSTANCE;
    }


    public byte[] getRawPicture() {
        return rawPicture;
    }

    public void setRawPicture(byte[] rawPicture) {
        this.rawPicture = rawPicture;
    }


    public OrientationMode getOrientationMode() {
        return orientationMode;
    }

    public void setOrientationMode(OrientationMode orientationMode) {
        this.orientationMode = orientationMode;
    }

    public OnLanguageChangeBean getOnLanguageChangeBean() {
        return onLanguageChangeBean;
    }

    public void setOnLanguageChangeBean(OnLanguageChangeBean onLanguageChangeBean) {
        this.onLanguageChangeBean = onLanguageChangeBean;
    }


}
