package DataIngester;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.encog.engine.network.activation.ActivationBiPolar;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.temporal.TemporalDataDescription;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.arrayutil.NormalizeArray;

public class DataIngester {
	// Number of data series in all files collectively
	int m_numDataSeries;

	// Names of data series
	private String[] m_dataNames;

	public int getPredictFieldIndex(String PREDICT_FILE, String PREDICT_LABEL) {
		int predictedField = -1;
		for (int i = 0; i < m_dataNames.length; i++) {
			if (m_dataNames[i].startsWith(PREDICT_FILE) && m_dataNames[i].endsWith(PREDICT_LABEL)) {
				predictedField = i;
				continue;
			}
		}
		return predictedField;
	}

	public int getPredictFieldIndex(String PREDICT_LABEL) {
		int predictedField = -1;
		for (int i = 0; i < m_dataNames.length; i++) {
			if (m_dataNames[i].equals(PREDICT_LABEL)) {
				predictedField = i;
				continue;
			}
		}
		return predictedField;
	}

	// Dates common to all data series
	private String[] m_dates;

	// Data for all data series; leftmost index corresponds to data series
	// number
	private double[][] m_data;

	public int getNumberOfDataSeries() {
		return m_data.length;
	}

	// Read in data file in Berts format
	public void readBertsData
	(
		int    DEBUG_LEVEL,
		String dataFile,
		int[]  desiredTimeLags
	)
	{

		BufferedReader reader = null;
		
		try {
			// Add default time lag of "1 day" if none were input
			if (desiredTimeLags == null || desiredTimeLags.length == 0){
				desiredTimeLags = new int[1];
				desiredTimeLags[0] = 1;
			}
		
			// Find maximum time lag
			int maxTimeLag = desiredTimeLags[0];
			for (int timeLagNum = 0; timeLagNum < desiredTimeLags.length; ++timeLagNum){
				if (desiredTimeLags[timeLagNum] < 1)
					throw new Exception("Time lags must each be > 0");

				if (maxTimeLag < desiredTimeLags[timeLagNum])
					maxTimeLag = desiredTimeLags[timeLagNum];
			}
		
			String[] desiredDataSeriesIDs = new String[3];
			desiredDataSeriesIDs[0] = "target";
			desiredDataSeriesIDs[1] = "percent";
			desiredDataSeriesIDs[2] = "volume";
			
			if (DEBUG_LEVEL >= 1)
				System.out.println("Reading in data in file " + dataFile);

			// Read in all file
			ArrayList<String           > dataNames = new ArrayList<String           >();
			ArrayList<String           > dates     = new ArrayList<String           >();
			ArrayList<ArrayList<Double>> data      = new ArrayList<ArrayList<Double>>();

			reader = new BufferedReader(new FileReader(dataFile));

			String[] headers = reader.readLine().split(",");
			for (int headerNum = 1; headerNum < headers.length; ++headerNum) {
				dataNames.add(headers[headerNum]);
				data.add(new ArrayList<Double>());
			}

			String dataLine;
			while ((dataLine = reader.readLine()) != null) {
				String[] dataLineVals = dataLine.split(",");
	
				String[] datePieces = dataLineVals[0].split("/");
				if (datePieces[0].length() < 2)
					datePieces[0] = "0" + datePieces[0];
				if (datePieces[1].length() < 2)
					datePieces[1] = "0" + datePieces[1];
				dates.add(datePieces[2] + "-" + datePieces[0] + "_" + datePieces[1]);

				for (int dataValNum = 1; dataValNum < dataLineVals.length; ++dataValNum)
					data.get(dataValNum - 1).add(Double.parseDouble(dataLineVals[dataValNum]));
			}

			// Remove undesired data series
			int dataSeriesNum = 0;
			while (dataSeriesNum < dataNames.size()) {
				boolean isDesiredDataSeries = false;
				for (int desiredDataSeriesNameNum = 0; desiredDataSeriesNameNum < desiredDataSeriesIDs.length; ++desiredDataSeriesNameNum)
					if (dataNames.get(dataSeriesNum).contains(desiredDataSeriesIDs[desiredDataSeriesNameNum])) {
						isDesiredDataSeries = true;
						break;
					}

				if (isDesiredDataSeries == false) {
					dataNames.remove(dataSeriesNum);
					data     .remove(dataSeriesNum);
				}
				else
					++dataSeriesNum;
			}

			// Make various time-lagged versions of data
			int numDataSeriesPerLag = dataNames.size();
			for (int desiredTimeLagNum = 1; desiredTimeLagNum < desiredTimeLags.length; ++desiredTimeLagNum)
			{
				int numNonTargetDataSeries = 0;
				for (dataSeriesNum = 0; dataSeriesNum< numDataSeriesPerLag; ++dataSeriesNum)
				{
					if (dataNames.get(dataSeriesNum).contains(desiredDataSeriesIDs[0]))
						continue;
					
					dataNames.add(dataNames.get(dataSeriesNum));
					data.add(new ArrayList<Double>());
					for (int dateNum = 0; dateNum < dates.size(); ++dateNum)
					{
						data.get(numDataSeriesPerLag + (desiredTimeLagNum - 1)*(numDataSeriesPerLag - 1) + numNonTargetDataSeries).add(data.get(dataSeriesNum).get(dateNum));
					}
					++numNonTargetDataSeries;
				}
			}
			
			// Move "target" data to beginning for convenience
			int targetDataSeriesNum = 0;
			while (dataNames.get(targetDataSeriesNum).contains(desiredDataSeriesIDs[0]) == false)
				++targetDataSeriesNum;
			
			dataNames.add(0, dataNames.get(targetDataSeriesNum));
			data     .add(0, data     .get(targetDataSeriesNum));

			dataNames.remove(targetDataSeriesNum + 1);
			data     .remove(targetDataSeriesNum + 1);
			
			for (int dataNum = 0; dataNum < maxTimeLag - 1; ++dataNum)
			{
				dates      .remove(0);
				data.get(0).remove(0);
			}
			
			for (int desiredTimeLagNum = 0; desiredTimeLagNum < desiredTimeLags.length; ++desiredTimeLagNum)
			{
				for (dataSeriesNum = 0; dataSeriesNum < numDataSeriesPerLag - 1; ++dataSeriesNum)
				{
					dataNames.set(1 + desiredTimeLagNum*(numDataSeriesPerLag - 1) + dataSeriesNum,
							dataNames.get(1 + desiredTimeLagNum*(numDataSeriesPerLag - 1) + dataSeriesNum) + "_delay" + desiredTimeLags[desiredTimeLagNum]);
					
					for (int dataNum = 0; dataNum < desiredTimeLags[desiredTimeLagNum] - 1; ++dataNum)
					{
						data.get(1 + desiredTimeLagNum*(numDataSeriesPerLag - 1) + dataSeriesNum).remove(data.get(1 + desiredTimeLagNum*(numDataSeriesPerLag - 1) + dataSeriesNum).size() - 1);
					}		

					for (int dataNum = 0; dataNum < maxTimeLag - desiredTimeLags[desiredTimeLagNum]; ++dataNum)
					{
						data.get(1 + desiredTimeLagNum*(numDataSeriesPerLag - 1) + dataSeriesNum).remove(0);
					}		
				}
			}
			
			// Copy over to old-fashioned arrays for Encog usage
			m_numDataSeries = dataNames.size();

			m_dataNames = new String[dataNames.size()];
			m_dates     = new String[dates.size()];
			m_data      = new double[dataNames.size()][dates.size()];

			for (dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
				m_dataNames[dataSeriesNum] = dataNames.get(dataSeriesNum);
				for (int dateNum = 0; dateNum < dates.size(); ++dateNum) {
					if (dataSeriesNum == 0)
						m_dates[dateNum] = dates.get(dateNum);
					m_data[dataSeriesNum][dateNum] = data.get(dataSeriesNum).get(dateNum);
				}
			}

			if (DEBUG_LEVEL >= 2) {
				System.out.printf("  Number of valid data sets found                = %d\n", m_numDataSeries);
				System.out.printf("  Number of data points in all valid datasets    = %d\n", m_dates.length);
				System.out.printf("  Least recent date common to all valid datasets = %s\n", m_dates[0]);
				System.out.printf("  Most recent date common to all valid datasets  = %s\n", m_dates[m_dates.length - 1]);
				for (int dataNameNum = 0; dataNameNum < m_numDataSeries; ++dataNameNum)
					System.out.printf("    Name of valid data series %2d = \"%s\"\n", dataNameNum, m_dataNames[dataNameNum]);
			}
			if (DEBUG_LEVEL >= 3) {
				System.out.println("  Full list of data:");
				for (int dateNum = 0; dateNum < dates.size(); ++dateNum) {
					System.out.printf("      %6d : %s", dateNum, m_dates[dateNum]);
					for (int dataNameNum = 0; dataNameNum < m_numDataSeries; ++dataNameNum)
						System.out.printf("  %12.2f", m_data[dataNameNum][dateNum]);
					System.out.printf("\n");
				}
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	// Read in, temporally sort in chronologically-increasing order, and remove
	// any data points taken at dates not common to all data series
	public void readData(int DEBUG_LEVEL, String[] dataFiles, boolean DATA_NEEDS_CLEANING) {
		BufferedReader reader = null;

		try {
			if (DEBUG_LEVEL >= 1)
				System.out.printf("Reading in data in %d files...\n", dataFiles.length);

			// Count number of data series in all data files and get header
			// name of each data series
			m_numDataSeries = 0;
			int[] numDataSeriesPerFile = new int[dataFiles.length];
			String[][] headerPieces = new String[dataFiles.length][];
			for (int dataFileNameNum = 0; dataFileNameNum < dataFiles.length; ++dataFileNameNum) {
				reader = new BufferedReader(new FileReader(dataFiles[dataFileNameNum]));

				String[] allHeaders = reader.readLine().split(",");
				headerPieces[dataFileNameNum] = new String[allHeaders.length - 1];
				for (int headerNum = 0; headerNum < allHeaders.length - 1; ++headerNum) {
					headerPieces[dataFileNameNum][headerNum] = allHeaders[headerNum + 1];
				}

				numDataSeriesPerFile[dataFileNameNum] = allHeaders.length - 1;
				m_numDataSeries += numDataSeriesPerFile[dataFileNameNum];
			}

			if (DEBUG_LEVEL >= 1)
				System.out.printf("Total of %d data series found\n", m_numDataSeries);

			// Construct name of each data series
			m_dataNames = new String[m_numDataSeries];

			m_numDataSeries = 0;
			for (int dataFileNameNum = 0; dataFileNameNum < dataFiles.length; ++dataFileNameNum) {
				String[] fileNamePieces = dataFiles[dataFileNameNum].split("/");
				fileNamePieces = (fileNamePieces[fileNamePieces.length - 1]).split("\\.");
				String fileName = fileNamePieces[0];

				for (int dataSetNumInFile = 0; dataSetNumInFile < numDataSeriesPerFile[dataFileNameNum]; ++dataSetNumInFile) {
					m_dataNames[m_numDataSeries + dataSetNumInFile] = fileName + "_"
							+ headerPieces[dataFileNameNum][dataSetNumInFile];
				}

				m_numDataSeries += numDataSeriesPerFile[dataFileNameNum];
			}

			// Read in dates and data
			ArrayList<ArrayList<String>> dateLists = new ArrayList<ArrayList<String>>(
					m_numDataSeries);
			ArrayList<ArrayList<Double>> dataLists = new ArrayList<ArrayList<Double>>(
					m_numDataSeries);
			for (int dataSetNum = 0; dataSetNum < m_numDataSeries; ++dataSetNum) {
				dateLists.add(new ArrayList<String>());
				dataLists.add(new ArrayList<Double>());
			}

			m_numDataSeries = 0;
			for (int dataFileNameNum = 0; dataFileNameNum < dataFiles.length; ++dataFileNameNum) {
				reader = new BufferedReader(new FileReader(dataFiles[dataFileNameNum]));
				reader.readLine();

				String line;
				while ((line = reader.readLine()) != null) {
					String[] linePieces = line.split(",");

					// Ignore data lines that do not match header
					if (linePieces.length != headerPieces[dataFileNameNum].length + 1) {
						continue;
					}

					for (int dataSeriesNumInFile = 0; dataSeriesNumInFile < numDataSeriesPerFile[dataFileNameNum]; ++dataSeriesNumInFile) {
						// Prepend either a "19" or "20" onto dates in order to
						// make them 4 digits long
						String[] datePieces = linePieces[0].split("/");
						int year = Integer.parseInt(datePieces[2]);
						if (year < 100) { // not a four digit year
							if (year <= 15) {
								datePieces[2] = "20" + datePieces[2];
							} else {
								datePieces[2] = "19" + datePieces[2];
							}
						}

						// prepend zeros to months and days for consistency
						String month = datePieces[0];
						if (month.length() < 2) {
							datePieces[0] = "0" + datePieces[0];
						}
						String day = datePieces[1];
						if (day.length() < 2) {
							datePieces[1] = "0" + datePieces[1];
						}

						// Invert date into a sortable format
						String date = datePieces[2] + "-" + datePieces[0] + "-" + datePieces[1];

						// Add date and datum to lists
						dateLists.get(m_numDataSeries + dataSeriesNumInFile).add(date);
						dataLists.get(m_numDataSeries + dataSeriesNumInFile).add(
								Double.parseDouble(linePieces[dataSeriesNumInFile + 1]));
					}
				}

				m_numDataSeries += numDataSeriesPerFile[dataFileNameNum];
			}

			// Sort each data series in chronologically-increasing order
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
				ArrayList<DataPoint> dataPointLists = new ArrayList<DataPoint>();
				for (int dateNum = 0; dateNum < dateLists.get(dataSeriesNum).size(); ++dateNum) {
					dataPointLists.add(new DataPoint(dateLists.get(dataSeriesNum).get(dateNum),
							dataLists.get(dataSeriesNum).get(dateNum)));
				}

				Collections.sort(dataPointLists);

				dateLists.get(dataSeriesNum).clear();
				dataLists.get(dataSeriesNum).clear();
				for (int dateNum = 0; dateNum < dataPointLists.size(); ++dateNum) {
					dateLists.get(dataSeriesNum).add(dataPointLists.get(dateNum).getDate());
					dataLists.get(dataSeriesNum).add(dataPointLists.get(dateNum).getDatum());
				}
			}

			// Remove any data entries taken at dates before that of most
			// recent data series
			if (DEBUG_LEVEL >= 1)
				System.out.printf("Removing data at times not common to all %d data series...\n",
						m_numDataSeries);

			String maxMinDate = dateLists.get(0).get(0);
			for (int dataSeriesNum = 1; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
				if (maxMinDate.compareTo(dateLists.get(dataSeriesNum).get(0)) < 0) {
					maxMinDate = dateLists.get(dataSeriesNum).get(0);
				}
			}

			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
				while (dateLists.get(dataSeriesNum).get(0).compareTo(maxMinDate) < 0) {
					dateLists.get(dataSeriesNum).remove(0);
					dataLists.get(dataSeriesNum).remove(0);
				}
			}

			// Remove any data for date entries not common to all data sets
			if (DATA_NEEDS_CLEANING) {
				for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
					if (DEBUG_LEVEL >= 1)
						System.out.printf("  %2d out of %d\n", dataSeriesNum + 1, m_numDataSeries);

					// Loop through each date element in each series
					int dateNum = 0;
					while (dateNum < dateLists.get(dataSeriesNum).size()) {
						// For convenience, grab current looked-for date
						String date = dateLists.get(dataSeriesNum).get(dateNum);

						// Check if date appears in all other data series or not
						boolean dateIsInAllOtherDataSeries = true;
						for (int otherDataSeriesNum = 0; otherDataSeriesNum < m_numDataSeries; ++otherDataSeriesNum) {
							if (dataSeriesNum == otherDataSeriesNum) {
								continue;
							}

							if (!dateLists.get(otherDataSeriesNum).contains(date)) {
								dateIsInAllOtherDataSeries = false;
								break;
							}
						}

						if (dateIsInAllOtherDataSeries == false) {
							dateLists.get(dataSeriesNum).remove((int) dateNum);
							dataLists.get(dataSeriesNum).remove((int) dateNum);
						} else {
							++dateNum;
						}
					}
				}
			}

			// Copy over to old-fashioned arrays for Encog usage
			m_dates = new String[dateLists.get(0).size()];
			for (int dateNum = 0; dateNum < dateLists.get(0).size(); ++dateNum) {
				m_dates[dateNum] = dateLists.get(0).get(dateNum);
			}

			m_data = new double[m_numDataSeries][dateLists.get(0).size()];
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
				for (int dateNum = 0; dateNum < dateLists.get(0).size(); ++dateNum) {
					m_data[dataSeriesNum][dateNum] = dataLists.get(dataSeriesNum).get(dateNum);
				}
			}

			if (DEBUG_LEVEL >= 2)
				System.out.printf("Num data points common to all %d datasets   = %d\n",
						m_numDataSeries, m_dates.length);
			if (DEBUG_LEVEL >= 2)
				System.out.printf("Least recent date common to all %d datasets = %s\n",
						m_numDataSeries, m_dates[0]);
			if (DEBUG_LEVEL >= 2)
				System.out.printf("Most recent date common to all %d datasets  = %s\n",
						m_numDataSeries, m_dates[m_dates.length - 1]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// for saving low/high for each data set
	// needed for denormalized printing
	double[] actualLowValues;
	double[] actualHighValues;

	public double[] getActualLowValues() {
		return actualLowValues;
	}

	public double[] getActualHighValues() {
		return actualHighValues;
	}

	// Normalize data to be within -1 and +1 in each data set
	public void normalizeData(int DEBUG_LEVEL, double NORMALIZED_LOW, double NORMALIZED_HIGH) {
		NormalizeArray norm = new NormalizeArray();
		norm.setNormalizedLow(NORMALIZED_LOW);
		norm.setNormalizedHigh(NORMALIZED_HIGH);

		actualLowValues = new double[m_data.length];
		actualHighValues = new double[m_data.length];

		if (DEBUG_LEVEL >= 2) {
			System.out.println("\n\nLow and high actual values for each dataset:");
		}

		for (int dataSetNum = 0; dataSetNum < m_data.length; ++dataSetNum) {
			if (DEBUG_LEVEL >= 2) {
				// Find min and max of data set
				double minVal = m_data[dataSetNum][0];
				double maxVal = m_data[dataSetNum][0];

				for (int dataNum = 0; dataNum < m_dates.length; ++dataNum) {
					if (minVal > m_data[dataSetNum][dataNum]) {
						minVal = m_data[dataSetNum][dataNum];
					}

					if (maxVal < m_data[dataSetNum][dataNum]) {
						maxVal = m_data[dataSetNum][dataNum];
					}
				}

				// save for later reference
				actualLowValues[dataSetNum] = minVal;
				actualHighValues[dataSetNum] = maxVal;

				System.out.printf("  %d : low = %f : high = %f :\n", dataSetNum,
						actualLowValues[dataSetNum], actualHighValues[dataSetNum]);
			}

			m_data[dataSetNum] = norm.process(m_data[dataSetNum]);
		}

	}

	public void createData(int DEBUG_LEVEL, String[] listOfDataFiles, double NORMALIZED_LOW,
			double NORMALIZED_HIGH, boolean DATA_NEEDS_CLEANING, boolean DATA_NEEDS_NORMALIZATION,
			String PREDICT_FILE, String PREDICT_LABEL, int[] TIME_LAGS) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Starting data import");

		// read in and normalize data
		readBertsData(DEBUG_LEVEL, listOfDataFiles[0], TIME_LAGS);
		//readData(DEBUG_LEVEL, listOfDataFiles, DATA_NEEDS_CLEANING);
		if (DATA_NEEDS_NORMALIZATION) {
			normalizeData(DEBUG_LEVEL, NORMALIZED_LOW, NORMALIZED_HIGH);
		}

		// list of data series names printout
		if (DEBUG_LEVEL >= 1) {
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum) {
				System.out.printf("\n  %6d : %s :", dataSeriesNum, m_dataNames[dataSeriesNum]);
			}
		}

		// header for data printout
		if (DEBUG_LEVEL >= 2) {
			System.out.printf("\n\n");
			System.out.printf("     s : YYYY-MM-DD :");
			for (int dataSeriesNum = 0; dataSeriesNum < m_data.length; ++dataSeriesNum) {
				System.out.printf(" %5.2f", (double) dataSeriesNum);
			}
			System.out.printf("\n");
		}

		// data printout
		if (DEBUG_LEVEL >= 2) {
			for (int dataNum = 0; dataNum < m_dates.length; ++dataNum) {
				System.out.printf("%6d : %s :", dataNum, m_dates[dataNum]);
				for (int dataSeriesNum = 0; dataSeriesNum < m_data.length; ++dataSeriesNum) {
					System.out.printf(" %5.2f", m_data[dataSeriesNum][dataNum]);
				}
				System.out.printf("\n");
			}
		}

		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nCompleted data import");
	}

