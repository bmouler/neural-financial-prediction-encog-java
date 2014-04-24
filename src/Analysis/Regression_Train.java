package Analysis;

import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;
import org.encog.ml.MLResettable;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.RequiredImprovementStrategy;
import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
import org.encog.util.simple.EncogUtility;

public class Regression_Train {
	/**
	 * Create and train a model. Use Encog factory codes to specify the model type that you want.
	 * 
	 * @param trainingData
	 *            The training data to use.
	 * @param methodName
	 *            The name of the machine learning method (or model).
	 * @param methodArchitecture
	 *            The type of architecture to use with that model.
	 * @param trainerName
	 *            The type of training.
	 * @param trainerArgs
	 *            Training arguments.
	 * @return The trained model.
	 */
	public static MLRegression trainModelRegression(MLDataSet trainingData, String methodName,
			String methodArchitecture, String trainerName, String trainerArgs, double TARGET_ERROR,
			int DEBUG_LEVEL) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nStarting training");

		// first, create the machine learning method (the model)
		MLMethodFactory methodFactory = new MLMethodFactory();
		MLMethod method = methodFactory.create(methodName, methodArchitecture,
				trainingData.getInputSize(), trainingData.getIdealSize());

		// second, create the trainer
		MLTrainFactory trainFactory = new MLTrainFactory();
		MLTrain train = trainFactory.create(method, trainingData, trainerName, trainerArgs);
		// reset if improve is less than 1% over 5 cycles
		if (method instanceof MLResettable && !(train instanceof ManhattanPropagation)) {
			train.addStrategy(new RequiredImprovementStrategy(500));
		}

		// third train the model
		EncogUtility.trainToError(train, TARGET_ERROR);
		
		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed training");

		return (MLRegression) train.getMethod();
	}
}
