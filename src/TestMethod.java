import java.io.File;


public class TestMethod {

  public static String getImmediateDirectory(String filePath) {
    if (filePath == null || filePath.trim().isEmpty() || !filePath.contains(File.separator)) {
      return "";
    }
    return filePath.substring(0, filePath.lastIndexOf(File.separator));

  }

  public static void main(String[] args){
    String txWorkingFile = "src/main/java/com/blueoptima/dwq/queue/consumer/AbstractConsumer.java";
    String fileName = "AbstractConsumer.java";


    String directory = getImmediateDirectory(txWorkingFile);
    System.out.println(directory);
    String filePathRelativeToSrc =
        directory.isEmpty() ? fileName : directory + File.separator + fileName;
    System.out.println(filePathRelativeToSrc);
  }


}
