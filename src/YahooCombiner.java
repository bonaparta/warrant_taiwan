import java.io.*;
import java.util.*;

public class YahooCombiner implements Combiner {
	public YahooCombiner() {
		ParameterReader pr = new ParameterReader();
		file = pr.readParameter("parameters.ini", "YahooToday", "file");
		crawlerList = new Vector<Thread>();
		callback = new YahooParser();
	}

	public void addThread(Thread t) {
		crawlerList.add(t);
	}

	public void waitThreads() {
		try{
			for(int i = 0; i < crawlerList.size(); ++i) {
				crawlerList.get(i).join();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void parse(String source_file) {
		TableRetriever easyParser = new TableRetriever();
		easyParser.parseBigTable(source_file, 8);
		callback.parseData(source_file);
	}

	public void combine() {
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			out.write("股票代號\t時間\t成交\t買進\t賣出\t漲跌\t張數\t昨收\t開盤\t最高\t最低\r\n");
			String[][] data = callback.getInfoData();

			for(String[] ss : data) {
				for(String s : ss) {
					out.write(s + "\t");
				}
				out.write("\r\n");
			}
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private Vector<Thread> crawlerList;
	private String file;
	private YahooParser callback;
}
