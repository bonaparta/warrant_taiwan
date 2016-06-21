import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.*;
import java.util.Vector;

// Implement the call back class. Just like a SAX content handler
class YahooParser extends ParserCallback {
	public void flush() throws BadLocationException{}
	public void handleComment(char[] data, int pos){}
	public void handleStartTag(Tag tag, MutableAttributeSet a, int pos){
		if(tag.toString().equals("td")) isLoading = true;
	}
	public void handleEndTag(Tag t, int pos){}
	public void handleSimpleTag(Tag t,MutableAttributeSet a, int pos){}
	public void handleError(String errorMsg, int pos){}
	public void handleEndOfLineString(String eol){}
	public void handleText(char[] data, int pos){
		if(isLoading){
			if(recordIndex == 17 || recordIndex == -1){
				recordIndex = 0;
				aRecord = new String[17];
			}
			String strData= "";
			for (char ch : data) strData = strData + ch;
			aRecord[recordIndex++] = strData;
			if(recordIndex == 17)
				add(tv, aRecord);
			else if(strData.equals("零股交易")) {
				recordIndex = -1;
				isLoading = false;
			}
		}
	}
	
	public String[][] getInfoData(){
		return tv.toArray(new String[0][]);
	}

	public void parseData(String source) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF-8"));
			ParserDelegator delegator = new ParserDelegator();
			delegator.parse(reader, this, false);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	synchronized public void add(Vector<String[]> stringArrayVector, String[] records) {
		stringArrayVector.add(records);
	}

	private static Vector<String[]> tv = new Vector<String[]>();
	private boolean isLoading = false;
	private String[] aRecord;
	private int recordIndex = -1;
	private String[][] infoData;

	public static void main(String [] args) throws Exception {
		ParserCallback callback = new YahooParser();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("./temp/stock_sii_paper.htm"), "UTF-8"));
		ParserDelegator delegator = new ParserDelegator();
		delegator.parse(reader, callback, false);
	}
}
