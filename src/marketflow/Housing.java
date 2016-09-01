package marketflow;

import java.util.Map;

public class Housing extends Entity
{
	private String Location;
	private String Allience;
	private Map<String, City> cityRef;
	private City home;
	private float taxRate = 1.5f;
	
	public Housing(String id, int x, int y, String location, Map<String, City> c_ref, Map<String, Stock> st_ref, int pm)
	{
		super(id, st_ref, x, y);
		
		cityRef = c_ref;
		Location = location;
		Allience = location;
		home=cityRef.get(Location);
		PopulationMax = pm;
	}
	public String Location()
	{
		return Location;
	}
	public String Allience()
	{
		return Allience;
	}
	public void Allience(String allience)
	{
		Allience=allience;
	}
	public void update(int count, int tickCount)
	{
		super.update(count, tickCount);
		
		float multiplier = (float)Population/(float)PopulationMax;
		if(tickCount%(int)(10/multiplier)==0)
		{//as population goes up this gets faster. slower as it declines
			if(Resource("Food")>0 && Resource("Water")>0)
			{//consume food and water
				incResource("Food", -1);
				incResource("Water", -1);
				if(Population<PopulationMax)
				{//have a baby
					Population++;
				}
			}
			else
			{//starve the weakest link!
				Population--;
			}
			
		}
		if(Resource("Food")<Population)
		{//purchase food
			Buy(home, "Food",home.Price("Food"),1);
		}
		if(Resource("Water")<Population)
		{//purchase water
			Buy(home, "Water",home.Price("Water"),1);
		}
		
		if(tickCount%100==0)
		{//collect rents
			//System.out.println(ID+": "+Math.round(taxRate*Population));
			incCredit(Math.round(taxRate*Population));
		}
	}
}
