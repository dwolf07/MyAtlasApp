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
public class FullParticipantProviderCallback implements ParticipantProvider.Callback {
    private final String TAG = FullParticipantProviderCallback.class.getSimpleName();
    private final AppIdCallback mAppIdCallback;
    private final Callback mCallback;

    public interface Callback {
        String getAuthToken();

        String getEmail();
    }

    public FullParticipantProviderCallback(AppIdCallback appIdCallback, Callback callback) {
        mAppIdCallback = appIdCallback;
        mCallback = callback;
    }

    @Override
    public List<Participant> getAllParticipants() {
        String appId = mAppIdCallback.getAppId();
        String authToken = mCallback.getAuthToken();
        String email = mCallback.getEmail();
        if (appId == null || authToken == null || email == null) return null;
        try {
            HttpGet get = new HttpGet("https://layer-identity-provider.herokuapp.com/users.json");
            get.setHeader("Content-Type", "application/json");
            get.setHeader("Accept", "application/json");
            get.setHeader("X_LAYER_APP_ID", appId);
            get.setHeader("X_AUTH_TOKEN", authToken);
            get.setHeader("X_AUTH_EMAIL", email);
            HttpResponse response = (new DefaultHttpClient()).execute(get);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
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
                participant.firstName = responseContact.optString("first_name");
                participant.lastName = responseContact.optString("last_name");
                participants.add(participant);
            }
            return participants;
        } catch (Exception e) {
            Log.e(TAG, "Error when fetching contacts", e);
        }
        return null;
    }
}
