# XVTServer
Server portion of a proxy for streaming torrented HD - Movies. Best when set up on a Windows VPS with a fast uplink. I knew I would be able to do it in Java because I knew I could:

1. Execute command lines in Java
2. Send messages over the web (Using websockets)
3. Start uTorrent using command line prompt
4. Run it off a VPS

## How it works

1. On server startup, all the movie links are acquired:
  1. PirateBay is parsed (first 33 pages of HD movies) for all YIFY links (since they are .mp4 and HD)
  2. Links are stored in a file located locally (This allows for AJAX to occur on clientside)
2. Server waits for incoming requests
  1. A message containing a delimiter is recieved as a request
  2. It contains a url to a movie, and an email
3. Server downloads .torrent file (parses HTML of the url provided)
4. Command line starts uTorrent with .torrent file location as a command line parameter
5. Once the file downloads, it moves the downloaded file to the public/html directory
6. Send email to the client who originally requested to download the movie.

# NO LONGER IN USE AND SOLELY FOR EDUCATIONAL PURPOSES


