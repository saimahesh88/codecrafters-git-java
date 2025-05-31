import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.err.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    
    final String command = args[0];
    
    switch (command) {
      case "init" -> {
        final File root = new File(".git");
        new File(root, "objects").mkdirs();
        new File(root, "refs").mkdirs();
        final File head = new File(root, "HEAD");
    
        try {
          head.createNewFile();
          Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
          System.out.println("Initialized git directory");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      case "cat-file" -> {
        // args -> cat-file -p <hash>
          final String objectHash = args[2];
          final String objectFolder = objectHash.substring(0, 2);
          final String objectFilename = objectHash.substring(2);
          try {
              byte[] data = Files.readAllBytes(Paths.get(".git/objects/" + objectFolder + "/" + objectFilename));
              Inflater inflater = new Inflater();
              inflater.setInput(data);

              try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
                  byte[] buffer = new byte[1024];
                  while (!inflater.finished()) {
                      int count = inflater.inflate(buffer); // decompress data into buffer and returns length
                      outputStream.write(buffer, 0, count);
                  }
                  String decompressedString = outputStream.toString("UTF-8");
                  System.out.print(decompressedString.substring(decompressedString.indexOf("\0")+1));

              } catch (DataFormatException e) {
                  throw new RuntimeException(e);
              }
          } catch (IOException e) {
              throw new RuntimeException(e);
          }

      }
      default -> System.out.println("Unknown command: " + command);
    }
  }
}
