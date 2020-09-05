import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import kong.unirest.Unirest;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;



@Command
public class SpotifyAuth implements Callable {

    private String authAPI;
    private String apiToken;
    private static String code;
    private static String accessToken;
    private static String clientID = "f270b2403a6540fc8f654e75a5e4a6a2"; // add your own clientID
    private static String clientSecret = "1a56c655f2f34822af4c4d1eb559bb32"; // add your own clientSecrets
    private static ServerHandler handler;

    public SpotifyAuth() {
        handler = new ServerHandler();
        authAPI = "https://accounts.spotify.com/authorize";
        apiToken = "https://accounts.spotify.com/api/token";
        code = null;
    }

    public Object call() throws IOException{
        initiateAuth();
        String aToken = getAccessToken();
        System.out.println(aToken);
        return null;
    }

    public static String generateState() {
        String state = null;
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int stringLength = 10;
        Random random = new Random();
        state = random.ints(stringLength, leftLimit, rightLimit +1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return state;
    }

    public String getRedirectUri(String authApi) {
        String response = Unirest.get(authApi)
                .queryString("client_id", "f270b2403a6540fc8f654e75a5e4a6a2")
                .queryString("response_type", "code")
                .queryString("redirect_uri", "http://127.0.0.1:8080/callback/")
                .queryString("state", generateState())
                .queryString("scope", "user-read-private%20playlist-read-private%20user-read-email")
                .getUrl();
        return response;
    }

    public String requestToken(String apiToken, String code, String clientId, String clientSecret) {
        String response = Unirest.post(apiToken)
                .field("grant_type","authorization_code")
                .field("code", code)
                .field("redirect_uri","http://127.0.0.1:8080/callback/")
                .field("client_id", clientId)
                .field("client_secret", clientSecret)
                .asString().getBody();
        return response;
    }

    public String refreshToken(String apiToken, String code) {
        String response = Unirest.post(apiToken)
                .queryString("grant_type","refresh_token")
                .queryString("refresh_token", code)
                .asString().getBody();
        return response;
    }

    public HttpServer initServer() throws IOException {
        InetAddress adr = InetAddress.getByName("127.0.0.1");
        HttpServer server = HttpServer.create(new InetSocketAddress(adr, 8080), 0);
        server.createContext("/callback/", handler);
        server.setExecutor(null); // default executor
        server.start();
        return server;
    }

    public void closeServer(HttpServer server) {
        server.stop(0);
    }

    public void initiateAuth() throws IOException {

        HttpServer server = initServer();

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = java.awt.Desktop.getDesktop();
            try {

                URI objectURI = new URI(getRedirectUri(getAuthApi()));
                desktop.browse(objectURI);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Unfortunately your browser can not open the following link automatically: "
                    + "\n" + getRedirectUri(getAuthApi()));
            System.out.println("Copy the link above and paste it in your browser in order to proceed");
        }

        Parser parser = new Parser();
        while (!handler.setCode) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (handler.setCode) {
            String token = requestToken(getApiToken(), getCode(), clientID, clientSecret);
            setAccessToken(parser.readAuthToken(token, "access_token"));
        }
        else {
            System.out.println("Something went wrong, access token is not set");
        }
        closeServer(server);
    }

    public String getAuthApi() {
        return authAPI;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}