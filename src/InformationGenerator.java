import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Vector;
import java.util.Arrays;
import java.math.BigDecimal;

public class InformationGenerator {
	private String nowFile, yesterdayFile, resultFile, detailFile;
	private String specialItems;
	private Stock stocks[], puts[], calls[];
	private Warrant warrants[];
	private double maxUp, maxDown, wHandleFee, wTradeFee, sHandleFee, sTradeFee, wRealBuy, wRealSell, sRealBuy, sRealSell, shortRate, wRealTrade, sRealTrade, sShortFee;
	private int unit;
	public InformationGenerator() {
		nowFile = ParameterReader.readParameter("parameters.ini","Result","all");
		yesterdayFile = ParameterReader.readParameter("parameters.ini","Result","close");
		resultFile = ParameterReader.readParameter("parameters.ini","Result","warrant");
		detailFile = ParameterReader.readParameter("parameters.ini","Result","detail");
		specialItems = ParameterReader.readParameter("parameters.ini","Others","specialItems");
		maxUp = 1.1;
		maxDown = 0.9;
		wHandleFee = 0.001425;
		wTradeFee = 0.001;
		sHandleFee = 0.001425;
		sTradeFee = 0.003;
		sShortFee = 0.0008;
		wRealBuy = 1 + wHandleFee;					//1.001425
		wRealSell = 1 + wHandleFee + wTradeFee;		//1.002425
		wRealTrade = 1 + 2 * wHandleFee + wTradeFee;//1.00385
		sRealBuy = 1 + sHandleFee;					//1.001425
		sRealSell = 1 + sHandleFee + sTradeFee;		//1.004425
		sRealTrade = 1 + 2 * sHandleFee + sTradeFee;//1.00585
		unit = 1000;
		shortRate = 0.9;
	}
	
	public void combineRealTimeWarrant(int sortType){
		load();
		for(int i = 0; i < warrants.length; ++i) warrants[i].isUpdated = false;
		try{
//			FileWriter out = new FileWriter(new File(resultFile));
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));
			out.write("權證代號\t股票代號\t盤整利得\t最高利得\t最高損失\t履約日利率\t最低價整\t最低價得\t最低價失\t權證價格\t證漲跌\t證交易量\t證最高\t證最低\t股票價格\t股漲跌\t股交易量\t股最高\t股最低\t實際買價\t權證名稱\t股票名稱\t距履約日\t履約價格\t套利買價\t套利實價\t套利賣價\t權淨收入\t總淨收入\t最低履約利\t執行比例\t槓桿使用率\t融券保證金\r\n");

