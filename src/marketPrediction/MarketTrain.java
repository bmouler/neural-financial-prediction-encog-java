package marketPrediction;

import java.io.File;

import org.encog.Encog;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.training.TrainingSetScore;
//import org.encog.neural.networks.training.genetic.NeuralGeneticAlgorithm; // where is this?!
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.lma.LevenbergMarquardtTraining;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
import org.encog.neural.networks.training.propagation.quick.QuickPropagation;
import org.encog.neural.networks.training.propagation.resilient.RPROPType;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.networks.training.propagation.scg.ScaledConjugateGradient;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.simple.EncogUtility;

/**
 * Load the training data from an Encog file and train it.
 */
public class MarketTrain {

	public static void train(PropsXML p, File dataDir) {

		if (p.DEBUG_LEVEL >= 1)
			System.out.println("Training network...");
		
		// Load training data and network
		final File trainingFile = new File(dataDir, p.TRAINING_FILE);
		final File networkFile  = new File(dataDir, p.NETWORK_FILE );
		
		if (!trainingFile.exists()) {
			System.out.println("Can't read training file: \n" + trainingFile.getAbsolutePath());
			System.out.println("Can't read training file: \n" + trainingFile.getPath());
			return;
		}
		
		final MLDataSet trainingSet = EncogUtility.loadEGB2Memory(trainingFile);

		if (!networkFile.exists()) {
			System.out.println("Can't read network file: \n" + networkFile.getAbsolutePath());
			System.out.println("Can't read network file: \n" + trainingFile.getPath());
			return;
		}
		
		BasicNetwork network = (BasicNetwork)EncogDirectoryPersistence.loadObject(networkFile);

		// Train neural network
		
		// use the below for TIMED training
		// also can be run like so EncogUtility.trainConsole(train, network, trainingSet, Config.TRAINING_MINUTES); where train is one of the below objects
//		EncogUtility.trainConsole(network, trainingSet, Config.TRAINING_MINUTES);
		
		// Train via back propagation
		if (p.TRAIN_ALG.toUpperCase().equals("BPROP")) {
			
			// backprop training (not working: NaN error)
			Backpropagation train =
				new Backpropagation(network, trainingSet, p.BPROP_LEARNING_RATE, p.BPROP_MOMENTUM);
			
			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		// Train via resilient propagation
		else if (p.TRAIN_ALG.toUpperCase().equals("RPROP")) {
			
			ResilientPropagation train = new ResilientPropagation(network, trainingSet);
			
			// Choose RPROP type
			if (p.RPROP_TYPE.equals("RPROPp"))
				train.setRPROPType(RPROPType.RPROPp);
			else if (p.RPROP_TYPE.equals("RPROPm"))
				train.setRPROPType(RPROPType.RPROPm);
			else if (p.RPROP_TYPE.equals("iRPROPm"))
				train.setRPROPType(RPROPType.iRPROPm);
			else
				train.setRPROPType(RPROPType.iRPROPp); // default is best one
			
			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}

		// Train via quick propagation
		else if (p.TRAIN_ALG.toUpperCase().equals("QPROP")) {
			
			// quickprop training (not working)
			// TODO: flags for learning rate, default = 2
			QuickPropagation train = new QuickPropagation(network, trainingSet, p.QPROP_LEARNING_RATE);

			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		// Train via Manhattan propagation
		else if (p.TRAIN_ALG.toUpperCase().equals("MPROP")) {
			
			// Manhattan update rule training (not working: NaN error)
			// TODO: multiple flags for Manhattan propagation options
			ManhattanPropagation train = new ManhattanPropagation(network, trainingSet, p.MPROP_LEARNING_RATE);
			
			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		// Train via Levenberg Marquardt propagation
		else if (p.TRAIN_ALG.toUpperCase().equals("LPROP")) {
			
			// LMA training (not working: memory error)
			// not working because of memory error?
			LevenbergMarquardtTraining train = new LevenbergMarquardtTraining(network, trainingSet);
			
			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		// Train via scaled conjugate gradient propagation
		else if (p.TRAIN_ALG.toUpperCase().equals("SPROP")) {

			// SCG training (not working: non-decreasing error function)
			ScaledConjugateGradient train = new ScaledConjugateGradient(network, trainingSet);

			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		else if (p.TRAIN_ALG.toUpperCase().equals("GENETIC")) {

			// genetic algorithm training
			// TODO: where is this package!?
			// TODO: add variables for GA options
			//CalculateScore score = new TrainingSetScore(trainingSet);
			//MLTrain train = new NeuralGeneticAlgorithm(network, new NguyenWidrowRandomizer(), score, 5000, 0.1, 0.25);

//			// Train network
//			int numIters = 0;
//			do {
//				train.iteration();
//				
//				++numIters;
//				
//				if (p.DEBUG_LEVEL >= 2) {
//					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
//				}
//			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		else if (p.TRAIN_ALG.toUpperCase().equals("ANNEAL")) {
		
			// simulated annealing training (not working: non-decreasing error function)
			// TODO: add variables for startingTemp, endingTemp, etc.
			CalculateScore score = new TrainingSetScore(trainingSet);
			MLTrain train = 
				new NeuralSimulatedAnnealing(network, score, p.ANNEAL_START_TEMP, p.ANNEAL_STOP_TEMP, p.ANNEAL_CYCLES);

			// Train network
			int numIters = 0;
			do {
				train.iteration();
				
				++numIters;
				
				if (p.DEBUG_LEVEL >= 2) {
					System.out.printf("  Iteration %4d training error: %10.6f\n", numIters, train.getError());
				}
			} while (train.getError() > p.TRAIN_THRESH);
		}
		
		// Write trained network to file
		EncogDirectoryPersistence.saveObject(networkFile, network);
		
		//Encog.getInstance().shutdown();

		if (p.DEBUG_LEVEL >= 2)
			System.out.println("  Final training error: " + network.calculateError(trainingSet));

		if (p.DEBUG_LEVEL >= 1)
			System.out.println("  Done training network...");
	}
}
