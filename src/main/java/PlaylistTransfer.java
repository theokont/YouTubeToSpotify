import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Command(
        name = "transfer",
        description = "Transfer a playlist's contents from Youtube to Spotify. First param = Youtube link, second " +
        "param = Spotify playlist"
)
public class PlaylistTransfer implements Runnable {
    SpotifyApi spotify = new SpotifyApi();
    YouTubeApi youtube = new YouTubeApi();
    List<String> IDs = new ArrayList<>();
    List<String> failed = new ArrayList<>();

    @Parameters (index = "0", description = "The url that contains the Youtube playlist to be transferred")
    String youtubeUrl;

    @Parameters (index = "1", description = "The Spotify playlist in which the tracks are going to be added")
    String spotifyPlaylist;

    public void transfer(String youtubeUrl, String spotifyPlaylist) throws IOException {
        String youtubeJson = youtube.requestPlaylistItems(Parser.getItem(youtubeUrl, "list"));
        List<String> playlist = youtube.retrievePlaylist(youtubeUrl, youtubeJson);
        System.out.println("Please wait..");

        if (playlist.size() == 0) {
            throw new IllegalStateException("Error: Playlist is empty");
        }

        // appends on uris StringBuilder every track ID found. Failed searches are added in failed list
        for (int i = 0; i < playlist.size(); i++) {
            if (Parser.readSearchResponse(spotify.searchTrack(playlist.get(i))) != null) {
                IDs.add(Parser.readSearchResponse(spotify.searchTrack(playlist.get(i))));
            }
            else {
                failed.add(playlist.get(i));
            }
        }

        if (!spotify.getPlaylists().containsKey(spotifyPlaylist)) {
            System.out.println(CommandLine.Help.Ansi.ON.string("@|fg(173) The Spotify playlist that" +
                    " you entered could not be found |@"));
            System.out.println("A new playlist with the name " + spotifyPlaylist + " is about to be created");
            spotify.createPlaylist(spotifyPlaylist);
        }

        // Add the tracks in the Spotify playlist
        if (IDs.size() <= 100) {
            spotify.addTracks(spotify.getPlaylists().get(spotifyPlaylist), IDs);
        }
        else {
            int times;
            if (IDs.size()%100 == 0) {
                times = IDs.size() / 100;
            }
            else {
                times = (IDs.size()/100) + 1;
            }
            List<String> temp;
            for (int i = 1; i <= times; i++) {
                int from = (i-1) * 100;
                if (i < times) {
                    int to = (i * 100); // exclusive high endpoint
                    temp = IDs.subList(from, to);
                }
                else {
                    temp = IDs.subList(from, IDs.size());
                }
                spotify.addTracks(spotify.getPlaylists().get(spotifyPlaylist), temp);
            }
        }

        int succeeded = playlist.size()-failed.size();
        if (succeeded != 0) {
            System.out.println(CommandLine.Help.Ansi.ON.string("@|fg(40) Added " + succeeded + " tracks " +
                    "successfully! |@"));
        }
        else {
            System.out.println(CommandLine.Help.Ansi.ON.string("@|fg(173) Something went wrong.." + '\n'
                   + youtubeJson  + " |@"));
        }
        if (!failed.isEmpty()) {
            System.out.println(CommandLine.Help.Ansi.ON.string("@|fg(173) Unfortunately the following" +
                    " tracks could not be added: |@") + '\n');
            System.out.println(failed.toString());
        }
    }

    @Override
    public void run() {
        try {
            transfer(youtubeUrl,spotifyPlaylist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }
}
