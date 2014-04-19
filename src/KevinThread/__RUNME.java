package KevinThread;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class __RUNME {

	/*
	 * This is the only setting for this file. All other settings are specified in the properties
	 * file in this directory. The file must be in the root of the workingDir and be named
	 * 'config.xml'.
	 */
	final static String WORKING_DIR = "./data/EncogFiles/PropertiesTest";

	public static void main(String[] args) throws Exception {

		// load properties file
		Properties props = new Properties();
		try {
			File file = new File(WORKING_DIR + "/config.xml");
			FileInputStream fileInput = new FileInputStream(file);
			props.loadFromXML(fileInput);
			fileInput.close();
			
			// add workingDir so we do not need to include it in the config.properties
			props.put("WORKING_DIR", WORKING_DIR);

			// print properties to console
			System.out.println("======");
			System.out.println("Properties File found. Configs are:");
			Enumeration enuKeys = props.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = props.getProperty(key);
				System.out.println(key + ": " + value);
			}
			System.out.println("======");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception("Error while attempting to read config.xml: file not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Error while attempting to read config.xml: io exception.");
		}

//		// get data, then train and evaluate the neural network
//		try {
//			
//
//			// Step 1. Create training data
//			TemporalMLDataSet trainingData = BuildTraining.createTraining(rawFile,
//					Integer.parseInt(props.getProperty("INPUT_WINDOW_SIZE")),
//					Integer.parseInt(props.getProperty("PREDICT_WINDOW_SIZE")));
//
//			// Step 2. Create and train the model.
//			// All sorts of models can be used here, see the XORFactory
//			// example for more info.
//			MLRegression model = Train.trainModel(trainingData, MLMethodFactory.TYPE_FEEDFORWARD,
//					"?:B->SIGMOID->25:B->SIGMOID->?", MLTrainFactory.TYPE_RPROP, "");
//
//			// Now predict
//			Predict.predict(rawFile, model,
//					Integer.parseInt(props.getProperty("INPUT_WINDOW_SIZE")),
//					Integer.parseInt(props.getProperty("PREDICT_WINDOW_SIZE")));
//
//			Encog.getInstance().shutdown();
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}

	}

}
