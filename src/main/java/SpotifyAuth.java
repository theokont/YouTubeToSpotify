import com.sun.net.httpserver.HttpServer;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;


@Command
public class SpotifyAuth implements Callable {

    private String authAPI;
    private String apiToken;
    public SpotifyApi spotify = new SpotifyApi();
    private static String code;
    private static String accessToken;
    private static String clientID = "f270b2403a6540fc8f654e75a5e4a6a2"; // add your own clientID
    private static String clientSecret = "1a56c655f2f34822af4c4d1eb559bb32"; // add your own clientSecrets
    private static ServerHandler handler;
    private static String scope = "user-read-private%20playlist-read-private%20user-read-email%20" +
            "playlist-modify-public%20playlist-modify-private";

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

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}