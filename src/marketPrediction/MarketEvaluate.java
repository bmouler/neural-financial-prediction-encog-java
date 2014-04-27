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
import org.encog.ml.data.market.loader.MarketLoader;
import org.encog.ml.data.market.loader.YahooFinanceLoader;
import org.encog.neural.data.NeuralData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

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

	public static MarketMLDataSet grabData() {
		MarketLoader loader = new YahooFinanceLoader();
		MarketMLDataSet result = new MarketMLDataSet(loader, Config.INPUT_WINDOW,
				Config.PREDICT_WINDOW);
		MarketDataDescription desc = new MarketDataDescription(Config.TICKER, MarketDataType.CLOSE,
				true, true);
		result.addDescription(desc);

		MarketDataDescription desc2 = new MarketDataDescription(Config.TICKER2,
				MarketDataType.CLOSE, true, false);
		result.addDescription(desc2);

		MarketDataDescription desc3 = new MarketDataDescription(Config.TICKER3,
				MarketDataType.CLOSE, true, false);
		result.addDescription(desc3);

		MarketDataDescription desc4 = new MarketDataDescription(Config.TICKER4,
				MarketDataType.CLOSE, true, false);
		result.addDescription(desc4);

		MarketDataDescription desc5 = new MarketDataDescription(Config.TICKER5,
				MarketDataType.CLOSE, true, false);
		result.addDescription(desc5);

		MarketDataDescription desc6 = new MarketDataDescription(Config.TICKER6,
				MarketDataType.CLOSE, true, false);
		result.addDescription(desc6);

		MarketDataDescription desc7 = new MarketDataDescription(Config.TICKER7,
				MarketDataType.CLOSE, true, false);
		result.addDescription(desc7);

		Calendar end = new GregorianCalendar();// end today
		Calendar begin = (Calendar) end.clone();// begin 30 days ago
		begin.add(Calendar.DATE, -200);

		result.load(begin.getTime(), end.getTime());
		result.generate();

		return result;

	}

	public static void evaluate(File dataDir) {

		File file = new File(dataDir, Config.NETWORK_FILE);

		if (!file.exists()) {
			System.out.println("Can't read file: " + file.getAbsolutePath());
			return;
		}

		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(file);

		MarketMLDataSet data = grabData();

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

			if (Config.DEBUG_LEVEL >= 2) {
			String wasCorrect = (actualDirection == predictDirection) ? "++" : "--";
			System.out.printf(
					"Day %3d : actual=%8.4f (%s) : predict=%8.4f (%s) : diff=%8.4f : match= %s\n",
					count, actual, actualDirection, predict, predictDirection, diff, wasCorrect);
			}
		}
		double percent = (double) correct / (double) count;
		System.out.println("Direction correct:" + correct + "/" + count);
		System.out.println("Directional Accuracy:" + format.format(percent * 100) + "%");

	}
}
