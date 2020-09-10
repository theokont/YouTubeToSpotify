import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Command (
        name = "credentials"
)
public class Credentials {

    final static String LOCATION = String.format("%s" + File.separator + "src" + File.separator
                    + "main" + File.separator + "resources" + File.separator + ".credentials" + File.separator
                    + "credentials.properties",
            System.getProperty("user.dir"));


    public static void init() throws IOException {
        File file = new File(LOCATION);
        File parent = file.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Could not create folder "+ parent.getAbsolutePath());
        }

        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new IllegalStateException("Could not create file " + file.getAbsolutePath());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        Properties prop = CredentialsLoader.loadCredentials();
        if (prop.isEmpty() && file.exists()) {
            initProperties();
        }
    }

    private static void initProperties() {
        try (FileOutputStream os = new FileOutputStream(new File(LOCATION), true)) {
            String token = "spotify.auth.token=" + '\n';
            String clientID = "spotify.client.id=" + '\n';
            String clientSecret = "spotify.client.secret=" + '\n';
            String youtubekey= "youtube.api.key=" + '\n';
            os.write(token.getBytes(StandardCharsets.UTF_8));
            os.write(clientID.getBytes(StandardCharsets.UTF_8));
            os.write(clientSecret.getBytes(StandardCharsets.UTF_8));
            os.write(youtubekey.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void store(String accessToken) {
        try (FileInputStream is = new FileInputStream(new File(LOCATION))) {
            Properties prp = new Properties();
            prp.load(is);
            is.close();

            FileOutputStream os = new FileOutputStream(new File(LOCATION));
            prp.setProperty("spotify.auth.token", accessToken);
            prp.store(os, null);
            os.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Command (
            name = "youtube"
    )
    public static void storeYoutubeKey(@Parameters (description = "Youtube Api key") String key ) {

        try (FileInputStream is = new FileInputStream(new File(LOCATION))) {
            Properties prp = new Properties();
            prp.load(is);
            is.close();

            FileOutputStream os = new FileOutputStream(new File(LOCATION));
            prp.setProperty("youtube.api.key", key);
            prp.store(os, null);
            os.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Command (
            name = "id"
    )
    public static void storeClientID(@Parameters (description = "ClientID") String id) {

        try (FileInputStream is = new FileInputStream(new File(LOCATION))) {
            Properties prp = new Properties();
            prp.load(is);
            is.close();

            FileOutputStream os = new FileOutputStream(new File(LOCATION));
            prp.setProperty("spotify.client.id", id);
            prp.store(os, null);
            os.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Command (
            name = "secret"
    )
    public static void storeClientSecret(@Parameters (description = "ClientSecret") String secret) {

        try (FileInputStream is = new FileInputStream(new File(LOCATION))) {
            Properties prp = new Properties();
            prp.load(is);
            is.close();

            FileOutputStream os = new FileOutputStream(new File(LOCATION));
            prp.setProperty("spotify.client.secret", secret);
            prp.store(os, null);
            os.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
