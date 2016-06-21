import java.io.*;
import java.net.*;

class Crawler {
	void process(String source, String destination, String in_format, String out_format) {
		try {
			URL url = new URL(source);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible;MSIE 5.5; Windows NT 5.0)");
			//^^^^^^^^^^^^^^^^^^^^^一定要設，否則無法執行
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), in_format));
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destination), out_format));

			String tempcontent;
			while((tempcontent = in.readLine()) != null)
				out.write(tempcontent + "\r\n");

			in.close();
			out.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	void process(String source, String destination, String in_format) {
		process(source, destination, in_format, "UTF-8");
	}

	void process(String source, String destination) {
		process(source, destination, "UTF-8");
	}
}
