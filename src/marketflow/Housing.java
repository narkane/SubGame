package marketflow;

import java.awt.*;
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

	public void update(int count)
	{

		float multiplier = (float)Population/(float)PopulationMax;

		if(count%(int)(100/multiplier)==0)
		{//as population goes up this gets faster. slower as it declines
			if(ID.equals("Shell Cove")) {

				System.out.println(((int)(100/multiplier)) + " - " + Population());
			}

			for(Stock s : stockRef.values())
			{
				if(s.Type().equals("consumable"))
				{
					//System.out.println(s.Name);
				}
			}
		}

		if(count%100==0)
		{//collect rents
			incCredit(Math.round(taxRate*Population));
		}
	}

	public void render(Graphics g){}
}
