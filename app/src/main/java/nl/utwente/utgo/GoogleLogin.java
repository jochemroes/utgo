package nl.utwente.utgo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public final class GoogleLogin {

    private static final String TAG = "Google Login";
    private static final int RC_SIGN_IN = 0;
    private static final int RC_LOGIN_ACT = 1;
    private static FirebaseAuth mAuth;
    @SuppressLint("StaticFieldLeak")
    private static GoogleSignInClient mGoogleSignInClient;
    @SuppressLint("StaticFieldLeak")
    private static Activity main;
    @SuppressLint("StaticFieldLeak")
    private static Activity login;

    public static void initGoogleLogin(MainActivity main) {
        GoogleLogin.main = main;
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(main.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(main, gso);
    }

    public static boolean isSignedIn() {
        // Check if user is signed in (non-null) and update UI accordingly.
        if(mAuth.getCurrentUser() != null) {
            setUserID();
            return true;
        }
        return false;
    }

    public static void deleteAccount() {
        mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
            signOut();
        });
    }

    public static void signOut() {
        signOutAndRevokeAccess();
        main.finish();
        main.startActivity(main.getIntent());
    }

    public static void signOutAndRevokeAccess() {
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.revokeAccess();
    }

    private static void setUserID() {
        Firestore.setUserID(mAuth.getCurrentUser().getUid());
    }

    public static void signIn(LoginActivity login) {
        GoogleLogin.login = login;
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        login.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public static void fetchGoogleAccount(int requestCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.i(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getEmail(), account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e);
            }
        }
    }

    private static void firebaseAuthWithGoogle(String email, String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task1 -> {
            if(task1.getResult().getSignInMethods().isEmpty()) {
                // create new account
                login.setContentView(R.layout.activity_login_username);

                EditText tiet = login.findViewById(R.id.EditText);
                Button button = login.findViewById(R.id.button);
                button.setOnClickListener(view -> {
                    String username = tiet.getText().toString();
                    Log.i(TAG, "Username: " + username);

                    // TODO: miss uniqueness van username nog checken

                    if (NameChecker.isNameCorrect(username)) {
                        Log.i(TAG, "New user created with Google");
                        signInWithCredential(credential, true, username);
                    }
                });
            } else {
                // use existing account
                signInWithCredential(credential, false, "");
            }
        });
    }

    private static void signInWithCredential(AuthCredential credential, boolean newAccount, String username) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(login, task2 -> {
            if (task2.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.i(TAG, "signInWithCredential:success");
                setUserID();
                if(newAccount) {
                    Firestore.createUser(username);
                }
                returnToMainActivity();
            } else {
                // If sign in fails, display a message to the user.
                Log.e(TAG, "signInWithCredential:failure", task2.getException());
            }
        });
    }

    /**
     * Finishes LoginActivity after sending the user's name, email, uid and photo URL to MainActivity
     */
    private static void returnToMainActivity() {
        Intent intent = new Intent(main, login.getClass());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        intent.putExtra("name", user.getDisplayName());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("uid", user.getUid());
        intent.putExtra("photo", user.getPhotoUrl().toString());
        login.setResult(RC_LOGIN_ACT, intent);
        login.finish();
    }
}
