import kong.unirest.Unirest;

import java.io.IOException;
import java.util.Properties;

public class YouTubeApi {
    private Properties config;
    private String apiKey = "";

    public YouTubeApi() {
        try {
            config = CredentialsLoader.loadCredentials();
            apiKey = config.getProperty("youtube.api.key");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String requestPlaylistItems(String playlistID) {
        String response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems")
                .queryString("key", apiKey)
                .queryString("part", "snippet")
                .queryString("playlistId", playlistID)
                .queryString("maxResults", 50)
                .asString().getBody();
        return response;
    }

}
