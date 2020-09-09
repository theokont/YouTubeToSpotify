import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CredentialsLoader {

    public static Properties loadCredentials() throws IOException {

        Properties config = new Properties();
        InputStream inputStream = new FileInputStream(Credentials.LOCATION);
        config.load(inputStream);
        inputStream.close();
        return config;
    }
}
