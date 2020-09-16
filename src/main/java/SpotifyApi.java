import com.google.gson.JsonObject;
import kong.unirest.Unirest;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Random;


@Command (
        name = "playlists",
        description = "Returns user's Spotify owned playlists"
)
public class SpotifyApi implements Runnable{

    private static SpotifyAuth token  = new SpotifyAuth();
    private static Map<String,String> playlists;
    Properties config;


    public SpotifyApi() {
        playlists = null;
        try {
            config = CredentialsLoader.loadCredentials();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String encodeSpaces(String title) {
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < title.length(); i++) {
            if (title.charAt(i) != ' ') {
                encoded.append(title.charAt(i));
            }
            else {
                encoded.append("%20");
            }
        }
        return String.valueOf(encoded);
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

    public String getRedirectUri(String authApi, String clientID) {
        String response = Unirest.get(authApi)
                .queryString("client_id", clientID)
                .queryString("response_type", "code")
                .queryString("redirect_uri", "http://127.0.0.1:8080/callback/")
                .queryString("state", generateState())
                .queryString("scope", SpotifyAuth.getScope())
                .getUrl();
        return response;
    }

    public String getToken(String apiToken, String code, String clientId, String clientSecret) {
        String response = Unirest.post(apiToken)
                .field("grant_type","authorization_code")
                .field("code", code)
                .field("redirect_uri","http://127.0.0.1:8080/callback/")
                .field("client_id", clientId)
                .field("client_secret", clientSecret)
                .asString().getBody();
        return response;
    }

    public String getRefreshToken(String apiToken, String code) {
        String response = Unirest.post(apiToken)
                .queryString("grant_type","refresh_token")
                .queryString("refresh_token", code)
                .asString().getBody();
        return response;
    }

    public String getUserID() throws IOException {

        String response = Unirest.get("https://api.spotify.com/v1/me")
                .header("Authorization", "Bearer " + getAccessToken())
                .asString().getBody();
        return Parser.readUserID(response);
    }

    public void setPlaylists() throws IOException {

        String response = Unirest.get("https://api.spotify.com/v1/me/playlists")
                .header("Authorization", "Bearer " + getAccessToken())
                .queryString("limit",50)
                .asString().getBody();
        playlists = Parser.readPlaylistsResponse(response);
    }

    public void createPlaylist(String playlistName) throws IOException {

        JsonObject data = new JsonObject();
        data.addProperty("name", playlistName);

        // collaborative is false on default, and public is true
        String post = Unirest.post("https://api.spotify.com/v1/users/" + getUserID() + "/playlists")
                .header("Authorization","Bearer " + getAccessToken())
                .header("Content-Type","application/json")
                .body(data)
                .asString().getBody();
        setPlaylists();
        if (playlists.containsKey(playlistName)) {
            System.out.println(picocli.CommandLine.Help.Ansi.ON.string("@|fg(40) Playlist " +
                    playlistName + " has been created! |@"));
        }
        else {
            System.out.println("Error, playlist hasn't been created");
        }
        setPlaylists();
    }

    public Map<String,String> getPlaylists() throws IOException {
        if (playlists == null) {
            setPlaylists();
        }
        return this.playlists;
    }

    public String searchTrack(String title) throws IOException {
        String search = Unirest.get("https://api.spotify.com/v1/search")
                .header("Authorization", "Bearer " + getAccessToken())
                .queryString("q", title)
                .queryString("type", "artist,track")
                .queryString("limit", 1)
                .asString().getBody();
        return search;
    }

    public void addTracks(String playlistID, String uris) throws IOException {
        String response = Unirest.post("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks")
                .header("Authorization", "Bearer " + getAccessToken())
                .header("Content-Type", "application/json")
                .queryString("uris", uris)
                .asString().getBody();
    }

    public String getAccessToken() throws IOException {
        return config.getProperty("spotify.auth.token");
    }

    @Override
    public void run() {
        try {
            Map<String,String> pl = getPlaylists();

            System.out.println('\n' +"Spotify playlists that you own: " + '\n');

            for (String playlist : pl.keySet()) {

                System.out.println(Ansi.ON.string("@|fg(185) "+ playlist +" |@"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
