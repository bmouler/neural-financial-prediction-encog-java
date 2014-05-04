package batcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import MiniThreadManager.MiniThread;
import MiniThreadManager.MiniThreadManager;
import Tracker.Tracker;
import marketPrediction.PropsXML;

public class Batcher {

	final static String WORKING_DIR = "./Configs/BatcherTest"; // no backslash
	final static String DATA_DIR = "./data/MarketPredict"; // no backslash
	final static String BATCHER_PROPS_FILE = "batcherConfig.xml"; //must be in WORKING_DIR
	final static String BASE_PROPS_FILE = "baseConfig.xml"; //must be in WORKING_DIR

	static List<PropsXML> propsList = new ArrayList<PropsXML>();
	private static String now;
	private static String outputDir;

	public static void main(String[] args) throws Exception {

		// tracker for time keeping
		Tracker tracker = new Tracker();
		tracker.start("entire app");

		// load batcher configs
		tracker.start("load batcher configs");
		PropsXMLBatcher pb = new PropsXMLBatcher(WORKING_DIR, BATCHER_PROPS_FILE);
		PropsXML p = new PropsXML(WORKING_DIR, BASE_PROPS_FILE);
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Batched and base configs loaded.");
		tracker.stop("load batcher configs");

		// produce all configs
		tracker.start("produce all configs");
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Starting to create PropsXML objects.");
		buildSetGeneral(p, pb);
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Finished creating PropsXML objects.");
		tracker.stop("produce all configs");

		// general file directory label creation
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd-HH_mm_ss");
		now = DateTime.now().toString(fmt);
		outputDir = WORKING_DIR + "/" + pb.BATCH_RUN_LABEL + "_" + now;

		// inform using of how many PropsMXL were created
		// ask if they want to continue
		tracker.start("inform using of how many PropsMXL were created");
		System.out.println("Number of PropsXMLs created from " + BATCHER_PROPS_FILE + " is: "
				+ propsList.size());
		System.out.println("Do you want to continue? y or n");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
		String response = null;
		try {
			response = br1.readLine();
		} catch (IOException ioe) {
			System.out.println("IO error trying to read response.");
			System.exit(1);
		}
		// if the reqponse is not 'y'
		// exit
		if (!response.equals("y")) {
			System.out.println("Exiting.");
			System.exit(1);
		} // else continue
		tracker.stop("inform using of how many PropsMXL were created");

		// make working directories
		tracker.start("make working directories");
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Starting create directories.");
		// make list of directories to create
		List<String> listOfRunLabels = new ArrayList<String>();
		for (PropsXML i : propsList) {
			listOfRunLabels.add(i.RUN_LABEL);
		}
		IOHelper.createDirectories(pb.DEBUG_LEVEL, listOfRunLabels, outputDir);
		if (pb.DEBUG_LEVEL >= 2)
			System.out.println("Created " + propsList.size() + " directories");
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Finished creating directories.");
		
		// copy the configs into the working dir
		copyFile(new File(WORKING_DIR + "/" + BATCHER_PROPS_FILE), new File(outputDir + "/"
				+ BATCHER_PROPS_FILE));
		copyFile(new File(WORKING_DIR + "/" + BASE_PROPS_FILE), new File(outputDir + "/"
				+ BASE_PROPS_FILE));
		tracker.stop("make working directories");

		// run everything with MiniThreadManager
		tracker.start("run everything with MiniThreadManager");
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Starting to run MiniThreadManger.");
		// create MiniThreads
		List<MarketPredict__RUN_BATCHED> threads = new ArrayList<MarketPredict__RUN_BATCHED>();
		for (PropsXML i : propsList) {
			threads.add(new MarketPredict__RUN_BATCHED(i, outputDir + "/" + i.RUN_LABEL,
					i.RUN_LABEL));
		}
		MiniThreadManager mm = new MiniThreadManager(pb.MTM_DEBUG_LEVEL, pb.BATCH_RUN_LABEL,
				threads, pb.MTM_MAX_THREADS_ACTIVE, pb.MTM_UPDATE_DELAY_IN_MILLI,
				pb.MTM_KILL_IF_LONG_RUNNING, pb.MTM_KILL_TIME_IN_MILLI, false);
		mm.start();
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Finished running MiniThreadManger.");
		tracker.stop("run everything with MiniThreadManager");

		// create summary
		tracker.start("create summary");
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Starting to create summary of thread completion.");
		// print quick summary
		Thread.sleep(3000); // to prevent the overwriting logging from thread output
		System.out.println("====================================");
		System.out.println("=====All MiniThread EndMessages=====");
		System.out.println("====================================");
		String summary = "";
		for (MiniThread i : threads) {
			String threadSummary = outputDir + "/" + i.threadLabel + "/" + p.LOG_FILE_NAME;
			String lastLine = IOHelper.getLastLinesOfFile(threadSummary, 2).replaceAll("\\n", "");
			String s = i.threadLabel + " , " + i.endMessage + " , " + lastLine;
			if (pb.USE_LOG_FILE) {
				summary += s + "\n";
			}
		}

		summary = processSummary(summary);
		System.out.println(summary);

		if (pb.USE_LOG_FILE) {
			IOHelper.writeStringToNewFile(outputDir + "/" + pb.LOG_FILE_NAME, summary);
		}
		System.out.println("====================================");
		System.out.println("====================================");
		System.out.println("====================================");
		if (pb.DEBUG_LEVEL >= 1)
			System.out.println("Finished creating summary of thread completion.");
		tracker.stop("create summary");

		// TODO consider adding the graph creation here or in the main area

		tracker.stop("entire app");

		// TODO print out the times to complete the tasks
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static String processSummary(String s) {
		String out = "";

		// add the header manually
		out += "threadLabel,endMessage,trials,percent\n";

		// sort the lines by highest percentage
		List<SummaryLine> list = new ArrayList<SummaryLine>();
		String[] lines = s.split("\\n");
		for (String line : lines) {
			String[] values = line.split(",");
			SummaryLine sl = new SummaryLine();

			if (values.length >= 0) {
				sl.threadLabel = values[0];
			}
			if (values.length >= 1) {
				sl.endMessage = values[1];
			}
			if (values.length >= 2) {
				sl.trials = values[2];
			}
			if (values.length >= 3) {
				sl.percent = values[3];
			}

			list.add(sl);
		}

		// from http://stackoverflow.com/questions/6957631/sort-java-collection
		Comparator<SummaryLine> comparator = new Comparator<SummaryLine>() {
			public int compare(SummaryLine c1, SummaryLine c2) {
				return c2.percent.compareTo(c1.percent);
			}
		};

		Collections.sort(list, comparator);

		for (SummaryLine sl : list) {
			out += sl.toCSV() + "\n";
		}

		return out;
	}

	/*
	 * Methods for creating sets of PropsXML objects
	 */
	public static void buildSetGeneral(PropsXML p, PropsXMLBatcher pb) {
		// general training settings
		for (int predict = pb.PREDICT_WINDOW_START; predict <= pb.PREDICT_WINDOW_END; predict += pb.PREDICT_WINDOW_STEP) {
			for (int input = pb.INPUT_WINDOW_START; input <= pb.INPUT_WINDOW_END; input += pb.INPUT_WINDOW_STEP) {
				for (int hidden1 = pb.HIDDEN1_COUNT_START; hidden1 <= pb.HIDDEN1_COUNT_END; hidden1 += pb.HIDDEN1_COUNT_STEP) {
					for (int hidden2 = pb.HIDDEN2_COUNT_START; hidden2 <= pb.HIDDEN2_COUNT_END; hidden2 += pb.HIDDEN2_COUNT_STEP) {
						for (double thresh = pb.TRAIN_THRESH_START; thresh <= pb.TRAIN_THRESH_END; thresh += pb.TRAIN_THRESH_STEP) {

							// create the PropXML object with deep copy
							PropsXML newP = (PropsXML) p.clone();

							// change required settings for this training method
							// from general
							newP.PREDICT_WINDOW = predict;
							newP.INPUT_WINDOW = input;
							newP.HIDDEN1_COUNT = hidden1;
							newP.HIDDEN2_COUNT = hidden2;
							newP.TRAIN_THRESH = thresh;

							// label creation
							newP.RUN_LABEL += "_p" + predict;
							newP.RUN_LABEL += "_i" + input;
							newP.RUN_LABEL += "_h" + hidden1;
							newP.RUN_LABEL += "_h" + hidden2;
							newP.RUN_LABEL += "_t" + thresh;

							// create PropsXML if enabled
							if (pb.TRAIN_ALG_BPROP) {
								buildSetBPROP(newP, pb);
							}
							if (pb.TRAIN_ALG_RPROP) {
								buildSetRPROP(newP, pb);
							}
							if (pb.TRAIN_ALG_QPROP) {
								buildSetQPROP(newP, pb);
							}
							if (pb.TRAIN_ALG_MPROP) {
								buildSetMPROP(newP, pb);
							}
							if (pb.TRAIN_ALG_LPROP) {
								buildSetLPROP(newP, pb);
							}
							if (pb.TRAIN_ALG_SPROP) {
								buildSetSPROP(newP, pb);
							}
							// skipped genetic
							if (pb.TRAIN_ALG_ANNEAL) {
								buildSetANNEAL(newP, pb);
							}

							// we are not actually using this newP, hint for memory to be freed
							newP = null;
						}
					}
				}
			}
		}
	}

	public static void buildSetBPROP(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "BPROP";

		// actual BPROP settings
		for (double rate = pb.BPROP_LEARNING_RATE_START; rate <= pb.BPROP_LEARNING_RATE_END; rate += pb.BPROP_LEARNING_RATE_STEP) {
			for (double momentum = pb.BPROP_MOMENTUM_STEP; momentum <= pb.BPROP_MOMENTUM_STEP; momentum += pb.BPROP_MOMENTUM_STEP) {

				// create the PropXML object with deep copy
				PropsXML newP = (PropsXML) p.clone();

				// local settings
				newP.TRAIN_ALG = TRAIN_ALG;
				newP.BPROP_LEARNING_RATE = rate;
				newP.BPROP_MOMENTUM = momentum;

				// label creation
				newP.RUN_LABEL += "__" + TRAIN_ALG;
				newP.RUN_LABEL += "_r" + rate;
				newP.RUN_LABEL += "_m" + momentum;

				// add to list for processing
				propsList.add(newP);
			}
		}
	}

	public static void buildSetRPROP(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "RPROP";

		// actual RPROP settings
		for (int i = 0; i < pb.RPROP_TYPEs.length; i++) {

			// create the PropXML object with deep copy
			PropsXML newP = (PropsXML) p.clone();

			// local settings
			newP.TRAIN_ALG = TRAIN_ALG;
			newP.RPROP_TYPE = pb.RPROP_TYPEs[i];

			// create explicit run label
			newP.RUN_LABEL += "__" + TRAIN_ALG;
			newP.RUN_LABEL += "_t" + pb.RPROP_TYPEs[i];

			// add to list for processing
			propsList.add(newP);
		}
	}

	public static void buildSetQPROP(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "QPROP";

		// actual QPROP settings
		for (double rate = pb.QPROP_LEARNING_RATE_START; rate <= pb.QPROP_LEARNING_RATE_END; rate += pb.QPROP_LEARNING_RATE_STEP) {

			// create the PropXML object with deep copy
			PropsXML newP = (PropsXML) p.clone();

			// local settings
			newP.TRAIN_ALG = TRAIN_ALG;
			newP.QPROP_LEARNING_RATE = rate;

			// create explicit run label
			newP.RUN_LABEL += "__" + TRAIN_ALG;
			newP.RUN_LABEL += "_r" + rate;

			// add to list for processing
			propsList.add(newP);
		}
	}

	public static void buildSetMPROP(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "MPROP";

		// actual MPROP settings
		for (double rate = pb.MPROP_LEARNING_RATE_START; rate <= pb.MPROP_LEARNING_RATE_END; rate += pb.MPROP_LEARNING_RATE_STEP) {

			// create the PropXML object with deep copy
			PropsXML newP = (PropsXML) p.clone();

			// local settings
			newP.TRAIN_ALG = TRAIN_ALG;
			newP.MPROP_LEARNING_RATE = rate;

			// create explicit run label
			newP.RUN_LABEL += "__" + TRAIN_ALG;
			newP.RUN_LABEL += "_r" + rate;

			// add to list for processing
			propsList.add(newP);
		}
	}

	public static void buildSetLPROP(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "LPROP";

		// actual LPROP settings
		// none for now

		// create the PropXML object with deep copy
		PropsXML newP = (PropsXML) p.clone();

		// local settings
		newP.TRAIN_ALG = TRAIN_ALG;

		// create explicit run label
		newP.RUN_LABEL += "__" + TRAIN_ALG;

		// add to list for processing
		propsList.add(newP);
	}

	public static void buildSetSPROP(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "SPROP";

		// actual SPROP settings
		for (double rate = pb.SPROP_LEARNING_RATE_START; rate <= pb.SPROP_LEARNING_RATE_END; rate += pb.SPROP_LEARNING_RATE_STEP) {

			// create the PropXML object with deep copy
			PropsXML newP = (PropsXML) p.clone();

			// local settings
			newP.TRAIN_ALG = TRAIN_ALG;
			newP.SPROP_LEARNING_RATE = rate;

			// create explicit run label
			newP.RUN_LABEL += "__" + TRAIN_ALG;
			newP.RUN_LABEL += "_r" + rate;

			// add to list for processing
			propsList.add(newP);
		}
	}

	// skipped genetic

	public static void buildSetANNEAL(PropsXML p, PropsXMLBatcher pb) {
		String TRAIN_ALG = "ANNEAL";

		// actual ANNEAL settings
		for (double startTemp = pb.ANNEAL_START_TEMP_START; startTemp <= pb.ANNEAL_START_TEMP_END; startTemp += pb.ANNEAL_START_TEMP_STEP) {
			for (double stopTemp = pb.ANNEAL_STOP_TEMP_START; stopTemp <= pb.ANNEAL_STOP_TEMP_END; stopTemp += pb.ANNEAL_STOP_TEMP_STEP) {
				for (int cycles = pb.ANNEAL_CYCLES_START; cycles <= pb.ANNEAL_CYCLES_END; cycles += pb.ANNEAL_CYCLES_STEP) {

					// create the PropXML object with deep copy
					PropsXML newP = (PropsXML) p.clone();

					// local settings
					newP.TRAIN_ALG = TRAIN_ALG;
					newP.ANNEAL_START_TEMP = startTemp;
					newP.ANNEAL_STOP_TEMP = stopTemp;
					newP.ANNEAL_CYCLES = cycles;

					// create explicit run label
					newP.RUN_LABEL += "__" + TRAIN_ALG;
					newP.RUN_LABEL += "_a" + startTemp;
					newP.RUN_LABEL += "_z" + stopTemp;
					newP.RUN_LABEL += "_c" + cycles;

					// add to list for processing
					propsList.add(newP);
				}
			}
		}
	}

	/*
	 * Helper methods
	 */

}
