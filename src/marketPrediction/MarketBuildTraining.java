package marketPrediction;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.market.MarketDataDescription;
import org.encog.ml.data.market.MarketDataType;
import org.encog.ml.data.market.MarketMLDataSet;
import org.encog.ml.data.market.TickerSymbol;
import org.encog.ml.data.market.loader.MarketLoader;
import org.encog.ml.data.market.loader.YahooFinanceLoader;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.pattern.ElmanPattern;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.simple.EncogUtility;

import batcher.IOHelper;

/**
 * Build the training data for the prediction and store it in an Encog file for
 * later training.
 */
public class MarketBuildTraining {

	public static void generate(PropsXML p, File dataDir) {
		
		if (p.DEBUG_LEVEL >= 1) {
			String s = "Generating temporal data series...";
			System.out.println(s);
			if (p.USE_LOG_FILE) {
				IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s);
			}
		}
		
		// Data will come from Yahoo! Finance
		final MarketLoader loader = new YahooFinanceLoader();
		
		// Set up data set
		final MarketMLDataSet market = 
			new MarketMLDataSet(loader, p.INPUT_WINDOW, p.PREDICT_WINDOW);

		// Add stock tickers
		int numInputDataSeries = 0;
		for (int tickerNum = 0; tickerNum < p.TICKERS.length; ++tickerNum) {
			MarketDataDescription desc = 
				new MarketDataDescription(new TickerSymbol(p.TICKERS[tickerNum]), MarketDataType.CLOSE, p.IS_INPUTS[tickerNum], p.IS_PREDICTS[tickerNum]);
			market.addDescription(desc);
			numInputDataSeries += (p.IS_INPUTS[tickerNum] == true) ? 1 : 0;
		}
		
		// Set beg and end dates of data to use for training
		Calendar end = new GregorianCalendar();
		Calendar beg = (Calendar) end.clone();
		
		beg.add(Calendar.DATE, -p.TRAIN_END_DAYS_AGO);
		end.add(Calendar.DATE, -p.TRAIN_END_DAYS_AGO);
		beg.add(Calendar.DATE, -p.TRAIN_BEG_DAYS_AGO);
		
		market.load(beg.getTime(), end.getTime());
		
		// Generate temporal data series
		market.generate();

		// Save data series to file
		EncogUtility.saveEGB(new File(dataDir, p.TRAINING_FILE), market);

		// Create a network
//		final BasicNetwork network = 
//			EncogUtility.simpleFeedForward(
//				market.getInputSize(), 
//				p.HIDDEN1_COUNT, 
//				p.HIDDEN2_COUNT, 
//				market.getIdealSize(), 
//				true
//			);
		
		// Create other kinds of networks
//		System.out.println("The input size is: "+market.getInputSize());
//		System.out.println("The ideal size is: "+market.getIdealSize());
		BasicLayer input, hidden;
//		BasicNetwork network = new BasicNetwork();
		BasicNetwork network = createElmanNetwork(market);
//		network.addLayer(input = new BasicLayer(market.getInputSize()));
//		network.addLayer(hidden = new BasicLayer(market.getInputSize()));
//		network.addLayer(new BasicLayer(market.getInputSize()/4));
//		network.addLayer(new BasicLayer(market.getInputSize()/8));
//		input.setContextFedBy(hidden);
//		network.getStructure().finalizeStructure();
		network.reset();
		
		

		
		// Save the network and the training
		EncogDirectoryPersistence.saveObject(new File(dataDir, p.NETWORK_FILE), network);

		// Print some useful info
		if (p.DEBUG_LEVEL >= 2) {
			String s = 
			"  Number of input data series      : " + numInputDataSeries + "\n" +
			"  Number of input nodes            : " + market.getInputSize() + "\n" +
			"  Number of nodes in hidden layer 1: " + p.HIDDEN1_COUNT + "\n" +
			"  Number of nodes in hidden layer 2: " + p.HIDDEN2_COUNT + "\n" +
			"  Number of output nodes           : " + market.getIdealSize() + "\n";
			System.out.print(s);
			if (p.USE_LOG_FILE) {
				IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s);
			}
		}

		if (p.DEBUG_LEVEL >= 1) {
			String s = "  Done generating temporal data series...";
			System.out.println(s);
			if (p.USE_LOG_FILE) {
				IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s);
			}
		}
		
	}
	// Elman network
	static BasicNetwork createElmanNetwork(MarketMLDataSet market) {
		  // construct an Elman type network
		  ElmanPattern pattern = new ElmanPattern();
		  pattern.setActivationFunction(new ActivationSigmoid());
		  pattern.setInputNeurons(market.getInputSize());
		  pattern.addHiddenLayer(market.getInputSize());
		  pattern.setOutputNeurons(1);
		  return (BasicNetwork)pattern.generate();
		}
	
}
