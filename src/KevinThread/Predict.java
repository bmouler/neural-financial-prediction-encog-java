package KevinThread;

import java.text.NumberFormat;

import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.temporal.TemporalMLDataSet;

public class Predict {

	public static TemporalMLDataSet predict(MLRegression model, int EVALUATE_START,
			int EVALUATE_END, int INPUT_WINDOW_SIZE) {

		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(4);
		f.setMinimumFractionDigits(4);

		System.out.println("Year\tActual\tPredict\tClosed Loop Predict");
		for (int year = EVALUATE_START; year < EVALUATE_END; year++) {
			MLData input = new BasicMLData(INPUT_WINDOW_SIZE);

			// for (int i = 0; i < input.size(); i++) {
			// input.setData(i, normalizedSunspots[(year - INPUT_WINDOW_SIZE) + i]);
			// }
			//
			// MLData output = model.compute(input);
			// double prediction = output.getData(0);
			//
			// closedLoopSunspots[year] = prediction;
			//
			// for (int i = 0; i < input.size(); i++) {
			// input.setData(i, closedLoopSunspots[(year - INPUT_WINDOW_SIZE) + i]);
			// }
			// output = model.compute(input);
			// double closedLoopPrediction = output.getData(0);
			//
			// System.out.println(year + "\t" + f.format(normalizedSunspots[year]) + "\t"
			// + f.format(prediction) + "\t" + f.format(closedLoopPrediction));
			// }

		}
		
		return null;
	}
}
