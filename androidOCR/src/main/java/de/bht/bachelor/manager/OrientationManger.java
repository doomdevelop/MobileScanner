package de.bht.bachelor.manager;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.bht.bachelor.camera.OrientationMode;

/**
 * Created by and on 15.02.15.
 */
public class OrientationManger {
    private static OrientationManger INSTANCE;
    private CALCULATION_MODE calculation_mode;
    private OrientationMode currentOrientationMode;
    private float lastAzimuth = Float.MIN_VALUE;
    private static final String TAG = OrientationManger.class.getSimpleName();
    private float lastOrientValue = Float.MIN_VALUE;
    private Map<OrientationMode, Float> orientationModeAzimuthMap = new LinkedHashMap<OrientationMode, Float>();
    private volatile boolean busy = false;

    private OrientationManger() {
        createMap();
    }

    private void createMap() {
        orientationModeAzimuthMap.put(OrientationMode.PORTRAIT, Float.MIN_VALUE);
        orientationModeAzimuthMap.put(OrientationMode.LANDSCAPE, Float.MIN_VALUE);
        orientationModeAzimuthMap.put(OrientationMode.PORTRAIT_UPSIDE_DOWN, Float.MIN_VALUE);
        orientationModeAzimuthMap.put(OrientationMode.LANDSCAPE_UPSIDE_DOWN, Float.MIN_VALUE);
    }

    public static void createInstance() {
        if (INSTANCE != null) {
            throw new RuntimeException("Already instanced");
        }
        INSTANCE = new OrientationManger();
    }

    public static OrientationManger getInstance() {
        return INSTANCE;
    }

    public OrientationMode calculateOrientation(float orient) {
        Log.d(TAG, "calculateOrientation()... " + orient);
        synchronized (this) {
            if (orient > -1 && calculation_mode != null && calculation_mode == CALCULATION_MODE.GRAVITY_SENSOR) {
                lastAzimuth = Float.MIN_VALUE;

            }
            if (orient == -1) {
                calculation_mode = CALCULATION_MODE.GRAVITY_SENSOR;
                return currentOrientationMode;
            } else {
                calculation_mode = CALCULATION_MODE.ORIENTATION_VALUE;
            }

            if ((orient > 315 && orient <= 360) || (orient >= 0 && orient <= 45)) {
                if ((orient <= 360 && orient >= 340) || (orient > 00 && orient <= 20)) {
                    lastOrientValue = orient;

                }
                this.currentOrientationMode = OrientationMode.PORTRAIT;

            } else if (orient <= OrientationMode.LANDSCAPE.maxDegrees && orient > OrientationMode.LANDSCAPE.minDegrees) {
                if (orient >= 250 && orient <= 290) {
                    lastOrientValue = orient;
                }
                this.currentOrientationMode = OrientationMode.LANDSCAPE;

            } else if (orient <= OrientationMode.PORTRAIT_UPSIDE_DOWN.maxDegrees && orient > OrientationMode.PORTRAIT_UPSIDE_DOWN.minDegrees) {
                if (orient >= 160 && orient <= 200) {
                    lastOrientValue = orient;
                }
                this.currentOrientationMode = OrientationMode.PORTRAIT_UPSIDE_DOWN;

            } else if (orient <= OrientationMode.LANDSCAPE_UPSIDE_DOWN.maxDegrees && orient > OrientationMode.LANDSCAPE_UPSIDE_DOWN.minDegrees) {
                if (orient >= 60 && orient <= 110) {
                    lastOrientValue = orient;
                }
                this.currentOrientationMode = OrientationMode.LANDSCAPE_UPSIDE_DOWN;
            }
            if (lastOrientValue == orient) {
                removeLastOrientationValueFromMap();
                updateOrientationValueInTheMap(currentOrientationMode, lastOrientValue);
            }
        }
        return currentOrientationMode;
    }

    private void removeLastOrientationValueFromMap() {
        synchronized (this) {
            Log.d(TAG, "removeLastOrientationValueFromMap()... ");
            orientationModeAzimuthMap.clear();
            createMap();
        }
    }

    private void updateOrientationValueInTheMap(OrientationMode orientationMode, float value) {
        synchronized (this) {
            Log.d(TAG, "updateOrientationValueInTheMap()... orientationMode: " + orientationMode.name + " with value : " + value);
            orientationModeAzimuthMap.put(orientationMode, value);
        }
    }

