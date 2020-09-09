import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Credentials {

    final static String LOCATION = String.format("%s" + File.separator + ".credentials" + File.separator
                    +"credentials.properties",
            System.getProperty("user.dir"));


    public static void init() {
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
    }

    public static void store(String accessToken) {
        try (FileOutputStream os = new FileOutputStream(new File(LOCATION))) {
            String input = "spotify.auth.token=" + accessToken + '\n';
            os.write(input.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
