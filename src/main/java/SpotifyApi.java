import kong.unirest.Unirest;
import java.io.IOException;
import java.util.Map;

public class SpotifyApi {

    private static SpotifyAuth token;

    public SpotifyApi() {
        token = new SpotifyAuth();
    }


    public String getUserID() throws IOException {

        String response = Unirest.get("https://api.spotify.com/v1/me")
                .header("Authorization", "Bearer " + getAccessToken())
                .asString().getBody();
        return Parser.readUserID(response);
    }

    public Map<String,String> getPlaylists() throws IOException {

        String response = Unirest.get("https://api.spotify.com/v1/me/playlists")
                .header("Authorization", "Bearer " + getAccessToken())
                .queryString("limit",50)
                .asString().getBody();
        Parser parser = new Parser();
        Map<String,String> playlists = parser.readPlaylistsResponse(response);
        System.out.println(getAccessToken());
        return playlists;
    }

    public String getAccessToken() {
        return token.getAccessToken();
    }
}
