package KevinThread;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;

public class __RUNME {

	/*
	 * This is the only setting for this file. All other settings are specified in the properties
	 * file in this directory. The file must be in the root of the workingDir and be named
	 * 'config.properties'.
	 */
	final static String WORKING_DIR = "./data/EncogFiles/PropertiesTest";

	public static void main(String[] args) throws Exception {

		// load properties file
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(WORKING_DIR + "/config.properties"));
		} catch (Exception e) {
			throw new Exception("Error while attempting to load propsFile:\n  " + e.getMessage());
		}

		// add workingDir so we do not need to include it in the config.properties
		props.put("WORKING_DIR", WORKING_DIR);

		// print properties to console
		System.out.println("======");
		System.out.println("Properties File found. Configs are:");
		for (String key : props.stringPropertyNames()) {
			String value = props.getProperty(key);
			System.out.println("  " + key + " => " + value);
		}
		System.out.println("======");

		// get data, then train and evaluate the neural network
		try {
			

			// Step 1. Create training data
			TemporalMLDataSet trainingData = BuildTraining.createTraining(rawFile,
					Integer.parseInt(props.getProperty("INPUT_WINDOW_SIZE")),
					Integer.parseInt(props.getProperty("PREDICT_WINDOW_SIZE")));

			// Step 2. Create and train the model.
			// All sorts of models can be used here, see the XORFactory
			// example for more info.
			MLRegression model = Train.trainModel(trainingData, MLMethodFactory.TYPE_FEEDFORWARD,
					"?:B->SIGMOID->25:B->SIGMOID->?", MLTrainFactory.TYPE_RPROP, "");

			// Now predict
			Predict.predict(rawFile, model,
					Integer.parseInt(props.getProperty("INPUT_WINDOW_SIZE")),
					Integer.parseInt(props.getProperty("PREDICT_WINDOW_SIZE")));

			Encog.getInstance().shutdown();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
