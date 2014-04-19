package KevinThread;

import java.io.File;

import org.encog.Encog;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
import org.encog.util.csv.ReadCSV;


public class Predict {
	
	public static TemporalMLDataSet predict(
			File rawFile,
			MLRegression model,
			int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE) {
		// You can also use the TemporalMLDataSet for prediction.  We will not use "generate"
		// as we do not want to generate an entire training set.  Rather we pass it each sun spot 
		// ssn and dev and it will produce the input to the model, once there is enough data.
		TemporalMLDataSet trainingData = BuildTraining.initDataSet(INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);
		ReadCSV csv = new ReadCSV(rawFile.toString(), true, ' ');
		while (csv.next()) {
			int year = csv.getInt(0);
			int month = csv.getInt(1);
			double sunSpotNum = csv.getDouble(2);
			double dev = csv.getDouble(3);
			
			// do we have enough data for a prediction yet?
			if( trainingData.getPoints().size()>=trainingData.getInputWindowSize() ) {
				// Make sure to use index 1, because the temporal data set is always one ahead
				// of the time slice its encoding.  So for RAW data we are really encoding 0.
				MLData modelInput = trainingData.generateInputNeuralData(1);
				MLData modelOutput = model.compute(modelInput);
				double ssn = BuildTraining.normSSN.deNormalize(modelOutput.getData(0));
				System.out.println(year + ":Predicted=" + ssn + ",Actual=" + sunSpotNum );
				
				// Remove the earliest training element.  Unlike when we produced training data,
				// we do not want to build up a large data set.  We just add enough data points to produce
				// input to the model.
				trainingData.getPoints().remove(0);
			}
			
			// we need a sequence number to sort the data. Here we just use
			// year * 100 + month, which produces output like "201301" for
			// January, 2013.
			int sequenceNumber = (year * 100) + month;

			TemporalPoint point = new TemporalPoint(trainingData.getDescriptions().size());
			point.setSequence(sequenceNumber);
			point.setData(0, BuildTraining.normSSN.normalize(sunSpotNum) );
			point.setData(1, BuildTraining.normDEV.normalize(dev) );
			trainingData.getPoints().add(point);
		}
		csv.close();
		
		// generate the time-boxed data
		trainingData.generate();
		return trainingData;
	}
}
