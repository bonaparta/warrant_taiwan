import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class OTCProcessor {
	private int nOfDays = 1;
	private String address, page;

	public OTCProcessor() {
		ParameterReader pr = new ParameterReader();
		address = pr.readParameter("parameters.ini", "OTCYesterday", "address");
		page = pr.readParameter("parameters.ini", "OTCYesterday", "otcName");
	}

	public void getReport() {
		final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
		Date date = new Date(new Date().getTime() - nOfDays * MILLIS_IN_A_DAY);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String yesterdayDate = dateFormat.format(date);

		Crawler crawler = new Crawler();
		yesterdayDate = convertTWDate(yesterdayDate, "yyyy/MM/dd","yyy/MM/dd");
		crawler.process(address.replace("***Date***", yesterdayDate), "./temp/" + page, "big5");
	}

	private String convertTWDate(String AD, String beforeFormat, String afterFormat) {//轉年月格式
		if (AD == null) return "";
		SimpleDateFormat df4 = new SimpleDateFormat(beforeFormat);
		SimpleDateFormat df2 = new SimpleDateFormat(afterFormat);
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(df4.parse(AD));
			if (cal.get(Calendar.YEAR) > 1492)   cal.add(Calendar.YEAR, -1911); 
			else cal.add(Calendar.YEAR, +1911); 
			return df2.format(cal.getTime());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		OTCProcessor tp = new OTCProcessor();
		if(args.length > 0) tp.nOfDays = Integer.parseInt(args[0]);
		tp.getReport();
	}
}
