package PropsXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class Props {

	static Properties props = null;

	/*
	 * Reads properties from an xml file.
	 * Adds the following keys:
	 *   WORKING_DIR
	 *   PROPS_FILE
	 *   PROPS_FILE_PATH: WORKING_DIR + "/" + PROPS_FILE
	 */
	public static Properties LoadProps(String WORKING_DIR, String PROPS_FILE) throws Exception {
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
			int DEBUG_LEVEL = GetInt(props, "DEBUG_LEVEL");
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

		return props;
	}

	static public String[] GetArrayOfStrings(int DEBUG_LEVEL, Properties props, String label) {
		List<String> listOfStrings = new ArrayList<String>();

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

		return listOfStrings.toArray(new String[listOfStrings.size()]);
	}

	static public List<String> GetListOfStrings(int DEBUG_LEVEL, Properties props, String label) {
		List<String> listOfStrings = new ArrayList<String>();

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

		return listOfStrings;
	}

	static public String GetString(Properties props, String label) {
		return props.getProperty(label);
	}

	static public boolean GetBool(Properties props, String label) {
		String value = props.getProperty(label);

		if (value.equals("true") || value.equals("TRUE")) {
			return true;
		}

		return false;
	}

	static public int GetInt(Properties props, String label) {
		String value = props.getProperty(label);

		return Integer.parseInt(value);
	}

	static public float GetFloat(Properties props, String label) {
		String value = props.getProperty(label);

		return Float.parseFloat(value);
	}

	static public double GetDouble(Properties props, String label) {
		String value = props.getProperty(label);

		return Double.parseDouble(value);
	}
}
