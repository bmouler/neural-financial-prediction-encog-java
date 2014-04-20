package KevinThread;

import java.io.File;
import java.text.NumberFormat;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;
import org.encog.util.csv.ReadCSV;

import DataIngester.DataIngester;

public class Predict {

	public static NormalizedField normPrice = new NormalizedField(NormalizationAction.Normalize,
			"price", 300, 0, 1, 0);

	public static void predict(TemporalMLDataSet temporalDataset, MLRegression model,
			int DEBUG_LEVEL, int INPUT_WINDOW_SIZE, int PREDICT_WINDOW_SIZE) {

		// initialize a new TemporalMLDataSet with the same settings
		DataIngester dataIngester = new DataIngester();
		TemporalMLDataSet testSet = dataIngester.initTemporalDataSet(DEBUG_LEVEL,
				INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);

		// cycle through each point in the temporalDataset
		// add each to testSet
		// when there are enough points to make a prediction, do that
		// remove old points to keep testSet small
		for (TemporalPoint point : temporalDataset.getPoints()) {
			// add this point to the testSet
			testSet.getPoints().add(point);

			// do we have enough data for a prediction yet?
			if (temporalDataset.getPoints().size() >= temporalDataset.getInputWindowSize()) {
				// Make sure to use index 1, because the temporal data set is always one ahead
				// of the time slice its encoding. So for RAW data we are really encoding 0.
				MLData modelInput = temporalDataset.generateInputNeuralData(1);
				MLData modelOutput = model.compute(modelInput);

				//denormal price for output to screen
				double predicted = normPrice.deNormalize(modelOutput.getData(0));
				
				//
				System.out.println(sequence + ":Predicted=" + predicted + ",Actual=" + actual);

				// Remove the earliest training element. Unlike when we produced training data,
				// we do not want to build up a large data set. We just add enough data points to
				// produce input to the model.
				temporalDataset.getPoints().remove(0);
			}

			// we need a sequence number to sort the data. Here we just use
			// year * 100 + month, which produces output like "201301" for
			// January, 2013.
			//int sequenceNumber = (year * 100) + month;
		}
	}
}