	// if you do not know the predictedFieldIndex, this will find it and pass to
	// initTemporalDataSetWork
	public TemporalMLDataSet initTemporalDataSet(int DEBUG_LEVEL, int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE, int numberOfDataSeries, String PREDICT_FILE,
			String PREDICT_LABEL) {

		// get target field
		int predictedFieldIndex = getPredictFieldIndex(PREDICT_LABEL);
		//int predictedFieldIndex = getPredictFieldIndex(PREDICT_FILE, PREDICT_LABEL);

		return initTemporalDataSetWork(DEBUG_LEVEL, INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE,
				numberOfDataSeries, predictedFieldIndex);
	}

	// if you do know the predictedFieldIndex, this will pass the work on to initTemporalDataSetWork
	public TemporalMLDataSet initTemporalDataSet(int DEBUG_LEVEL, int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE, int numberOfDataSeries, int predictedFieldIndex) {
		return initTemporalDataSetWork(DEBUG_LEVEL, INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE,
				numberOfDataSeries, predictedFieldIndex);
	}

	private TemporalMLDataSet initTemporalDataSetWork(int DEBUG_LEVEL, int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE, int numberOfDataSeries, int predictedFieldIndex) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Starting init for temporal dataset");

		// create blank TemporalMLDataSet
		TemporalMLDataSet result = new TemporalMLDataSet(INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);

