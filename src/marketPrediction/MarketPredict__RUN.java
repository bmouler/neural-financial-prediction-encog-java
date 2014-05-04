package marketPrediction;

import java.io.File;

import org.encog.Encog;

import batcher.IOHelper;

/**
 * Driver for one complete network training and evaluation
 */
public class MarketPredict__RUN {

	final static String WORKING_DIR = "./Configs"; // no backslash
	final static String DATA_DIR    = "./data/MarketPredict"; // no backslash
	final static String PROPS_FILE  = "config.xml";

	public static void main(String[] args) throws Exception {

		// Read in properties file
		PropsXML p = new PropsXML(WORKING_DIR, PROPS_FILE);
		
		// create the log file if needed
		if (p.USE_LOG_FILE) {
			IOHelper.writeStringToNewFile(DATA_DIR + "/" + p.LOG_FILE_NAME, "");
		}
		
		// Declare directory where to write/read results
		File dataDir = new File(DATA_DIR);

		// Generate temporal data series and network structure
		MarketBuildTraining.generate(p, dataDir);

		// Train network with temporal data
		MarketTrain.train(p, dataDir);

		// Evalute trained network on test data
		MarketEvaluate.evaluate(p, dataDir, p.NETWORK_FILE);

		// Prune network
		// MarketPrune.incremental(dataDir);

		Encog.getInstance().shutdown();
	}
}
