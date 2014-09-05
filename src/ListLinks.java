import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Example program to list links from a URL.
 */
public class ListLinks {
    public static void main(String[] args) throws IOException {
        URL yahoo = new URL("http://thepiratebay.se/browse/207/0/7/0");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        yahoo.openStream()));

        String inputLine;

        while ((inputLine = in.readLine()) != null)
            System.out.println(inputLine);

        in.close();
    }
}