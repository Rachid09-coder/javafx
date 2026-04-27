package com.edusmart.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.StringReader;
import java.util.Arrays;

public class GoogleAuthService {

    // REMARQUE IMPORTANTE :
    // Remplacez ces valeurs par votre Client ID et Client Secret générés depuis la
    // Google Cloud Console.
    // Sans cela, l'authentification retournera une erreur "invalid_client".
    private static final String CLIENT_ID = "GOOGLE_CLIENT_ID";
    private static final String CLIENT_SECRET = "GOOGLE_CLIENT_SECRET";

    public Userinfo authenticate() throws Exception {
        NetHttpTransport httpTransport = new NetHttpTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        String clientSecretsJson = String.format(
                "{\"installed\":{\"client_id\":\"%s\",\"project_id\":\"edusmart\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"%s\",\"redirect_uris\":[\"http://localhost\"]}}",
                CLIENT_ID, CLIENT_SECRET);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new StringReader(clientSecretsJson));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets,
                Arrays.asList("email", "profile", "openid"))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("EduSmart")
                .build();

        return oauth2.userinfo().get().execute();
    }
}
