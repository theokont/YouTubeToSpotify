import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ServerHandler implements HttpHandler {

    public Parser parser;
    public SpotifyAuth auth;
    public static boolean setCode;

    public ServerHandler() {
        parser = new Parser();
        setCode = false;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        auth = new SpotifyAuth();
        String url = exchange.getRequestURI().getQuery();
        String code = parser.getItem(url,"code");
        auth.setCode(code);
        setCode = true;
        String message = "Code has been received, you may close this tab";
        exchange.sendResponseHeaders(200, message.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }
}
