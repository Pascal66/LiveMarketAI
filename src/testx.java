import java.net.*;
import java.io.*;

public class testx {

	public static void main(String[] args) throws Exception {
		
		Stock("AAPL");

	}
	
	public static void Stock(String ticker) throws Exception {
		String base = "https://marketdata.websol.barchart.com/getQuote.json?";
		String key = "apikey=xxxxxxxxxxxxxxxxxsymbols=";
		String url = base + key + ticker;
		
		URL x = new URL(url);
		URLConnection y = x.openConnection();
		BufferedReader z = new BufferedReader(new InputStreamReader(y.getInputStream()));
		String m = z.readLine();
		
		m = m.replace("\"","");
		String[] n = m.split("lastPrice:");
		String[] o = n[1].split(",");
		
		m = o[0];
		
		System.out.println(m);
	}

}
