import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;


public class TorrentSocket {
    public static ArrayList<String> torrents = new ArrayList<String>(), links = new ArrayList<String>();
    public static int CURRENT_DOWNLOADS = 0;
    public static JSONArray MOVIE_ARRAY;
    public static JFrame mainFrame = new JFrame("Window.");



    public static void main(String[] args) throws Exception {
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(300, 300);

        File file = new File("C:\\xampp\\htdocs\\movies.json");
        System.out.println("LOF: " + file.length());
        if (file.length() > 0) {
            JSONParser parser = new JSONParser();
            Object object = parser.parse(new FileReader("C:\\xampp\\htdocs\\movies.json"));
            MOVIE_ARRAY = (JSONArray)object;
            for (int i = 0; i < MOVIE_ARRAY.size(); i++) {
                JSONObject current = (JSONObject)MOVIE_ARRAY.get(i);
                torrents.add((String)current.get("movie"));
                links.add((String)current.get("link"));
            }
        }

        JSONArray pbayArray = new JSONArray();
        for (int i = 0; i < 100; i++) {
            Document doc = Jsoup.connect("http://thepiratebay.se/browse/207/" + i +"/7/0").userAgent("Mozilla").get();

            Elements headLines = doc.getElementsByClass("detName");

            System.out.println("Here" + headLines.size());
            for (Element src : headLines) {
                String movieTitle = src.text();
                System.out.println(movieTitle);
                if (movieTitle.contains("BrRip") && movieTitle.contains("YIFY")) {
                    movieTitle = movieTitle.substring(0, movieTitle.indexOf("BrRip"));
                    System.out.println("Title: " + movieTitle);

                    Element child = src.child(0);
                    String link = child.attr("href");
                    link = "http://thepiratebay.se" +link;
                    System.out.println("Link: " + link);

                    JSONObject current = new JSONObject();
                    current.put("title", movieTitle);
                    current.put("link", link);
                    pbayArray.add(current);

                }
            }
        }

        FileWriter writer = new FileWriter("C:\\xampp\\htdocs\\pbay.json");
        writer.write(pbayArray.toJSONString());
        writer.flush();
        writer.close();

        Server server = new Server(8080);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(SocketHandler.class);
            }
        };
        server.setHandler(wsHandler);
        server.start();
        server.join();
    }
}