			for(Stock c : calls){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(c, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = warrants[wi].stockPrice;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = stocks[si].bid / stocks[si].prevClose - 1;
					warrants[wi].stockVolume = stocks[si].volume;
					warrants[wi].stockPriceHighest = stocks[si].highest;
					warrants[wi].stockPriceLowest = stocks[si].lowest;
					warrants[wi].closePrice = warrants[wi].price;
					warrants[wi].price = c.bid;
					warrants[wi].change = c.bid / c.prevClose - 1;
					warrants[wi].volume = c.volume;
					warrants[wi].priceHighest = c.highest;
					warrants[wi].priceLowest = c.lowest;
					warrants[wi].buyingPrice = warrants[wi].exePrice + warrants[wi].price / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].buyingPriceLow = warrants[wi].exePrice + warrants[wi].priceLowest / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].cost = (int)(warrants[wi].price * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].costLow = (int)(warrants[wi].priceLowest * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].stockCost = (int)(warrants[wi].stockPrice * unit * (shortRate + sRealSell - 1 + sShortFee));
					warrants[wi].totalCost = warrants[wi].cost + warrants[wi].stockCost;
					warrants[wi].leverage = (double)warrants[wi].stockCost / warrants[wi].cost;
					warrants[wi].tomorrowBest = (double)((warrants[wi].stockClosePrice * maxUp / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorst = (double)((warrants[wi].stockClosePrice * maxDown / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGain = (double)((warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGainLow = (double)((warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].costLow);
					warrants[wi].tomorrowBestLow = (double)((warrants[wi].stockClosePrice * maxUp / (1 + sTradeFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].costLow);
					warrants[wi].tomorrowWorstLow = (double)((warrants[wi].stockClosePrice * maxDown / (1 + sTradeFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].costLow);
					warrants[wi].netIncome = (warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit;
					warrants[wi].netIncomeTotal = warrants[wi].netIncome - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee + sShortFee) * unit;
					warrants[wi].interest = (double)((warrants[wi].stockPrice / (sRealTrade + sTradeFee + sShortFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].totalCost * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].interestLow = (double)((warrants[wi].stockPrice / (sRealTrade + sTradeFee + sShortFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / (warrants[wi].costLow + warrants[wi].stockCost) * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].value = (int)((warrants[wi].stockPrice / sRealBuy - warrants[wi].exePrice * sTradeFee) * unit);//還要加上融券被扣押的錢喔
					warrants[wi].arbitragePrice = (int)((warrants[wi].price / warrants[wi].exchangeScale / wRealSell - warrants[wi].stockPrice * sHandleFee) * unit);//還要加上融券被扣押的錢喔!!!
					warrants[wi].shortDeposit = (int)(warrants[wi].stockPrice * shortRate * unit);
					warrants[wi].isUpdated = true;
				}
			}

			for(Stock p : puts){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(p, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = warrants[wi].stockPrice;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = stocks[si].bid / stocks[si].prevClose - 1;
					warrants[wi].stockVolume = stocks[si].volume;
					warrants[wi].stockPriceHighest = stocks[si].highest;
					warrants[wi].stockPriceLowest = stocks[si].lowest;
					warrants[wi].closePrice = warrants[wi].price;
					warrants[wi].price = p.bid;
					warrants[wi].change = p.bid / p.prevClose - 1;
					warrants[wi].volume = p.volume;
					warrants[wi].priceHighest = p.highest;
					warrants[wi].priceLowest = p.lowest;
					warrants[wi].buyingPrice = warrants[wi].exePrice - warrants[wi].price / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].buyingPriceLow = warrants[wi].exePrice - warrants[wi].priceLowest / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].cost = (int)(warrants[wi].price * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].costLow = (int)(warrants[wi].priceLowest * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].stockCost = (int)(warrants[wi].stockPrice * unit * sRealBuy);
					warrants[wi].totalCost = warrants[wi].cost + warrants[wi].stockCost;
					warrants[wi].leverage = (double)warrants[wi].stockCost / warrants[wi].cost;
					warrants[wi].tomorrowBest = (double)((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxDown * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorst = (double)((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxUp * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGain = (double)((warrants[wi].buyingPrice - warrants[wi].stockPrice * (1 + sHandleFee) - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit / warrants[wi].cost);
					warrants[wi].unchangeGainLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockPrice * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowBestLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockClosePrice * maxDown * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorstLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockClosePrice * maxUp * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].netIncome = (warrants[wi].buyingPrice - warrants[wi].stockPrice - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit;
					warrants[wi].netIncomeTotal = warrants[wi].netIncome - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee) * unit;
					warrants[wi].interest = (double)((warrants[wi].buyingPrice - warrants[wi].stockPrice * (sRealTrade + sHandleFee) - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit / warrants[wi].totalCost * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].interestLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockPrice * (sRealTrade + sHandleFee) - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit / (warrants[wi].costLow + warrants[wi].stockCost) * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].value = (int)((warrants[wi].exePrice / sRealSell - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee)) * unit);
					warrants[wi].arbitragePrice = (int)((warrants[wi].price / warrants[wi].exchangeScale / wRealSell + warrants[wi].stockPrice / sRealSell) * unit);
					warrants[wi].shortDeposit = 0;
					warrants[wi].isUpdated = true;
				}
			}

			Arrays.sort(warrants, new WarrantComparator(sortType));
			for(Warrant w : warrants){
				out.write(w.code+"\t"+w.stockCode+"\t"+w.unchangeGain+"\t"+w.tomorrowBest+"\t"+w.tomorrowWorst+"\t"+w.interest+"\t"+w.unchangeGainLow+"\t"+w.tomorrowBestLow+"\t"+w.tomorrowWorstLow+"\t"+w.price+"\t"+w.change+"\t"+w.volume+"\t"+w.priceHighest+"\t"+w.priceLowest+"\t"+w.stockPrice+"\t"+w.stockChange+"\t"+w.stockVolume+"\t"+w.stockPriceHighest+"\t"+w.stockPriceLowest+"\t"+w.buyingPrice+"\t"+w.name+"\t"+w.stockName+"\t"+w.daysFromExpiration+"\t"+w.exePrice+"\t"+w.totalCost+"\t"+w.value+"\t"+w.arbitragePrice+"\t"+w.netIncome+"\t"+w.netIncomeTotal+"\t"+w.interestLow+"\t"+w.exchangeScale+"\t"+w.leverage+"\t"+w.shortDeposit+"\r\n");
			}
			
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void combineDetailWarrant(int sortType){
		load();
		for(int i = 0; i < warrants.length; ++i) warrants[i].isUpdated = false;
		try{
			FileWriter out = new FileWriter(new File(detailFile));
			out.write("權證代號\t股票代號\t盤整利得\t最高利得\t最高損失\t價差\t一張股權證價\t一張股票價\t總價\t權淨收入\t總淨收入\t權證價格\t漲跌\t交易量\t最高\t最低\t股票價格\t漲跌\t交易量\t最高\t最低\t權證名稱\t股票名稱\t距履約日\t實際買價\t履約價\t執行比例\t槓桿使用率\t履約日\t發行日\t上市日\r\n");

			for(Stock c : calls){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(c, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = warrants[wi].stockPrice;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = stocks[si].bid / stocks[si].prevClose - 1;
					warrants[wi].stockVolume = stocks[si].volume;
					warrants[wi].stockPriceHighest = stocks[si].highest;
					warrants[wi].stockPriceLowest = stocks[si].lowest;
					warrants[wi].exchangeMargin = warrants[wi].exePrice / stocks[si].bid - 1;
					warrants[wi].closePrice = warrants[wi].price;
					warrants[wi].price = c.bid;
					warrants[wi].change = c.bid / c.prevClose - 1;
					warrants[wi].volume = c.volume;
					warrants[wi].priceHighest = c.highest;
					warrants[wi].priceLowest = c.lowest;
					warrants[wi].buyingPrice = warrants[wi].exePrice + warrants[wi].price / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].priceDiff = warrants[wi].buyingPrice / warrants[wi].stockPrice;
					warrants[wi].cost = (int)(warrants[wi].price * unit * wRealBuy / warrants[wi].exchangeScale);
					warrants[wi].stockCost = (int)(warrants[wi].stockPrice * unit * sRealBuy);
					warrants[wi].totalCost = warrants[wi].cost + warrants[wi].stockCost;
					--warrants[wi].daysFromExpiration;
					warrants[wi].leverage = (double)warrants[wi].stockCost / warrants[wi].cost;
					warrants[wi].tomorrowBest = (double)((warrants[wi].stockClosePrice * maxUp * maxUp * maxUp / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorst = (double)((warrants[wi].stockClosePrice * maxDown * maxDown * maxDown / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGain = (double)((warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].netIncome = (warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit;
					warrants[wi].netIncomeTotal = warrants[wi].netIncome - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee) * unit;
					warrants[wi].isUpdated = true;
				}
			}

			for(Stock p : puts){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(p, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = warrants[wi].stockPrice;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = stocks[si].bid / stocks[si].prevClose - 1;
					warrants[wi].stockVolume = stocks[si].volume;
					warrants[wi].stockPriceHighest = stocks[si].highest;
					warrants[wi].stockPriceLowest = stocks[si].lowest;
					warrants[wi].exchangeMargin = warrants[wi].exePrice / stocks[si].bid - 1;
					warrants[wi].closePrice = warrants[wi].price;
					warrants[wi].price = p.bid;
					warrants[wi].change = p.bid / p.prevClose - 1;
					warrants[wi].volume = p.volume;
					warrants[wi].priceHighest = p.highest;
					warrants[wi].priceLowest = p.lowest;
					warrants[wi].buyingPrice = warrants[wi].exePrice - warrants[wi].price / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].priceDiff = warrants[wi].stockPrice / warrants[wi].buyingPrice;
					warrants[wi].cost = (int)(warrants[wi].price * unit * wRealBuy / warrants[wi].exchangeScale);
					warrants[wi].stockCost = (int)(warrants[wi].stockPrice * unit * sRealBuy);
					warrants[wi].totalCost = warrants[wi].cost + warrants[wi].stockCost;
					--warrants[wi].daysFromExpiration;
					warrants[wi].leverage = (double)warrants[wi].stockCost / warrants[wi].cost;
					warrants[wi].tomorrowBest = (double)((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxDown * maxDown * maxDown * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorst = (double)((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxUp * maxUp * maxUp * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGain = (double)((warrants[wi].buyingPrice - warrants[wi].stockPrice * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].netIncome = (warrants[wi].buyingPrice - warrants[wi].stockPrice * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit;
					warrants[wi].netIncomeTotal = warrants[wi].netIncome - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee) * unit;
					warrants[wi].isUpdated = true;
				}
			}

			Arrays.sort(warrants, new WarrantComparator(sortType));
			for(Warrant w : warrants){
				out.write(w.code+"\t"+w.stockCode+"\t"+w.unchangeGain+"\t"+w.tomorrowBest+"\t"+w.tomorrowWorst+"\t"+w.priceDiff+"\t"+w.cost+"\t"+w.stockCost+"\t"+w.totalCost+"\t"+w.netIncome+"\t"+w.netIncomeTotal+"\t"+w.price+"\t"+w.change+"\t"+w.volume+"\t"+w.priceHighest+"\t"+w.priceLowest+"\t"+w.stockPrice+"\t"+w.stockChange+"\t"+w.stockVolume+"\t"+w.stockPriceHighest+"\t"+w.stockPriceLowest+"\t"+w.name+"\t"+w.stockName+"\t"+w.daysFromExpiration+"\t"+w.buyingPrice+"\t"+w.exePrice+"\t"+w.exchangeScale+"\t"+w.leverage+"\t"+w.expirationDate+"\t"+w.publishDate+"\t"+w.marketDate+"\r\n");
			}
			
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void combineFreshWarrantReportAdd50(int sortType) {
		load();
		
		try{
			File f = new File(yesterdayFile + ".bak");
			FileWriter out = new FileWriter(f);
			out.write("權證\t權證\t標的\t標的\t發行\t上市\t到期\t最新\t最新執\t發行\t價內外\t標的\t權證\t權證\t成交量\t隱含\t槓桿\t股票\r\n" +
					"代碼\t名稱\t代碼\t股票\t日期\t日期\t日期\t履約價\t行比例\t數量\t％\t收盤價\t收盤價\t漲跌幅％\t\t波動率\t倍數\t漲跌幅％\r\n");

			for(Stock c : calls){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(c, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = stocks[si].bid;
					warrants[wi].stockPrice = stocks[si].prevClose;
					warrants[wi].stockChange = 100 * (stocks[si].bid / stocks[si].prevClose - 1);
					//warrants[wi].exchangeMargin = warrants[wi].exePrice / stocks[si].bid - 1;
					warrants[wi].closePrice = c.bid;
					warrants[wi].price = c.prevClose;
					warrants[wi].change = 100 * (c.bid / c.prevClose - 1);
					warrants[wi].volume = c.volume;
					warrants[wi].buyingPrice = warrants[wi].exePrice + warrants[wi].price / warrants[wi].exchangeScale;
					warrants[wi].priceDiff = 100 * (1 - warrants[wi].stockClosePrice / warrants[wi].exePrice);
					warrants[wi].totalCost = (int)(warrants[wi].price * 1000 / warrants[wi].exchangeScale);
					warrants[wi].leverage = warrants[wi].stockPrice * 1000 / warrants[wi].totalCost;
					warrants[wi].tomorrowBest = (float)(((warrants[wi].stockClosePrice * maxUp * maxUp * 0.997 - warrants[wi].buyingPrice) * 1000 - warrants[wi].totalCost * 0.001425) / warrants[wi].totalCost * 1.001425);
					warrants[wi].tomorrowWorst = (float)(((warrants[wi].stockClosePrice * maxDown * maxDown * 0.997 - warrants[wi].buyingPrice) * 1000 - warrants[wi].totalCost * 0.001425) / warrants[wi].totalCost * 1.001425);
					warrants[wi].unchangeGain = (float)(((warrants[wi].stockPrice * 0.997 - warrants[wi].buyingPrice) * 1000 - warrants[wi].totalCost * 0.001425) / warrants[wi].totalCost * 1.001425);
					warrants[wi].isUpdated = true;
				}
			}

			for(Stock p : puts){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(p, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = warrants[wi].stockPrice;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = 100 * (stocks[si].bid / stocks[si].prevClose - 1);
					warrants[wi].exchangeMargin = warrants[wi].exePrice / stocks[si].bid - 1;
					warrants[wi].closePrice = warrants[wi].price;
					warrants[wi].price = p.bid;
					warrants[wi].change = 100 * (p.bid / p.prevClose - 1);
					warrants[wi].volume = p.volume;
					warrants[wi].buyingPrice = warrants[wi].exePrice - warrants[wi].price / warrants[wi].exchangeScale;
					warrants[wi].priceDiff = 100 * (1 - warrants[wi].exePrice / warrants[wi].stockClosePrice);
					warrants[wi].totalCost = (int)(warrants[wi].price * 1000 / warrants[wi].exchangeScale);
					warrants[wi].leverage = warrants[wi].stockPrice * 1000 / warrants[wi].totalCost;
					warrants[wi].tomorrowBest = (float)(((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxDown * maxDown * 1.001425 - warrants[wi].exePrice * 0.003) * 1000 - warrants[wi].totalCost * 0.001425) / warrants[wi].totalCost * 1.001425);
					warrants[wi].tomorrowWorst = (float)(((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxUp * maxUp * 1.001425 - warrants[wi].exePrice * 0.003) * 1000 - warrants[wi].totalCost * 0.001425) / warrants[wi].totalCost * 1.001425);
					warrants[wi].unchangeGain = (float)(((warrants[wi].buyingPrice - warrants[wi].stockPrice * 1.001425 - warrants[wi].exePrice * 0.003) * 1000 - warrants[wi].totalCost * 0.001425) / warrants[wi].totalCost * 1.001425);
					warrants[wi].isUpdated = true;
				}
			}

			Arrays.sort(warrants, new WarrantComparator(sortType));
			for(Warrant w : warrants)
				out.write(w.code+"\t"+w.name+"\t"+w.stockCode+"\t"+w.stockName+"\t"+w.publishDate+"\t"+w.marketDate+"\t"+w.expirationDate+"\t"+w.exePrice+"\t"+w.exchangeScale+"\t"+w.publishQuantity+"\t"+w.priceDiff+"\t"+w.stockClosePrice+"\t"+w.closePrice+"\t"+w.change+"\t"+w.volume+"\t"+w.impliedVolatility+"\t"+w.leverage+"\t"+w.stockChange+"\r\n");
			
			out.close();
			File yf = new File(yesterdayFile);
			if(yf.exists()) yf.delete();
			f.renameTo(yf);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void combineFreshWarrantReport(int sortType) {//_dailyResult
		load();
		try{
			File f = new File(yesterdayFile + ".bak");
			FileWriter out = new FileWriter(f);
			out.write("權證\t權證\t權證\t權證\t權證\t成交量\t標的\t標的\t標的\t最新\t價內外\t最新\t到期\t隱含\t溢價比\t槓桿\r\n" +
					"代碼\t名稱\t類別\t漲跌幅\t價格\t逆向排序順向排序\t代碼\t股票\t收盤價\t履約價\t逆向排序順向排序\t執行\t日期\t波動率\t逆向排序順向排序\t倍數\r\n" +
					"逆向排序順向排序\t逆向排序順向排序\t逆向排序順向排序\t逆向排序順向排序\t逆向排序順向排序\t\t逆向排序順向排序\t逆向排序順向排序\t逆向排序順向排序\t逆向排序順向排序\t\t比例\t逆向排序順向排序\t逆向排序順向排序\t\t逆向排序順向排序\r\n" +
					"\t\t\t\t\t\t\t\t\t\t\t逆向排序順向排序\r\n\r\n");

			for(Warrant w : warrants) {
				int wi, si = findStockIndexInStocks(w.stockCode, stocks);
				if(w.code.endsWith("P")) {
					wi = findStockIndexInStocks(w.code, puts);
					if(si == -1 || wi == -1) continue;
					w.stockClosePrice = stocks[si].bid;
					w.closePrice = puts[wi].bid;
					w.priceDiff = 100 * (1 - w.exePrice / w.stockClosePrice);
					w.change = 100 * (puts[wi].bid / puts[wi].prevClose - 1);
					w.volume = puts[wi].volume;
					w.leverage = w.stockClosePrice * 1000 / w.closePrice * w.exchangeScale;
					w.isUpdated = true;
				} else {
					wi = findStockIndexInStocks(w.code, calls);
					if(si == -1 || wi == -1) continue;
					w.stockClosePrice = stocks[si].bid;
					w.closePrice = calls[wi].bid;
					w.priceDiff = 100 * (1 - w.stockClosePrice / w.exePrice);
					w.change = 100 * (calls[wi].bid / calls[wi].prevClose - 1);
					w.volume = calls[wi].volume;
					w.leverage = w.stockClosePrice * 1000 / w.closePrice * w.exchangeScale;
					w.isUpdated = true;
				}
			}
			
			Arrays.sort(warrants, new WarrantComparator(sortType));
			for(Warrant w : warrants)
				out.write(w.code+"\t"+w.name+"\t"+((w.code.endsWith("P")? "賣":"買"))+"\t"+w.change+"\t"+w.closePrice+"\t"+w.volume+"\t"+w.stockCode+"\t"+w.stockName+"\t"+w.stockClosePrice+"\t"+w.exePrice+"\t"+w.priceDiff+"\t"+w.exchangeScale+"\t"+w.expirationDate+"\t"+w.impliedVolatility+"\t"+w.stockChange+"\t"+w.leverage+"\r\n");
			
			out.close();
			File yf = new File(yesterdayFile);
			if(yf.exists()) yf.delete();
			f.renameTo(yf);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void combineFreshWarrantReport_warrantReport(int sortType) {
		load();
		try{
			File f = new File(yesterdayFile + ".bak");
			FileWriter out = new FileWriter(f);
			out.write("權證\t權證\t標的\t標的\t發行\t上市\t到期\t最新\t最新執\t發行\t價內外\t標的\t權證\t權證\t成交量\t隱含\t槓桿\r\n" +
					"代碼\t名稱\t代碼\t股票\t日期\t日期\t日期\t履約價\t行比例\t數量\t％\t收盤價\t收盤價\t漲跌幅\t\t波動率\t倍數\r\n" +
					"\t\t\t\t\t\t\t\t\t\t\t\t\t％\t\t\t\r\n");

			for(Warrant w : warrants) {
				int wi, si = findStockIndexInStocks(w.stockCode, stocks);
				if(w.code.endsWith("P")) {
					wi = findStockIndexInStocks(w.code, puts);
					if(si == -1 || wi == -1) continue;
					w.stockClosePrice = stocks[si].bid;
					w.closePrice = puts[wi].bid;
					w.priceDiff = 100 * (1 - w.exePrice / w.stockClosePrice);
					w.change = 100 * (puts[wi].bid / puts[wi].prevClose - 1);
					w.volume = puts[wi].volume;
					w.leverage = w.stockClosePrice * 1000 / w.closePrice * w.exchangeScale;
					w.isUpdated = true;
				} else {
					wi = findStockIndexInStocks(w.code, calls);
					if(si == -1 || wi == -1) continue;
					w.stockClosePrice = stocks[si].bid;
					w.closePrice = calls[wi].bid;
					w.priceDiff = 100 * (1 - w.stockClosePrice / w.exePrice);
					w.change = 100 * (calls[wi].bid / calls[wi].prevClose - 1);
					w.volume = calls[wi].volume;
					w.leverage = w.stockClosePrice * 1000 / w.closePrice * w.exchangeScale;
					w.isUpdated = true;
				}
			}
			
			Arrays.sort(warrants, new WarrantComparator(sortType));
			for(Warrant w : warrants)
				out.write(w.code+"\t"+w.name+"\t"+w.stockCode+"\t"+w.stockName+"\t"+w.publishDate+"\t"+w.marketDate+"\t"+w.expirationDate+"\t"+w.exePrice+"\t"+w.exchangeScale+"\t"+w.publishQuantity+"\t"+w.priceDiff+"\t"+w.stockClosePrice+"\t"+w.closePrice+"\t"+w.change+"\t"+w.volume+"\t"+w.impliedVolatility+"\t"+w.leverage+"\r\n");
			
			out.close();
			File yf = new File(yesterdayFile);
			if(yf.exists()) yf.delete();
			f.renameTo(yf);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void combineRealTimeWarrantWithReferencePrice(int sortType, int referenceGain, int daysFromExpire){
		load();
		for(int i = 0; i < warrants.length; ++i) warrants[i].isUpdated = false;
		try{
//			FileWriter out = new FileWriter(new File(resultFile));
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));
			out.write("權證代號\t距履約日\t股票代號\t股票名稱\t盤整利得\t最高利得\t最高損失\t履約日利率\t權證價格\t證交易量\t證最低\t跌停價\t" + referenceGain +
			"%參考\t執行比例\t槓桿使用率\t最低價整\t最低價得\t最低價失\t證漲跌\t證最高\t股票價格\t股漲跌\t股交易量\t股最高\t股最低\t實際買價\t權證名稱\t履約價格\t套利買價\t套利實價\t套利賣價\t權淨收入\t總淨收入\t最低履約利\t融券保證金\r\n");
			for(Stock c : calls){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(c, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = stocks[si].prevClose;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = stocks[si].bid / stocks[si].prevClose - 1;
					warrants[wi].stockVolume = stocks[si].volume;
					warrants[wi].stockPriceHighest = stocks[si].highest;
					warrants[wi].stockPriceLowest = stocks[si].lowest;
					warrants[wi].closePrice = c.prevClose;
					warrants[wi].price = c.bid;
					warrants[wi].change = c.bid / c.prevClose - 1;
					warrants[wi].volume = c.volume;
					warrants[wi].priceHighest = c.highest;
					warrants[wi].priceLowest = c.lowest;
					warrants[wi].priceHighestLimit = warrants[wi].closePrice + warrants[wi].stockClosePrice * (maxUp - 1) * warrants[wi].exchangeScale;
					warrants[wi].priceLowestLimit = new BigDecimal(warrants[wi].closePrice - warrants[wi].stockClosePrice * (1 - maxDown) * warrants[wi].exchangeScale).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
					warrants[wi].buyingPrice = warrants[wi].exePrice + warrants[wi].price / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].buyingPriceLow = warrants[wi].exePrice + warrants[wi].priceLowest / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].cost = (int)(warrants[wi].price * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].costLow = (int)(warrants[wi].priceLowest * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].stockCost = (int)(warrants[wi].stockPrice * unit * (shortRate + sRealSell - 1 + sShortFee));
					warrants[wi].totalCost = warrants[wi].cost + warrants[wi].stockCost;
					warrants[wi].leverage = (double)warrants[wi].stockCost / warrants[wi].cost;
					warrants[wi].tomorrowBest = (double)((warrants[wi].stockClosePrice * maxUp / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorst = (double)((warrants[wi].stockClosePrice * maxDown / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGain = (double)((warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGainLow = (double)((warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].costLow);
					warrants[wi].tomorrowBestLow = (double)((warrants[wi].stockClosePrice * maxUp / (1 + sTradeFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].costLow);
					warrants[wi].tomorrowWorstLow = (double)((warrants[wi].stockClosePrice * maxDown / (1 + sTradeFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].costLow);
					warrants[wi].netIncome = (warrants[wi].stockPrice / (1 + sTradeFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit;
					warrants[wi].netIncomeTotal = warrants[wi].netIncome - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee + sShortFee) * unit;
					warrants[wi].interest = (double)((warrants[wi].stockPrice / (sRealTrade + sTradeFee + sShortFee) - warrants[wi].buyingPrice - warrants[wi].exePrice * sHandleFee) * unit / warrants[wi].totalCost * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].interestLow = (double)((warrants[wi].stockPrice / (sRealTrade + sTradeFee + sShortFee) - warrants[wi].buyingPriceLow - warrants[wi].exePrice * sHandleFee) * unit / (warrants[wi].costLow + warrants[wi].stockCost) * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].buyingPrice = warrants[wi].exePrice + warrants[wi].price / warrants[wi].exchangeScale * wRealBuy + warrants[wi].stockPrice * (sHandleFee + sTradeFee) + warrants[wi].exePrice * sHandleFee;//修正版本
					warrants[wi].value = (int)((warrants[wi].stockPrice / sRealBuy - warrants[wi].exePrice * sTradeFee) * unit);//還要加上融券被扣押的錢喔
					warrants[wi].arbitragePrice = (int)((warrants[wi].price / warrants[wi].exchangeScale / wRealSell - warrants[wi].stockPrice * sHandleFee) * unit);//還要加上融券被扣押的錢喔!!!
					warrants[wi].shortDeposit = (int)(warrants[wi].stockPrice * shortRate * unit);
					warrants[wi].referencePrice = (double)(warrants[wi].price * (1 + warrants[wi].unchangeGain) / (1 + referenceGain / 100.0));
					warrants[wi].isUpdated = true;
				}
			}

			for(Stock p : puts){
				int wi, si = -1;
				wi = findStockCodeIndexInWarrant(p, warrants);
				if(wi != -1) si = findStockIndexInStocks(warrants[wi].stockCode, stocks);
				if(wi != -1 && si != -1){
					warrants[wi].stockClosePrice = stocks[si].prevClose;
					warrants[wi].stockPrice = stocks[si].bid;
					warrants[wi].stockChange = stocks[si].bid / stocks[si].prevClose - 1;
					warrants[wi].stockVolume = stocks[si].volume;
					warrants[wi].stockPriceHighest = stocks[si].highest;
					warrants[wi].stockPriceLowest = stocks[si].lowest;
					warrants[wi].closePrice = p.prevClose;
					warrants[wi].price = p.bid;
					warrants[wi].change = p.bid / p.prevClose - 1;
					warrants[wi].volume = p.volume;
					warrants[wi].priceHighest = p.highest;
					warrants[wi].priceLowest = p.lowest;
					warrants[wi].priceLowestLimit = new BigDecimal(warrants[wi].closePrice - warrants[wi].stockClosePrice * (maxUp - 1) * warrants[wi].exchangeScale).setScale(2, BigDecimal.ROUND_CEILING).doubleValue();
					warrants[wi].buyingPrice = warrants[wi].exePrice - warrants[wi].price / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].buyingPriceLow = warrants[wi].exePrice - warrants[wi].priceLowest / warrants[wi].exchangeScale * wRealBuy;
					warrants[wi].cost = (int)(warrants[wi].price * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].costLow = (int)(warrants[wi].priceLowest * unit / warrants[wi].exchangeScale * wRealBuy);
					warrants[wi].stockCost = (int)(warrants[wi].stockPrice * unit * sRealBuy);
					warrants[wi].totalCost = warrants[wi].cost + warrants[wi].stockCost;
					warrants[wi].leverage = (double)warrants[wi].stockCost / warrants[wi].cost;
					warrants[wi].tomorrowBest = (double)((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxDown * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorst = (double)((warrants[wi].buyingPrice - warrants[wi].stockClosePrice * maxUp * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].unchangeGain = (double)((warrants[wi].buyingPrice - warrants[wi].stockPrice * (1 + sHandleFee) - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit / warrants[wi].cost);
					warrants[wi].unchangeGainLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockPrice * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowBestLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockClosePrice * maxDown * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].tomorrowWorstLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockClosePrice * maxUp * (1 + sHandleFee) - warrants[wi].exePrice * sTradeFee) * unit / warrants[wi].cost);
					warrants[wi].netIncome = (warrants[wi].buyingPrice - warrants[wi].stockPrice - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit;
					warrants[wi].netIncomeTotal = warrants[wi].netIncome - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee) * unit;
					warrants[wi].interest = (double)((warrants[wi].buyingPrice - warrants[wi].stockPrice * (sRealTrade + sHandleFee) - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit / warrants[wi].totalCost * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].interestLow = (double)((warrants[wi].buyingPriceLow - warrants[wi].stockPrice * (sRealTrade + sHandleFee) - warrants[wi].exePrice * (sHandleFee + sTradeFee)) * unit / (warrants[wi].costLow + warrants[wi].stockCost) * 365 / warrants[wi].daysFromExpiration);
					warrants[wi].buyingPrice = warrants[wi].exePrice - warrants[wi].price / warrants[wi].exchangeScale * wRealBuy - warrants[wi].stockPrice * sHandleFee - warrants[wi].exePrice * (sHandleFee + sTradeFee);//修正版本
					warrants[wi].value = (int)((warrants[wi].exePrice / sRealSell - warrants[wi].stockPrice * (2 * sHandleFee + sTradeFee)) * unit);
					warrants[wi].arbitragePrice = (int)((warrants[wi].price / warrants[wi].exchangeScale / wRealSell + warrants[wi].stockPrice / sRealSell) * unit);
					warrants[wi].shortDeposit = 0;
					warrants[wi].referencePrice = (double)(warrants[wi].price * (1 + warrants[wi].unchangeGain) / (1 + referenceGain / 100.0));
					warrants[wi].isUpdated = true;
				}
			}

			Arrays.sort(warrants, new WarrantComparator(sortType));
			for(Warrant w : warrants)
				if(daysFromExpire < 1 || w.daysFromExpiration <= daysFromExpire)
					out.write(w.code+"\t"+w.daysFromExpiration+"\t"+w.stockCode+"\t"+w.stockName+"\t"+w.unchangeGain+"\t"+w.tomorrowBest+"\t"+w.tomorrowWorst+"\t"+w.interest+"\t"+w.price+"\t"+w.volume+"\t"+w.priceLowest+"\t"+w.priceLowestLimit+"\t"+w.referencePrice+"\t"+w.exchangeScale+"\t"+w.leverage+"\t"+w.unchangeGainLow+"\t"+w.tomorrowBestLow+"\t"+w.tomorrowWorstLow+"\t"+w.change+"\t"+w.priceHighest+"\t"+w.stockPrice+"\t"+w.stockChange+"\t"+w.stockVolume+"\t"+w.stockPriceHighest+"\t"+w.stockPriceLowest+"\t"+w.buyingPrice+"\t"+w.name+"\t"+w.exePrice+"\t"+w.totalCost+"\t"+w.value+"\t"+w.arbitragePrice+"\t"+w.netIncome+"\t"+w.netIncomeTotal+"\t"+w.interestLow+"\t"+w.shortDeposit+"\r\n");
			
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private int findStockIndexInStocks(String s, Stock w[]){
		for(int i = 0; i < w.length; ++i){
			if(w[i].code.equals(s))
				return i;
		}
		return -1;
	}

	private int findStockCodeIndexInWarrant(Stock s, Warrant w[]){
		for(int i = 0; i < w.length; ++i)
			if(w[i].code.equals(s.code))
				return i;
		return -1;
	}
	
	private void load(){
		loadTodayStocksAndWarrants();
		loadYesterdayWarrants();
	}
	
	private void loadTodayStocksAndWarrants(){
		Vector<Stock> s = new Vector<Stock>();
		Vector<Stock> c = new Vector<Stock>();
		Vector<Stock> p = new Vector<Stock>();
		
		try{
//			BufferedReader in = new BufferedReader(new FileReader(new File(nowFile)));
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(nowFile), "UTF-8"));
			String line;
			String[] data, codeName;
			in.readLine();
			while((line = in.readLine()) != null && line.length() > 2){
				data = line.split("\t");
				Stock a = new Stock();
				codeName = data[0].split(" ");
				a.name = codeName[1];
				a.code = codeName[0];
				a.time = data[1];
				if(!data[2].equals("－")) a.bid = Float.parseFloat(killDot(data[2])); else a.bid = 0;
				if(!data[3].equals("－")) a.buy = Float.parseFloat(killDot(data[3])); else a.buy = 0;
				if(!data[4].equals("－")) a.sell = Float.parseFloat(killDot(data[4])); else a.sell = 0;
				//a.diff = Float.parseFloat(data[5]);
				if(!data[6].equals("－")) a.volume = Integer.parseInt(killDot(data[6])); else a.volume = 0;
				if(!data[7].equals("－")) a.prevClose = Float.parseFloat(killDot(data[7])); else a.prevClose = 0;
				if(a.bid == 0 && a.prevClose != 0) a.bid = a.prevClose;
				a.diff = a.bid - a.prevClose;
				if(!data[8].equals("－")) a.open = Float.parseFloat(killDot(data[8])); else a.open = 0;
				if(!data[9].equals("－")) a.highest = Float.parseFloat(killDot(data[9])); else a.highest = 0;
				if(!data[10].equals("－")) a.lowest = Float.parseFloat(killDot(data[10])); else a.lowest = 0;
				if(a.code.length() < 5) s.add(a);
				else if(a.code.length() == 6 && a.code.startsWith("9")) s.add(a);
				else if(a.code.startsWith("TW")) s.add(a);
				else if(a.code.endsWith("P")) p.add(a);
				else c.add(a);
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		stocks = s.toArray(new Stock[0]);
		calls = c.toArray(new Stock[0]);
		puts = p.toArray(new Stock[0]);
	}

	private void loadYesterdayWarrants_warrantReport(){
		Vector<Warrant> w = new Vector<Warrant>();
		
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(yesterdayFile), "UTF-8"));
			String line;
			String[] data;

			while((line = in.readLine()) != null && line.length() > 2) {
				data = line.split("\t");
				Warrant b = new Warrant();
				b.code = data[0];
				while(b.code.length() < 5) b.code = "0" + b.code;
				b.name = data[1];
				b.stockCode = data[2];
				while(b.stockCode.length() < 4) b.stockCode = "0" + b.stockCode;
				b.stockName = data[3];
				b.publishDate = data[4];
				b.marketDate = data[5];
				b.expirationDate = data[6];
				b.exePrice = Float.parseFloat(killDot(data[7]));
				b.exchangeScale = Float.parseFloat(data[8]);
				b.publishQuantity = Integer.parseInt(data[9]);
				b.stockPrice = Float.parseFloat(killDot(data[11]));
				//b.stockChange = Float.parseFloat(data[7].substring(0, data[7].length() - 1));
				b.exchangeMargin = Float.parseFloat(data[10]);
				b.price = Float.parseFloat(data[12]);
				if(data[13].equals("N/A")) b.change = 0; else b.change = Float.parseFloat(killDot(data[13]));
				//b.theoreticalPrice = Float.parseFloat(killDot(data[11]));
				//b.theoreticalDiff = Float.parseFloat(data[12].substring(0, data[12].length() - 1));
				b.leverage = Float.parseFloat(killDot(data[16]));
				b.volume = Integer.parseInt(killDot(data[14]));
				if(data[15].equals("N/A")) b.impliedVolatility = 0; else b.impliedVolatility = Float.parseFloat(killDot(data[15]));
				b.daysFromExpiration = MDate.todayFromDate(b.expirationDate);
				b.isCall = !b.code.endsWith("P");
				w.add(b);
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		warrants = w.toArray(new Warrant[0]);
	}

	private void loadYesterdayWarrants(){//_dailyResult(){
		Vector<Warrant> w = new Vector<Warrant>();
		Vector<String> fBITetc = new Vector<String>();
		String[] items = specialItems.split("&");
		
		for(int i = 0; i < items.length; ++i)
			fBITetc.add(items[i]);
		
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(yesterdayFile), "UTF-8"));
			String line;
			String[] data;
			
			in.readLine();in.readLine();in.readLine();in.readLine();in.readLine();
			while((line = in.readLine()) != null && line.length() > 2){
				data = line.split("\t");
				Warrant b = new Warrant();
				b.code = data[0];
				while(b.code.length() < 6) b.code = "0" + b.code;
				b.name = data[1];
				b.stockCode = data[6];
				while(b.stockCode.length() < 4) b.stockCode = "0" + b.stockCode;
				b.stockName = data[7];
				if(fBITetc.indexOf(b.stockName) != -1)
					while(b.stockCode.length() < 6) b.stockCode = "0" + b.stockCode;
				b.publishDate = data[12];
				b.marketDate = data[12];
				b.expirationDate = data[12];
				b.exePrice = Float.parseFloat(killDot(data[9]));
				b.exchangeScale = Float.parseFloat(data[11]);
				b.publishQuantity = Integer.parseInt(killDot(data[5]));
				b.stockPrice = Float.parseFloat(killDot(data[8]));
				//b.stockChange = Float.parseFloat(data[7].substring(0, data[7].length() - 1));
				//b.exchangeMargin = Float.parseFloat(data[14]);
				b.price = Float.parseFloat(data[4]);
				if(data[3].equals("N/A")) b.change = 0; else b.change = Float.parseFloat(killDot(data[3]));
				//b.theoreticalPrice = Float.parseFloat(killDot(data[11]));
				//b.theoreticalDiff = Float.parseFloat(data[12].substring(0, data[12].length() - 1));
				b.leverage = Float.parseFloat(killDot(data[15]));
				b.volume = Integer.parseInt(killDot(data[5]));
				if(data[13].equals("N/A") || data[13].equals("") || data[13].equals("null")) b.impliedVolatility = 0; else b.impliedVolatility = Float.parseFloat(killDot(data[13]));
				if(data[14].equals("N/A") || data[14].equals("") || data[13].equals("null")) b.exchangeMargin = 0; else b.exchangeMargin = Float.parseFloat(killDot(data[14]));
				b.daysFromExpiration = Integer.parseInt(b.expirationDate);
				if(data[2].equals("買")) b.isCall = true; else b.isCall = false;
				w.add(b);
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		warrants = w.toArray(new Warrant[0]);
	}

	private String killDot(String s){
		return s.replaceAll(",|\"","");
	}

	public static void main(String[] args){
		int resultType = 0, sortType = 0, daysFromExpire = 0, referenceGain = 0;
		if(args.length > 0) resultType = Integer.parseInt(args[0]);
		if(args.length > 1) sortType = Integer.parseInt(args[1]);
		if(args.length > 2) referenceGain = Integer.parseInt(args[2]);
		if(args.length > 3) daysFromExpire = Integer.parseInt(args[3]);
		if(args.length > 5) System.out.println("java Combiner [function] [sort type] [reference gain] [days from expired]\n" +
												"function=0 real time rough\nf=1 real time detail\n" +
												"function=2 update warrant_today by real time data\n" +
												"function=3 real time rough with reference price\n" +
												"function=other update warrant_today by warrant_today\n" +
												"type=0 no sort\ntype=1 call=>put by 價內外\ntype=2 only by 價內外");
		
		InformationGenerator c = new InformationGenerator();
		switch(resultType) {
			case 0:			//更新即時報價獲利
				c.combineRealTimeWarrant(sortType);
				break;
			case 1:			//更新收盤詳細即時報價獲利
				c.combineDetailWarrant(sortType);
				break;
			case 2:			//更新收盤資料加台灣50
				c.combineFreshWarrantReportAdd50(sortType);
				break;
			case 3:
				c.combineRealTimeWarrantWithReferencePrice(sortType, referenceGain, daysFromExpire);
				break;
			default:		//自行更新收盤資料
				c.combineFreshWarrantReport(sortType);
		}
	}
}
