package com.layer.atlas.messenger.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.layer.atlas.messenger.AppIdCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by steven on 5/24/15.
 */
public class FullIdentityProvider extends IdentityProvider implements FullParticipantProviderCallback.Callback {
    private final static String TAG = FullIdentityProvider.class.getSimpleName();
    private SharedPreferences mSharedPreferences;

    private String mAuthToken;
    private String mEmail;

    public FullIdentityProvider(Context context, AppIdCallback appIdCallback) {
        super(appIdCallback);
        mSharedPreferences = context.getSharedPreferences("identity", Context.MODE_PRIVATE);
        load();
    }

    @Override
    public Result getIdentityToken(String nonce, String userEmail, String userPassword) {
        try {
            JSONObject rootObject = new JSONObject();
            rootObject.put("nonce", nonce);
            rootObject.put("user", new JSONObject()
                    .put("email", userEmail)
                    .put("password", userPassword));
            StringEntity entity = new StringEntity(rootObject.toString(), "UTF-8");
            entity.setContentType("application/json");

            HttpPost post = new HttpPost("https://layer-identity-provider.herokuapp.com/users/sign_in.json");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setHeader("X_LAYER_APP_ID", mAppIdCallback.getAppId());
            post.setEntity(entity);
            HttpResponse response = (new DefaultHttpClient()).execute(post);
            if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode() && HttpStatus.SC_CREATED != response.getStatusLine().getStatusCode()) {
                Log.e(TAG, String.format("Got status %d when logging in", response.getStatusLine().getStatusCode()));
                return null;
            }

            String responseString = EntityUtils.toString(response.getEntity());
            JSONObject jsonResp = new JSONObject(responseString);
            Result result = new Result();
            result.error = jsonResp.optString("error", null);
            result.identityToken = jsonResp.optString("layer_identity_token");

            mAuthToken = jsonResp.optString("authentication_token");
            mEmail = userEmail;
            save();

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error when fetching identity token", e);
        }
        return null;
    }

    @Override
    public String getAuthToken() {
        return mAuthToken;
    }

    @Override
    public String getEmail() {
        return mEmail;
    }

    void load() {
        mAuthToken = mSharedPreferences.getString("authToken", null);
        mEmail = mSharedPreferences.getString("email", null);
    }

    void save() {
        mSharedPreferences.edit()
                .putString("authToken", mAuthToken)
                .putString("email", mEmail)
                .apply();
    }
}
