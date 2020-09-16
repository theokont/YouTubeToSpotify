import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Parser {

    // parses the URL given and extracts the chosen item from it
    public static String getItem(String URL, String name) {
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

    // takes a String and removes parentheses and brackets in it, along with their content
    public static String removeExplanatory(String title) {
        StringBuilder finalTitle = new StringBuilder();
        boolean startParenthesis = false;
        boolean startBracket = false;
        boolean verticalBarStart = false;
        for (int i = 0; i < title.length(); i++) {

            if (title.charAt(i) == '(') {
                startParenthesis = true;
            }
            else if (title.charAt(i) == ')') {
                startParenthesis = false;
                continue;
            }
            else if (title.charAt(i) == '[') {
                startBracket = true;
            }
            else if (title.charAt(i) == ']') {
                startBracket = false;
                continue;
            }
            else if (title.charAt(i) == '|') {
                verticalBarStart = true;
            }

            if (startBracket == false && startParenthesis == false && !verticalBarStart) {
                if (title.charAt(i) != ' ') {
                    finalTitle.append(title.charAt(i));
                }
                // checks if there is already a space (' ') in the previous index to ensure single spaces
                else {
                    if ((finalTitle.length()-1) >= 0) {
                        if (finalTitle.charAt(finalTitle.length()-1) != ' ') {
                            finalTitle.append(title.charAt(i));
                        }
                        else if (finalTitle.charAt(finalTitle.length()-1) == ' ') {
                            continue;
                        }
                    }
                }
            }
        }
        return String.valueOf(finalTitle);
    }

    public static String readAuthToken(String token, String item) throws IOException {

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
        if (value == null) {
            throw new IllegalStateException("Token" + item + "was not retrieved");
        }
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

    public static List<String> readYouTubeResponse(String jsonResponse) throws IOException {

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

    public static List<String> readItemsArray(JsonReader reader, List<String> playlist) throws IOException {

        reader.beginArray();
        while (reader.hasNext()) {
            playlist = readItemsElements(reader, playlist);
        }
        reader.endArray();
        return playlist;
    }

    public static List<String> readItemsElements(JsonReader reader, List<String> playlist) throws IOException {

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

    public static void readSnippet(JsonReader reader, List<String> playlist) throws IOException{

        String title = null;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("title")) {
                title = reader.nextString();
                playlist.add(removeExplanatory(title));
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    // methods readPlaylistsResponse, readPlaylistsItemsArray, appendPlaylists and checkOwner are used
    // in order to access the Json's hierarchical layers

    public static Map<String,String> readPlaylistsResponse(String playlistsJson) throws IOException {

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

    public static Map<String,String> readPlaylistsItemsArray(JsonReader reader, Map<String,String> playlists) throws IOException {

        reader.beginArray();
        while(reader.hasNext()) {
            appendPlaylists(reader, playlists);
        }
        reader.endArray();
        return playlists;
    }

    public static void appendPlaylists(JsonReader reader, Map<String,String> playlists) throws IOException {

        SpotifyApi spotify = new SpotifyApi();
        String userID = spotify.getUserID();
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
            }
        }
        reader.endObject();
    }

    public static Boolean checkOwner(JsonReader reader, Boolean isOwner, String userID) throws IOException {
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

    public static String readSearchResponse(String response) throws IOException {

        JsonReader reader = new JsonReader(new StringReader(response));
        String uri = null;
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("tracks")) {
               uri = readTracks(reader, uri);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return uri;
    }

    public static String readTracks(JsonReader reader, String uri) throws IOException {

        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("items")) {
                uri = readTrackItemsArray(reader, uri);
            }
            else {
                reader.skipValue();
            }
        }
        return uri;
    }

    public static String readTrackItemsArray(JsonReader reader, String uri) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            uri = readTrackItemsElements(reader, uri);
        }
        reader.endArray();
        return uri;
    }

    public static String readTrackItemsElements(JsonReader reader, String uri) throws IOException {

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("uri")) {
                uri = reader.nextString();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return uri;
    }




}





















