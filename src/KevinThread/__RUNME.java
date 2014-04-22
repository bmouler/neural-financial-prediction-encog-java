package KevinThread;

import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;

import DataIngester.DataIngester;
import PropsXML.PropsXML;

public class __RUNME {

	/*
	 * These are the only settings for this file. All other settings are specified in the properties
	 * file in this directory. The file must be in the root of the workingDir and be named
	 * as described in PROPS_FILE.
	 */
	 final static String WORKING_DIR = "./Configs/KevinThreadTest"; // no backslash
	// final static String WORKING_DIR = "./Configs/BertCleanTestClassification"; // no backslash
	// final static String WORKING_DIR = "./Configs/BertCleanTestRegression"; // no backslash
	final static String PROPS_FILE = "config.xml";

	public static void main(String[] args) throws Exception {

		// load properties file and required values
		PropsXML p = new PropsXML(WORKING_DIR, PROPS_FILE);

		// get data, then train and evaluate the neural network
		try {
			// Step 1. Create training data
			DataIngester dataIngester = new DataIngester();
			dataIngester.createData(p.DEBUG_LEVEL, p.DATA_FILES, p.NORMALIZED_LOW,
					p.NORMALIZED_HIGH, p.DATA_NEEDS_CLEANING, p.DATA_NEEDS_NORMALIZATION,
					p.PREDICT_FILE, p.PREDICT_LABEL);
			int numberOfDataSeries = dataIngester.getNumberOfDataSeries();
			int predictFieldIndex = dataIngester.getPredictFieldIndex(p.PREDICT_FILE,
					p.PREDICT_LABEL);
			if (predictFieldIndex < 0) {
				throw new Exception("Predict field not found.");
			}

			// get actual low/high values for use in prediction output
			double[] actualLowValues = dataIngester.getActualLowValues();
			double[] actualHighValues = dataIngester.getActualHighValues();

			TemporalMLDataSet temporalDataset = dataIngester.makeTemporalDataSet(p.DEBUG_LEVEL,
					p.INPUT_WINDOW_SIZE, p.PREDICT_WINDOW_SIZE, p.PREDICT_FILE, p.PREDICT_LABEL);

			switch (p.PREDICT_MODEL) {
			case "Regression":
				// Step 2. Create and train the model
				String trainerArgs = "";
				MLRegression model = Regression_Train.trainModelRegression(temporalDataset,
						MLMethodFactory.TYPE_FEEDFORWARD, p.ACTIVATION_FUNCTION,
						MLTrainFactory.TYPE_RPROP, trainerArgs, p.TARGET_ERROR, p.DEBUG_LEVEL);

				// Step 3. Predict
				Regression_Predict.predict(temporalDataset, model, p.DEBUG_LEVEL,
						p.INPUT_WINDOW_SIZE, p.PREDICT_WINDOW_SIZE, p.NORMALIZED_LOW,
						p.NORMALIZED_HIGH, actualLowValues, actualHighValues, numberOfDataSeries,
						predictFieldIndex, p.PRINT_DENORMALIZED);
				break;
			case "Classification":
				Classification_Train.trainModel(p.DEBUG_LEVEL, temporalDataset);
				break;
			default:
				throw new Exception("No model specified.");
			}

			// Step 4. Evaluate

			// Step 5. Prune

			// shutdown
			Encog.getInstance().shutdown();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
