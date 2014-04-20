
package KevinThread;

import java.io.File;

import org.encog.Encog;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;
import org.encog.ml.MLResettable;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.RequiredImprovementStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.simple.EncogUtility;

public class Train {
	/**
	 * Create and train a model.  Use Encog factory codes to specify the model type that you want.
	 * @param trainingData The training data to use.
	 * @param methodName The name of the machine learning method (or model).
	 * @param methodArchitecture The type of architecture to use with that model.
	 * @param trainerName The type of training.
	 * @param trainerArgs Training arguments.
	 * @return The trained model.
	 */
	public static MLRegression trainModel(
			MLDataSet trainingData,
			String methodName, 
			String methodArchitecture,
			String trainerName, 
			String trainerArgs) {
		
		// first, create the machine learning method (the model)
		MLMethodFactory methodFactory = new MLMethodFactory();		
		MLMethod method = methodFactory.create(methodName, methodArchitecture, trainingData.getInputSize(), trainingData.getIdealSize());

		// second, create the trainer
		MLTrainFactory trainFactory = new MLTrainFactory();	
		MLTrain train = trainFactory.create(method,trainingData,trainerName,trainerArgs);				
		// reset if improve is less than 1% over 5 cycles
		if( method instanceof MLResettable && !(train instanceof ManhattanPropagation) ) {
			train.addStrategy(new RequiredImprovementStrategy(500));
		}

		// third train the model
		EncogUtility.trainToError(train, 0.002);
		
		return (MLRegression)train.getMethod();		
	}
}
