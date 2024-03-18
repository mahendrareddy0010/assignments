package mr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AtomicFile {
    String originalFileName;
    File tempFile;
    FileWriter writer;

    public AtomicFile(String prefix, String suffix, String originalFileName) throws IOException {
        this.originalFileName = originalFileName;
        this.tempFile = File.createTempFile("temp-map-out", ".txt");
        this.writer = new FileWriter(tempFile);

    }

    public void write(String content) throws IOException {
        writer.write(content);
    }

    public boolean close() {
        File newFile = new File(originalFileName);
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("In atomic file");
        }
        if (tempFile.renameTo(newFile)) {
            System.out.println("File renamed atomically to: " + originalFileName);            
            return true;
        } else {
            System.out.println("Atomic rename failed.");
            return false;
        }
    }

}
