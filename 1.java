import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class Sample {
    public static void main(String[] args) throws IOException {
        File file = new File("./input.txt");
        FileWriter writer = new FileWriter(file);
        writer.write("hello" + " " + 2 + "\n");
        writer.close();
        File directory = new File("./");
        for (String fileName : directory.list()) {
            if (fileName.endsWith(".java")) {
                System.out.println(fileName);
            }
        }
    }
}
