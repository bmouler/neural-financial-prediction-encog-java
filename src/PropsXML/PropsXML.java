package PropsXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PropsXML {

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

	// debug printing
	public int DEBUG_LEVEL;

	// data files
	public String PREDICT_MODEL;
	public String[] DATA_FILES;
	public boolean DATA_NEEDS_CLEANING;
	public boolean DATA_NEEDS_NORMALIZATION;

	// temporal settings
	public double NORMALIZED_LOW;
	public double NORMALIZED_HIGH;
	public int INPUT_WINDOW_SIZE;
	public int PREDICT_WINDOW_SIZE;
	public int[] TIME_LAGS;

	// training
	public String ACTIVATION_FUNCTION;
	public double TARGET_ERROR;

	// predict
	public String PREDICT_FILE;
	public String PREDICT_LABEL;

	// output
	public boolean PRINT_DENORMALIZED;

	public void InitializeValues() {
		// debug printing
		DEBUG_LEVEL = GetInt("DEBUG_LEVEL");

		// data files
		PREDICT_MODEL = GetString("PREDICT_MODEL"); // TODO not used yet
		DATA_FILES = GetArrayOfStrings(DEBUG_LEVEL, "DATA_FILE_");
		DATA_NEEDS_CLEANING = GetBool("DATA_NEEDS_CLEANING");
		DATA_NEEDS_NORMALIZATION = GetBool("DATA_NEEDS_NORMALIZATION");

		// temporal settings
		NORMALIZED_LOW = GetDouble("NORMALIZED_LOW");
		NORMALIZED_HIGH = GetDouble("NORMALIZED_HIGH");
		INPUT_WINDOW_SIZE = GetInt("INPUT_WINDOW_SIZE");
		PREDICT_WINDOW_SIZE = GetInt("PREDICT_WINDOW_SIZE");
		TIME_LAGS = GetArrayOfInts(DEBUG_LEVEL, "TIME_LAG_");
		
		// training
		ACTIVATION_FUNCTION = GetString("ACTIVATION_FUNCTION");
		TARGET_ERROR = GetDouble("TARGET_ERROR");

		// predict
		PREDICT_FILE = GetString("PREDICT_FILE");
		PREDICT_LABEL = GetString("PREDICT_LABEL");

		// output
		PRINT_DENORMALIZED = GetBool("PRINT_DENORMALIZED");
	}

	// constructor
	public PropsXML(String WORKING_DIR, String PROPS_FILE) throws Exception {
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
			if (DEBUG_LEVEL >= 1) {
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

			if (DEBUG_LEVEL >= 1) {
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
