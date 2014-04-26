package Analysis;

import org.encog.engine.network.activation.ActivationBiPolar;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

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
		network.addLayer(new BasicLayer(null, true, temporal.getInputNeuronCount()));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 10));
		network.addLayer(new BasicLayer(new ActivationBiPolar(), false, 1));
		network.getStructure().finalizeStructure();
		network.reset();

		// create training data
		MLDataSet trainingSet = temporal;

		// train the neural network
		CalculateScore score = new TrainingSetScore(trainingSet);
		final NeuralSimulatedAnnealing train = new NeuralSimulatedAnnealing(network,score,10,2,1);

		int epoch = 1;

		do {
			train.iteration();
			System.out.println("Epoch #" + epoch + " Error:" + train.getError());
			epoch++;
		} while (train.getError() > TARGET_ERROR);
		train.finishTraining();

		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed training");

		// test the neural network
		if (DEBUG_LEVEL >= 2)
			System.out.println("\n\nNeural Network Results:");

		int countTotal = 0;
		int countCorrect = 0;

		for (MLDataPair pair : trainingSet) {
			final MLData output = network.compute(pair.getInput());

			double actual = output.getData(0);
			double ideal = pair.getIdeal().getData(0);

			String outCome = null;
			countTotal++;
			if (actual == ideal) {
				outCome = "correct++";
				countCorrect++;
			} else {
				outCome = "wrong----";
			}

			if (DEBUG_LEVEL >= 2)
				System.out.printf("%5.2f :: %5.2f :: actual= %2.0f :: ideal= %2.0f :: %s\n", pair
						.getInput().getData(0), pair.getInput().getData(1), actual, ideal, outCome);
		}

		double percentCorrect = ((((double) countCorrect) / ((double) countTotal)) * 100);
		System.out.printf("\nSummary: %5.2f%% or {%5d of %5d}\n", percentCorrect, countCorrect,
				countTotal);
	}
}
