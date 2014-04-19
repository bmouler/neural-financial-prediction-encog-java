package stockpredictor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

public class StockPredictor
{
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
	public void readData(String[] dataFiles)
	{
		BufferedReader reader = null;
		
		try
		{
			System.out.printf("Reading in data in %d files...\n", dataFiles.length);
			
			// Count number of data series in all data files and get header
			// name of each data series
			m_numDataSeries = 0;
			int[] numDataSeriesPerFile = new int[dataFiles.length];
			String[][] headerPieces = new String[dataFiles.length][];
			for (int dataFileNameNum = 0; dataFileNameNum < dataFiles.length; ++dataFileNameNum)
			{
				reader = new BufferedReader(new FileReader(dataFiles[dataFileNameNum]));
				
				String[] allHeaders = reader.readLine().split(",");
				headerPieces[dataFileNameNum] = new String[allHeaders.length - 1];
				for (int headerNum = 0; headerNum < allHeaders.length - 1; ++headerNum)
				{
					headerPieces[dataFileNameNum][headerNum] = allHeaders[headerNum + 1];
				}
				
				numDataSeriesPerFile[dataFileNameNum] = allHeaders.length - 1;
				m_numDataSeries += numDataSeriesPerFile[dataFileNameNum];
	 		}
			
			System.out.printf("Total of %d data series found\n", m_numDataSeries);
			
			// Construct name of each data series
			m_dataNames = new String[m_numDataSeries];
			
			m_numDataSeries = 0;
			for (int dataFileNameNum = 0; dataFileNameNum < dataFiles.length; ++dataFileNameNum)
			{
				String[] fileNamePieces = dataFiles[dataFileNameNum].split("/");
				fileNamePieces = (fileNamePieces[fileNamePieces.length - 1]).split("\\.");
				String fileName = fileNamePieces[0];
				
				for (int dataSetNumInFile = 0; dataSetNumInFile < numDataSeriesPerFile[dataFileNameNum]; ++dataSetNumInFile)
				{
					m_dataNames[m_numDataSeries + dataSetNumInFile] = fileName + "_" + headerPieces[dataFileNameNum][dataSetNumInFile];
				}
				
				m_numDataSeries += numDataSeriesPerFile[dataFileNameNum];
	 		}
			
			// Read in dates and data
			ArrayList<ArrayList<String>> dateLists = new ArrayList<ArrayList<String>>(m_numDataSeries);
			ArrayList<ArrayList<Double>> dataLists = new ArrayList<ArrayList<Double>>(m_numDataSeries);
			for (int dataSetNum = 0; dataSetNum <m_numDataSeries; ++dataSetNum)
			{
				dateLists.add(new ArrayList<String>());
				dataLists.add(new ArrayList<Double>());
			}
			
			m_numDataSeries = 0;
			for (int dataFileNameNum = 0; dataFileNameNum < dataFiles.length; ++dataFileNameNum)
			{
				reader = new BufferedReader(new FileReader(dataFiles[dataFileNameNum]));
				reader.readLine();
				
				String line;
				while ((line = reader.readLine()) != null)
				{
					String[] linePieces = line.split(",");

					// Ignore data lines that do not match header
					if (linePieces.length != headerPieces[dataFileNameNum].length + 1)
					{
						continue;
					}
					
					for (int dataSeriesNumInFile = 0; dataSeriesNumInFile < numDataSeriesPerFile[dataFileNameNum]; ++dataSeriesNumInFile)
					{	
						// Prepend either a "19" or "20" onto dates in order to
						// make them 4 digits long
						String[] datePieces = linePieces[0].split("/");
						if (Integer.parseInt(datePieces[2]) <= 15)
						{
							datePieces[2] = "20" + datePieces[2];
						}
						else
						{
							datePieces[2] = "19" + datePieces[2];
						}
						
						// Invert date into a sortable format
						String date = datePieces[2] + "-" + datePieces[0] + "-" + datePieces[1];
						
						// Add date and datum to lists
						dateLists.get(m_numDataSeries + dataSeriesNumInFile).add(date);
						dataLists.get(m_numDataSeries + dataSeriesNumInFile).add(Double.parseDouble(linePieces[dataSeriesNumInFile + 1]));
					}
		 		}
				
				m_numDataSeries += numDataSeriesPerFile[dataFileNameNum];
			}

			// Sort each data series in chronologically-increasing order
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum)
			{
				ArrayList<DataPoint> dataPointLists = new ArrayList<DataPoint>();
				for (int dateNum = 0; dateNum < dateLists.get(dataSeriesNum).size(); ++dateNum)
				{
					dataPointLists.add(new DataPoint(dateLists.get(dataSeriesNum).get(dateNum), dataLists.get(dataSeriesNum).get(dateNum)));
				}
				
				Collections.sort(dataPointLists);
				
				dateLists.get(dataSeriesNum).clear();
				dataLists.get(dataSeriesNum).clear();
				for (int dateNum = 0; dateNum < dataPointLists.size(); ++dateNum)
				{
					dateLists.get(dataSeriesNum).add(dataPointLists.get(dateNum).getDate ());
					dataLists.get(dataSeriesNum).add(dataPointLists.get(dateNum).getDatum());
				}
			}
			
			// Remove any data entries taken at dates before that of most
			// recent data series
			System.out.printf("Removing data at times not common to all %d data series...\n", m_numDataSeries);
			
			String maxMinDate = dateLists.get(0).get(0);
			for (int dataSeriesNum = 1; dataSeriesNum < m_numDataSeries; ++dataSeriesNum)
			{
				if (maxMinDate.compareTo(dateLists.get(dataSeriesNum).get(0)) < 0)
				{
					maxMinDate = dateLists.get(dataSeriesNum).get(0);
				}
			}
			
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum)
			{
				while(dateLists.get(dataSeriesNum).get(0).compareTo(maxMinDate) < 0)
				{
					dateLists.get(dataSeriesNum).remove(0);
					dataLists.get(dataSeriesNum).remove(0);
				}
			}
			
