package nl.utwente.utgo;

import androidx.fragment.app.Fragment;

public abstract class FullScreenFragment extends Fragment {

    private boolean coverAnimation;

    public void setCoverAnimation(boolean bool) {
        coverAnimation = bool;
    }

    public boolean getCoverAnimation() {
        return coverAnimation;
    }

}