		// create description of the TemporalMLDataSet
		for (int dataSeriesNum = 0; dataSeriesNum < numberOfDataSeries; ++dataSeriesNum) {
			TemporalDataDescription desc = null;
			if (dataSeriesNum != predictedFieldIndex) {
				// not a predicted field
				desc = new TemporalDataDescription(TemporalDataDescription.Type.RAW, true, false);
			} else {
				// is a predicted field and not used as input
				desc = new TemporalDataDescription(TemporalDataDescription.Type.RAW, false, true);
			}
			result.addDescription(desc);
		}

		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed init for temporal dataset");

		return result;
	}

	public TemporalMLDataSet makeTemporalDataSet(int DEBUG_LEVEL, int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE, String PREDICT_FILE, String PREDICT_LABEL) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nStarting transform to temporal dataset");

		TemporalMLDataSet result = initTemporalDataSet(DEBUG_LEVEL, INPUT_WINDOW_SIZE,
				PREDICT_WINDOW_SIZE, m_data.length, PREDICT_FILE, PREDICT_LABEL);

		// transform to TemporalPoint and insert into TemporalMLDataSet
		for (int dataNum = 0; dataNum < m_dates.length; dataNum++) {
			TemporalPoint point = new TemporalPoint(m_data.length);
			point.setSequence(dataNum);
			for (int dataSeriesNum = 0; dataSeriesNum < m_data.length; ++dataSeriesNum) {
				point.setData(dataSeriesNum, m_data[dataSeriesNum][dataNum]);
			}
			result.getPoints().add(point);
		}

		// generate the TemporalMLDataSet from the input data
		result.generate();

		// summary and header for temporal data printout
		if (DEBUG_LEVEL >= 1) {
			System.out.println("Printing temporal data");
			System.out.println("Summary:");
			System.out.println("  m_data.length: " + m_data.length);
			System.out.println("  m_dates.length:" + m_dates.length);
			System.out.println("  m_numDataSeries:" + m_numDataSeries);
			System.out.println("  m_dataNames.length:" + m_dataNames.length);
			System.out.println("  INPUT_WINDOW_SIZE:" + INPUT_WINDOW_SIZE);
			System.out.println("  PREDICT_WINDOW_SIZE:" + PREDICT_WINDOW_SIZE);

			System.out.println("  Temporal Record count: " + result.getRecordCount());
			System.out.println("    Which is {the total number of records} minus "
					+ "{the INPUT_WINDOW_SIZE} minus {one}");
			System.out.println("          or " + m_dates.length + " - " + INPUT_WINDOW_SIZE
					+ " - 1 = " + (m_dates.length - INPUT_WINDOW_SIZE - 1));
			System.out.println("  Output neuron count: " + result.getOutputNeuronCount());
			System.out.println("  Input size: " + result.getInputSize());
			System.out.println("    Which is {INPUT_WINDOW_SIZE} * ({m_numDataSeries} - {PREDICT_WINDOW_SIZE})");
			System.out.println("          or " + INPUT_WINDOW_SIZE + " * (" + m_numDataSeries
					+ " - " + PREDICT_WINDOW_SIZE +") = " + result.getInputSize());
		}

		// print each imported entry
		if (DEBUG_LEVEL >= 2) {
			for (TemporalPoint tp : result.getPoints()) {
				System.out.println(tp.toString());
			}
		}

		// complete message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Completed transform to temporal data");

		return result;
	}

	public static void main(String args[]) {
		// String[] DATA_FILE_NAMES = { "./data/dataIngesterDefaultData/INDEX_GSPC.csv",
		// "./data/dataIngesterDefaultData/DGS2.csv",
		// "./data/dataIngesterDefaultData/DGS10.csv",
		// "./data/dataIngesterDefaultData/EURUSD.csv",
		// "./data/dataIngesterDefaultData/INDEX_RTS_RS.csv",
		// "./data/dataIngesterDefaultData/JPYUSD.csv" };
		// boolean DATA_IS_CLEAN = false
		// boolean DATA_NEEDS_NORMALIZATION = false;
		// String PREDICT_FILE = "INDEX_GSPC";
		// String PREDICT_LABEL = "Open";

		int[] TIME_LAGS = new int[3];
		TIME_LAGS[0] = 4;
		TIME_LAGS[1] = 2;
		TIME_LAGS[2] = 3;
		
//		DataIngester di = new DataIngester();
//		di.readBertsData(2, "./data/BertClean.20140421.001/dat1.csv", desiredTimeLags);
//		
//		int a = 0; while(a < 1){a = 0;}

		String[] DATA_FILE_NAMES = { "./data/BertClean.20140421.001/dat1.csv" };
		boolean DATA_NEEDS_CLEANING = false;
		boolean DATA_NEEDS_NORMALIZATION = false;
		String PREDICT_FILE = "dat1";
		String PREDICT_LABEL = "target";

		// 2 means print everything
		int DEBUG_LEVEL = 2;

		DataIngester dataIngester = new DataIngester();
		dataIngester.createData(DEBUG_LEVEL, DATA_FILE_NAMES, 0, 1.0, DATA_NEEDS_CLEANING,
				DATA_NEEDS_NORMALIZATION, PREDICT_FILE, PREDICT_LABEL, TIME_LAGS);

		TemporalMLDataSet temporal = null;
		temporal = dataIngester.makeTemporalDataSet(DEBUG_LEVEL, 12, 1, PREDICT_FILE, PREDICT_LABEL);

		// remember to add desired output values into the training set

		// basic network 3-3-1
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, 3));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 3));
		network.addLayer(new BasicLayer(new ActivationBiPolar(), false, 1));
		network.getStructure().finalizeStructure();
		network.reset(); // randomize initial weights
		MLDataSet trainingSet = temporal;
		final MLTrain train = new ResilientPropagation(network, trainingSet); // could use other
																				// training method

		do {
			train.iteration();
		} while (train.getError() > 0.001);

		double error = network.calculateError(trainingSet);
		System.out.println("Network trained until error = " + error);
	}
}