			// Remove any data for date entries not common to all data sets
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum)
			{
				System.out.printf("  %2d out of %d\n", dataSeriesNum + 1, m_numDataSeries);
				
				// Loop through each date element in each series
				int dateNum = 0;
				while (dateNum < dateLists.get(dataSeriesNum).size())
				{
					// For convenience, grab current looked-for date
					String date = dateLists.get(dataSeriesNum).get(dateNum);

					// Check if date appears in all other data series or not
					boolean dateIsInAllOtherDataSeries = true;
					for (int otherDataSeriesNum = 0; otherDataSeriesNum < m_numDataSeries; ++otherDataSeriesNum)
					{
						if (dataSeriesNum == otherDataSeriesNum)
						{
							continue;
						}

						if (!dateLists.get(otherDataSeriesNum).contains(date))
						{
							dateIsInAllOtherDataSeries = false;
							break;
						}
					}
					
					if (dateIsInAllOtherDataSeries == false)
					{
						dateLists.get(dataSeriesNum).remove((int)dateNum);
						dataLists.get(dataSeriesNum).remove((int)dateNum);
					}
					else
					{
						++dateNum;
					}
				}
			}
			
			// Copy over to old-fashioned arrays for Encog usage
			m_dates = new String[dateLists.get(0).size()];
			for (int dateNum = 0; dateNum < dateLists.get(0).size(); ++dateNum)
			{
				m_dates[dateNum] = dateLists.get(0).get(dateNum);
			}
			
			m_data = new double[m_numDataSeries][dateLists.get(0).size()];
			for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum)
			{
				for (int dateNum = 0; dateNum < dateLists.get(0).size(); ++dateNum)
				{
					m_data[dataSeriesNum][dateNum] = dataLists.get(dataSeriesNum).get(dateNum);
				}
			}
			
			System.out.printf("Num data points common to all %d datasets   = %d\n", m_numDataSeries, m_dates.length);
			System.out.printf("Least recent date common to all %d datasets = %s\n", m_numDataSeries, m_dates[0]);
			System.out.printf("Most recent date common to all %d datasets  = %s\n", m_numDataSeries, m_dates[m_dates.length - 1]);
		}
		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	// Normalize data to be within -1 and +1 in each data set
	public void normalizeData()
	{
		NormalizeArray norm = new NormalizeArray();
		norm.setNormalizedLow (-1.0);
		norm.setNormalizedHigh( 1.0);

		for (int dataSetNum = 0; dataSetNum < m_data.length; ++dataSetNum)
		{
			// Find min and max of data set
			double minVal = m_data[dataSetNum][0];
			double maxVal = m_data[dataSetNum][0];
			
			for (int dataNum = 0; dataNum < m_dates.length; ++dataNum)
			{
				if (minVal > m_data[dataSetNum][dataNum])
				{
					minVal = m_data[dataSetNum][dataNum];
				}
				
				if (maxVal < m_data[dataSetNum][dataNum])
				{
					maxVal = m_data[dataSetNum][dataNum];
				}
			}
			
			m_data[dataSetNum] = norm.process(m_data[dataSetNum]);
		}
	}

