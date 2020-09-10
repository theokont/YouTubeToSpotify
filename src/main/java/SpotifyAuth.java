import com.sun.net.httpserver.HttpServer;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import picocli.CommandLine.Command;


@Command(
        name = "auth",
        description = "Get the authorization access token"
)
public class SpotifyAuth implements Runnable {

    private String authAPI;
    private String apiToken;
    public SpotifyApi spotify = new SpotifyApi();
    private static String code;
    private static String clientID;
    private static String clientSecret;
    private Properties config;
    private static ServerHandler handler = new ServerHandler();
    private static String scope = "user-read-private%20playlist-read-private%20user-read-email%20" +
            "playlist-modify-public%20playlist-modify-private";

    public SpotifyAuth() {
        authAPI = "https://accounts.spotify.com/authorize";
        apiToken = "https://accounts.spotify.com/api/token";
        code = null;
        try {
            config = CredentialsLoader.loadCredentials();
            clientID = config.getProperty("spotify.client.id");
            clientSecret = config.getProperty("spotify.client.secret");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            initiateAuth();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Authorization is successful!");
        System.exit(0);
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

                URI objectURI = new URI(spotify.getRedirectUri(getAuthApi()));
                desktop.browse(objectURI);
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("Unfortunately your browser can not open the following link automatically: "
                    + "\n" + spotify.getRedirectUri(getAuthApi()));
            System.out.println("Copy the link above and paste it in your browser in order to proceed");
        }

        while (!handler.setCode) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // if authentication code is retrieved, get spotify token in json as string
        // and then parse it in order to get the access token
        if (handler.setCode) {
            String token = spotify.getToken(getApiToken(), getCode(), clientID, clientSecret);
            setAccessToken(Parser.readAuthToken(token, "access_token"));
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

    public static String getScope() {
        return scope;
    }

    public String getAccessToken() throws IOException {
//        Properties config = CredentialsLoader.loadCredentials();
        return config.getProperty("spotify.auth.token");
    }

    public void setAccessToken(String accessToken) {
        Credentials.store(accessToken);
//        this.accessToken = accessToken;
    }
}