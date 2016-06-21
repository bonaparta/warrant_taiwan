import java.math.BigDecimal;

public class Stock{
	public String name;					//股票名稱
	public String code;					//股票代號
	public String time;					//時間
	public double bid;					//成交價
	public double buy;					//買進價
	public double sell;					//賣出價
	public double diff;					//漲跌幅
	public int volume;					//成交量
	public int prevVolume;				//昨量
	public double open;					//開盤
	public double prevClose;			//昨收
	public double highest;				//今日最高價
	public double lowest;				//今日最低價
	public double highestLimit;			//漲停價
	public double lowestLimit;			//跌停價

	public static double getRealPriceCeiling(double d) {
		if(d >= 1000) { // 1
			return new BigDecimal(d).setScale(0, BigDecimal.ROUND_CEILING).doubleValue();
		} else if(d >= 500) { // 0.5
			return new BigDecimal(d).setScale(1, BigDecimal.ROUND_CEILING).doubleValue();
		} else if(d >= 500) { // 0.1
			return new BigDecimal(d).setScale(1, BigDecimal.ROUND_CEILING).doubleValue();
		} else if(d >= 500) { // 0.05
			return new BigDecimal(d).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
		} else { // 0.01
			return new BigDecimal(d).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
		}
	}

	public static double getRealPriceFloor(double d) {
		if(d >= 1000) { // 1
			return new BigDecimal(d).setScale(0, BigDecimal.ROUND_FLOOR).doubleValue();
		} else if(d >= 500) { // 0.5
			return new BigDecimal(d).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
		} else if(d >= 500) { // 0.1
			return new BigDecimal(d).setScale(1, BigDecimal.ROUND_FLOOR).doubleValue();
		} else if(d >= 500) { // 0.05
			return new BigDecimal(d).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
		} else { // 0.01
			return new BigDecimal(d).setScale(2, BigDecimal.ROUND_FLOOR).doubleValue();
		}
	}
}