import java.io.*;
import java.net.*;
import java.util.*;

public class EntrustProcessor {
	public EntrustProcessor() {
		ParameterReader pr = new ParameterReader();
		inFile = pr.readParameter("parameters.ini", "WarrantYesterday", "inFileEasy");
		outFile = pr.readParameter("parameters.ini", "WarrantYesterday", "file");
		inLines = new Vector<String[]>();
	}

	public void readFile() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			String s;

			in.readLine();
			while((s = in.readLine()) != null) {
				String[] fields = s.split(",");
				inLines.add(fields);
			}
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void cleanData() {
		cleanEqualsChar(0);
		cleanEqualsChar(2);
		for(int i = 0; i < inLines.size(); ++i)
			for(int j = 0; j < inLines.get(i).length; ++j)
				if(inLines.get(i)[j].equals(""))
					inLines.get(i)[j] = "0";
	}

	private void cleanEqualsChar(int index) {
		for(int i = 0; i < inLines.size(); ++i)
			inLines.get(i)[index] = inLines.get(i)[index].substring(inLines.get(i)[index].indexOf("\"") + 1, inLines.get(i)[index].lastIndexOf("\""));
	}

	public void writeFile() {
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
			out.write("權證代碼\t權證名稱\t權證類別\t權證漲跌幅\t權證價格\t成交量\t標的代碼\t標的股票\t標的收盤價\t最新履約價\t價內外\t最新執行比例\t到期日\t隱含波動率\t溢價比\t槓桿倍數\r\n");

			for(String[] ss : inLines) {
if(ss.length < 12) {
	System.out.println(ss[0] + "\t" + ss.length);
	continue;
}
				out.write(ss[0] + "\t");
				out.write(ss[1] + "\t");
				out.write("\t");
				out.write("0\t");
				out.write(ss[4] + "\t");
				out.write("0\t");
				out.write(ss[2] + "\t");
				out.write(ss[3] + "\t");
				out.write("0\t");
				out.write(ss[7] + "\t");
				out.write("\t");
				out.write(ss[8] + "\t");
				out.write(ss[10] + "\t");
				out.write(ss[12] + "\t");
				out.write("\t");
				out.write(ss[11] + "\r\n");
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Vector<String[]> inLines;
	private String inFile, outFile;

	public static void main(String[] args) {
		EntrustProcessor ep = new EntrustProcessor();
		ep.readFile();
		ep.cleanData();
		ep.writeFile();
	}
}
