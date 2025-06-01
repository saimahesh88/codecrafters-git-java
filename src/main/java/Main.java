import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
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
      case "hash-object" -> {
        final String fileName = args[2];
        try{
          MessageDigest md = MessageDigest.getInstance("SHA-1");
          File inputFile = new File(fileName);
          byte[] content = Files.readAllBytes(inputFile.toPath());
          String header = "blob " + content.length + "\0";
          byte[] fullContent = concatenate(header.getBytes(), content);
          byte[] digest = md.digest(fullContent);
              
          StringBuilder hexString = new StringBuilder();
              
          for (byte b : digest) {
              hexString.append(String.format("%02x", b));
          }
          System.out.print(hexString);
          String dir = ".git/objects/"+hexString.substring(0,2);
          String filePath = dir + "/" + hexString.substring(2);
          new File(dir).mkdirs();
          try (FileOutputStream fos = new FileOutputStream(filePath); //FileOutputStream allows writing byte data to a file, creating a new file if it doesn't exist or overwriting an existing one.
              DeflaterOutputStream dos = new DeflaterOutputStream(fos)) { //compresses data using the DEFLATE algorithm (used in ZIP files, for example). Any data written to dos will be compressed before being passed to the file output stream.
              dos.write(fullContent);
          }
        }
        catch (IOException e) {
          //System.out.println("An error occurred.");
          e.printStackTrace();
        }
        catch(NoSuchAlgorithmException e){
          throw new RuntimeException(e);
        } 
      }

      case "ls-tree" -> {
          final String objectHash = args[2];
          final String objectFolder = objectHash.substring(0, 2);
          final String objectFilename = objectHash.substring(2);
          final File treeObject = new File(".git/objects/" + objectFolder + "/" + objectFilename);
          try {
              byte[] data = Files.readAllBytes(treeObject.toPath());
              Inflater inflater = new Inflater();
              inflater.setInput(data);

              try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
                  byte[] buffer = new byte[1024];
                  while (!inflater.finished()) {
                      int count = inflater.inflate(buffer); // decompress data into buffer and returns length
                      outputStream.write(buffer, 0, count);
                  }
                  String decompressedString = outputStream.toString("UTF-8");
                  String[] arr = decompressedString.split("\0");
                  for(int i=1;i<arr.length;i++){
                    String[] permissionsAndName = arr[i].split(" ");
                    if(permissionsAndName.length < 2){break;}
                    System.out.println(permissionsAndName[1]);
                  }
              } catch (DataFormatException e) {
                  throw new RuntimeException(e);
              }
          
            // try (Stream<Path> stream = Files.list(path)) {
            //     stream.filter(Files::isDirectory)
            //           .forEach(dir -> System.out.println(dir.getFileName()));
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }
          }catch (IOException e) {
              throw new RuntimeException(e);
          }
      }
      default -> System.out.println("Unknown command: " + command);
    }
  }
    private static byte[] concatenate(byte[] a, byte[] b) {
          byte[] result = new byte[a.length + b.length];
          System.arraycopy(a, 0, result, 0, a.length);
          System.arraycopy(b, 0, result, a.length, b.length);
          return result;
    }

  }


