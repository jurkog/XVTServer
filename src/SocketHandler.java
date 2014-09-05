import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.simple.JSONObject;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;

@WebSocket
public class SocketHandler {

    String email = "@", currentTorrent, fold, sendLink, moviesName;
    Session client;
    FileWriter fileWriter;


    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {

        JSONObject object = new JSONObject();
        object.put("title", fold);
        object.put("movie", currentTorrent);
        object.put("link", sendLink);

        TorrentSocket.MOVIE_ARRAY.add(0, object);
        System.out.println(TorrentSocket.MOVIE_ARRAY.toJSONString());
        try {
            fileWriter = new FileWriter(moviesName);
            fileWriter.write(TorrentSocket.MOVIE_ARRAY.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Written");
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error:");
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        client = session;
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        // Var declare
        boolean movieRepeat = false;

        // Debug
        System.out.println("Message: " + message);

        // Message Handle
        if (message.contains("torrents")) { // Starting to download the torrent file.
            String data[] = message.split("//d//");
            message = data[0];
            email = data[1];
            System.out.println(message+"|"+email);
            for (int i = 0; i < TorrentSocket.torrents.size(); i++) {
                if (TorrentSocket.torrents.get(i).trim().equals(message.trim())) {
                    try {
                        GoogleMail.Send("vid.torrent.email", "guba1234", email, "Movie Downloaded!", "<html><a href='"+TorrentSocket.links.get(i)+"'>Click Here!</a></html>");
                        client.getRemote().sendString("Movie is already downloaded! Check your email.");
                        movieRepeat = true;
                        break;
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!movieRepeat) {
                currentTorrent = message.trim();
                try {
                    client.getRemote().sendString("Movie is Downloading! Please wait patiently for an email.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileDownload(message.trim(), "C:\\Users\\Administrator\\Documents");
            }
        }

    }

    private void fileDownload(String address, String destinationDir) {
        int slashIndex = address.lastIndexOf('/');
        int periodIndex = address.lastIndexOf('.');

        if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < address.length() - 1) {
            fileUrl(address, TorrentSocket.CURRENT_DOWNLOADS+".torrent", destinationDir);
            downloadTorrent();
            TorrentSocket.CURRENT_DOWNLOADS++;
        }
    }

    private void fileUrl(String address, String localFile, String destinationDir) {
        OutputStream outputStream = null;
        URLConnection urlConnection;
        InputStream is = null;

        try {
            // Downloading the .torrent File
            URL siteUrl;
            byte[] buffer;
            int byteRead, byteWritten = 0;
            siteUrl = new URL(address);
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationDir+"\\"+localFile));

            urlConnection = siteUrl.openConnection();
            is = urlConnection.getInputStream();
            buffer = new byte[1024];
            while ((byteRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, byteRead);
                byteWritten += byteRead;
            }
            System.out.println(".torrent File Succesfully Downloaded.");
            System.out.println("File name:\""+localFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Closing our connection with the net
        finally {
            try {
                is.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void downloadTorrent() {
        try {
            // Adding directory listener
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path tempPath = Paths.get("C:\\xampp\\htdocs\\movies\\");
            tempPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            // Starting uTorrent with Command Line
            Process p = Runtime.getRuntime().exec("cmd /c cd C:\\Users\\Administrator\\AppData\\Roaming\\uTorrent & start uTorrent.exe /DIRECTORY C:\\Users\\Administrator\\Documents C:\\Users\\Administrator\\Documents\\"+ TorrentSocket.CURRENT_DOWNLOADS + ".torrent");
            p.waitFor();

            // Searching for WatchKey events
            String path = "C:\\xampp\\htdocs\\movies\\", subPath = "null";
            boolean directoryFound = false;

            while (!directoryFound) {
                WatchKey key = watcher.take();

                // Poll all the events queued for the key.
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();
                    if (kind.name().endsWith("ENTRY_CREATE")) {

                        fold = (event.context().toString());
                        System.out.println(fold);
                        subPath = event.context().toString().trim();
                        path += subPath+"\\";
                        directoryFound = true;
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

            // File Declarations
            String files;
            File folder;
            File[] listOfFiles;
            Thread.sleep(10000);

            folder = new File(path);
            listOfFiles = folder.listFiles();

            //Beginning of file search loop
            boolean fileDownloaded = false;
            while (!fileDownloaded) {
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        files = listOfFiles[i].getName();
                        if (files.contains(".mp4") && !files.contains(".part") && listOfFiles[i].length() > 524288000) {

                                    try {
                                        URL link = new URL("http://209.159.150.117/movies/"+subPath+"/"+listOfFiles[i].getName());
                                        GoogleMail.Send("vid.torrent.email", "guba1234", email, "Movie Downloaded!", "<html><a href='"+link.toString()+"'>Click Here!</a></html>");
                                        fileDownloaded = true;

                                        // Writing movie
                                        moviesName = "C:\\xampp\\htdocs\\movies.json"; // Torrent link
                                        TorrentSocket.torrents.add(currentTorrent);
                                        TorrentSocket.links.add(link.toString());
                                        System.out.println("Movie successfully downloaded.");


                                    } catch (AddressException e) {
                                    } catch (MessagingException e) {

                                    }
                                    break;

                            }
                            break;
                        }
                    }
                }

           } catch (IOException e) {

        } catch (InterruptedException e) {

        }

    }
}