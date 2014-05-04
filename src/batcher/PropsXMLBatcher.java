package batcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PropsXMLBatcher {

	/*
	 * Reads properties from an xml file.
	 * Adds the following keys:
	 *   WORKING_DIR
	 *   PROPS_FILE
	 *   PROPS_FILE_PATH: WORKING_DIR + "/" + PROPS_FILE
	 */

	Properties props = null;

	/*
	 * !! ADD NEW PROPERTIES HERE and load them in InitializeValues()
	 */

	// Debug printing
	public int DEBUG_LEVEL;
	
	// Batching settings
	public String BATCH_RUN_LABEL;
	public int MTM_DEBUG_LEVEL;
	public int MTM_MAX_THREADS_ACTIVE;
	public long MTM_UPDATE_DELAY_IN_MILLI;
	public boolean MTM_KILL_IF_LONG_RUNNING;
	public long MTM_KILL_TIME_IN_MILLI;

	//Network setup parameters -->
	public int PREDICT_WINDOW_STEP;
	public int PREDICT_WINDOW_START;
	public int PREDICT_WINDOW_END;
	public int INPUT_WINDOW_STEP;
	public int INPUT_WINDOW_START;
	public int INPUT_WINDOW_END;
	public int HIDDEN1_COUNT_STEP;
	public int HIDDEN1_COUNT_START;
	public int HIDDEN1_COUNT_END;
	public int HIDDEN2_COUNT_STEP;
	public int HIDDEN2_COUNT_START;
	public int HIDDEN2_COUNT_END;

	//Training algorithm and properties -->
	public double TRAIN_THRESH_STEP;
	public double TRAIN_THRESH_START;
	public double TRAIN_THRESH_END;

	//Back Propagation -->
	public boolean TRAIN_ALG_BPROP;
	public double BPROP_LEARNING_RATE_STEP;
	public double BPROP_LEARNING_RATE_START;
	public double BPROP_LEARNING_RATE_END;
	public double BPROP_MOMENTUM_STEP;
	public double BPROP_MOMENTUM_START;
	public double BPROP_MOMENTUM_END;

	//Resilient Propagation -->
	public boolean TRAIN_ALG_RPROP;
	public String[] RPROP_TYPEs;

	//Quick Propagation -->
	public boolean TRAIN_ALG_QPROP;
	public double QPROP_LEARNING_RATE_STEP;
	public double QPROP_LEARNING_RATE_START;
	public double QPROP_LEARNING_RATE_END;

	//Manhattan Propagation -->
	public boolean TRAIN_ALG_MPROP;
	public double MPROP_LEARNING_RATE_STEP;
	public double MPROP_LEARNING_RATE_START;
	public double MPROP_LEARNING_RATE_END;

	//Levenberg Marquardt Propagation -->
	public boolean TRAIN_ALG_LPROP;

	//Scaled Conjugate Gradient Propagation -->
	public boolean TRAIN_ALG_SPROP;
	public double SPROP_LEARNING_RATE_STEP;
	public double SPROP_LEARNING_RATE_START;
	public double SPROP_LEARNING_RATE_END;

	//Genetic Algorithm -->
	//public TRAIN_ALG">GENETIC</entry> -->

	//Simulated Annealing -->
	public boolean TRAIN_ALG_ANNEAL;
	public double ANNEAL_START_TEMP_STEP;
	public double ANNEAL_START_TEMP_START;
	public double ANNEAL_START_TEMP_END;
	public double ANNEAL_STOP_TEMP_STEP;
	public double ANNEAL_STOP_TEMP_START;
	public double ANNEAL_STOP_TEMP_END;
	public int ANNEAL_CYCLES_STEP;
	public int ANNEAL_CYCLES_START;
	public int ANNEAL_CYCLES_END;
	
	public void InitializeValues() {

		// Debug printing
		DEBUG_LEVEL = GetInt("DEBUG_LEVEL");
		
		// Batching settings
		BATCH_RUN_LABEL = GetString("BATCH_RUN_LABEL");
		MTM_DEBUG_LEVEL = GetInt("MTM_DEBUG_LEVEL");
		MTM_MAX_THREADS_ACTIVE = GetInt("MTM_MAX_THREADS_ACTIVE");
		MTM_UPDATE_DELAY_IN_MILLI = GetLong("MTM_UPDATE_DELAY_IN_MILLI");
		MTM_KILL_IF_LONG_RUNNING = GetBool("MTM_KILL_IF_LONG_RUNNING");
		MTM_KILL_TIME_IN_MILLI = GetLong("MTM_KILL_TIME_IN_MILLI");

		//Network setup parameters -->
		PREDICT_WINDOW_STEP = GetInt("PREDICT_WINDOW_STEP");
		PREDICT_WINDOW_START = GetInt("PREDICT_WINDOW_START");
		PREDICT_WINDOW_END = GetInt("PREDICT_WINDOW_END");
		INPUT_WINDOW_STEP = GetInt("INPUT_WINDOW_STEP");
		INPUT_WINDOW_START = GetInt("INPUT_WINDOW_START");
		INPUT_WINDOW_END = GetInt("INPUT_WINDOW_END");
		HIDDEN1_COUNT_STEP = GetInt("HIDDEN1_COUNT_STEP");
		HIDDEN1_COUNT_START = GetInt("HIDDEN1_COUNT_START");
		HIDDEN1_COUNT_END = GetInt("HIDDEN1_COUNT_END");
		HIDDEN2_COUNT_STEP = GetInt("HIDDEN2_COUNT_STEP");
		HIDDEN2_COUNT_START = GetInt("HIDDEN2_COUNT_START");
		HIDDEN2_COUNT_END = GetInt("HIDDEN2_COUNT_END");

		//Training algorithm and properties -->
		TRAIN_THRESH_STEP = GetDouble("TRAIN_THRESH_STEP");
		TRAIN_THRESH_START = GetDouble("TRAIN_THRESH_START");
		TRAIN_THRESH_END = GetDouble("TRAIN_THRESH_END");

		//Back Propagation -->
		TRAIN_ALG_BPROP = GetBool("TRAIN_ALG_BPROP");
		BPROP_LEARNING_RATE_STEP = GetDouble("BPROP_LEARNING_RATE_STEP");
		BPROP_LEARNING_RATE_START = GetDouble("BPROP_LEARNING_RATE_START");
		BPROP_LEARNING_RATE_END = GetDouble("BPROP_LEARNING_RATE_END");
		BPROP_MOMENTUM_STEP = GetDouble("BPROP_MOMENTUM_STEP");
		BPROP_MOMENTUM_START = GetDouble("BPROP_MOMENTUM_START");
		BPROP_MOMENTUM_END = GetDouble("BPROP_MOMENTUM_END");

		//Resilient Propagation -->
		TRAIN_ALG_RPROP = GetBool("TRAIN_ALG_RPROP");
		RPROP_TYPEs = GetArrayOfStrings(DEBUG_LEVEL, "RPROP_TYPE_");
		
		//Quick Propagation -->
		TRAIN_ALG_QPROP = GetBool("TRAIN_ALG_QPROP");
		QPROP_LEARNING_RATE_STEP = GetDouble("QPROP_LEARNING_RATE_STEP");
		QPROP_LEARNING_RATE_START = GetDouble("QPROP_LEARNING_RATE_START");
		QPROP_LEARNING_RATE_END = GetDouble("QPROP_LEARNING_RATE_END");

		//Manhattan Propagation -->
		TRAIN_ALG_MPROP = GetBool("TRAIN_ALG_MPROP");
		MPROP_LEARNING_RATE_STEP = GetDouble("MPROP_LEARNING_RATE_STEP");
		MPROP_LEARNING_RATE_START = GetDouble("MPROP_LEARNING_RATE_START");
		MPROP_LEARNING_RATE_END = GetDouble("MPROP_LEARNING_RATE_END");

		//Levenberg Marquardt Propagation -->
		TRAIN_ALG_LPROP = GetBool("TRAIN_ALG_LPROP");

		//Scaled Conjugate Gradient Propagation -->
		TRAIN_ALG_SPROP = GetBool("TRAIN_ALG_SPROP");
		SPROP_LEARNING_RATE_STEP = GetDouble("SPROP_LEARNING_RATE_STEP");
		SPROP_LEARNING_RATE_START = GetDouble("SPROP_LEARNING_RATE_START");
		SPROP_LEARNING_RATE_END = GetDouble("SPROP_LEARNING_RATE_END");

		//Genetic Algorithm -->
		//public TRAIN_ALG">GENETIC</entry> -->

		//Simulated Annealing -->
		TRAIN_ALG_ANNEAL = GetBool("TRAIN_ALG_ANNEAL");
		ANNEAL_START_TEMP_STEP = GetDouble("ANNEAL_START_TEMP_STEP");
		ANNEAL_START_TEMP_START = GetDouble("ANNEAL_START_TEMP_START");
		ANNEAL_START_TEMP_END = GetDouble("ANNEAL_START_TEMP_END");
		ANNEAL_STOP_TEMP_STEP = GetDouble("ANNEAL_STOP_TEMP_STEP");
		ANNEAL_STOP_TEMP_START = GetDouble("ANNEAL_STOP_TEMP_START");
		ANNEAL_STOP_TEMP_END = GetDouble("ANNEAL_STOP_TEMP_END");
		ANNEAL_CYCLES_STEP = GetInt("ANNEAL_CYCLES_STEP");
		ANNEAL_CYCLES_START = GetInt("ANNEAL_CYCLES_START");
		ANNEAL_CYCLES_END = GetInt("ANNEAL_CYCLES_END");
		
	}

	// constructor
	public PropsXMLBatcher(String WORKING_DIR, String PROPS_FILE) throws Exception {
		props = new Properties();

		try {
			String PROPS_FILE_PATH = WORKING_DIR + "/" + PROPS_FILE;
			File file = new File(WORKING_DIR + "/" + PROPS_FILE);
			FileInputStream fileInput = new FileInputStream(file);
			props.loadFromXML(fileInput);
			fileInput.close();

			// add workingDir so we do not need to include it in the config.properties
			props.put("WORKING_DIR", WORKING_DIR);
			props.put("PROPS_FILE", PROPS_FILE);
			props.put("PROPS_FILE_PATH", PROPS_FILE_PATH);

			// print properties to console
			int DEBUG_LEVEL = GetInt("DEBUG_LEVEL");
			if (DEBUG_LEVEL > 1) {
				System.out.println("======");
				System.out.println("Properties File found. Configs are:\n");
				Enumeration<Object> enuKeys = props.keys();
				while (enuKeys.hasMoreElements()) {
					String key = (String) enuKeys.nextElement();
					String value = props.getProperty(key);
					System.out.println(key + ": " + value);
				}
				System.out.println("======");
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception("Error while attempting to read config.xml: file not found.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Error while attempting to read config.xml: io exception.");
		}

		// populate the values declared in..
		InitializeValues();

		// all values are now accessible by referring to this object.VALUE
	}

	public String[] GetArrayOfStrings(int DEBUG_LEVEL, String label) {
		List<String> listOfStrings = ListOfStringsWork(DEBUG_LEVEL, label);
		return listOfStrings.toArray(new String[listOfStrings.size()]);
	}

	public int[] GetArrayOfInts(int DEBUG_LEVEL, String label) {
		String[] strVals = GetArrayOfStrings(DEBUG_LEVEL, label);
		int[] intVals = new int[strVals.length];
		for (int i = 0; i < strVals.length; ++i)
			intVals[i] = Integer.parseInt(strVals[i]);
		return intVals;
	}

	public boolean[] GetArrayOfBooleans(int DEBUG_LEVEL, String label) {
		String[] strVals = GetArrayOfStrings(DEBUG_LEVEL, label);
		boolean[] intVals = new boolean[strVals.length];
		for (int i = 0; i < strVals.length; ++i)
			intVals[i] = Boolean.parseBoolean(strVals[i]);
		return intVals;
	}

	public List<String> GetListOfStrings(int DEBUG_LEVEL, String label) {
		return ListOfStringsWork(DEBUG_LEVEL, label);
	}

	private List<String> ListOfStringsWork(int DEBUG_LEVEL, String label) {
		List<String> listOfStrings = new ArrayList<String>();

		try {
			Enumeration<Object> enuKeys = props.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				if (key.startsWith(label)) {
					listOfStrings.add(props.getProperty(key));
				}
			}

			if (DEBUG_LEVEL > 1) {
				System.out.println("======");
				System.out.println("List for '" + label + "' is:");

				for (String s : listOfStrings) {
					System.out.println(s);
				}

				System.out.println("======");

			}

		} catch (Exception ex) {
			configReadError("list of Strings", label + "#", ex);
		}

		return listOfStrings;
	}

	public String GetString(String label) {
		String result = null;

		try {
			result = props.getProperty(label);
		} catch (Exception ex) {
			configReadError("String", label, ex);
		}
		return result;
	}

	public boolean GetBool(String label) {
		try {
			String value = props.getProperty(label);

			if (value.equals("true") || value.equals("TRUE")) {
				return true;
			} else if (value.equals("false") || value.equals("FALSE")) {
				//nothing, proceed to end
			} else {
				configParseError("boolean", label, new Exception() );
			}
		} catch (Exception ex) {
			configReadError("boolean", label, ex);
		}

		return false;
	}

	public int GetInt(String label) {
		int result = -1;

		try {
			String value = props.getProperty(label);

			try {
			result = Integer.parseInt(value);
			} catch (Exception ex) {
				configParseError("int", label, ex);
			}
		} catch (Exception ex) {
			configReadError("int", label, ex);
		}

		return result;
	}
	
	public long GetLong(String label) {
		long result = -1;

		try {
			String value = props.getProperty(label);

			try {
			result = Long.parseLong(value);
			} catch (Exception ex) {
				configParseError("long", label, ex);
			}
		} catch (Exception ex) {
			configReadError("long", label, ex);
		}

		return result;
	}


	public float GetFloat(String label) {
		float result = -1f;

		try {
			String value = props.getProperty(label);

			try {
				result = Float.parseFloat(value);
			} catch (Exception ex) {
				configParseError("float", label, ex);
			}
		} catch (Exception ex) {
			configReadError("float", label, ex);
		}

		return result;
	}

	public double GetDouble(String label) {
		double result = -1;

		try {
			String value = props.getProperty(label);

			try {
				result = Double.parseDouble(value);
			} catch (Exception ex) {
				configParseError("double", label, ex);
			}
		} catch (Exception ex) {
			configReadError("double", label, ex);
		}

		return result;
	}

	private void configReadError(String type, String label, Exception ex) {
		System.out.println("\n\nPropsXML Error:");
		System.out.println("!! Missing mandatory config field (" + type + "): '" + label + "'");
		System.out.println("!! See default config at ./Configs/DEFAULT.xml");
		System.out.println("\n\n");
		ex.printStackTrace();
		System.exit(-1);
	}

	private void configParseError(String type, String label, Exception ex) {
		System.out.println("\n\nPropsXML Error:");
		System.out.println("!! Failed to parse (" + type + ") from field: '" + label + "'");
		if (type.equals("boolean")) {
			System.out.println("!! Accepted values are: true, TRUE, false, FALSE");
		}
		System.out.println("!! See default config at ./Configs/DEFAULT.xml");
		System.out.println("\n\n");
		ex.printStackTrace();
		System.exit(-1);
	}
}
