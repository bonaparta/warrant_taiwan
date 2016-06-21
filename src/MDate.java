import java.util.Calendar;
public class MDate{
	public static int todayFromDate(String date){
		Calendar cal = Calendar.getInstance();
		long da = cal.getTimeInMillis();
		String[] ta = date.split("/");
		cal.set(Integer.parseInt(ta[0]), Integer.parseInt(ta[1]) - 1, Integer.parseInt(ta[2]));
		long db = cal.getTimeInMillis();
		long daterange = db - da;
		long time = 1000*3600*24;
		return (int)(daterange/time);
	}
}
