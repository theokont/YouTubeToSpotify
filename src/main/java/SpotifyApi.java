import kong.unirest.Unirest;
import java.io.IOException;
import java.util.Map;

public class SpotifyApi {

    public String getUserID(String authToken) throws IOException {

        String response = Unirest.get("https://api.spotify.com/v1/me")
                .header("Authorization", "Bearer " + authToken)
                .asString().getBody();
        return Parser.readUserID(response);
    }

    public Map<String,String> getPlaylists(String authToken, String userID) throws IOException {

        String response = Unirest.get("https://api.spotify.com/v1/me/playlists")
                .header("Authorization", "Bearer " + authToken)
                .queryString("limit",50)
                .asString().getBody();
        Parser parser = new Parser();
        Map<String,String> playlists = parser.readPlaylistsResponse(response);

        return playlists;
    }

}
