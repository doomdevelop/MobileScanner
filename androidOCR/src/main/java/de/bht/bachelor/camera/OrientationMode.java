package de.bht.bachelor.camera;

public enum OrientationMode {

	PORTRAIT("PORTRAIT",-200,5,-25,25,45,315),//315-360 / 0-45
    LANDSCAPE("LANDSCAPE",-180,180,-15,90,225,315),
    PORTRAIT_UPSIDE_DOWN("PORTRAIT_UPSIDE_DOWN",-20,140,-25,25,135,225),
    LANDSCAPE_UPSIDE_DOWN("LANDSCAPE_UPSIDE_DOWN",-25,25,-140,20,45,135);

    public String name;
    public final int minPitch;
    public final int maxPitch;
    public final int minRoll;
    public final int maxRoll;
    public final int minDegrees;
    public final int maxDegrees;
    private OrientationMode(String name,int minPitch,int maxPitch,int minRoll,int maxRoll,int minDegrees,int maxDegrees){
        this.name = name;
        this.maxPitch = maxPitch;
        this.minPitch = minPitch;
        this.minRoll = minRoll;
        this.maxRoll = maxRoll;
        this.minDegrees = minDegrees;
        this.maxDegrees = maxDegrees;
    }
}
