package nl.utwente.utgo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.HandMotionView;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nl.utwente.utgo.arcorelocation.DemoUtils;
import nl.utwente.utgo.arcorelocation.LocationMarker;
import nl.utwente.utgo.arcorelocation.LocationScene;
import nl.utwente.utgo.arcorelocation.NFSArFragment;
import nl.utwente.utgo.arcorelocation.sensor.DeviceLocation;
import nl.utwente.utgo.arcorelocation.utils.ARLocationPermissionHelper;
import nl.utwente.utgo.content.PopupContent;
import nl.utwente.utgo.quests.AR3DObject;
import nl.utwente.utgo.quests.Puzzle;
import nl.utwente.utgo.quests.Puzzle.PUZZLETYPE;

import static nl.utwente.utgo.R.layout.test_label;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayFragment extends FullScreenFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int ONLY_RENDER_WHEN_WITHIN = 30;
    private DeviceLocation deviceLocation;

    // TODO: Rename and change types of parameter
    private String mParam1;
    private String mParam2;

    private static final String TAG = "JAAP PLAYFRAGMENT";

    /* AR */
    private Context context;
    private NFSArFragment arFragment;
    private ArSceneView myArSceneView;
    private LocationScene locationScene;
    private boolean installRequested = true;
    private PlayAugmentedImage augmentedImage;
    private ArrayList<LocationMarker> locationMarkers = new ArrayList<>();

    private PopupContent popup;
    private boolean GPSPopup = false;
    private Node rotate;
    private Puzzle currentPuzzle;
    private Session session;
    private View popupButton;

    public PlayFragment(PopupContent popup) {
        this.popup = popup;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters
     * @return A new instance of fragment Play
     */
    // TODO: Rename and change types and number of parameters
    public static PlayFragment newInstance(PopupContent popup) {
        PlayFragment fragment = new PlayFragment(popup);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, "");
        args.putString(ARG_PARAM2, "");
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Oncreate.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        this.context = this.getContext();
    }

    /**
     * Resets the locationScene and does the animation.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        locationScene = null; //TODO save as much information without crashing later
        popupButton = view.findViewById(R.id.popup_button);
        popupButton.setOnClickListener(v -> {
            popup.show(this);
            popupButton.setVisibility(View.GONE);
        });
        return view;
    }

    public void showPopupButton() {
        popupButton.setVisibility(View.VISIBLE);
    }

    public void hidePopupButton() {
        popupButton.setVisibility(View.GONE);
    }

    /**
     * Creates the arFragment, locationScene and LocationMarkers.
     * also adds the OnUpdateListener that will look for augmented images and location based objects.
     *
     * @param view               the view of the Fragment
     * @param savedInstanceState the saved InstanceState
     */
    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /* AR */
        arFragment = (NFSArFragment) getChildFragmentManager().findFragmentById(R.id.ux_fragment);
        assert arFragment != null; //TODO iets netter? eigenlijk is arFragment nooit null volgensmij maarja
        myArSceneView = arFragment.getArSceneView();
        myArSceneView.setCameraDistance(50f);
        myArSceneView.getScene().addOnUpdateListener(
                frameTime -> {
                    if (arFragment.getArSceneView().getSession() == null) {
                        return;
                    }
                    if (locationScene == null) {
                        locationScene = new LocationScene(this.getActivity(), myArSceneView);
                        this.deviceLocation = locationScene.getDeviceLocation();
//                        LocationMarker hengel = addTextViewMarker(6.824097024465574, 52.264528509978405, "Hengel", 0, false);
//                        addTextViewMarker(6.899614881701486, 52.228087805136504, "De Vluchte", 0, false);
//                        addTextViewMarker(6.90033076988158, 52.22817570529443, "Enschede road", 0, false);
//                        addTextViewMarker(6.1544635508111885, 52.41039908561903, "Wijhe", 0, false);
//                        addTextViewMarker(6.890726040861125, 52.23123385798188, "de winkel enzo", 0, false);
//                        addTextViewMarker(6.154751643476858, 52.41032806905541, "schuur", 5, false);
//                        add3dModelMarker(6.90033076988158, 52.22817570529443, "dice enschede road", 20, false, "out.glb");
//                        add3dModelMarker(6.824097024465574, 52.264528509978405, "dice hengel", 20, false, "out.glb");
//                        add3dModelMarker(6.851412772558786, 52.24364839253651, "dice", 20, false, "out.glb");

//                        hengel.setRenderEvent(new LocationNodeRender() {
//                            @SuppressLint("SetTextI18n")
//                            @Override
//                            public void render(LocationNode node) {
//                                //voorbeeld renderEvent
//                                //Toast.makeText(context, "SPAWNED IN HENGEL!!!!", Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    } else {
                        Frame frame = null;
                        try {
                            frame = arFragment.getArSceneView().getSession().update();
                        } catch (CameraNotAvailableException e) {
                            e.printStackTrace();
                        }
                        if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                            return;
                        }
                        if (augmentedImage != null && currentPuzzle != null  &&currentPuzzle.getType() == PUZZLETYPE.AUGMENTED_IMAGE) {
                            augmentedImage.drawAugmentedImages(frame, myArSceneView);
                        }
                        Location current_location = locationScene.deviceLocation.currentBestLocation;
                        LocationManager temp = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
                        if (current_location != null && temp.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            popup.GPSFound(this);
                        } else {
                            popup.GPSMessage(this);
                            return;
                        }
                        if (locationMarkers.size() > 0) {
                            locationScene.processFrame(frame);
                        }
                    }

                });
    }

    /**
     * When we continue the fragment.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (locationScene != null) {
            locationScene.resume();
        }
        if (myArSceneView == null) {
            return;
        }

        if (myArSceneView.getSession() == null) {
//             If the session wasn't created yet, don't resume rendering.
//             This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                session = DemoUtils.createArSession(this.getActivity(), installRequested);
                arFragment.getPlaneDiscoveryController().hide();
                arFragment.getPlaneDiscoveryController().setInstructionView(null);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this.getActivity());
                    return;
                } else {
                    Config config = session.getConfig();
                    augmentedImage = new PlayAugmentedImage(context, session, config);
                    config.setFocusMode(Config.FocusMode.AUTO);
                    //config.setLightEstimationMode(Config.LightEstimationMode.DISABLED); // could be turned back off for speed

//                    String link1 = "https://cdn.discordapp.com/attachments/783067194904805386/821345737710370876/jpegggg.jpg";
//                    String link2 = "https://cdn.discordapp.com/attachments/783067194904805386/821345517912195072/negen.png";
//                    ArrayList<Pair<String, String>> x;
//                    x = new ArrayList<>();
//                    x.add(new Pair<>(link1, link2));
//                    if (!augmentedImage.setupAugmentedImage(x)) {
//                        //messageSnackbarHelper.showError(this, "Could not setup augmented image database");
//                        Toast.makeText(context, "error", Toast.LENGTH_SHORT);
//                    }
                    session.configure(config);

                    myArSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this.getActivity(), e);
            }
        }
        try {
            myArSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this.getContext(), "Unable to get camera", ex);
            //finish();
            return;
        }

        try {
            myArSceneView.getSession().resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        if (myArSceneView.getSession() != null) {
            //showLoadingMessage();
        }
        arFragment.onResume();
    }

    /**
     * Makes sure we call locationScene.pause() and myArSceneView.pause;.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (locationScene != null) {
            locationScene.pause();
        }
        myArSceneView.pause();
        arFragment.onPause();
    }

    /**
     * Calls on the destuction of the fragment.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        myArSceneView.destroy();
        arFragment.onDestroy();
    }

    private Node getTextViewNode(String name) {
        View view = getView();
        ViewGroup dummy = new FrameLayout(context);
        TextView tempview = (TextView) LayoutInflater.from(context).inflate(test_label, dummy, false);
        tempview.setText(name); //TODO known problem that setText is sometimes slower then the builder!
        Node base = new Node();
        ViewRenderable.builder()
                .setView(this.getContext(), tempview)
                .build()
                .thenAccept(renderable -> base.setRenderable(renderable)).exceptionally(
                throwable -> {
                    Toast toast =
                            Toast.makeText(this.getContext(), "Unable to load renderable ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Log.i(TAG, "unable to make the textViewRenderable :(");
                    return null;
                });
        base.setEnabled(true);
        Context c = this.getContext();
        base.setOnTapListener((v, event) -> {
            Toast.makeText(
                    c, name + " touched.", Toast.LENGTH_LONG)
                    .show();
        });
        return base;
    }

    /**
     * makes a node that has the 3d object found in the url. should be a sceneform compatible glb file.
     *
     * @param url refers to the sceneform 1.17 .glb file
     * @param rotate
     * @return the Node that renders the 3d object
     */
    private Node get3DNode(String url, boolean rotate) {
        TransformableNode base = new TransformableNode(arFragment.getTransformationSystem());
        base.setEnabled(true);
        RotatingNode innerNode = new RotatingNode(false, true, 0f);

        if (url.contains(".glb")) {
            AtomicInteger i = new AtomicInteger(1);

            WeakReference<PlayFragment> weakActivity = new WeakReference<>(this);
            ModelRenderable.builder()
                    .setSource(
                            context,
                            Uri.parse(url))
                    .setIsFilamentGltf(true)
                    .build()
                    .thenAccept(
                            modelRenderable -> {
                                if (rotate) {
                                    locationScene.forceUpdate(); //force an update so that the animations work
                                }
                                modelRenderable.setShadowCaster(false);
                                modelRenderable.setShadowReceiver(false);
                                innerNode.setRenderable(modelRenderable);
                            }
                    )
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(context, "Unable to load 3d renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                Log.i(TAG, "unable to make the 3d renderable! :( ");
                                return null;
                            });
        } else {
            new ImageRenderableFromUrl(innerNode).execute(url);
        }
        this.rotate = new Node();
        if (rotate) {
            this.rotate.setWorldRotation(Quaternion.axisAngle(new Vector3(0.5f, 0.5f, 0), 45));
            innerNode.onActivate();
            innerNode.setDegreesPerSecond(36f);
        }
        this.rotate.setParent(base);
        innerNode.setParent(this.rotate);
        innerNode.setLocalScale(new Vector3(10f, 10f, 10f));


        innerNode.setOnTapListener((v, event) -> {
            locationScene.forceUpdate();
        });
        return base;
    }

    /**
     * Creates a LocationMarker with the given parameters.
     *
     * @param longitude double the longitude on which the marker is located
     * @param latitude  double the latitude on which the marker is located
     * @param name      a String that holds the name of the marker
     * @param height    an int that with the height of the marker in meters
     * @return The LocationMarker with all parameters set
     */
    private LocationMarker addTextViewMarker(double longitude, double latitude,
                                             String name, int height, boolean isQuest) {
        LocationMarker marker = new LocationMarker(
                longitude, latitude, getTextViewNode(name)
        );
        marker.setOnlyRenderWhenWithin(40);
        marker.setScalingMode(LocationMarker.ScalingMode.FIXED_SIZE_ON_SCREEN);
        marker.setHeight(height);
        if (isQuest) {
            locationMarkers.add(marker);
        }
        locationScene.mLocationMarkers.add(marker);
        return marker;
    }

    /**
     * Removes all location markers that are used by a puzzle or quest.
     *
     * @return always true when done
     */
    public boolean removeQuestLocationMarkers() {

        for (LocationMarker lm : locationMarkers) {
            if (lm.anchorNode != null) {
                lm.anchorNode.getAnchor().detach();
                lm.anchorNode.setEnabled(false);
                lm.anchorNode = null;
                locationScene.mLocationMarkers.remove(lm);
            }
        }
        return true;
    }

    /**
     * Adds a 3d model (hardcoded right now) to the locationScene.
     *
     * @param longitude longitude of the position
     * @param latitude  latitude of the position
     * @param name      name of the marker (should be unique)
     * @param height    height of the marker relative of the phone
     * @param isQuest   true if it is a quest marker. this ensures deletion
     * @return the locationmarker that
     */
    private LocationMarker add3dModelMarker(double longitude, double latitude, String name,
                                            int height, boolean isQuest, String url, boolean rotate) {
        LocationMarker marker = new LocationMarker(longitude, latitude, get3DNode(url, rotate));
        marker.setOnlyRenderWhenWithin(ONLY_RENDER_WHEN_WITHIN);
        marker.setHeight(height);
        if (isQuest) {
            locationMarkers.add(marker);
        }
//        marker.setRenderEvent(new LocationNodeRender() { //TODO it is being called every time it moves apparently
//                                  @SuppressLint("SetTextI18n")
//                                  @Override
//                                  public void render(LocationNode node) {
//                                      Activity activity = (Activity) context;
//                                      Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE); // Stolen from stackoverflow https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate-with-different-frequency
//                                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                          v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
//                                      } else {
//                                          //deprecated in API 26
//                                          v.vibrate(50);
//
//                                      }
//                                  }
//                              });
        locationScene.mLocationMarkers.add(marker);
        return marker;
    }


    /**
     * Should be called when starting a new puzzle!
     * @param puzzle the Puzzle that has to be loaded
     */
    public void setPuzzle(Puzzle puzzle) {
        //hidePopup();
        //showStory(puzzle);
        this.currentPuzzle = puzzle;
        setPuzzleHelper(puzzle, Firestore.getQuestRole());
    }

    /**
     * Generates an alert dialog that shows the story (could be nicer but this is the easy solution)
     * @param puzzle - which story to show
     */
    private void showStory(Puzzle puzzle) {
        String message = "";
        if (puzzle.getOrder() == 0) { message = message + "Hey, " + Firestore.player.getName() + "!\n\n"; }
        message = message + puzzle.getStory();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        builder.setCancelable(false)
                .setTitle("Story:")
                .setMessage(message)
                .setPositiveButton("Next", null);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(arg0 -> {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.text_color));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.text_color));
        });
        dialog.show();
    }

    /**
     * Sets the augmented images or location based objects.
     *
     * @param puzzle the puzzle that has to be loaded
     * @param teamPosition the player's position in the team
     */
    public void setPuzzleHelper(Puzzle puzzle, int teamPosition) {
        //String title = puzzle.getEnclosingQuest().getTitle() + " (" +
        //        (puzzle.getOrder() +1) + "/" + puzzle.getEnclosingQuest().getnPuzzles() + ")";
        //displaySubmit(title, puzzle.getPrompt(teamPosition), puzzle);
        popup.submit(puzzle, this);
        popup.show(this);
        switch (puzzle.getType()) {
            case LOCATION_BASED:
                arFragment.getArSceneView().getScene().getCamera().setFarClipPlane(40);//TASTERDAYSETTING
                Log.i(TAG, "setting Location based quest with teamPosition: " + teamPosition);
                List<AR3DObject> listThreeD = puzzle.getAr3DObjectsMap().get(teamPosition);
                for (AR3DObject threeD : listThreeD) {
                    add3dModelMarker(threeD.getLocation().longitude, threeD.getLocation().latitude,
                            "", threeD.getHeight(), true, threeD.getUrl(), threeD.isTurning());
                }
                setLocationBased();
                locationScene.forceUpdate();
                break;
            case AUGMENTED_IMAGE: // TODO teamPosition
                arFragment.getArSceneView().getScene().getCamera().setFarClipPlane(30);//TASTERDAYSETTING
                Log.i(TAG, "setting augmented image quest with teamPosition: " + teamPosition);
                Map<Integer, List<Pair<String, String>>> list = puzzle.getAugmentedImageUrlMap();
                List<Pair<String, String>> list2 = list.get(teamPosition);
                setAugmentedImage();
                augmentedImage.setupAugmentedImage((ArrayList<Pair<String, String>>) list2);
                break;
            case REGULAR:
                arFragment.getArSceneView().getScene().getCamera().setFarClipPlane(30);//TASTERDAYSETTING
                setRegular();
                Log.i(TAG, "regular quest nothing has to be done in AR");
                break;
        }
    }

    private void setLocationBased() {
        Config config = session.getConfig().setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
        session.configure(config);
        arFragment.getPlaneDiscoveryController().show();
        arFragment.getPlaneDiscoveryController().setInstructionView(new HandMotionView(context));

    }

    private void setAugmentedImage() {
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        //config is set in PlayAugmented Image GetBitmapFromUrl's onPostExecute() function.
    }

    private void setRegular() {
        Config config = session.getConfig().setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
        session.configure(config);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
    }
    /**
     * Pings google.com to check if we are connected to the internet or not.
     *
     * @return true if device has internet
     */
    private boolean getConnectedInternet() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    public DeviceLocation getDeviceLocation() {
        return deviceLocation;
    }


    public class ImageRenderableFromUrl extends AsyncTask<String, Void, Bitmap> {
        private Node node;

        public ImageRenderableFromUrl(Node node) {
            this.node = node;
        }

        @Override
        protected Bitmap doInBackground(String... url) {
            String pictureURL = url[0];
            Bitmap result = null;
            boolean downloaded = false;
            while (!downloaded) { // can be stuck if puzzle solved without object
                try {
                    InputStream inputStreamAugmented = new URL(pictureURL).openStream();
                    result = BitmapFactory.decodeStream(inputStreamAugmented);
                    downloaded = true;
                } catch (IOException e) {
                    Log.i(TAG, "no internet for downloading location based image/object");
                    Log.i(TAG, e.getMessage());
                    //e.printStackTrace();
                }
            }
            return result;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Node node = this.node;
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(bitmap);

            ViewRenderable.builder().setView(context, imageView).build().thenAccept(renderable -> {
                renderable.setShadowCaster(false);
                renderable.setShadowReceiver(false);
                node.setRenderable(renderable);
                node.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), -90));
                locationScene.forceUpdate();
            });

                //augmentedImageDatabase.addImage(bitmap.get(i).second, bitmap.get(i).first);
        }
    }

    public PlayAugmentedImage getAugmentedImage() {
        return augmentedImage;
    }

}