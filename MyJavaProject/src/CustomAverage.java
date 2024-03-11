public class CustomAverage implements org.h2.api.AggregateFunction{
    java.util.LinkedList<Integer> values = new java.util.LinkedList<Integer>();

    @Override
    public void init(java.sql.Connection cnctn) throws java.sql.SQLException {
        // I ignored this
    }

    @Override
    public int getType(int[] ints) throws java.sql.SQLException {
       return java.sql.Types.INTEGER;
    }

    @Override
    public void add(Object o) throws java.sql.SQLException {
        values.add((Integer)o);
    }

    @Override
    public Object getResult() throws java.sql.SQLException {
        double average = 0;
        java.util.Iterator<Integer> i;

        // Get average value
        for( i = values.iterator(); i.hasNext(); ) {
            average += i.next();    
        }
        average = average / values.size();

        // Return Average
        return average;
    }
}