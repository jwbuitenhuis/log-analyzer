import java.io.*;
import java.nio.file.*;
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

	public static void main(String[] argv) throws IOException {
		if (argv.length < 1) {
			System.out.println("ERROR: Provide access log filename");
			return;
		}
		
		List<Request> requests = getRequests(argv[0]);

		reportLoggedIn(requests);
		//reportWebsiteRequests(requests);
	}
}