//	public final static double[] SUNSPOTS = {
//        0.0262,  0.0575,  0.0837,  0.1203,  0.1883,  0.3033,  
//        0.1517,  0.1046,  0.0523,  0.0418,  0.0157,  0.0000,  
//        0.0000,  0.0105,  0.0575,  0.1412,  0.2458,  0.3295,  
//        0.3138,  0.2040,  0.1464,  0.1360,  0.1151,  0.0575,  
//        0.1098,  0.2092,  0.4079,  0.6381,  0.5387,  0.3818,  
//        0.2458,  0.1831,  0.0575,  0.0262,  0.0837,  0.1778,  
//        0.3661,  0.4236,  0.5805,  0.5282,  0.3818,  0.2092,  
//        0.1046,  0.0837,  0.0262,  0.0575,  0.1151,  0.2092,  
//        0.3138,  0.4231,  0.4362,  0.2495,  0.2500,  0.1606,  
//        0.0638,  0.0502,  0.0534,  0.1700,  0.2489,  0.2824,  
//        0.3290,  0.4493,  0.3201,  0.2359,  0.1904,  0.1093,  
//        0.0596,  0.1977,  0.3651,  0.5549,  0.5272,  0.4268,  
//        0.3478,  0.1820,  0.1600,  0.0366,  0.1036,  0.4838,  
//        0.8075,  0.6585,  0.4435,  0.3562,  0.2014,  0.1192,  
//        0.0534,  0.1260,  0.4336,  0.6904,  0.6846,  0.6177,  
//        0.4702,  0.3483,  0.3138,  0.2453,  0.2144,  0.1114,  
//        0.0837,  0.0335,  0.0214,  0.0356,  0.0758,  0.1778,  
//        0.2354,  0.2254,  0.2484,  0.2207,  0.1470,  0.0528,  
//        0.0424,  0.0131,  0.0000,  0.0073,  0.0262,  0.0638,  
//        0.0727,  0.1851,  0.2395,  0.2150,  0.1574,  0.1250,  
//        0.0816,  0.0345,  0.0209,  0.0094,  0.0445,  0.0868,  
//        0.1898,  0.2594,  0.3358,  0.3504,  0.3708,  0.2500,  
//        0.1438,  0.0445,  0.0690,  0.2976,  0.6354,  0.7233,  
//        0.5397,  0.4482,  0.3379,  0.1919,  0.1266,  0.0560,  
//        0.0785,  0.2097,  0.3216,  0.5152,  0.6522,  0.5036,  
//        0.3483,  0.3373,  0.2829,  0.2040,  0.1077,  0.0350,  
//        0.0225,  0.1187,  0.2866,  0.4906,  0.5010,  0.4038,  
//        0.3091,  0.2301,  0.2458,  0.1595,  0.0853,  0.0382,  
//        0.1966,  0.3870,  0.7270,  0.5816,  0.5314,  0.3462,  
//        0.2338,  0.0889,  0.0591,  0.0649,  0.0178,  0.0314,  
//        0.1689,  0.2840,  0.3122,  0.3332,  0.3321,  0.2730,  
//        0.1328,  0.0685,  0.0356,  0.0330,  0.0371,  0.1862,  
//        0.3818,  0.4451,  0.4079,  0.3347,  0.2186,  0.1370,  
//        0.1396,  0.0633,  0.0497,  0.0141,  0.0262,  0.1276,  
//        0.2197,  0.3321,  0.2814,  0.3243,  0.2537,  0.2296,  
//        0.0973,  0.0298,  0.0188,  0.0073,  0.0502,  0.2479,  
//        0.2986,  0.5434,  0.4215,  0.3326,  0.1966,  0.1365,  
//        0.0743,  0.0303,  0.0873,  0.2317,  0.3342,  0.3609,  
//        0.4069,  0.3394,  0.1867,  0.1109,  0.0581,  0.0298,  
//        0.0455,  0.1888,  0.4168,  0.5983,  0.5732,  0.4644,  
//        0.3546,  0.2484,  0.1600,  0.0853,  0.0502,  0.1736,  
//        0.4843,  0.7929,  0.7128,  0.7045,  0.4388,  0.3630,  
//        0.1647,  0.0727,  0.0230,  0.1987,  0.7411,  0.9947,  
//        0.9665,  0.8316,  0.5873,  0.2819,  0.1961,  0.1459,  
//        0.0534,  0.0790,  0.2458,  0.4906,  0.5539,  0.5518,  
//        0.5465,  0.3483,  0.3603,  0.1987,  0.1804,  0.0811,  
//        0.0659,  0.1428,  0.4838,  0.8127 
//      };
//
//public final static int STARTING_YEAR = 1700;
//public final static int WINDOW_SIZE = 30;
//public final static int TRAIN_START = WINDOW_SIZE;
//public final static int TRAIN_END = 259;
//public final static int EVALUATE_START = 260;
//public final static int EVALUATE_END = SUNSPOTS.length-1;
//
///**
// * This really should be lowered, I am setting it to a level here that will
// * train in under a minute.
// */
//public final static double MAX_ERROR = 0.01;
//
//private double[] normalizedSunspots;
//private double[] closedLoopSunspots;
//
//public MLDataSet generateTraining()
//{
//	TemporalMLDataSet result = new TemporalMLDataSet(WINDOW_SIZE,1);
//	
//	TemporalDataDescription desc = new TemporalDataDescription(
//			TemporalDataDescription.Type.RAW,true,true);
//	result.addDescription(desc);
//	
//	for(int year = TRAIN_START;year<TRAIN_END;year++)
//	{
//		TemporalPoint point = new TemporalPoint(1);
//		point.setSequence(year);
//		point.setData(0, this.normalizedSunspots[year]);
//		result.getPoints().add(point);
//	}
//	
//	result.generate();
//	
//	return result;
//}
//
//public BasicNetwork createNetwork()
//{
//	BasicNetwork network = new BasicNetwork();
//	network.addLayer(new BasicLayer(WINDOW_SIZE));
//	network.addLayer(new BasicLayer(10));
//	network.addLayer(new BasicLayer(1));
//	network.getStructure().finalizeStructure();
//	network.reset();
//	return network;
//}
//
//public void train(BasicNetwork network,MLDataSet training)
//{
//	final Train train = new ResilientPropagation(network, training);
//
//	int epoch = 1;
//
//	do {
//		train.iteration();
//		System.out
//				.println("Epoch #" + epoch + " Error:" + train.getError());
//		epoch++;
//	} while(train.getError() > MAX_ERROR);
//}
//
//public void predict(BasicNetwork network)
//{
//	NumberFormat f = NumberFormat.getNumberInstance();
//	f.setMaximumFractionDigits(4);
//	f.setMinimumFractionDigits(4);
//	
//	System.out.println("Year\tActual\tPredict\tClosed Loop Predict");
//	
//	for(int year=EVALUATE_START;year<EVALUATE_END;year++)
//	{
//		// calculate based on actual data
//		MLData input = new BasicMLData(WINDOW_SIZE);
//		for(int i=0;i<input.size();i++)
//		{
//			input.setData(i,this.normalizedSunspots[(year-WINDOW_SIZE)+i]);
//		}
//		MLData output = network.compute(input);
//		double prediction = output.getData(0);
//		this.closedLoopSunspots[year] = prediction;
//		
//		// calculate "closed loop", based on predicted data
//		for(int i=0;i<input.size();i++)
//		{
//			input.setData(i,this.closedLoopSunspots[(year-WINDOW_SIZE)+i]);
//		}
//		output = network.compute(input);
//		double closedLoopPrediction = output.getData(0);
//		
//		// display
//		System.out.println((STARTING_YEAR+year)
//				+"\t"+f.format(this.normalizedSunspots[year])
//				+"\t"+f.format(prediction)
//				+"\t"+f.format(closedLoopPrediction)
//		);
//		
//	}
//}

	public void run()
	{
		readData(Config.DATA_FILE_NAMES);
		normalizeData();

		for (int dataSeriesNum = 0; dataSeriesNum < m_numDataSeries; ++dataSeriesNum)
		{
			System.out.printf("%6d : %s :", dataSeriesNum, m_dataNames[dataSeriesNum]);
		}
		
		for (int dataNum = 0; dataNum < m_dates.length; ++dataNum)
		{
			System.out.printf("%6d : %s :", dataNum, m_dates[dataNum]);
			for (int dataSeriesNum = 0; dataSeriesNum < m_data.length; ++dataSeriesNum)
			{
				System.out.printf(" %5.2f", m_data[dataSeriesNum][dataNum]);
			}
			System.out.printf("\n");
		}

		//BasicNetwork network = createNetwork();
		//MLDataSet training = generateTraining();
		//train(network,training);
		//predict(network);
	}

	public static void main(String args[])
	{
		StockPredictor stockPredictor = new StockPredictor();
		stockPredictor.run();
	}
}
