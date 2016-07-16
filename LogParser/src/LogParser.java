import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class LogParser {

	private static void readLog(String fileName)  throws IOException {
		Map<String, List<Request>> sessions = Files.lines(Paths.get(fileName))
			.filter(line -> line.indexOf("logfile turned over") == -1)
			.map(Request::new)
			.filter(Request::isValid)
			//.peek(System.out::println)
			.collect(Collectors.groupingBy(Request::getUserName));
		
		List<Session> sessionList = sessions
			.entrySet()
			.stream()
			.map(entry -> new Session(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		Collections.sort(sessionList);
		
		sessionList.forEach(Session::print);
//		System.out.println(sessions);
	}

	public static void main(String[] argv) throws IOException {
		if (argv.length < 1) {
			System.out.println("ERROR: Provide access log filename");
			return;
		}

		readLog(argv[0]);
	}
}
