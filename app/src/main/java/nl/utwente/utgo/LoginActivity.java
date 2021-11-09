package nl.utwente.utgo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setGoogleButton();
    }

    public void setGoogleButton() {

        // Hide the android studio title bar since we made our own
        getSupportActionBar().hide();

        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            switch (v.getId()) {
                case R.id.sign_in_button:
                    GoogleLogin.signIn(this);
                    break;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleLogin.fetchGoogleAccount(requestCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        setContentView(R.layout.activity_login);
        GoogleLogin.signOutAndRevokeAccess();
        setGoogleButton();
    }
}