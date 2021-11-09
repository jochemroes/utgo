package nl.utwente.utgo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Handeles all augmented images for {@link PlayFragment}. Setting the database and
 * calling the drawaugmentedImages function will update all augmented images in the AR session.
 *
 */
public class PlayAugmentedImage {

    private static final String TAG = "AugmentedImage";
    private final Context context;
    private final Config config;
    private HashMap<String, Bitmap> hashmapImagesToDisplay;
    private boolean useSingleImage = true;
    private Session session;
    private final Map<Integer, Pair<AugmentedImage, Anchor>> augmentedImageMap = new HashMap<>();
    private Map<AugmentedImage, Pair<Boolean, Node>> drawnImages = new HashMap<>();
    private AugmentedImageDatabase augmentedImageDatabase;


    /**
     * Constructor PlayAugmented Image.
     *
     * @param context the Context of the arFragment not null
     * @param session the Session of the arFragment not null
     */
    PlayAugmentedImage(Context context, Session session, Config config) {
        this.context = context;
        this.session = session;
        this.config = config;
        assert (context != null && session != null); //TODO Jaap code
    }

    /**
     * Draws the augmentedImages if found and tracked. deletes them if they are not tracked.
     *
     * @param frame       the Frame of the Session
     * @param mySceneView the SceneView of the ArFragment
     */
    public void drawAugmentedImages(
            Frame frame, ArSceneView mySceneView) {
        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);

        // Iterate to update augmentedImageMap, remove elements we cannot draw.
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
//                    String text = String.format("Detected Image %d", augmentedImage.getIndex());
//                    Toast.makeText(context, "text", Toast.LENGTH_SHORT);
                    //messageSnackbarHelper.showMessage(this, text);
                    int x = 0;
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
//                    this.runOnUiThread(
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    fitToScanView.setVisibility(View.GONE);
//                                }
//                            });

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage.getIndex())) {
                        Anchor centerPoseAnchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
                        augmentedImageMap.put(
                                augmentedImage.getIndex(), Pair.create(augmentedImage, centerPoseAnchor));
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage.getIndex());
                    break;

                default:
                    break;
            }
        }

        // Draw all images in augmentedImageMap
        for (Pair<AugmentedImage, Anchor> pair : augmentedImageMap.values()) {
            AugmentedImage augmentedImage = pair.first;
            Anchor centerAnchor = augmentedImageMap.get(augmentedImage.getIndex()).second;
            switch (augmentedImage.getTrackingState()) {
                case TRACKING:
                    Pair<Boolean, Node> drawn = drawnImages.get(augmentedImage);
                    if (drawn == null || !drawn.first) {

                        if (drawn != null) {
                            drawn.second.setEnabled(true);
                            drawnImages.put(augmentedImage, Pair.create(true, drawn.second));
                            break;
                        }
                        //vibrate
                        Activity activity = (Activity) context;

                        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE); // Stolen from stackoverflow https://stackoverflow.com/questions/13950338/how-to-make-an-android-device-vibrate-with-different-frequency
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            //deprecated in API 26
                            v.vibrate(200);
                        }

                        String draw = augmentedImage.getName();
                        //int id = Integer.parseInt(draw);
                        Node node = new Node();
                        //getAdjustedImage(id, node);
                        if (!setPictureRenderable(node, draw)) {
                            break; //image url not in map yet
                        }

                        //augmentedImageNode = new AugmentedImageNode(context, "jpeggg"); //TODO in construction
                        //augmentedImageNode.setImage(augmentedImage);

                        //Pose myPose = centerAnchor.getPose();
                        //Anchor myAnchor = mySceneView.getSession().createAnchor(myPose);
                        AnchorNode anchorNode = new AnchorNode(centerAnchor);
                        anchorNode.setParent(mySceneView.getScene());

//                        TransformableNode transnode = new TransformableNode(arFragment.getTransformationSystem());
//                        transnode.setParent(anchorNode);
//                        transnode.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), -90));
//                        transnode.setEnabled(true);
//                        transnode.setRenderable(renderable);
                        node.setParent(anchorNode);
                        node.setLocalRotation(Quaternion.axisAngle(new Vector3(1, 0, 0), -90));
                        node.setEnabled(true);
                        //renderable.setShadowCaster(false); //now handeled in getAdjusted Image yet to be tested
                        //node.setRenderable(renderable); // now handeled in getAdjusted Image yet to be tested.
                        float width = augmentedImage.getExtentZ();
                        node.setLocalScale(new Vector3(width, width, width));
                        node.setLocalPosition(new Vector3(0, 0, (width)));

                        //transnode.setWorldScale(new Vector3(0, 0 , 0));
                        drawnImages.put(augmentedImage, Pair.create(true, node));
                    }
                    break;
                case STOPPED: //delete
                    // KNOWN ISSUE, more than one shadow will spawn on the spawned images if shadows are enabled
                    //this indicates that the node removal goes wrong. or sceneform does something wrong.
                    Node node = drawnImages.get(augmentedImage).second;
                    node.setEnabled(false);
