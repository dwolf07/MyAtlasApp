package com.layer.atlas.messenger.provider;

import com.layer.atlas.messenger.Participant;
import com.layer.atlas.messenger.AppIdCallback;

import java.util.List;

/**
 * IdentityProvider is a mechanism for retrieving Identity Tokens for authenticating LayerClients.
 */
public abstract class IdentityProvider {
    protected final AppIdCallback mAppIdCallback;

    public static class Result {
        // Optional Identity Token present if authentication was successful
        public String identityToken;

        // Optional error string from authenticating with the backend identity service
        public String error;

        // Optional contacts provided by the identity service on successful login
        public List<Participant> participants;
    }

    public IdentityProvider(AppIdCallback appIdCallback) {
        mAppIdCallback = appIdCallback;
    }

    /**
     * Retrieves an Identity Token - the response to an authentication challenge - from a backend
     * identity service.  When the LayerClient provides an authentication challenge ("nonce"), the
     * nonce and relevant credentials are provided to the external identity service for validation
     * and token generation.  With valid credentials, the resulting Identity Token is provided to
     * the LayerClient's answerChallenge(token) method to complete authentication with Layer.
     *
     * @param nonce        The `nonce` challenge from the LayerClient.
     * @param userName     The username to authenticate with.
     * @param userPassword The password to authenticate with.
     * @return
     */
    public abstract Result getIdentityToken(String nonce, String userName, String userPassword);
}
