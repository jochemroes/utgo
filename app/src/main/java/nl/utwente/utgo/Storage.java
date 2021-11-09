package nl.utwente.utgo;

import android.net.Uri;
import android.util.Log;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;

public final class Storage {

    private static final String TAG = "Storage";
    private static final FirebaseStorage fs = FirebaseStorage.getInstance("gs://ut-go-5542f.appspot.com");
    protected static List<Uri> vrijhofUris = new ArrayList<>();

    /**
     * Downlaods files from Firebase Storage needed for specific quest.
     * @param quest Quest location name. Example: "vrijhof"
     */
    public static void downloadFiles(String quest) {
        StorageReference storageReference = fs.getReference(quest);
        storageReference.listAll().addOnCompleteListener(task -> {
            List<StorageReference> items = task.getResult().getItems();


            for(int i = 0; i < items.size(); i++) {
                String filename = items.get(i).getName();
                Log.d(TAG, "Downloading: " + filename);
                createUri(storageReference.child(filename));
            }
        });
    }

    /**
     * Creates a Uri link based on the Firebase Storage reference and adds it to a list
     * @param storageReference reference to Firebase Storage folder
     */
    private static void createUri(StorageReference storageReference) {
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d(TAG, "Download successful: " + uri);
            vrijhofUris.add(uri);
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "Downloading image failed miserably.");
        });
    }
}
