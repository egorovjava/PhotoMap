package com.gmail.egorovsonalexey;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;

public class GoogleDriveSetup {

    static final String CLIENT_ID = "1078260760548-en0npkq7mmo5fs0q4bvdq8rv40vi8q7l.apps.googleusercontent.com";
    static final String CLIENT_SECRET = "MUVvRCmDmy_fV5Me29N1Llh2";
    static final String ACCESS_TOKEN = "ya29.a0AfH6SMCh8yKXzwiRbYx4XydUA01nnzE1HQeuEiCB0JKyub0GxCdayjtcgoQWCXxe6Ag1YzuchVSVO44dAVkocAwkswIvEYT4QSphlFDlDSGfYgltjj2LrI3PfJz68dEfaRjoj6hr-6fvnAl6tFvJn8Z189Sv_HwHgr8";
    static final String REFRESH_TOKEN = "1//04zaBfuzdxAokCgYIARAAGAQSNwF-L9Irxg-kshhiEugZb2VhfO6smZBy8QcHHMTT7FmXCD8mGika-QnAURMmC3I0RcrB1ASoIRs";
    static final String APPLICATION_NAME = "Photo Map";
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setJsonFactory(JSON_FACTORY)
                .setTransport(HTTP_TRANSPORT)
                .setClientAuthentication(new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET))
                .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
                .build()
            .setAccessToken(ACCESS_TOKEN)
            .setRefreshToken(REFRESH_TOKEN)
            .setExpirationTimeMilliseconds(3000L);
    }

}