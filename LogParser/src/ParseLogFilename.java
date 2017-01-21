import java.util.Date;
import java.util.regex.*;

public class ParseLogFilename {
	static String extractDate(String path) {
		String[] parts = path.split("/");
		String filename = parts[parts.length - 1];

		parts = filename.split("\\.");
		return parts[0];
	}
}
