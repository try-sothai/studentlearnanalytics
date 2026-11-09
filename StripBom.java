import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class StripBom {
    public static void main(String[] args) throws Exception {
        String path = "D:/Development/Projects/studentlearnanalytics/app/src/main/java/com/social7/studentlearnanalytics/admin/AdminDashboardFragment.java";
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        if (bytes.length > 3 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            byte[] newBytes = Arrays.copyOfRange(bytes, 3, bytes.length);
            Files.write(Paths.get(path), newBytes);
        }
    }
}