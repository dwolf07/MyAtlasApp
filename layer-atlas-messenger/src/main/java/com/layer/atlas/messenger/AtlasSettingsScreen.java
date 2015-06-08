package com.layer.atlas.messenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.layer.atlas.Atlas;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerAuthenticationListener;
import com.layer.sdk.listeners.LayerConnectionListener;

import org.w3c.dom.Text;

/**
 * @author Oleg Orlov
 * @since 17 Apr 2015
 */
public class AtlasSettingsScreen extends Activity {

    public static final String EXTRA_FORCE_LOGOUT = "settings.force.logout";

    private App101 app;
    private TextView usernameTextView;
    private TextView statusTextView;
    
    private final LayerAuthenticationListener authListener = new LayerAuthenticationListener() {
        @Override
        public void onAuthenticated(LayerClient layerClient, String s) {
            updateValues();
        }

        @Override
        public void onDeauthenticated(LayerClient layerClient) {
            updateValues();
        }

        @Override
        public void onAuthenticationChallenge(LayerClient layerClient, String s) {
            updateValues();
        }

        @Override
        public void onAuthenticationError(LayerClient layerClient, LayerException e) {
            updateValues();
        }
    };
 
    private final LayerConnectionListener connectionListener = new LayerConnectionListener() {
        @Override
        public void onConnectionConnected(LayerClient layerClient) {
            updateValues();
        }

        @Override
        public void onConnectionDisconnected(LayerClient layerClient) {
            updateValues();
        }

        @Override
        public void onConnectionError(LayerClient layerClient, LayerException e) {
            updateValues();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App101) getApplication();
        
        setContentView(R.layout.atlas_screen_settings);
        
        TextView logout = (TextView) findViewById(R.id.atlas_screen_settings_logout);
        logout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                app.getLayerClient().deauthenticate();
                final Intent data = new Intent();
                data.putExtra(EXTRA_FORCE_LOGOUT, true);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        usernameTextView = (TextView)findViewById(R.id.atlas_settings_username_text);
        statusTextView = (TextView)findViewById(R.id.atlas_settings_login_status_text);

        prepareActionBar();
        updateValues();
    }

    public void updateValues() {
        App101 app = (App101)getApplication();
        LayerClient client = app.getLayerClient();
        String userId = (client == null)? null : client.getAuthenticatedUserId();
        Participant participant = (userId == null)? null : app.getParticipantProvider().get(userId);

        usernameTextView.setText(participant == null ? null : Atlas.getFullName(participant)); 
        statusTextView.setText((client != null && client.isConnected())? "Connected" : "Disconnected");
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.getLayerClient()
                .registerAuthenticationListener(authListener)
                .registerConnectionListener(connectionListener);
    }

    @Override
    protected void onPause() {
        app.getLayerClient()
                .unregisterAuthenticationListener(authListener)
                .unregisterConnectionListener(connectionListener);
        super.onPause();
    }

    private void prepareActionBar() {
        ImageView menuBtn = (ImageView) findViewById(R.id.atlas_actionbar_left_btn);
        menuBtn.setImageResource(R.drawable.atlas_ctl_btn_back);
        menuBtn.setVisibility(View.VISIBLE);
        menuBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
