import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HWID {
    public static String getHWID() {
        try {
            Process process = Runtime.getRuntime().exec("system_profiler SPHardwareDataType | awk '/UUID/ {print $3}'"); // Windows
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.readLine(); // Skip first line
            return reader.readLine().trim(); // Read UUID
        } catch (IOException e) {
            e.printStackTrace();
            return "UNKNOWN_HWID";
        }
    }
}