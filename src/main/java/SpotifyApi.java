import com.google.gson.JsonObject;
import kong.unirest.Unirest;
import java.io.IOException;
import java.util.Map;

public class SpotifyApi {

    private static SpotifyAuth token;
    private static Map<String,String> playlists;

    public SpotifyApi() {
        token = new SpotifyAuth();
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
            System.out.println("> Playlist " + playlistName + "has been created successfully!");
        }
        else {
            System.out.println("Error, playlist hasn't been created");
        }
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
