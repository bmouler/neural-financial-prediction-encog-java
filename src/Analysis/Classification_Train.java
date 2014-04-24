package Analysis;

import org.encog.engine.network.activation.ActivationBiPolar;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;
import org.encog.ml.MLResettable;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.RequiredImprovementStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.simple.EncogUtility;

public class Classification_Train {
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
	public static void trainModel(int DEBUG_LEVEL, TemporalMLDataSet temporal, double TARGET_ERROR) {
		
		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nStarting training");

		// TODO add config parameters for activation function
		// TODO add config parameters for layers: number of hidden and number of visible?
		// create a neural network, without using a factory
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null,true,temporal.getInputNeuronCount()));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),true,10));
		network.addLayer(new BasicLayer(new ActivationSigmoid(),false,1));
		network.getStructure().finalizeStructure();
		network.reset();

		// create training data
		MLDataSet trainingSet = temporal;
		
		// train the neural network
		final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

		int epoch = 1;

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while(train.getError() > TARGET_ERROR);
		train.finishTraining();
		
		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed training");
		
		

		// test the neural network
		System.out.println("\n\nNeural Network Results:");
		for(MLDataPair pair: trainingSet ) {
			final MLData output = network.compute(pair.getInput());
			System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
					+ ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
		}
		

	}
}
