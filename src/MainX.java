// This file contains your main backend system used for this program

import java.net.*;
import java.text.*;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MainX {
	
	public static DecimalFormat z = new DecimalFormat("0.000%");
	public static DecimalFormat yx = new DecimalFormat("0.00%");
	public static DecimalFormat mx = new DecimalFormat("0.000000");
	
	// The main component of your back-end system
	
	public static void Backend(JTextField time, JLabel timeBox, JTable corr, JTable opT, JTable risk, JLabel qfin) throws Exception{
		
		System.out.println("RUNNING!\n");
		
		// The start of the backend
		
		String[] tick = {"SPY","GS","JPM","GLD","OIL","SJNK","GSG","XLK","XLE","XLV","XLY","XLI","IBB","IYR","XLB"};
		
		String[] names = {"S&P500","GoldmanSachs","JPMorgan","Gold","Oil","HYBond","Comdts.","Tech","Energy","Healthcare","Discretionary","Staples","BioTech","RealEstate","Materials","Bitcoin","Ethereum","Litecoin"};
		
		int m = tick.length;
		int n = names.length;
		int dx = 10;
		int runs = 1;
		int xz = 0;
		
		double[] regPrice = new double[n];
		double[] previous = new double[n];
		double[] delta = new double[n];
		double[][] storage = new double[n][dx];
		double[][] stats = new double[n][n];
		double[][] covariance = new double[n][n];
		double[][] correlation = new double[n][n];
		double[] sharpe = new double[n];
		double[] weight = new double[n];
		double[] VaR = new double[n];
		double[] beta = new double[n];
		
		double[][] printCorr = new double[n][n];
		
		String data = "";

		
		while(true){
			
			// Fetches data from your first API
			
			for(int i = 0; i < tick.length; i++){
				regPrice[i] = Double.parseDouble(WebData(tick[i],0));
			}
			
			// Fetches data from your second API
			
			data = WebData("",1);
			
			double[] temp = ParseCrypto(data);
			
			regPrice[m] = temp[0];
			regPrice[m+1] = temp[1];
			regPrice[m+2] = temp[2];
			
			// Starts to analyze data
			
			if(runs > 1){
				
				if(xz == dx){  // Makes sure memory array doesnt go out of the loop
					xz = 0;
				}
				
				delta = Compute.Delta(regPrice, previous);
				storage = Compute.StoreData(storage, delta, xz);
				stats = Compute.RR(storage, dx);
				covariance = Compute.Covariance(storage, stats, dx);
				correlation = Compute.Correlation(covariance, stats);
				sharpe = Compute.Sharpe(stats);
				weight = Compute.NeuralNet(sharpe);
				VaR = Compute.VaR(storage);
				beta = Compute.Beta(storage, stats, dx);
				
				printCorr = Compute.FixCorr(correlation);
				
				WriteCorr(corr, names, printCorr);  // Displays your correlation matrix
				WriteOp(opT, names, weight);          // Displays your optimizer
				WriteRisk(risk, names, stats, VaR, beta); // Displays your risk metrics

				
				String ishthiaq = Compute.MainPage(correlation, stats, VaR, weight, names);
				
				qfin.setText(ishthiaq);
				
				xz += 1;
			}
			
			
			previous = Compute.prePrice(regPrice);
			
			System.out.println("Run: " + runs);
			runs += 1;
			Timer(time,timeBox);  // Calls timer function
		}	
	}
	
	
	// This method will parse your crypto currency API
	// "BTC_LTC":{"id":50,"last":"0.01642197","lowestAsk":"0.01643987","highestBid":"0.01642198","percentChange":"-0.00472909","baseVolume":"1788.34098232","quoteVolume":"107338.80048768","isFrozen":"0","high24hr":"0.01697000","low24hr":"0.01637113"}
	public static double[] ParseCrypto(String data){
		
		double[] result = new double[3];
		
		data = data.replace("\"","");
		data = data.replace("{","");
		data = data.replace("}}", "");
		String[] hold = data.split("},");
		String[][] temp = new String[hold.length][12];
		
		int j = 0;
		
		for(int i = 0; i < hold.length; i++){
			temp[i] = hold[i].split(",");
			String[] x = temp[i][0].split(":");
			if(x[0].equals("BTC_LTC") || x[0].equals("BTC_ETH") || x[0].equals("USDT_BTC")){
				result[j] = Double.parseDouble(temp[i][1].replaceAll("[^.0-9]",""));
				j += 1;
			}
		}

		return result;
		
	}
	
	// This API will fetch your data on ETF's, Investment Banks, and several popular crypto currencies
	public static String WebData(String tick, int k) throws Exception{
		
		String x = "";
		String url = "";
				
		if(k == 0){
			String base = "https://marketdata.websol.barchart.com/getQuote.json?";
			String key = "apikey=6d556ee05fb060f2e2d66f4609d9a4a8&symbols=";
			url = base + key + tick;
		} else {
			url = "https://poloniex.com/public?command=returnTicker";           // Fetches your crypto currency prices
		}
		
		// Following commands connect java to the internet
		
		URL moe = new URL(url);
		URLConnection sharieff = moe.openConnection();
		BufferedReader b = new BufferedReader(new InputStreamReader(sharieff.getInputStream()));
				
		if(k == 1){
			x = b.readLine();
		} else {
			x = b.readLine();
			x = x.replace("\"","");
			String[] n = x.split("lastPrice:");
			String[] o = n[1].split(",");
			
			x = o[0];
		}
		
		return x;
	}
	
	
	// This function allows your program to have a timer which will refresh 
	// in a certain inputed time interval
	
	public static void Timer(JTextField time,JLabel timeBox) throws Exception{
		
		long timeX = Long.parseLong(time.getText()) * 1000;
		long start = System.currentTimeMillis();
		long end = 0;
		
		while(end - start <= timeX){
			end = System.currentTimeMillis();
			String temp = Long.toString((timeX - (end - start))/1000);
			timeBox.setText(temp);
		}
		
		
		
	}
	
	// This method will write your correlation table
	
	public static void WriteCorr(JTable X, String[] ticks, double[][] corr){
		
		for(int i = 0; i < corr.length; i++){
			X.setValueAt(ticks[i], 0, 1 + i);
			X.setValueAt(ticks[i], 1 + i, 0);
			for(int j = 0; j < corr.length; j++){
				if(corr[i][j] != 0){X.setValueAt(z.format(corr[i][j]),1 + i, 1 + j);}
			}
		}
				
	}
	
	// This method will write your portfolio optimizer  table
	
	public static void WriteOp(JTable X, String[] ticks, double[] w){
		
		for(int i = 0; i < w.length; i++){
			X.setValueAt(ticks[i],i,0);
			X.setValueAt(z.format(w[i]),i,1);
		}
		
	}
	
	// This method will write your Risk table
	
	public static void WriteRisk(JTable X, String[] ticks, double[][] rr, double[] varX, double[] beta){
		
		X.setValueAt("Name", 0, 0);
		X.setValueAt("VaR (95%)", 0, 1);
		X.setValueAt("Volatility", 0, 2);
		X.setValueAt("Beta S&P",0,3);
		
		for(int i = 0; i < varX.length; i++){
			X.setValueAt(ticks[i], 1 + i, 0);
			X.setValueAt(z.format(varX[i]), 1+i, 1);
			X.setValueAt(z.format(rr[i][1]), 1+i, 2);
			X.setValueAt(mx.format(beta[i]), 1+i, 3);
		}
		
	}
	
	
}
