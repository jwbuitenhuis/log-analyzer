import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.*;

public class LogParser {
	
	private static void reportLoggedIn(List<Request> requests) {
		Map<String, List<Request>> sessions = requests.stream().filter(Request::isLoggedIn)
			//.peek(System.out::println)
			.collect(Collectors.groupingBy(Request::getUserName));
		
		List<Session> sessionList = sessions
			.entrySet()
			.stream()
			.map(entry -> new Session(entry.getKey(), entry.getValue()))
			.collect(Collectors.toList());

		Collections.sort(sessionList);
		
		sessionList.forEach(Session::print);
	}
	
	private static void reportWebsiteRequests(List<Request> requests) {
		// gather all requests into sessions using ip address
		// look for sessions that have at least 3 requests
		Map<String, List<Request>> sessions = requests.stream()
			.filter(request -> !request.isTracking())
			.collect(Collectors.groupingBy(Request::getIp));
		
		List<Session> sessionList = sessions
				.entrySet()
				.stream()
				.map(entry -> new WebsiteSession(entry.getKey(), entry.getValue()))
				.filter(WebsiteSession::isValid)
				.collect(Collectors.toList());


		Collections.sort(sessionList);

		sessionList.forEach(Session::print);
	}
	
	private static List<Request> getRequests(String fileName) throws IOException {
		List<Request> requests = Files.lines(Paths.get(fileName))
			.filter(line -> line.indexOf("logfile turned over") == -1)
			.map(Request::new)
			.collect(Collectors.toList());

		return requests;
	}

	private static void processLogFile(String filename) {
		try {
		List<Request> requests = getRequests(filename);
		reportLoggedIn(requests);
		} catch (IOException e) {
			System.out.println("error: " + e.getMessage());
		}
	}
	
	private static boolean afterDate(String filename, String date) {
		String extracted = ParseLogFilename.extractDate(filename);

		if (extracted.equals("access")) {
			return true;
		}

		LocalDate parsed = LocalDate.parse(extracted);
		LocalDate threshold = LocalDate.parse(date);
		
		return parsed.isAfter(threshold);
	}

	private static void findLogFilesIncluding(String directory, String date)
			throws IOException {
		
		Files.list(Paths.get(directory))
			.map(path -> path.toAbsolutePath().toString())
			.filter(filename -> filename.indexOf("access.log") != -1)
			.filter(filename -> afterDate(filename, date))
			.forEach(filename -> processLogFile(filename));
	}

	public static void main(String[] argv) throws IOException {
		if (argv.length < 1) {
			System.out.println("ERROR: Provide access log filename");
			return;
		}
		
		if (argv.length == 2) {
			findLogFilesIncluding(argv[0], argv[1]);
		} else {
			processLogFile(argv[0]);
		}
		
		System.out.println("-> For older logs, type e.g. 'serverlogs 2016-12-23'");
	}
}
