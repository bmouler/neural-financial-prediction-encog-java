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



/**
 * Use the saved market neural network, and now attempt to predict for today, and the
 * last 60 days and see what the results are.
 */
public class MarketPredict {
		
	public static void main(String[] args)
	{		
		if( args.length<1 ) {
			System.out.println("MarketPredict [data dir] [generate/train/incremental/evaluate]");
		}
		else
		{
			File dataDir = new File(args[0]);
			if( args[1].equalsIgnoreCase("generate") ) {
				MarketBuildTraining.generate(null, dataDir);
			} 
			else if( args[1].equalsIgnoreCase("train") ) {
				MarketTrain.train(null, dataDir);
			} 
			else if( args[1].equalsIgnoreCase("evaluate") ) {
				// note: requires a PropsXML now
				// note: I ran around the code for about 20 minutes due to refs to Config.java
				//       then I deleted Config.java and ref'd PropsXML where needed. -Kevin
				//MarketEvaluate.evaluate(dataDir);
			} else if( args[1].equalsIgnoreCase("prune") ) {
				// note: see above for same Config.java issue -Kevin
				//MarketPrune.incremental(dataDir);
			} 
			Encog.getInstance().shutdown();
		}
	}
	
}
