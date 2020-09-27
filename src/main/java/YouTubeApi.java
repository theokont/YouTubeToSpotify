import kong.unirest.Unirest;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class YouTubeApi {
    private Properties config;
    private String apiKey;
    private static String nextPageToken;
    private static boolean pageTokenSet;

    public YouTubeApi() {
        try {
            config = CredentialsLoader.loadCredentials();
            apiKey = config.getProperty("youtube.api.key");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.nextPageToken = null;
        this.pageTokenSet = false;
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

    public String requestPlaylistItems(String playlistID, String nextPageToken) {
        String response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems")
                .queryString("key", apiKey)
                .queryString("part", "snippet")
                .queryString("playlistId", playlistID)
                .queryString("maxResults", 50)
                .queryString("pageToken", nextPageToken)
                .asString().getBody();
        return response;
    }

    public List<String> retrievePlaylist(String youtubeUrl, String youtubeJson) throws IOException {
        List<String> playlist = Parser.readYouTubeResponse(youtubeJson);

        if (pageTokenSet) {
            while (pageTokenSet) {
                setPageTokenSet(false);
                youtubeJson = requestPlaylistItems(Parser.getItem(youtubeUrl, "list"), getNextPageToken());
                playlist = Parser.readYouTubeResponse(youtubeJson, playlist);
            }
        }
        return playlist;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getNextPageToken() {
        return this.nextPageToken;
    }

    public void setPageTokenSet(boolean state) {
        pageTokenSet = state;
    }

    public boolean getPageTokenSet() {
        return pageTokenSet;
    }
}
