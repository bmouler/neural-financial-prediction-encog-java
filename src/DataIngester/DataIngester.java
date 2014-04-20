package DataIngester;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.encog.ml.data.temporal.TemporalDataDescription;
import org.encog.ml.data.temporal.TemporalMLDataSet;
import org.encog.ml.data.temporal.TemporalPoint;
//import java.text.NumberFormat;
//
//import org.encog.ml.data.MLData;
//import org.encog.ml.data.MLDataSet;
//import org.encog.ml.data.basic.BasicMLData;
//import org.encog.ml.data.temporal.TemporalDataDescription;
//import org.encog.ml.data.temporal.TemporalMLDataSet;
//import org.encog.ml.data.temporal.TemporalPoint;
//import org.encog.neural.networks.BasicNetwork;
//import org.encog.neural.networks.layers.BasicLayer;
//import org.encog.neural.networks.training.Train;
//import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.util.arrayutil.NormalizeArray;

public class DataIngester {
	// Number of data series in all files collectively
	int m_numDataSeries;

	// Names of data series
	private String[] m_dataNames;

	// Dates common to all data series
	private String[] m_dates;

	// Data for all data series; leftmost index corresponds to data series
	// number
	private double[][] m_data;

	// Read in, temporally sort in chronologically-increasing order, and remove
	// any data points taken at dates not common to all data series
	public void readData(int DEBUG_LEVEL, String[] dataFiles) {
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
						if (Integer.parseInt(datePieces[2]) <= 15) {
							datePieces[2] = "20" + datePieces[2];
						} else {
							datePieces[2] = "19" + datePieces[2];
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

	// Normalize data to be within -1 and +1 in each data set
	public void normalizeData() {
		NormalizeArray norm = new NormalizeArray();
		norm.setNormalizedLow(-1.0);
		norm.setNormalizedHigh(1.0);

		for (int dataSetNum = 0; dataSetNum < m_data.length; ++dataSetNum) {
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

			m_data[dataSetNum] = norm.process(m_data[dataSetNum]);
		}
	}

	public void createData(int DEBUG_LEVEL, String[] listOfDataFiles) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("Starting data import");

		// read in and normalize data
		readData(DEBUG_LEVEL, listOfDataFiles);
		normalizeData();

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
			System.out.println("Completed data import");
	}

	public TemporalMLDataSet makeTemporalDataSet(int DEBUG_LEVEL, int INPUT_WINDOW_SIZE,
			int PREDICT_WINDOW_SIZE) {

		// start message
		if (DEBUG_LEVEL >= 1)
			System.out.println("\n\nStarting transform to temporal dataset");

		// create blank TemporalMLDataSet
		TemporalMLDataSet result = new TemporalMLDataSet(INPUT_WINDOW_SIZE, PREDICT_WINDOW_SIZE);

		// create description of the TemporalMLDataSet
		TemporalDataDescription desc = new TemporalDataDescription(
				TemporalDataDescription.Type.RAW, true, true);
		result.addDescription(desc);

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
		if (DEBUG_LEVEL >= 2) {
			System.out.println("Printing temporal data");
			System.out.println("Summary:");
			System.out.println("  Record count: " + result.getRecordCount());
			System.out.println("    Which is {the total number of records} minus "
					+ "{the INPUT_WINDOW_SIZE} minus one");
			System.out.println("          or " + m_dates.length + " - " + INPUT_WINDOW_SIZE
					+ " - 1 = " + (m_dates.length - INPUT_WINDOW_SIZE - 1));
			System.out.println("  Input size: " + result.getInputSize());
			System.out.println("  Output neuron count: " + result.getOutputNeuronCount());
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
		String[] DATA_FILE_NAMES = { "./data/stockPredictorDefaultData/INDEX_GSPC.csv",
				"./data/stockPredictorDefaultData/DGS2.csv",
				"./data/stockPredictorDefaultData/DGS10.csv",
				"./data/stockPredictorDefaultData/EURUSD.csv",
				"./data/stockPredictorDefaultData/FUTURE_CL1.csv",
				"./data/stockPredictorDefaultData/INDEX_RTS_RS.csv",
				"./data/stockPredictorDefaultData/JPYUSD.csv" };

		// 2 means print everything
		int DEBUG_LEVEL = 2;

		DataIngester dataIngester = new DataIngester();
		dataIngester.createData(DEBUG_LEVEL, DATA_FILE_NAMES);

		TemporalMLDataSet temporal = null;
		temporal = dataIngester.makeTemporalDataSet(DEBUG_LEVEL, 12, 1);

	}
}
