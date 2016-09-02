
package marketflow;

import java.awt.Graphics;
import java.util.Map;

public class Generator extends Entity
{
	private String Location;
	private String Allience;
	private enum State
	{
		WAITING,
		LOADING,//Buying up the input ingredients
		PRODUCING,//Workers are creating the product until conditions are no longer favorable for production
		UNLOADING,//Bringing goods to market
		
	}
	private State state = State.PRODUCING;
	private int Cost;
	private int Output;
	private String[] Inputs;
	private String Product;
	private Map<String, City> cityRef;
	private int Time;
	private City home;
	private int price;
	
	public Generator(String id, int x, int y, int c, int output, String[] inputs, String product, String location, Map<String, City> c_ref, Map<String, Stock> st_ref, int t, int pm)
	{
		super(id, st_ref, x, y);
		Location = location;
		Allience = location;
		Inputs = inputs;
		Output=output;
		Product = product;
		cityRef = c_ref;
		home=cityRef.get(Location);
		Cost=c;
		Time = t;
		PopulationMax = pm;
		price = 0;
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
	public String Product()
	{
		return Product;
	}
	
	public boolean Produce()
	{
		//Check if cost of production is available
		if(Credit<Cost*Population)
		{
			state=State.UNLOADING;
			return false;
		}
		for(int i = 0; i < Inputs.length; i++)
		{//Check for sufficient ingredients
			if(Resource(Inputs[i])<=0)
			{//don't have enough ingredients
				state=State.LOADING;
				return false;
			}
		}
		//Spend cost of production
		incCredit(-Cost*Population);
		for(int i = 0; i < Inputs.length; i++)
		{//Spend each ingredient resource
			incResource(Inputs[i],-1);
		}
		//produce output product
		incResource(Product,Output);
		return true;
	}
	
	public int Price(){return price;}
	
	public void update(int count, int tickCount)
	{
		super.update(count, tickCount);
		
		float amt = (float)Resource(Product);
		float ttl = (float)amt;
		for(int i = 0; i < home.generators.size(); i++)
		{
			ttl+=home.Generator(home.generators.get(i)).Resource(Product);
		}
		float max = (float)home.BasePrices().get(Product);
		float mlt = 0.0f;
		if(amt>0&&ttl>0){mlt = amt/ttl;}
		mlt=mlt+1.0f;
		float fin = max/mlt;
		price = Math.round(fin);
		
		switch(state)
		{
		case WAITING:
			if(home.Resource(Product)<500)
			{
				state=State.PRODUCING;
			}
			
			break;
		case LOADING:
			String needed = null;
			int min=Integer.MAX_VALUE;
			for(int i = 0; i < Inputs.length; i++)
			{//Find most needed input
				if(Resource(Inputs[i])<min)
				{
					needed=Inputs[i];
					min=Resource(needed);
				}
			}
			if(needed!=null)
			{
				if(!Buy(home, needed, home.Price(needed), 1))
				{
					state=State.PRODUCING;
				}
			}
			else
			{
				state=State.PRODUCING;
			}
			break;
		case PRODUCING:
			if(home.Resource(Product)>=500)
			{
				state=State.WAITING;
				break;
			}
			float multiplier = (float)Population/(float)PopulationMax;
			if(tickCount%(int)(Time/multiplier)==0)
			{//for every time of production
				if(price<home.Price(Product))
				{//if the price is right... start selling
					state=State.UNLOADING;
				}
				int cost=Cost*Population;
				for(int i = 0; i < Inputs.length; i++)
				{//calculate total cost of production
					cost+=home.Price(Inputs[i]);
				}
				if(cost < home.Price(Product)*Output)
				{//if production is profitable then PRODUUUCE!
					Produce();
				}
				else if(Population>1)
				{//cut costs by laying off! fuckin' ruthless!
					Emigrate(home.leastPopulatedHouse(), 1);
					state=State.UNLOADING;
				}
				else
				{//Go sell your shit when you're out of money
					state=State.UNLOADING;
				}
			}
			break;
		case UNLOADING:
			if(Resource(Product)<=0)
			{//If your done unloading the product
				state=State.PRODUCING;
			}
			//Sell your product to market
			if(!Sell(home, Product, price, 1))
			{//Poor ass city can't afford your shit.
				state=State.LOADING;
			}
			break;
		}
		
		if(tickCount%100==0)
		{//every once and a while
			if(Population<PopulationMax)
			{//hire a new guy
				Immigrate(home.mostPopulatedHouse(), 1);
			}
			
		}
	}
	
	public void render(Graphics g)
	{
		super.render(g);
	}
}
