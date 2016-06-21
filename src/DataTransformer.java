import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

public class DataTransformer {
	private String stockFile = "stock.auto.txt";
	private String warrantFile = "warrant_today.txt";
	private final int MAXIMAL_BET = 60000;
	private final int STOCK_UNIT = 1000;
	private final int BROKER_CONSTRAINT_UNIT = 99000;
	private final int priceRank[] = {5, 10, 50, 100, 500, 1000};
	private final double priceTickStock[] = {0.01, 0.01, 0.05, 0.1, 0.5, 1, 5};
	private final double priceTickWarrant[] = {0.01, 0.05, 0.1, 0.5, 1, 5, 5};

	public double priceCorrectionStock(double a) {
		for(int i = 0; i < priceRank.length; ++i) {
			if(a < priceRank[i])
				return ((int)(a / priceTickStock[i])) * priceTickStock[i];
		}
		return ((int)a / priceTickStock[priceTickStock.length - 1]) * priceTickStock[priceTickStock.length - 1];
	}

	public double priceCorrectionWarrant(double a) {
		for(int i = 0; i < priceRank.length; ++i) {
			if(a < priceRank[i])
				return ((int)(a / priceTickWarrant[i])) * priceTickWarrant[i];
		}
		return ((int)a / priceTickWarrant[priceTickWarrant.length - 1]) * priceTickWarrant[priceTickWarrant.length - 1];
	}

	public static void main(String args[]) {
		int unit;
		double price;

		DecimalFormat df = new DecimalFormat("##.##");
		DataTransformer dt = new DataTransformer();
		try{
			BufferedReader in = new BufferedReader(new FileReader(new File(dt.warrantFile)));
			String line;
			String[] data;
			in.readLine();

			FileWriter out = new FileWriter(new File(dt.stockFile));
			out.write("名稱\t買賣\t價格\t數量\t資券\t整零\t市場\r\n");

			while((line = in.readLine()) != null && line.length() > 2){
				data = line.split("\t");
				unit = (int)((dt.MAXIMAL_BET / Double.parseDouble(data[8])) / dt.STOCK_UNIT) * dt.STOCK_UNIT;
				if(unit > dt.BROKER_CONSTRAINT_UNIT)
					unit = dt.BROKER_CONSTRAINT_UNIT;
				price = Double.parseDouble(df.format(dt.priceCorrectionWarrant(Double.parseDouble(data[8]) + 0.0001)));
				out.write(data[0] + "\tB\t" + price + "\t" + unit + "\t0\tN\tT\r\n");
			}

			in.close();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
