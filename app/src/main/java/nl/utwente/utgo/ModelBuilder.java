package nl.utwente.utgo;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.atomic.AtomicReference;

public class ModelBuilder {

    public AtomicReference<ViewRenderable> buildTestView(Context context) {
        AtomicReference<ViewRenderable> testViewRenderable = null;

        ViewRenderable.builder()
                .setView(context, R.layout.test_label)
                .build()
                .thenAccept(renderable -> testViewRenderable.set(renderable)).exceptionally(
                throwable -> {
                    Toast toast =
                            Toast.makeText(context, "Unable to load renderable ", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                });
        return testViewRenderable;
    }
}
