package com.layer.atlas.messenger;

import java.util.Iterator;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.layer.atlas.messenger.provider.DemoIdentityProvider;
import com.layer.atlas.messenger.provider.DemoParticipantProviderCallback;
import com.layer.atlas.messenger.provider.FullIdentityProvider;
import com.layer.atlas.messenger.provider.FullParticipantProviderCallback;
import com.layer.atlas.messenger.provider.IdentityProvider;
import com.layer.atlas.messenger.provider.ParticipantProvider;
import com.layer.sdk.LayerClient;
import com.layer.sdk.LayerClient.Options;

/**
 * @author Oleg Orlov
 * @since March 3, 2015
 */
public class App101 extends Application implements AppIdCallback {
    private static final String TAG = App101.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final String GCM_SENDER_ID = "565052870572";

    public static final boolean DEMO_MODE = true;   // Enables QR code flow with user limits */

    // Set appId here to bypass QR code scanning.
    private String appId = null;
    //private String appId = "b257c416-016a-11e5-9933-84d0e30072a2"; // steven QR-code production
    //private String appId = "9ec30af8-5591-11e4-af9e-f7a201004a3b"; // non-QR-code production

    private LayerClient layerClient;
    private IdentityProvider identityProvider;
    private ParticipantProvider participantProvider;

    public interface keys {
        String CONVERSATION_URI = "conversation.uri";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //LayerClient.enableLogging();
        LayerClient.applicationCreated(this);
        loadAppId();

        if (DEMO_MODE) {
            identityProvider = new DemoIdentityProvider(this);
            participantProvider = new ParticipantProvider(this, new DemoParticipantProviderCallback(this));
        } else {
            identityProvider = new FullIdentityProvider(getApplicationContext(), this);
            participantProvider = new ParticipantProvider(this, new FullParticipantProviderCallback(this, (FullIdentityProvider) identityProvider));
        }
    }

    public LayerClient getLayerClient() {
        return layerClient;
    }

    public void initLayerClient(final String localAppId) {
        final LayerClient client = LayerClient.newInstance(this, localAppId, new Options()
                .broadcastPushInForeground(true)
                .googleCloudMessagingSenderId(GCM_SENDER_ID));
        if (DEBUG) Log.w(TAG, "onCreate() client created");

        setAppId(localAppId);
        layerClient = client;

        if (!client.isAuthenticated()) client.authenticate();
        else if (!client.isConnected()) client.connect();
        if (DEBUG) Log.w(TAG, "onCreate() Layer launched");

        if (DEBUG) Log.d(TAG, "onCreate() Refreshing Contacts");
        getParticipantProvider().refresh();
    }

    public ParticipantProvider getParticipantProvider() {
        return participantProvider;
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public void setAppId(String appId) {
        this.appId = appId;
        getSharedPreferences("app", MODE_PRIVATE).edit().putString("appId", appId).commit();
    }

    private void loadAppId() {
        if (appId == null) {
            appId = getSharedPreferences("app", MODE_PRIVATE).getString("appId", null);
        }
    }

    @Override
    public String getAppId() {
        return appId;
    }

    /**
     * Converts a Bundle to the human readable string.
     *
     * @param bundle the collection for example, {@link java.util.ArrayList}, {@link java.util.HashSet} etc.
     * @return the converted string
     */
    public static String toString(Bundle bundle) {
        return toString(bundle, ", ", "");
    }

    public static String toString(Bundle bundle, String separator, String firstSeparator) {
        if (bundle == null) return "null";
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (Iterator<String> itKey = bundle.keySet().iterator(); itKey.hasNext(); i++) {
            String key = itKey.next();
            sb.append(i == 0 ? firstSeparator : separator).append(i).append(": ");
            sb.append(key).append(" : ").append(bundle.get(key));
        }
        sb.append("]");
        return sb.toString();
    }
}
