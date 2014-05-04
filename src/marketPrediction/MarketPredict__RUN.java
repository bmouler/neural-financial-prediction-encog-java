package marketPrediction;

import java.io.File;

import org.encog.Encog;

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
