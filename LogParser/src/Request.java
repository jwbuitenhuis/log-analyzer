import java.time.*;
import java.time.format.*;
import java.util.*;

public class Request {
	String ip;
	String userName;
	ZonedDateTime timeStamp;
	String request;
	String status;
	String browserString;

	static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

	static DateTimeFormatter timeFormatter =
		DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

	static DateTimeFormatter dateFormatter =
		DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

		// 162.158.99.162 - - [05/Jun/2016:05:46:10 +0200] "GET /administrator/index.php HTTP/1.1" 404 162 "-" "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0"
	public Request(String request) {
		List<String> parts = parse(request);
		ip = parts.get(0);
		//
		userName = parts.get(2);
		timeStamp = parseTimeStamp(parts.get(3));
		this.request = decodeUrl(parts.get(4));
		status = parts.get(5);
		// length?
		//
		browserString = parts.get(8);
	}
	
	private String decodeUrl(String request) {
		try {
			return java.net.URLDecoder.decode(request, "UTF-8");
		} catch(Exception e) {
			System.err.println(e.getMessage() + " in: " + request);
			//e.printStackTrace();
		}
		return "ERROR - UNABLE TO DECODE REQUEST";
	}
	
	public ZonedDateTime getTimestamp() {
		return timeStamp;
	}
	
	public boolean isRouteView() {
		return request.indexOf("Route-view") != -1;
	}

	public boolean shouldReport() {
		return (isTracking() && !isKeepAlive()) || isImage();		
	}

	public boolean isImage() {
		return request.indexOf("GET /images/assets/") == 0;
	}

	public boolean isTracking() {		
		return request.indexOf("tracking.gif") != -1;
	}
	
	public boolean isKeepAlive() {
		return request.indexOf("keep-alive") != -1;
	}
	
	public boolean isValid() {
		return !userName.equals("-") &&
			!userName.equals("admin") &&
			status.equals("200");
	}
	
	public String getUserName() {
		return userName;
	}
	
	private ZonedDateTime parseTimeStamp(String timeStamp) throws DateTimeParseException {
		// [08/May/2016:06:43:37 +0200]
		// get rid of [, ]
		String cleaned = timeStamp.replace("[", "").replace("]", "");

		ZonedDateTime date = ZonedDateTime.now(); // if parsing fails we need something
		try {
			date = ZonedDateTime.parse(cleaned, formatter);
		} catch(DateTimeParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	private List<String> parse(String request) {
		// loop through the string. If a space is encountered, add the current buffer to the array.
		// if a quote is encountered, add everything up until the next quote to next item 
		StringBuffer buffer = new StringBuffer();
		List<String> result = new ArrayList<>();

		for(int i = 0; i < request.length(); i++) {
			char c = request.charAt(i);
			if (c == ' ') {
				result.add(buffer.toString());
				buffer.setLength(0); //clear
			} else if (c == '"') {
				int end = request.indexOf('"', i + 1);
				buffer.append(request.substring(i + 1, end));
				i = end;
			} else if (c == '[') {
				int end = request.indexOf(']', i + 1);
				buffer.append(request.substring(i + 1, end));
				i = end;
			} else {
				buffer.append(c);
			}
		}
		result.add(buffer.toString());
		return result;
	}

	static String ROUTE_VIEW = "send-event-Route-view-";
	private String formatRouteView() {
		
		int beginIndex = request.indexOf(ROUTE_VIEW) + ROUTE_VIEW.length();
		int endIndex = request.indexOf(" ", beginIndex);
		return "Page view: " + request.substring(beginIndex, endIndex).replace('_', ' ');
	}

	private String formatRequest() {
		if (isTracking()) {
			if (isRouteView()) {
				return formatRouteView();
			} else {
				int beginIndex = request.indexOf("_");
				int endIndex = request.indexOf(" ", beginIndex);
				return request.substring(beginIndex + 1, endIndex).replace('_', ' ');
			}
		}
		return request;
	}
	
	public String formatTime() {
		return timeStamp.format(timeFormatter);
	}

	public String formatDate() {
		return timeStamp.format(dateFormatter);
	}

	public String toString() {
		
		return formatTime() + " - " + formatRequest();
	}
}
