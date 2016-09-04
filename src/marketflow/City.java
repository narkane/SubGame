package marketflow;

import java.awt.*;
import java.util.Map;

import engine.Game;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class City extends Entity
{
	public List<String> ships = new ArrayList<>();
	public List<String> generators = new ArrayList<>();
	public List<String> housing = new ArrayList<>();
	
	private Map<String, Ship> shipRef;
	//private float shipTax = 0.1f;
	private Map<String, Generator> genRef;
	//private float genTax = 0.05f;
	private Map<String, Housing> houseRef;
	private float houseTax = 0.1f;
	private Map<String, Integer> prices;
	private Map<String, Integer> basePrices;
	
	public City(String id, String desc, int x, int y, Map<String, Ship> sh_ref, Map<String, Generator> g_ref, Map<String, Housing> h_ref, Map<String, Stock> st_ref)
	{
		super(id, desc, st_ref, x, y);
		shipRef=sh_ref;
		genRef=g_ref;
		houseRef=h_ref;
		prices = new HashMap<String, Integer>();
		img = Game.gfx.load("res/city.png");
		hitbox=new Rectangle(posX-img.getWidth()/2, posY-img.getHeight()/2, img.getWidth(), img.getHeight());
		//TODO: remove this. temporary
		Game.clickables.put(this,hitbox);
	}

	public void BasePrices(Map<String, Integer> in)
	{
		basePrices=in;

	}

	public Map<String, Integer> BasePrices()
	{
		return basePrices;
	}

	public void update(int count)
	{

		for(Stock s : stockRef.values())
		{//Find appropriate prices.
			float amt = (float)s.Resource(ID);
			float ttl = (float)s.Total();
			float max = (float)basePrices.get(s.Name);
			float mlt = 0.0f;
			if(amt>0){mlt = amt/ttl;}
			mlt=mlt+1.0f;
			float fin = max/mlt;
			int price = Math.round(fin);
			prices.put(s.Name, price);
		}

	}
	
	public void render(Graphics g)
	{
		super.render(g);
	}
	
	public Housing leastPopulatedHouse()
	{//Find the Least Populated Housing unit
		Housing house = null;
		int pop=Integer.MAX_VALUE;
		for(int i = 0; i < housing.size(); i++)
		{
			if(houseRef.get(housing.get(i)).Population()<pop)
			{
				house = houseRef.get(housing.get(i));
				pop = house.Population();
			}
		}
		return house;
	}
	
	public Housing mostPopulatedHouse()
	{//Find the Least Populated Housing unit
		Housing house = null;
		int pop=-1;
		for(int i = 0; i < housing.size(); i++)
		{
			if(houseRef.get(housing.get(i)).Population()>pop)
			{
				house = houseRef.get(housing.get(i));
				pop = house.Population();
			}
		}
		return house;
	}
	
	public int X(){return posX;}
	public int Y(){return posY;}
	
	public int Price(String rid){return prices.get(rid);}
	public Ship Ship(String sid){return shipRef.get(sid);}
	public Generator Generator(String gid){return genRef.get(gid);}
	public Housing House(String hid){return houseRef.get(hid);}
	
	
	public void Population(int amt){System.out.println("Cannot Change Population Via City Method.");}
	public int Population()
	{
		int pop = 0;
		for(Housing h : houseRef.values())
		{
			if(h.Home().ID.equals(ID))
			{
				pop=pop+h.Population;
			}
		}
		return pop;
	}
	public void incPopulation(int amt){System.out.println("Cannot Change Population Via City Method.");}
	public void incPopulationMax(int amt){PopulationMax+=amt;}
}
