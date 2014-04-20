package KevinThread;

import java.io.File;

import org.encog.ml.data.temporal.TemporalDataDescription;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;
import org.encog.util.csv.ReadCSV;

public class BuildTraining {

	public static TemporalMLDataSet createTraining(
			File rawFile,
			int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE) {
		
		TemporalMLDataSet trainingData = initDataSet(INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);
		ReadCSV csv = new ReadCSV(rawFile.toString(), true, ' ');
		while (csv.next()) {
			int year = csv.getInt(0);
			int month = csv.getInt(1);
			double sunSpotNum = csv.getDouble(2);
			double dev = csv.getDouble(3);

			// we need a sequence number to sort the data. Here we just use
			// year * 100 + month, which produces output like "201301" for
			// January, 2013.
			int sequenceNumber = (year * 100) + month;

			TemporalPoint point = new TemporalPoint(trainingData
					.getDescriptions().size());
			point.setSequence(sequenceNumber);
			point.setData(0, normSSN.normalize(sunSpotNum) );
			point.setData(1, normDEV.normalize(dev) );
			trainingData.getPoints().add(point);
		}
		csv.close();
		
		// generate the time-boxed data
		trainingData.generate();
		return trainingData;
	}
	
	/**
	 * Used to normalize the SSN (sun spot number) from a range of 0-300 
	 * to 0-1.
	 */
	public static NormalizedField normSSN = new NormalizedField(
			NormalizationAction.Normalize, "ssn", 300, 0, 1, 0);
	
	/**
	 * Used to normalize the dev from a range of 0-100 
	 * to 0-1.
	 */
	public static NormalizedField normDEV = new NormalizedField(
			NormalizationAction.Normalize, "dev", 100, 0, 1, 0);
	
	public static TemporalMLDataSet initDataSet(int INPUT_WINDOW_SIZE, int PREDICT_WINDOW_SIZE) {
		// create a temporal data set
		TemporalMLDataSet dataSet = new TemporalMLDataSet(INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);

		// we are dealing with two columns.
		// The first is the sunspot number. This is both an input (used to
		// predict) and an output (we want to predict it), so true,true.
		TemporalDataDescription sunSpotNumberDesc = new TemporalDataDescription(TemporalDataDescription.Type.RAW, true, true);
					
		// The second is the standard deviation for the month. This is an
		// input (used to predict) only, so true,false.
		TemporalDataDescription standardDevDesc = new TemporalDataDescription(TemporalDataDescription.Type.RAW, true, false);
		dataSet.addDescription(sunSpotNumberDesc);
		dataSet.addDescription(standardDevDesc);
		return dataSet;
	}
}
