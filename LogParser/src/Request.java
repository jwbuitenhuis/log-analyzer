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
	String rawRequest;
	String encoded;

	static DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

	static DateTimeFormatter timeFormatter =
		DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.of("Europe/London"));

	static DateTimeFormatter dateFormatter =
		DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

		// 162.158.99.162 - - [05/Jun/2016:05:46:10 +0200] "GET /administrator/index.php HTTP/1.1" 404 162 "-" "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0"
	public Request(String request) {
		rawRequest = request;
		List<String> parts = parse(request);
		ip = parts.get(0);
		//
		userName = parts.get(2);
		timeStamp = parseTimeStamp(parts.get(3));
		encoded = parts.get(4);
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
			//System.err.println(e.getMessage() + " in: " + request);
			//e.printStackTrace();
		}
		return "ERROR - UNABLE TO DECODE REQUEST";
	}
	
	public String getIp() {
		return ip;
	}
	
	public ZonedDateTime getTimestamp() {
		return timeStamp;
	}
	
	public boolean isRouteView() {
		return request.indexOf("Route-view") != -1;
	}
	
	static String IFRAME_CLICK = "send-event-IFrameClick-view-";
	private boolean isYoutubeView() {
		return request.indexOf(IFRAME_CLICK) != -1 &&
				request.toLowerCase().indexOf("youtube") != -1;
	}

	private String formatYoutubeView() {
		int beginIndex = encoded.indexOf(IFRAME_CLICK) + IFRAME_CLICK.length();
		int endIndex = encoded.indexOf(" ", beginIndex);
		String current = decodeUrl(encoded.substring(beginIndex, endIndex)).replace('_', ' ');
		return "YouTube View: " + current;
	}

	public boolean isMouseMove() {
		return request.indexOf("mousemove") != -1;
	}

	public boolean shouldReport() {
		return (isTracking() && !isKeepAlive() && !isMouseMove()) || isImage();		
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
	
	public boolean isLoggedIn() {
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

		ZonedDateTime date = null;
		try {
			date = ZonedDateTime.parse(cleaned, formatter);
		} catch(DateTimeParseException e) {
			//e.printStackTrace();
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
		String current = request.substring(beginIndex, endIndex).replace('_', ' ');
		String formatted = current.equals("/") ? "/Single Review" : current;
		return "Page view: " + formatted;
	}
	
	public boolean isWebsiteRequest() {
		return browserString.toLowerCase().indexOf("googlebot") == -1 &&
				request.indexOf("GET / ") != -1;
	}
	
	public boolean isRequestMatching(String match) {
		System.out.println("request: " + request + ", match: " + match);
		return request.indexOf(match) != -1;
	}

	public String formatRequest() {
		if (isTracking()) {
			if (isRouteView()) {
				return formatRouteView();
			} else if (isYoutubeView()) {
				return formatYoutubeView();
			} else {
				int beginIndex = encoded.indexOf("_");
				int endIndex = encoded.indexOf(" ", beginIndex);
				return decodeUrl(encoded.substring(beginIndex + 1, endIndex)).replace('_', ' ');
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
