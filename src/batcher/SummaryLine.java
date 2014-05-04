package batcher;

public class SummaryLine {
	String threadLabel = "";
	String endMessage = "";
	String trials = "";
	String percent = "";
	
	public String toCSV() {
		return threadLabel + "," + endMessage + "," + trials + "," + percent;
	}
}
