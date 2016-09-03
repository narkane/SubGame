package marketflow;

import java.util.Map;

public class Housing extends Entity
{

	private Map<String, City> cityRef;
	private City home;
	private float taxRate = 1.5f;
	
	public Housing(String id, String desc, int x, int y, String location, Map<String, City> c_ref, Map<String, Stock> st_ref, int pm)
	{
		super(id, desc, st_ref, x, y);
		
		cityRef = c_ref;
		home=cityRef.get(location);
		PopulationMax = pm;
	}
	public City Home()
	{
		return home;
	}

	public void update(int count, int tickCount)
	{
		super.update(count, tickCount);
		
		float multiplier = (float)Population/(float)PopulationMax;
		if(tickCount%(int)(10/multiplier)==0)
		{//as population goes up this gets faster. slower as it declines

		}
		
		if(tickCount%100==0)
		{//collect rents
			//System.out.println(ID+": "+Math.round(taxRate*Population));
			incCredit(Math.round(taxRate*Population));
		}
	}
}
