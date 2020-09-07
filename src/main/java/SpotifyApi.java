import com.google.gson.JsonObject;
import kong.unirest.Unirest;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class SpotifyApi {

    private static SpotifyAuth token  = new SpotifyAuth();
    private static Map<String,String> playlists;

    public SpotifyApi() {
        playlists = null;
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

    public String getRedirectUri(String authApi) {
        String response = Unirest.get(authApi)
                .queryString("client_id", "f270b2403a6540fc8f654e75a5e4a6a2")
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
            System.out.println("> Playlist " + playlistName + " has been created successfully!");
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

    public String searchTrack(String title) {
        String search = Unirest.get("https://api.spotify.com/v1/search")
                .header("Authorization", "Bearer " + getAccessToken())
                .queryString("q", title)
                .queryString("type", "artist,track")
                .queryString("limit", 1)
                .asString().getBody();
        return search;
    }

    public void addTracks(String playlistID, String uris) {
        String response = Unirest.post("https://api.spotify.com/v1/playlists/" + playlistID + "/tracks")
                .header("Authorization", "Bearer " + getAccessToken())
                .header("Content-Type", "application/json")
                .queryString("uris", uris)
                .asString().getBody();
    }

    public String getAccessToken() {
        return token.getAccessToken();
    }
}
