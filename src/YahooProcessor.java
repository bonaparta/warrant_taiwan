import java.io.*;
import java.net.*;

public class YahooProcessor {
	public YahooProcessor() {
		ParameterReader pr = new ParameterReader();
		String address = pr.readParameter("parameters.ini", "YahooToday", "address");
		String pages = pr.readParameter("parameters.ini", "YahooToday", "name");
		String[] page = pages.split("&");
		System.out.println("page: " + page.length);
		File folder = new File("./temp");
		folder.mkdir();
		combiner = new YahooCombiner();
		Service service = new Service(combiner);
		for(String p : page) { 
			String[] url_htm = p.split("//");
			System.out.println("傳送資料" + p);
			try {
				service.accept(address + url_htm[0], "./temp/" + url_htm[1], "big5");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		combiner.waitThreads();
		for(String p : page) { 
			String[] url_htm = p.split("//");
			combiner.parse("./temp/" + url_htm[1]);
		}
		combiner.combine();
	}

	private YahooCombiner combiner;

	public static void main(String[] args) {
		YahooProcessor yp = new YahooProcessor();
	}
}
