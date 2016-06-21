import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class ParameterReader {
	public static String readParameter(String file, String label, String parameter) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			if(in == null) return null;
			String line;
			while((line = in.readLine()) != null)
				if(line.indexOf("["+label+"]") != -1) {
					while((line = in.readLine()) != null)
						if(line.startsWith(parameter+"=") && line.length() > parameter.length() + 1)
							return line.substring(parameter.length() + 1);
					return null;
				}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}