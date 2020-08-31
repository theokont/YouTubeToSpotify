import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Parser {


    public String parseCallBackCode(String link) {
        StringBuilder code = new StringBuilder();
        int pointer = 0;
        for (int i = 0; i < link.length(); i++) {
            if (link.charAt(i) == '=') {
                String subStr = link.substring(i-4,i);
                if (subStr.equals("code")) {
                    pointer = i;
                }
            }
            if (i > pointer && pointer != 0) {
                if (link.charAt(i) == '&') {
                    break;
                }
                else {
                    code.append(link.charAt(i));
                }
            }
        }
        if (code.length() == 0) {
            throw new IllegalStateException("Something went wrong, URI didn't contain a code");
        }
        else {
            return String.valueOf(code);
        }
    }

    public String parseListID(String URL) {
        StringBuilder listID = new StringBuilder();
        int pointer = 0;
        for (int i = 0; i < URL.length(); i++) {
            if (URL.charAt(i) == '=') {
                String subStr = URL.substring(i-4,i); // creates a substring of the 4 characters before '='
                if (subStr.equals("list")) {
                    pointer = i;
                }
            }
            if (i > pointer && pointer != 0) {
                if (URL.charAt(i) == '&') {
                    break;
                }
                else {
                    listID.append(URL.charAt(i));
                }
            }
        }
        if (listID.length() == 0) {
            throw new IllegalStateException("Something went wrong, URL didn't contain a list ID");
        }
        else {
            return String.valueOf(listID);
        }
    }

    public ArrayList<String> readJsonResponse(String jsonResponse) throws IOException {

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