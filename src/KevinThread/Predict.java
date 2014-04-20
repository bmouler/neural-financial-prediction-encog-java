package KevinThread;

import java.util.Iterator;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import DataIngester.DataIngester;

public class Predict {

	public static void predict(TemporalMLDataSet temporalDataset, MLRegression model,
			int DEBUG_LEVEL, int INPUT_WINDOW_SIZE, int PREDICT_WINDOW_SIZE, double NORMALIZED_LOW,
			double NORMALIZED_HIGH, double[] actualLowValues, double[] actualHighValues) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nStarting prediction");

		// indicate the input/output datasets so that output can be denormalized
		// final int INPUT_DATASET = 0;
		// final int OUTPUT_DATASET = 0;
		// NormalizedField normPrice = new NormalizedField(NormalizationAction.Normalize, "price",
		// 300, 0, NORMALIZED_HIGH, NORMALIZED_LOW);

		// initialize a new TemporalMLDataSet with the same settings
		DataIngester dataIngester = new DataIngester();
		TemporalMLDataSet testSet = dataIngester.initTemporalDataSet(DEBUG_LEVEL,
				INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);

		// header for printout
		if (DEBUG_LEVEL >= 1)
			System.out.println("   Seq : Predicted  : Actual     : Error");

		// cycle through each point in the temporalDataset
		// add each to testSet
		// when there are enough points to make a prediction, do that
		// remove old points to keep testSet small
		TemporalPoint point = null;
		for (int i = 0; i < temporalDataset.getPoints().size(); i++) {
			// add this point to the testSet
			point = temporalDataset.getPoints().get(i);
			testSet.getPoints().add(point);

			// do we have enough data for a prediction yet?
			if (testSet.getPoints().size() >= testSet.getInputWindowSize()) {
				// Make sure to use index 1, because the temporal data set is always one ahead
				// of the time slice its encoding. So for RAW data we are really encoding 0.
				MLData modelInput = testSet.generateInputNeuralData(1);
				MLData modelOutput = model.compute(modelInput);

				// TODO denormalize output to screen, partially complete
				// double predicted = normPrice.deNormalize(modelOutput.getData(0));

				// TODO need to figure out what the output actual means to go any further
				if (DEBUG_LEVEL >= 1) {
					System.out.printf(" %5d : %5.8f : %5.8f : %6.2f \n", point.getSequence(),
							modelOutput.getData(0), 0f, 0f, 0f);
					// + " : Actual = " + actual);
				}

				// Remove the earliest training element. Unlike when we produced training data,
				// we do not want to build up a large data set. We just add enough data points to
				// produce input to the model.
				testSet.getPoints().remove(0);
			}

			// we need a sequence number to sort the data. Here we just use
			// year * 100 + month, which produces output like "201301" for
			// January, 2013.
			// int sequenceNumber = (year * 100) + month;
		}

		// footer for printout
		if (DEBUG_LEVEL >= 1)
			System.out.println("   Seq : Predicted  : Actual     : Error");

		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed prediction");
	}
}
