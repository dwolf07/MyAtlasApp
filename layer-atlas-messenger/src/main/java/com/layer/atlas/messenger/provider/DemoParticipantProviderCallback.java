package com.layer.atlas.messenger.provider;

import android.util.Log;

import com.layer.atlas.messenger.Participant;
import com.layer.atlas.messenger.AppIdCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steven on 5/24/15.
 */
public class DemoParticipantProviderCallback implements ParticipantProvider.Callback {
    private final String TAG = DemoParticipantProviderCallback.class.getSimpleName();
    private final AppIdCallback mAppIdCallback;

    public DemoParticipantProviderCallback(AppIdCallback appIdCallback) {
        mAppIdCallback = appIdCallback;
    }

    @Override
    public List<Participant> getAllParticipants() {
        String appId = mAppIdCallback.getAppId();
        if (appId == null) return null;
        try {
            HttpGet get = new HttpGet("https://layer-identity-provider.herokuapp.com/apps/" + appId + "/atlas_identities");
            get.setHeader("Content-Type", "application/json");
            get.setHeader("Accept", "application/json");
            get.setHeader("X_LAYER_APP_ID", appId);
            HttpResponse response = (new DefaultHttpClient()).execute(get);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                Log.e(TAG, String.format("Got status %d when fetching contacts", response.getStatusLine().getStatusCode()));
                return null;
            }

            String responseString = EntityUtils.toString(response.getEntity());
            JSONArray responseContacts = new JSONArray(responseString);
            List<Participant> participants = new ArrayList<Participant>(responseContacts.length());
            for (int i = 0; i < responseContacts.length(); i++) {
                JSONObject responseContact = responseContacts.getJSONObject(i);
                Participant participant = new Participant();
                participant.userId = responseContact.getString("id");
                participant.firstName = responseContact.optString("name");
                participants.add(participant);
            }
            return participants;
        } catch (Exception e) {
            Log.e(TAG, "Error when fetching contacts", e);
        }
        return null;
    }
}
