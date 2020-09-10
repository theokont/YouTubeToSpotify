import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ServerHandler implements HttpHandler {

    public SpotifyAuth auth;
    public static boolean setCode;

    public ServerHandler() {
        setCode = false;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        auth = new SpotifyAuth();
        String url = exchange.getRequestURI().getQuery();
        String code = Parser.getItem(url,"code");
        auth.setCode(code);
        setCode = true;
        String message = "Authentication is complete, you may close this tab";
        exchange.sendResponseHeaders(200, message.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }
}
