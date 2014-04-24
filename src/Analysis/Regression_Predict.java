package Analysis;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import DataIngester.DataIngester;

public class Regression_Predict {

	public static void predict(TemporalMLDataSet temporalDataset, MLRegression model,
			int DEBUG_LEVEL, int INPUT_WINDOW_SIZE, int PREDICT_WINDOW_SIZE, double NORMALIZED_LOW,
			double NORMALIZED_HIGH, double[] actualLowValues, double[] actualHighValues,
			int numberOfDataSeries, int predictFieldIndex, boolean PRINT_DENORMALIZED) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nStarting prediction");

		NormalizedField normPrice = new NormalizedField(NormalizationAction.Normalize, "price",
				actualHighValues[predictFieldIndex], actualLowValues[predictFieldIndex],
				NORMALIZED_HIGH, NORMALIZED_LOW);

		// initialize a new TemporalMLDataSet with the same settings
		DataIngester dataIngester = new DataIngester();
		TemporalMLDataSet testSet = dataIngester.initTemporalDataSet(DEBUG_LEVEL,
				INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE, numberOfDataSeries, predictFieldIndex);

		// header for printout
		if (DEBUG_LEVEL >= 1)
			System.out.println("   Seq : Predicted  : Actual     : Error      : Error Percent");

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
				
				//get the results for this iteration
				double predicted = modelOutput.getData(0); //zero due to only having one output
				double actual = temporalDataset.getPoints().get(i).getData(predictFieldIndex);
				double error = actual - predicted;
				double errorPercent = ((actual - predicted) / actual) * 100;
				
				// denormalize if specified in config.xml
				if (PRINT_DENORMALIZED) {
					predicted = normPrice.deNormalize(predicted);
					actual = normPrice.deNormalize(actual);

					error = actual - predicted;
					errorPercent = ((actual - predicted) / actual) * 100;
				}
				
				// print results from this iteration
				if (DEBUG_LEVEL >= 1) {
					System.out.printf(" %5d : %10.2f : %10.2f : %10.2f : %10.2f%% \n",
							point.getSequence(), predicted, actual, error, errorPercent);
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
			System.out.println("   Seq : Predicted  : Actual     : Error      : Error Percent");

		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed prediction");
	}
}