    public OrientationMode calculateOrientationWithSensor(float[] gravSensorVals) {
//        Log.d(TAG, "calculateOrientationWithSensor()... ");

        synchronized (this) {
            if (busy) {
                return currentOrientationMode;
            }
            busy = true;
            if (calculation_mode != CALCULATION_MODE.GRAVITY_SENSOR) {
                lastAzimuth = gravSensorVals[0];
                busy = false;
                return currentOrientationMode;
            }
            if (currentOrientationMode == null) {
                busy = false;
                return null;
            }
            final float currentAzimuth = gravSensorVals[0];
//            Log.d(TAG, "currentAzimuth: " + currentAzimuth + ", lastOrientValue: " + lastOrientValue);
            if (lastOrientValue != Float.MIN_VALUE && orientationModeAzimuthMap.containsValue(lastOrientValue)) {
                Iterator<OrientationMode> it = orientationModeAzimuthMap.keySet().iterator();
                OrientationMode om = null;
                float insertedAzimuth = Float.MIN_VALUE;
                while (it.hasNext()) {
                    om = it.next();

                    if (orientationModeAzimuthMap.get(om).equals(lastOrientValue)) {
                        insertedAzimuth = currentAzimuth;
                        orientationModeAzimuthMap.put(om, insertedAzimuth);
                        Log.d(TAG, "founded FIRST correct orientation mode in map : " + om.name);
                        Log.d(TAG, "update azimuth for this value in the map with azimuth:  " + insertedAzimuth);
                    } else if (insertedAzimuth != Float.MIN_VALUE) {
                        Log.d(TAG, "founded NEXT correct orientation mode in map : " + om.name);
                        if (insertedAzimuth - 90 >= 0) {
                            insertedAzimuth -= 90;
                            orientationModeAzimuthMap.put(om, insertedAzimuth);
                        } else {
                            float dev = 90 - insertedAzimuth;
                            insertedAzimuth = 360 - dev;
                            orientationModeAzimuthMap.put(om, insertedAzimuth);
                        }
                        Log.d(TAG, "update azimuth for this value in the map with azimuth:  " + insertedAzimuth);
                    }
                }
                Log.d(TAG, "Now I will iterate the rest not updated orientations:  ");
                List<OrientationMode> list = new ArrayList<OrientationMode>(orientationModeAzimuthMap.keySet());
                printList(list);
                float lastAzimuthValue = Float.MIN_VALUE;
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (orientationModeAzimuthMap.get(list.get(i)) > Float.MIN_VALUE) {
                        lastAzimuthValue = orientationModeAzimuthMap.get(list.get(i));
                        Log.d(TAG, "founded azimuth " + lastAzimuthValue + " for orientation mode in map : " + list.get(i).name);

                    } else if (lastAzimuthValue > Float.MIN_VALUE) {
                        if (lastAzimuthValue + 90 <= 360) {
                            lastAzimuthValue += 90;
                            orientationModeAzimuthMap.put(list.get(i), lastAzimuthValue);
                        } else {
                            lastAzimuthValue = (lastAzimuthValue + 90) - 360;
                            orientationModeAzimuthMap.put(list.get(i), lastAzimuthValue);
                        }

                        Log.d(TAG, "update azimuth the map with value: " + insertedAzimuth + " for orientation mode in map :" + list.get(i).name);
                    }

                }
            } else {
                if (orientationModeAzimuthMap.containsValue(Float.MIN_VALUE)) {
                    Log.e(TAG, "can not compute azimuth for other orientations, no lastOrientValue!");
                } else {
                    Log.d(TAG, "The map is full up to date!  ");
                }
            }

            if (currentAzimuth == lastAzimuth) {
                busy = false;
                return currentOrientationMode;
            }
            if (!orientationModeAzimuthMap.containsValue(Float.MIN_VALUE)) {
                Iterator<OrientationMode> it = orientationModeAzimuthMap.keySet().iterator();
                OrientationMode bestResult = null;
                float bestValue = 0;
                float insertedAzimuth = Float.MIN_VALUE;
                while (it.hasNext()) {
                    OrientationMode om = it.next();
                    float value = orientationModeAzimuthMap.get(om);
                    float dif = Math.abs(currentAzimuth - value);
                    if (bestResult == null) {
                        bestValue = dif;
                        bestResult = om;
                    } else {
                        if (dif < bestValue) {

                            bestValue = dif;
                            bestResult = om;
                        }
                    }

                }
                if (bestResult != null) {
                    currentOrientationMode = bestResult;
                    Log.d(TAG, "FOUND NEW ORIENTATION BY AZIMUTH !: " + currentOrientationMode.name);
                }
            }
            busy = false;

        }
        return currentOrientationMode;
    }

    private void printList(List<OrientationMode> list) {
        Log.d(TAG, "list order:");
        for (OrientationMode mode : list) {
            Log.d(TAG, mode.name);
        }
    }

    public void reset() {
        lastAzimuth = Float.MIN_VALUE;
        calculation_mode = null;
        currentOrientationMode = null;
        lastOrientValue = Float.MIN_VALUE;
        orientationModeAzimuthMap.clear();
        createMap();
    }

    public static OrientationMode getOrientationModeByName(String orientationModeName) {
        OrientationMode orientationMode = null;
        if (orientationModeName.equals(OrientationMode.LANDSCAPE.name())) {
            orientationMode = OrientationMode.LANDSCAPE;
        } else if (orientationModeName.equals(OrientationMode.PORTRAIT.name())) {
            orientationMode = OrientationMode.PORTRAIT;
        } else if (orientationModeName.equals(OrientationMode.LANDSCAPE_UPSIDE_DOWN.name())) {
            orientationMode = OrientationMode.LANDSCAPE_UPSIDE_DOWN;
        } else if (orientationModeName.equals(OrientationMode.PORTRAIT_UPSIDE_DOWN.name())) {
            orientationMode = OrientationMode.PORTRAIT_UPSIDE_DOWN;
        }
        return orientationMode;
    }

    public OrientationMode getCurrentOrientationMode() {
        return currentOrientationMode;
    }

    private enum CALCULATION_MODE {
        ORIENTATION_VALUE, GRAVITY_SENSOR;
    }
}
