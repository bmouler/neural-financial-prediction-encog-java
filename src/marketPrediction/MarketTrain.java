/*
 * Encog(tm) Java Examples v3.2
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-examples
 *
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
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
 * Load the training data from an Encog file, produced during the
 * "build training step", and attempt to train.
 * 
 * @author jeff
 * 
 */
public class MarketTrain {

	public static void train(File dataDir) {

		final File networkFile = new File(dataDir, Config.NETWORK_FILE);
		final File trainingFile = new File(dataDir, Config.TRAINING_FILE);

		// network file
		if (!networkFile.exists()) {
			System.out.println("Can't read file: " + networkFile.getAbsolutePath());
			return;
		}
		
		BasicNetwork network = (BasicNetwork)EncogDirectoryPersistence.loadObject(networkFile);

		// training file
		if (!trainingFile.exists()) {
			System.out.println("Can't read file: " + trainingFile.getAbsolutePath());
			return;
		}
		
		final MLDataSet trainingSet = EncogUtility.loadEGB2Memory(trainingFile);

		// train the neural network
		// use the below for TIMED training
		// also can be run like so EncogUtility.trainConsole(train, network, trainingSet, Config.TRAINING_MINUTES); where train is one of the below objects
//		EncogUtility.trainConsole(network, trainingSet, Config.TRAINING_MINUTES);
		
		// backprop training (not working: NaN error)
		// TODO: multiple flags for backprop options
//		Backpropagation train = new Backpropagation(network, trainingSet, 0.7, 0.3);
//		do {
//			train.iteration();
//			System.out.println("Error: "+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		
		// resilient propagation training (working; 4 types)
		// TODO: multiple flags by RPROP type
		ResilientPropagation train = new ResilientPropagation(network, trainingSet);
		// switch for the four below:
		train.setRPROPType(RPROPType.RPROPp); // default
		train.setRPROPType(RPROPType.RPROPm); // default
		train.setRPROPType(RPROPType.iRPROPp); // default //supposed to be the best one
		train.setRPROPType(RPROPType.iRPROPm); // default
		do {
			train.iteration();
			System.out.println("Error: "+train.getError());
		} while (train.getError() > Config.TRAIN_THRESH);
		
		// quickprop training (not working)
		// TODO: flags for learning rate, default = 2
//		QuickPropagation train = new QuickPropagation(network, trainingSet, 2.0);
//		do {
//			train.iteration();
//			System.out.println("Error: "+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		// Manhattan update rule training (not working: NaN error)
		// TODO: multiple flags for Manhattan propagation options
//		ManhattanPropagation train = new ManhattanPropagation(network, trainingSet, 0.00001);
//		do {
//			train.iteration();
//			System.out.println("Error: "+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		// LMA training (not working: memory error)
		// not working because of memory error?
//		LevenbergMarquardtTraining train = new LevenbergMarquardtTraining(network, trainingSet);
//		do {
//			train.iteration();
//			System.out.println("Error :"+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		// SCG training (not working: non-decreasing error function)
//		ScaledConjugateGradient train = new ScaledConjugateGradient(network, trainingSet);
//		do {
//			train.iteration();
//			System.out.println("Error :"+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		// genetic algorithm training
		// TODO: where is this package!?
		// TODO: add variables for GA options
//		CalculateScore score = new TrainingSetScore(trainingSet);
//		MLTrain train = new NeuralGeneticAlgorithm(network, new NguyenWidrowRandomizer(), score, 5000, 0.1, 0.25);
//		do {
//			train.iteration();
//			System.out.println("Error: "+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		// simulated annealing training (not working: non-decreasing error function)
		// TODO: add variables for startingTemp, endingTemp, etc.
//		CalculateScore score = new TrainingSetScore(trainingSet);
//		MLTrain train = new NeuralSimulatedAnnealing(network, score, 10, 2, 10);
//		do {
//			train.iteration();
//			System.out.println("Error: "+train.getError());
//		} while (train.getError() > Config.TRAIN_THRESH);
		
		System.out.println("Final Error: " + network.calculateError(trainingSet));
		System.out.println("Training complete, saving network.");
		EncogDirectoryPersistence.saveObject(networkFile, network);
		System.out.println("Network saved.");
		
		Encog.getInstance().shutdown();

	}
}