//                    AnchorNode anchorNode = (AnchorNode) node.getParent();
//                    anchorNode.getAnchor().detach();
                    drawnImages.put(augmentedImage, Pair.create(false, node));
                case PAUSED:
                    Node nodePaused = drawnImages.get(augmentedImage).second;
                    nodePaused.setEnabled(false);
//                    AnchorNode anchorNodePaused = (AnchorNode) nodePaused.getParent();
//                    anchorNodePaused.getAnchor().detach();
                    drawnImages.put(augmentedImage, Pair.create(false, nodePaused));
            }
        }
    }

    public void removeNodesAugmented() {
        for (Pair<Boolean, Node> key : drawnImages.values()) {
            Node node =key.second;
            AnchorNode anchorNode = (AnchorNode) node.getParent();
            anchorNode.setEnabled(false);
            anchorNode.getAnchor().detach();
        }
    }

    /**
     * Creates a imageDatabase which has one image or a imagedb.
     * This method is temporary as long as Quest.java is not done.
     *
     * @return true if the setup was successful
     */
    public boolean setupAugmentedImageDatabase() {
        // There are two ways to configure an AugmentedImageDatabase:
        // 1. Add Bitmap to DB directly
        // 2. Load a pre-built AugmentedImageDatabase
        // Option 2) has
        // * shorter setup time
        // * doesn't require images to be packaged in apk.
        if (useSingleImage) {
            Bitmap augmentedImageBitmap = loadAugmentedImageBitmap("jpegggg.jpg");
            if (augmentedImageBitmap == null) {
                return false;
            }

            augmentedImageDatabase = new AugmentedImageDatabase(session);
            augmentedImageDatabase.addImage(String.valueOf(R.drawable.richpng), augmentedImageBitmap);
            // If the physical size of the image is known, you can instead use:
            //     augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
            // This will improve the initial detection speed. ARCore will still actively estimate the
            // physical size of the image as it is viewed from multiple viewpoints.
        } else {
            // This is an alternative way to initialize an AugmentedImageDatabase instance,
            // load a pre-existing augmented image database.
            try (InputStream is = context.getAssets().open("sample_database.imgdb")) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image database.", e);
                return false;
            }
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    /**
     * set the augmentedImageDatabase to look for one image.
     *
     * @param filename   the file name of the picture to look for. example: "jpegggg.jpg" (in assets)
     * @param drawableID the id of the picture to draw example: R.drawable.numbernine
     * @return returns false if setting the augmentedImageDatabase threw an error
     */
    private boolean setAugmentedImage(String filename, int drawableID) {
        Bitmap augmentedImageBitmap = loadAugmentedImageBitmap(filename);
        if (augmentedImageBitmap == null) {
            return false;
        }

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage(String.valueOf(R.drawable.richpng), augmentedImageBitmap);
        // If the physical size of the image is known, you can instead use:
        //     augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
        // This will improve the initial detection speed. ARCore will still actively estimate the
        // physical size of the image as it is viewed from multiple viewpoints.
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    /**
     * Set multiple augmented images in the config.
     *
     * @param filename the name of the imgdb file. example: "sample_database.imgdb
     * @return true if successful, false if an error occurred
     */
    public boolean setAugmentedImagesDatabase(String filename) {
        try (InputStream is = context.getAssets().open(filename)) {
            augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image database.", e);
            return false;
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    /**
     * Set an augmentedImageDatabase with the filenames.
     *
     * @param augmentedImageFilenames filenames of the picture to look for. example: "jpegggg.jpg" (in assets)
     * @param arPictureFilenames      the pictures to load in AR. example: String.valueOf(R.drawable.numbernine)
     * @return true if setting the augmentedImageDatabase was successful and false if not
     */
    public boolean makeAugmentedImagesDatabase(String[] augmentedImageFilenames, String[] arPictureFilenames) {
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        Bitmap bitmap = null;
        for (int i = 0; i < augmentedImageFilenames.length; i++) {
            String filename = augmentedImageFilenames[i];
            try (InputStream inputStream = context.getAssets().open(filename)) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                Log.e(TAG, "I/O exeption loading augmented image bitmap", e);
                return false;
            }
            augmentedImageDatabase.addImage(arPictureFilenames[i], bitmap);
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }


    /**
     * Load a single image in a BitMap.
     *
     * @param filename the name of the file found in assets
     * @return the Bitmap with the image found with the filename
     */
    private Bitmap loadAugmentedImageBitmap(String filename) {
        try (InputStream is = context.getAssets().open(filename)) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        return null;
    }

    /**
     * Sets the ImageView renderable to use with an augmented image.
     *
     * @param id the id of the imageView to display
     */
    private void getAdjustedImage(int id, Node node) {
        Drawable d = ContextCompat.getDrawable(context, id);
        ImageView imgView = new ImageView(context);
        imgView.setImageResource(id);
        imgView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        ViewRenderable.builder()
                .setView(context, imgView)
                .build()
                .thenAccept(renderable -> {
                    renderable.setShadowCaster(false);
                    node.setRenderable(renderable);
                }).exceptionally(
                throwable -> {
                    Toast toast =
                            Toast.makeText(context, "Unable to load renderable ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
    }

    /**
     * Reset the augmentedDataBase.
     */
    public boolean removeAugmentedDataBase() {
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        config.setAugmentedImageDatabase(null);
        return true;
    }

    /**
     * Setup the AugmentedImageDatabase with two arraylists of the pictures to look for and display.
     *
     * @param augmentedAndTodisplay a list that pairs the urls of the augmented image and the image to display in that order
     * @return true if successful
     */
    public boolean setupAugmentedImage(ArrayList<Pair<String, String>> augmentedAndTodisplay) {
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        //for (int i = 0; i < URLAugmentedImage.length ; i++) {
        new GetBitmapFromUrl().execute(augmentedAndTodisplay);
        return true;
    }

    /**
     * Sets the Image Found in the url on the noe as a renderable.
     *
     * @param node the node that the image should be desplayed on
     * @param url the url that contains the image to download and display
     * @return true if successful
     */
    public boolean setPictureRenderable(Node node, String url) {
        Bitmap bitmap = hashmapImagesToDisplay.get(url); // TODO can be null. if so return false
        if (bitmap == null) { return false; }
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);

        ViewRenderable.builder().setView(context, imageView).build().thenAccept(renderable -> {
            renderable.setShadowCaster(false);
            node.setRenderable(renderable);
        });
        return true;
    }

    /**
     * Helper Class for PlayAugmentedImage, it will handle downloading the different images in an AscyncTask.
     * can be used with an .execute
     */
    public class GetBitmapFromUrl extends AsyncTask<ArrayList<Pair<String, String>>, Void, ArrayList<Pair<Bitmap, String>>> {
        private ArrayList<Pair<Bitmap, String>> bitmap;

        public GetBitmapFromUrl() {
            this.bitmap = null;
            hashmapImagesToDisplay = new HashMap<String, Bitmap>();
        }

        /**
         * Downloads the different URL's given and converts them into bitmaps.
         *
         * @param url the different url's. The first String is the image to look for the second one is the image to display.
         * @return the different Bitmaps corresponding too the url picture
         */
        @Override
        protected ArrayList<Pair<Bitmap, String>> doInBackground(ArrayList<Pair<String, String>>... url) {
            ArrayList<Pair<Bitmap, String>> bitmaplist = new ArrayList<>();
            ArrayList<Pair<String, String>> list = url[0];
            for (int i = 0; i < list.size(); i++) {
                String augmentUrl = list.get(i).first;
                String toPutUrl = list.get(i).second;
                boolean downloaded = false;
                while (!downloaded) { // can be stuck if puzzle solved without object
                    try {
                        InputStream inputStreamAugmented = new URL(augmentUrl).openStream();
                        bitmaplist.add(new Pair<Bitmap, String>(BitmapFactory.decodeStream(inputStreamAugmented), toPutUrl));
                        InputStream inputStreamDisplay = new java.net.URL(toPutUrl).openStream();
                        hashmapImagesToDisplay.put(toPutUrl, BitmapFactory.decodeStream(inputStreamDisplay));
                        downloaded = true;
                    } catch (IOException e) {
                        Log.i(TAG, "No internet!");
                        //e.printStackTrace();
                    }
                }
            }
            return bitmaplist;
        }

        /**
         * Uses the bitmaps found to add the images to the AugmentedImageDatabase.
         *
         * @param bitmap the list with bitmaps.
         */
        @Override
        protected void onPostExecute(ArrayList<Pair<Bitmap, String>> bitmap) {
            super.onPostExecute(bitmap);
            this.bitmap = bitmap;
            for (int i = 0; i < bitmap.size(); i++) {
                augmentedImageDatabase.addImage(bitmap.get(i).second, bitmap.get(i).first);
            }
            config.setAugmentedImageDatabase(augmentedImageDatabase);
            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
            session.configure(config); //this will cause a big framedrop \\TODO load symbol?
        }
    }
}

