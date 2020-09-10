import picocli.CommandLine;
import picocli.CommandLine.Command;
import java.io.IOException;

@Command(
        name = "app",
        mixinStandardHelpOptions = true,
        subcommands = {
                PlaylistTransfer.class,
                SpotifyAuth.class,
                SpotifyApi.class,
                Credentials.class
        }
)
public class Application implements Runnable{


    public static void main(String[] args) throws IOException {

        Credentials.init();
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {

    }
}


















