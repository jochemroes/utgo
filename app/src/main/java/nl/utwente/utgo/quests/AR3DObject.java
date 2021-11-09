package nl.utwente.utgo.quests;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class is a representation of a 3D augmented reality object
 * containing a location and height to spawn it at, a boolean value indicating whether it
 * should turn and a URL pointing to the stored 3D object file
 */
public class AR3DObject {
    private LatLng location;
    private String url;
    private int height;
    private boolean turns;

    /**
     * Creates a new AR3DObject, following parameters must be given
     * @param loc - the location within the real world where the 3D object should spawn
     * @param url - the URL pointing to where the 3D object file is stored
     * @param height - the height at which the object should be spawned
     * @param turns - indicates whether it should turn or not
     */
    public AR3DObject(LatLng loc, String url, int height, boolean turns) {
        this.location = loc;
        this.url = url;
        this.height = height;
        this.turns = turns;
    }

    public LatLng getLocation() { return location; }

    public String getUrl() { return url; }

    public int getHeight() { return height; }

    public boolean isTurning() { return turns; }
}