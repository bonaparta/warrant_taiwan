import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class TWSEProcessor {
	private int nOfDays = 1;
	private String address, page;

	public TWSEProcessor() {
		ParameterReader pr = new ParameterReader();
		address = pr.readParameter("parameters.ini", "TWSEYesterday", "address");
		page = pr.readParameter("parameters.ini", "TWSEYesterday", "twseName");
	}

	public void getReport() {
		final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
		Date date = new Date(new Date().getTime() - nOfDays * MILLIS_IN_A_DAY);
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String todayDate = dateFormat.format(date);
		dateFormat = new SimpleDateFormat("yyyyMM");
		String yearMonth = dateFormat.format(date);

		Crawler crawler = new Crawler();
		crawler.process(address.replace("***YearMonth***", yearMonth).replace("***Date***", todayDate), "./temp/" + page, "big5");
	}

	public static void main(String[] args) {
		TWSEProcessor tp = new TWSEProcessor();
		if(args.length > 0) tp.nOfDays = Integer.parseInt(args[0]);
		tp.getReport();
	}
}
