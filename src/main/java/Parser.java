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

    public ArrayList<String> readYouTubeResponse(String jsonResponse) throws IOException {

        ArrayList<String> playlist = new ArrayList<>();
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

    public ArrayList<String> readItemsArray(JsonReader reader, ArrayList<String> playlist) throws IOException {

        reader.beginArray();
        while (reader.hasNext()) {
            playlist = readItemsElements(reader, playlist);
        }
        reader.endArray();
        return playlist;
    }

    public ArrayList<String> readItemsElements(JsonReader reader, ArrayList<String> playlist) throws IOException {

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

    public void readSnippet(JsonReader reader, ArrayList<String> playlist) throws IOException{

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
}