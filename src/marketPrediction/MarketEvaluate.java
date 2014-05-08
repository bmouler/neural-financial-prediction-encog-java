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
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.market.MarketDataDescription;
import org.encog.ml.data.market.MarketDataType;
import org.encog.ml.data.market.MarketMLDataSet;
import org.encog.ml.data.market.TickerSymbol;
import org.encog.ml.data.market.loader.MarketLoader;
import org.encog.ml.data.market.loader.YahooFinanceLoader;
import org.encog.neural.data.NeuralData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import batcher.IOHelper;

public class MarketEvaluate {

	enum Direction {
		up, dn
	};

	public static Direction determineDirection(double d) {
		if (d < 0)
			return Direction.dn;
		else
			return Direction.up;
	}

	public static MarketMLDataSet grabData(PropsXML p) {
		MarketLoader loader = new YahooFinanceLoader();
		MarketMLDataSet result = new MarketMLDataSet(loader, p.INPUT_WINDOW, p.PREDICT_WINDOW);

		for (int i = 0; i < p.TICKERS.length; i++) {
			MarketDataDescription desc = new MarketDataDescription(new TickerSymbol(p.TICKERS[i]),
					MarketDataType.CLOSE, p.IS_INPUTS[i], p.IS_PREDICTS[i]);
			result.addDescription(desc);
		}

		Calendar end = new GregorianCalendar();// end today
		Calendar begin = (Calendar) end.clone();// begin 30 days ago
		begin.add(Calendar.DATE, -200);

		result.load(begin.getTime(), end.getTime());
		result.generate();

		return result;

	}

	public static void evaluate(PropsXML p, File dataDir, String networkFileName) {

		File file = new File(dataDir, networkFileName);

		if (!file.exists()) {
			String s = "Evaluate: Can't read file: " + file.getAbsolutePath();
			System.out.println(s);
			if (p.USE_LOG_FILE) {
				IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s);
			}
			return;
		}

		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(file);

		MarketMLDataSet data = grabData(p);

		DecimalFormat format = new DecimalFormat("#0.0000");

		int count = 0;
		int correct = 0;
		for (MLDataPair pair : data) {
			MLData input = pair.getInput();
			MLData actualData = pair.getIdeal();
			MLData predictData = network.compute(input);

			double actual = actualData.getData(0);
			double predict = predictData.getData(0);
			double diff = Math.abs(predict - actual);

			Direction actualDirection = determineDirection(actual);
			Direction predictDirection = determineDirection(predict);

			if (actualDirection == predictDirection)
				correct++;

			count++;

			if (p.DEBUG_LEVEL >= 2) {
				String wasCorrect = (actualDirection == predictDirection) ? "++" : "--";

				String s = String
						.format("Day %3d : actual=%8.4f (%s) : predict=%8.4f (%s) : diff=%8.4f : match= %s\n",
								count, actual, actualDirection, predict, predictDirection, diff,
								wasCorrect);

				System.out.print(s);
				if (p.USE_LOG_FILE) {
					IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s);
				}
			}
		}

		double percent = (double) correct / (double) count;

		String s1 = "Direction correct:" + correct + "/" + count;
		System.out.println(s1);
		if (p.USE_LOG_FILE) {
			IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s1);
		}

		String s2 = "Directional Accuracy:" + format.format(percent * 100) + "%";
		System.out.println(s2);
		if (p.USE_LOG_FILE) {
			IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s2);
		}

		if (p.USE_LOG_FILE) {
			String s3 = correct + "/" + count + " , " + format.format(percent * 100) + "%";
			IOHelper.writeStringToFileAppend(dataDir + "/" + p.LOG_FILE_NAME, s3);
		}

		System.out.println("INPUT COUNT: "+network.getInputCount());
		System.out.println("LAYER COUNT: "+network.getLayerCount());
//		System.out.println("ERROR COUNT: "+network.calculateError(data));

	}
}
