package marketflow;

import java.util.HashMap;
import java.util.Map;

public class Stock
{
	private Map<String, Integer> _stock;
	public String Name;
	public Stock(String name)
	{
		Name = name;
		_stock = new HashMap<String, Integer>();
	}
	
	public void initStock(String key, int initVal)
	{
		_stock.put(key, initVal);
	}
	
	public void incResource(String id, int amt)
	{//increments the given stock count by amt amount
		_stock.put(id, _stock.get(id)+amt);
	}
	public int Resource(String id, int amt)
	{//sets stock to amt value
		int old = _stock.get(id);
		_stock.put(id, amt);
		return old;
	}
	public int Resource(String id)
	{//returns given stock count given a container ID
		return _stock.get(id);
	}
	
	public int Total()
	{
		int total = 0;
		for(int t : _stock.values())
		{
			total+=t;
		}
		return total;
	}
}
