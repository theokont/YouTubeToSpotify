import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Parser {


    public String getItem(String URL, String name) {
        StringBuilder item = new StringBuilder();
        int pointer = 0;
        for (int i = 0; i < URL.length(); i++) {
            if (URL.charAt(i) == '=') {
                // create a substring to check if the word before '=' is the desired item
                String subStr = URL.substring(i-name.length(),i);
                if (subStr.equals(name)) {
                    pointer = i;
                }
            }
            if (i > pointer && pointer != 0) {
                if (URL.charAt(i) == '&') {
                    break;
                }
                else {
                    item.append(URL.charAt(i));
                }
            }
        }
        if (item.length() == 0) {
            throw new IllegalStateException("Something went wrong, URI didn't contain " + item);
        }
        else {
            return String.valueOf(item);
        }
    }

    public String readAuthToken(String token, String item) throws IOException {

        String value = null;
        JsonReader reader = new JsonReader(new StringReader(token));
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals(item)) {
                value = reader.nextString();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return value;

    }

    public static String readUserID(String response) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(response));
        String userID = null;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                userID = reader.nextString();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return userID;
    }

    // methods readYoutubeResponse, readItemsArray, readItemsElements and readSnippet are used in order to
    // access the Json's hierarchical layers

    public List<String> readYouTubeResponse(String jsonResponse) throws IOException {

        List<String> playlist = new ArrayList<>();
        JsonReader reader = new JsonReader(new StringReader(jsonResponse));
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("items")) {
                playlist = readItemsArray(reader, playlist);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return playlist;
    }

    public List<String> readItemsArray(JsonReader reader, List<String> playlist) throws IOException {

        reader.beginArray();
        while (reader.hasNext()) {
            playlist = readItemsElements(reader, playlist);
        }
        reader.endArray();
        return playlist;
    }

    public List<String> readItemsElements(JsonReader reader, List<String> playlist) throws IOException {

        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("snippet")) {
                readSnippet(reader, playlist);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return playlist;
    }

    public void readSnippet(JsonReader reader, List<String> playlist) throws IOException{

        String title = null;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("title")) {
                title = reader.nextString();
                playlist.add(title);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    // methods readPlaylistsResponse, readPlaylistsItemsArray, appendPlaylists and readOwner are used
    // in order to access the Json's hierarchical layers

    public Map<String,String> readPlaylistsResponse(String playlistsJson) throws IOException {

        Map<String,String> playlists = new HashMap<>();
        JsonReader reader = new JsonReader(new StringReader(playlistsJson));
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("items")) {
                playlists = readPlaylistsItemsArray(reader, playlists);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return playlists;
    }

    public Map<String,String> readPlaylistsItemsArray(JsonReader reader, Map<String,String> playlists) throws IOException {

        reader.beginArray();
        while(reader.hasNext()) {
            appendPlaylists(reader, playlists);
        }
        reader.endArray();
        return playlists;
    }

    public void appendPlaylists(JsonReader reader, Map<String,String> playlists) throws IOException {

        SpotifyApi spotify = new SpotifyApi();
        SpotifyAuth token = new SpotifyAuth();
        String userID = spotify.getUserID(token.getAccessToken());
        String playlistName = null;
        String playlistID = null;
        Boolean isOwner = false;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            // checks if the playlist belongs to the current user
            if (name.equals("owner")) {
                isOwner = checkOwner(reader, isOwner, userID);
            }
            else if (name.equals("id")) {
                playlistID = reader.nextString();
            }
            else if (name.equals("name")) {
                playlistName = reader.nextString();
            }
            else {
                reader.skipValue();
            }
            if ( playlistID != null && playlistName != null && isOwner) {
                playlists.put(playlistName, playlistID);
                playlistID = null;
                playlistName = null;
                continue;
            }
        }
        reader.endObject();
    }

    public Boolean checkOwner(JsonReader reader, Boolean isOwner, String userID) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String ownerObjName = reader.nextName();
            if (ownerObjName.equals("id")) {
                String id = reader.nextString();
                if (id.equals(userID)) {
                    isOwner = true;
                }
                else {
                    isOwner = false;
                }
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return isOwner;
    }
}





















