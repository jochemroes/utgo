package nl.utwente.utgo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.acra.*;
import org.acra.annotation.*;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.Configuration;
import org.acra.config.ConfigurationBuilder;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.MailSenderConfiguration;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilderFactory;
import org.acra.config.ToastConfigurationBuilder;
import org.acra.data.StringFormat;


/**
 * UTGO Application class which is instantiated before all activities
 * Registered under AndroidManifest/manifest/application/android:name
 *
 * Current behavior (other frontends/backends are possible):
 * ACRA starts -> activities start -> app crashes -> present toasts with info to user
 * -> opens mail client of user with message ready to send
 * -> user presses send -> winning winning winning
 */
//annotation needed for ACRA
@AcraCore(buildConfigClass = BuildConfig.class)
public class UtgoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        //create new configuration builder
        CoreConfigurationBuilder builder = new CoreConfigurationBuilder(this);

        //core config
        builder
                .setBuildConfigClass(BuildConfig.class)
                    .setReportFormat(StringFormat.JSON);

        //configure crash toast
        builder
                .getPluginConfigurationBuilder(ToastConfigurationBuilder.class)
                    .setEnabled(true)
                    .setResText(R.string.crash_toast_text)
                    .setLength(Toast.LENGTH_LONG);
                    //.setReportSendSuccessToast(R.string.crash_toast_success)
                    //.setReportSendFailureToast("Crash report fail :(")

        //configure mail sender
        builder
                .getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class)
                    .setEnabled(true)
                    .setMailTo(getResources().getString(R.string.utgomail))
                    .setReportAsFile(true)
                    .setReportFileName(getResources().getString(R.string.crash_trace_fname))
                    .setSubject(getResources().getString(R.string.crash_mail_subject))
                    .setBody(getResources().getString(R.string.crash_mail_body));

        //more toasts
        builder
                .setReportSendSuccessToast(getResources().getString(R.string.crash_toast_success))
                .setReportSendFailureToast(getResources().getString(R.string.crash_toast_failure));

        //initialize ACRA with current config
        ACRA.init(this,builder);
    }
}
