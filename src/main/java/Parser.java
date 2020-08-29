import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Parser {


    public String parseListID(String URL) {
        StringBuilder listID = new StringBuilder();
        int pointer = 0;
        int idLength = 0; // listID length is fixed size at 34 characters
        for (int i = 0; i < URL.length(); i++) {
            if (URL.charAt(i) == '=') {
                String subStr = URL.substring(i-4,i); // creates a substring of the 4 characters before '='
                if (subStr.equals("list")) {
                    pointer = i;
                }
            }
            if (i > pointer && pointer != 0 && idLength < 34) {
                listID.append(URL.charAt(i));
                idLength++;
            }
        }
        return String.valueOf(listID);
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