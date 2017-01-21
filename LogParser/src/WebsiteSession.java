import java.util.List;

public class WebsiteSession extends Session {
	String ip;

	public WebsiteSession(String ip, List<Request> requests) {
		super("", requests);

		this.ip = ip;
		this.requests = requests;
	}
	
	public boolean contains(String match) {
		boolean result = requests.stream().anyMatch(request -> request.isRequestMatching(match));
		System.out.println("match: " + match + ", result: " + result);
		return result;
	}

	public boolean isValid() {
		if (requests.size() < 5) {
			return false;
		}

		// it should at least have a index, 2 css and the front cover image
		return contains("GET /css/normalize-") &&
				contains("GET /css/style-") &&
				contains("GET / ") &&
				contains("GET /images/Songs-for-the-Spirit-album-front-cover-with-text-");
	}
	
	public void print() {
		System.out.println("ip: " + ip);
		System.out.println("requests: " + requests.size());
		
		requests.forEach(System.out::println);
	}
	
}
