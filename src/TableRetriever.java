import java.io.*;
import java.nio.file.*;

public class TableRetriever {
	public void parseTable(String file, int num) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String s;
			String fileString = "";

			for(int t = 0; t <= num && (s = in.readLine()) != null;)
				if(s.indexOf("<table") != -1) {
					if(t == num) {
						fileString += s.substring(s.indexOf("<table"));
						while((s = in.readLine()) != null && s.indexOf("</table>") == -1)
							fileString += s;
						fileString += s.substring(0, s.indexOf("</table>") + "</table>".length());
						break;
					} else
						++t;
				}
			in.close();
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			out.write(fileString);
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	synchronized public void parseBigTable(String file, int num) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String s;

			for(int t = 0; t <= num && (s = in.readLine()) != null;)
				if(s.indexOf("<table") != -1) {
					if(t == num) {
						Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp.txt"), "UTF-8"));
						out.write(s.substring(s.indexOf("<table")));
						while((s = in.readLine()) != null && s.indexOf("</table>") == -1) out.write(s);
						out.write(s.substring(0, s.indexOf("</table>") + "</table>".length()));
						out.close();
						break;
					} else
						++t;
				}
			in.close();

			Path source = Paths.get("tmp.txt");
			Path destination = Paths.get(file);
			Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
