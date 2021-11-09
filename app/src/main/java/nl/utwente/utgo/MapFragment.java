package nl.utwente.utgo;

import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.ArrayList;

import nl.utwente.utgo.quests.Quest;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends FullScreenFragment implements OnMapReadyCallback
        , GoogleMap.OnMarkerClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER ok
    private static final String TAG = "MAP";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int STD_ZOOM = 15;
    private static final LatLng UT_LOC = new LatLng(52.241748, 6.853155);
    private static final String MAP_PREF = "map_pref";
    private static final String LAST_LAT = "last_latitude";
    private static final String LAST_LON = "last_longitude";

    //map
    private GoogleMap mMap;

    //location updates
    private FusedLocationProviderClient mFusedLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLastLocation;
    private boolean moved = false;

    //database things
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference xpquestCol = db.collection("xp_quest");
    private CollectionReference rewardquestCol = db.collection("reward_quest");
    private OnCompleteListener<QuerySnapshot> questsToMarker;
    private List<Marker> questMarkers;

    private View cover;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Constructor of MapFragment
     */
    public MapFragment() {
        questMarkers = new ArrayList<>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Map.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //initialization location updater
        mFusedLocation = LocationServices.getFusedLocationProviderClient(getActivity());

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locRes) {
                List<Location> locList = locRes.getLocations();
                if (locList.size() > 0) {
                    //last location in the list is up-to-date...
                    mLastLocation = locList.get(locList.size() - 1);
                    Log.i(TAG, "Location update (lat/lon): " +
                            mLastLocation.getLatitude() + " " +
                            mLastLocation.getLongitude());

                    LatLng curLoc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                    if (moved) {return;}

                    //compute where to move the camera and move it
                    float[] distance = new float[10];
                    Location.distanceBetween(curLoc.latitude, curLoc.longitude,
                            UT_LOC.latitude, UT_LOC.longitude, distance);

                    if (mMap == null) {return;}

                    if (distance[0] < 1000) {
                        //location is within UT -> zoom on my location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, STD_ZOOM));
                    } else {
                        //location outside UT -> zoom on UT
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UT_LOC, STD_ZOOM));
                    }
                    moved = true;
                }
            }
        };

        try {
            mFusedLocation.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.myLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Failed permission check for FusedLocation");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        cover = view.findViewById(R.id.CoverMap);

        //stuff to register the map
        SupportMapFragment supMapFrag = new SupportMapFragment();
        //workaround because mapView is not accessible from manager directly (?)
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.mapView, supMapFrag)
                .commit();
        moved = false;

        supMapFrag.getMapAsync(MapFragment.this);
        Log.i(TAG, "onMapReadyCallback registered!");
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            if (getCoverAnimation()) {
                onResume();
                Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.cover_fade_out);
                cover.startAnimation(fadeOut);
            }
            cover.setVisibility(View.INVISIBLE);
        }
        else {
            onPause();
            /*Animation fadeIn = AnimationUtils.loadAnimation(getContext(),R.anim.cover_fade_in);
            cover.startAnimation(fadeIn);
            cover.setVisibility(View.VISIBLE);*/
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mLastLocation == null) {Log.e(TAG, "no last location, no save"); return;}
//
//        SharedPreferences pref = getContext().getSharedPreferences(MAP_PREF, 0);
//        pref.edit().putFloat(LAST_LAT, (float) mLastLocation.getLatitude())
//                .putFloat(LAST_LON, (float) mLastLocation.getLongitude())
//                .apply();
        Log.i(TAG, "PAUSED");
    }

    @Override
    public void onResume() {
        super.onResume();
//        SharedPreferences pref = getContext().getSharedPreferences(MAP_PREF, 0);
//        Location res = new Location("");
//        res.setLatitude((double) pref.getFloat(LAST_LAT, 0));
//        res.setLongitude((double) pref.getFloat(LAST_LON, 0));
//        mLastLocation = res;
//        if (mMap == null) {return;}
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(res.getLatitude(), res.getLongitude()), STD_ZOOM));
        Log.i(TAG, "RESUMED");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * @param googleMap is the map object that is returned from the API
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //zoom default
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UT_LOC, STD_ZOOM));
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission check failed for getting Location");
        }

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //adding quest markers from database
        loadMarkers();

        // set the map style
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        // set the Click Listener for the markers
        mMap.setOnMarkerClickListener(this);

        //positioning of the maps view
        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition());
    }

    /**
     * Loads all quests previously loaded from the database into markers
     */
    public void loadMarkers() {
        if(mMap == null) { return; }
        for (Quest q : Firestore.getQuestsList()) {
            MarkerOptions opt = new MarkerOptions().position(q.getLocation()).title(q.getTitle());
            opt.icon(BitmapDescriptorFactory.defaultMarker(PrettyPrint.rgbToHue(q.getColor())));
            opt.snippet(q.getDescription());
            Marker m = mMap.addMarker(opt);
            m.setTag(q);
            questMarkers.add(m);
        }
    }

    /**
     * This method gets called whenever a marker is clicked by the user
     * @param marker - the clicked marker on the map
     * @return - true if success, false if not
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), STD_ZOOM), 500, null);
        return true;
    }

    /**
     * Checks the given quest against marker tags, used for viewing a marker in map from QuestsFragment
     * @param quest - the selected quest to view in map
     */
    public void selectMarker(Quest quest) {
        for (Marker marker : questMarkers) {
            if (marker.getTag() == quest) {
                marker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), STD_ZOOM));
                break;
            }
        }
    }

    /**
     * @return - the map object of this fragment
     */
    public GoogleMap getMap() { return mMap; }
}