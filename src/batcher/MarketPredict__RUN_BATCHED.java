package batcher;

import java.io.File;

import marketPrediction.MarketBuildTraining;
import marketPrediction.MarketEvaluate;
import marketPrediction.MarketTrain;
import marketPrediction.PropsXML;

import org.encog.Encog;

import com.google.gson.Gson;

import MiniThreadManager.MiniThread;

/**
 * Driver for one complete network training and evaluation
 */
public class MarketPredict__RUN_BATCHED extends MiniThread {

	public PropsXML p;
	public String DATA_DIR;

	public MarketPredict__RUN_BATCHED(PropsXML p, String DATA_DIR, String threadLabel) {
		this.p = p;
		this.DATA_DIR = DATA_DIR;

		this.threadLabel = threadLabel;
	}

	public void run() {
		// create the log file if needed
		if (p.USE_LOG_FILE) {
			IOHelper.writeStringToNewFile(DATA_DIR + "/" + p.LOG_FILE_NAME, "");
		}

		// write out JSON file of the PropsXML in case we want it later
		// note: we could load this object back in with this json
		Gson gson = new Gson();
		String json = gson.toJson(p);
		IOHelper.writeStringToNewFile(DATA_DIR + "/PropsXML.json", json);

		// Declare directory where to write/read results
		File dataDir = new File(DATA_DIR);

		// Generate temporal data series and network structure
		MarketBuildTraining.generate(p, dataDir);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Train network with temporal data
		MarketTrain.train(p, dataDir);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Evalute trained network on test data
		MarketEvaluate.evaluate(p, dataDir, p.NETWORK_FILE);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Prune network
		// MarketPrune.incremental(dataDir);

		Encog.getInstance().shutdown();

		endMessage = "Thread completed successfully.";
	}
}
