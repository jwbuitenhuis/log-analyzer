import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Session implements Comparable<Session> {
	String userName;
	List<Request> requests;
	ZonedDateTime startTime;
	Request firstRequest;
	Request lastRequest;

	public Session(String userName, List<Request> requests) {
		this.userName = userName;
		this.requests = requests;

		firstRequest = requests.get(0);
		lastRequest = requests.get(requests.size() - 1);

		startTime = requests.get(0).getTimestamp();
	}
	
	// default sorting is by start timestamp
	public int compareTo(Session other) {
		return startTime.compareTo(other.startTime);
	}
	
	public void print() {
		System.out.println("------------------------");
		System.out.println("Date: " + requests.get(0).formatDate());
		System.out.println("Username: " + userName);		
		System.out.println("Session from " + sessionStart() + " to " + sessionEnd());
		System.out.println("");

		formatPageViews();
		System.out.println("");

		requests.stream()
			.filter(Request::shouldReport)
			.forEach(System.out::println);

		System.out.println("");
	}

	/* measure the time between route events or the last route event
	 * and the end of the session 
	 */
	private void formatPageViews() {
		List<Request> pageViews = requests.stream()
			.filter(Request::isRouteView)
			.collect(Collectors.toList());

		pageViews.add(lastRequest);

		ZonedDateTime previous = startTime;
		Request previousRequest = null;
		for(Request request: pageViews) {
			if (previousRequest != null) {
				ZonedDateTime current = request.getTimestamp();
				Duration duration = Duration.between(previous,  current);
				System.out.format("%-40s:%20s\n", previousRequest.toString(), "spent " + formatDuration(duration));
				previous = current;
			}
			previousRequest = request;
		}
	}

	private String formatDuration(Duration duration) {
		long s = duration.getSeconds();
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}

	private String sessionStart() {
		Request start = requests.get(0);
		return start.formatTime();
	}
	
	private String sessionEnd() {
		Request end = requests.get(requests.size() - 1);
		
		return end.formatTime();
	}
}