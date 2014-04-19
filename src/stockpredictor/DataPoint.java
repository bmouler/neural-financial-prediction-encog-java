package stockpredictor;

// Holds data point (i.e., date and datum pair); used only purposes of sorting
// data
public class DataPoint implements Comparable<DataPoint>
{
	// Date of datum
	private String m_date;
	
	// Datum
	private double m_datum;
	
	// Constructor
	public DataPoint(String date, double datum)
	{
		m_date  = date;
		m_datum = datum;
	}
	
	// Get date
	public String getDate()
	{
		return m_date;
	}
	
	// Get datum
	public double getDatum()
	{
		return m_datum;
	}
	
	// Comparison operation overload
	public int compareTo(DataPoint rhs)
	{
		return m_date.compareTo(rhs.m_date);
	}
}

