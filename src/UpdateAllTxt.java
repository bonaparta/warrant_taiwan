import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UpdateAllTxt {
	public UpdateAllTxt() {
		String twseInputFile = ParameterReader.readParameter("parameters.ini","CloseReference","twseName");
		String otcInputFile = ParameterReader.readParameter("parameters.ini","CloseReference","otcName");
		String updateFile = ParameterReader.readParameter("parameters.ini","CloseReference","warrant_update");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("./temp/" + twseInputFile), "UTF-8"));
			String s;
			reader.readLine();reader.readLine();reader.readLine();
			while((s = reader.readLine()) != null && s.length() > 0) {
				String[] data = s.split(",");
				String tempS;
				if(data[0].startsWith("="))
					tempS = data[0].substring(2, data[0].lastIndexOf("\""));
				else
					tempS = data[0];
				if(tempS.length() > 4) {
					Warrant w = new Warrant();
					w.code = tempS;
					w.closePrice = Float.parseFloat(data[3].replace("\"", ""));
					warrant_close.add(w);
				}
			}
			reader.close();

			reader = new BufferedReader(new InputStreamReader(new FileInputStream("./temp/" + otcInputFile), "UTF-8"));
			reader.readLine();reader.readLine();reader.readLine();
			while((s = reader.readLine()) != null && s.length() > 0) {
				String[] data = s.split(",");
				if(data[0].length() > 6) {
					Warrant w = new Warrant();
					w.code = data[0].substring(1, data[0].lastIndexOf("\""));
					w.closePrice = Float.parseFloat(data[14].replace("\"", ""));
					warrant_close.add(w);
				}
			}
			reader.close();

			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp.txt"), "UTF-8"));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(updateFile), "UTF-8"));
			while((s = reader.readLine()) != null && s.length() > 0) {
				int index;
				String[] allData = s.split("\t");
				index = indexOf(warrant_close, allData[0]);
				if(index == -1) {
					out.write(s + "\r\n");
				} else {
					for(int i = 0; i < 6; ++i)
						out.write(allData[i] + "\t");
					out.write(warrant_close.get(index).closePrice + "\t");
					for(int i = 8; i < allData.length; ++i)
						out.write(allData[i] + "\t");
					out.write("\r\n");
				}
			}
			reader.close();
			out.close();

			Path source = Paths.get("tmp.txt");
			Path destination = Paths.get(updateFile);
			Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private int indexOf(Vector<Warrant> all, String warrant) {
		for(int i = 0; i < all.size(); ++i) {
			if(all.get(i).code.equals(warrant))
				return i;
		}
		return -1;
	}

	private Vector<Warrant> warrant_close = new Vector<Warrant>();
	public static void main(String[] args) {
		UpdateAllTxt updater = new UpdateAllTxt();
	}
}
