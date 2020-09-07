import kong.unirest.Unirest;

public class YouTubeApi {


    public String requestPlaylistItems(String playlistID) {
        String response = Unirest.get("https://www.googleapis.com/youtube/v3/playlistItems")
                .queryString("key", "AIzaSyBpAe6_QZMpqLk7tCjBD6oN5_LTsgUJnmg")
                .queryString("part", "snippet")
                .queryString("playlistId", playlistID)
                .queryString("maxResults", 50)
                .asString().getBody();
        return response;
    }

}
