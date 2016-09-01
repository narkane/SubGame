package marketflow;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;

import engine.Game;

public class Ship extends Entity
{
	private String Location;
	private String Allience;
	private String Destination;
	private String Cargo;
	private enum State
	{
		WAITING,//Just sitting around...
		ASSESSING,//Determines where to go bases on global prices of goods compared to local goods until profit threshold is found
		LOADING,//Converts credit to resources until either credit reserve limit is reached or prices no longer match threshold
		ENROUTE,//Jacks off until distance of city is traversed
		REASSESSING,//Determines if the profit threshold is still valid for previous transactions or if there are better deals elsewhere now
		UNLOADING//Converts resources to credit until profit of transactions are no longer favorable
	}
	private State state = State.ASSESSING;
	private Map<String, City> cityRef;
	private City home;
	private City dest;
	private City dock;
	
	private int speed = 35;
	//private Map<String, Map<String, Integer>> priceDeltas;
	
	public Ship(String id, int x, int y, String location, String allience, Map<String, City> c_ref, Map<String, Stock> ref, int pm)
	{
		super(id, ref, x, y);
		Location = location;
		PopulationMax=pm;
		cityRef=c_ref;
		Allience=allience;
		Location=location;
		Cargo=null;
		Destination=null;
		home=cityRef.get(allience);
		dock=cityRef.get(location);
		dest=null;
		img = Game.gfx.load("res/ship.png");
	}

	public String Location()
	{
		return Location;
	}
	public String Destination()
	{
		return Destination;
	}
	public String Allience()
	{
		return Allience;
	}
	public void Allience(String newAllience)
	{
		Allience=newAllience;
	}
	
	int stallCount = 0;
	public void update(int count, int tickCount)
	{
		super.update(count, tickCount);
		
		switch(state)
		{
		case WAITING:
			stallCount++;
			if(stallCount>=200)
			{
				stallCount=0;
				state=State.ASSESSING;
			}
			
			break;
		case ASSESSING:
			if(tickCount>100)
			{
				if(findPriceDeltas())
				{
					state=State.LOADING;
				}
				else
				{
					Entry<String, City> nextCity = findClosestCity();
					if(nextCity!=null)
					{
						Destination=nextCity.getKey();
						dest=nextCity.getValue();
						Cargo=null;
						//System.out.println(ID+": From "+Location+" to "+Destination+" in hopes of a better trade.");
						Cell cell = row.createCell(14);
						cell.setCellValue("From "+Location+" to "+Destination+" in hopes of a better trade.");
						state = State.ENROUTE;
					}
					else
					{
						if(Location.equals(Allience))
						{
							System.out.println(ID+": Chilling out in "+Location+" for a bit");
							Cell cell = row.createCell(14);
							cell.setCellValue("Chilling out in "+Location+" for a bit");
							noGos.clear();
							state = State.WAITING;
						}
						else
						{
							Destination=Allience;
							dest=cityRef.get(Allience);
							Cargo=null;
							//System.out.println(ID+": From "+Location+" to "+Destination+" in hopes of a better trade.");
							Cell cell = row.createCell(14);
							cell.setCellValue("Fuck This I'm going home. ("+Allience+")");
							state = State.ENROUTE;
						}
					}
				}
			}
			break;
		case LOADING:
			boolean doneLoading=true;//There is nothing left to buy
			if(Resource("Water")<Population)
			{//If you're running low on water
				if(Buy(dock, "Water", dock.Price("Water"), 1))
				{//Buy water
					doneLoading=false;
				}
			}
			if(Resource("Food")<Population)
			{//if you're running low on Food
				if(Buy(dock, "Food", dock.Price("Food"), 1))
				{//Buy Food
					doneLoading=false;
				}
			}
			if(Resource("Fuel")<Population)
			{//If you're running low on Fuel
				if(Buy(dock, "Fuel", dock.Price("Fuel"), 1))
				{//Buy Fuel
					doneLoading=false;
				}
			}
			
			if(!Buy(dock, Cargo, dock.Price(Cargo), 1))
			{//With what ever's left over, buy your most profitable cargo
				//If you've got some extra change....
				if(Buy(dock, "Food", dock.Price("Food"), 1))
				{//Buy Food
					doneLoading=false;
				}
				if(Buy(dock, "Water", dock.Price("Water"), 1))
				{//Buy water
					doneLoading=false;
				}
			}
			else
			{
				doneLoading=false;
			}
			
			//If there's nothing left to buy... Bon Voyage!
			if(doneLoading){state = State.ENROUTE;}
			
			break;
		case ENROUTE:
			
			//eventually you wanna calculate distance and tick a counter...
			boolean doneTraveling= false;
			
			posX += speed * Math.cos(Math.atan2(dest.Y()-posY,dest.X()-posX));
			posY += speed * Math.sin(Math.atan2(dest.Y()-posY,dest.X()-posX));
			
			double a = dest.X()-posX;
			double b = dest.Y()-posY;
			
			if(Math.sqrt(a*a+b*b)<speed){doneTraveling=true;}
			
			if(doneTraveling){state=State.UNLOADING;}
			
			break;
		case UNLOADING:
			boolean doneUnloading = true;
			
			if(Cargo!=null)
			{
				if(!Sell(dest, Cargo, dest.Price(Cargo), 1))
				{
					if(dock.Price("Food")<dest.Price("Food")&&Resource("Food")>Population)
					{//If the price is right and you've food to spare...
						if(Sell(dest, "Food", dest.Price("Food"), 1))
						{//Buy Food
							doneUnloading=false;
						}
					}
					
					if(dock.Price("Water")<dest.Price("Water")&&Resource("Water")>Population)
					{//If the price is right and you've water to spare...
						if(Sell(dest, "Water", dest.Price("Water"), 1))
						{//Buy Water
							doneUnloading=false;
						}
					}
				}
				else
				{
					doneUnloading=false;
				}
			}
			
			if(doneUnloading)
			{
				dock=cityRef.get(Destination);
				Location=Destination;
				state=State.ASSESSING;
			}
			break;
		case REASSESSING:
			break;
		default:
			break;
		}
	}
	public void render(Graphics g)
	{
		super.render(g);
	}
	
	ArrayList<String> noGos = new ArrayList<String>();
	
	private boolean findPriceDeltas()
	{//Searches for a profitable route. returns boolean if you find one!
		int amt=Integer.MIN_VALUE;
		String oldCargo = Cargo;//make sure you dont pick the same cargo twice..
		for(String stock : stockRef.keySet())
		{//For every resource
			if(!stock.equals("Food")&&!stock.equals("Water"))
			{//Don't trade food and water... its too much
				for(String city : cityRef.keySet())
				{//for every city
					//find delta
					int delta =  cityRef.get(city).Price(stock) - home.Price(stock);
					boolean ok = true;//Is it ok to go to this city?
					for(String s : home.ships)
					{//As long as someone else isn't already headed there.. (This spreads out trade routes)
						if(home.Ship(s).Destination()==city)
						{
							ok=false;
							break;
						}
					}
					if(		delta>amt					//if its better profit
							&&ok						//if no one else is already traveling here.
							&&city!=Location			//if you arent already in this city..
							&&!stock.equals(oldCargo)	//if you haven't already traded this cargo
							&&dock.Price(stock)<Credit	//if you can afford atleast 1 cargo.
							&&dock.Resource(stock)>0	//if the city has atleast one...
					)
					{//If the profit is better, set this one to your destination / cargo
						Destination=city;
						dest=cityRef.get(city);
						Cargo=stock;
						amt=delta;
					}
				}
			}
		}
		if(amt<=0)
		{//if there is no profit to be had
			return false;
		}
		noGos.clear();
		System.out.println(ID+": From "+Location+" to "+Destination+" with "+Cargo+" for $"+amt);
		Cell cell = row.createCell(14);
		cell.setCellValue("From "+Location+" to "+Destination+" with "+Cargo+" for $"+amt);
		return true;
		//return deltaTable;
	}
	
	private double findDistance(City c1, City c2)
	{
		double a = c1.X()-c2.X();
		double b = c1.Y()-c2.Y();
		
		return Math.sqrt(a*a+b*b);
	}
	
	private Entry<String, City> findClosestCity()
	{//Just go somewhere else.. anywhere else. This city stinks!
		Entry<String, City> nextCity=null;
		double amt = Double.MAX_VALUE;
		for(Entry<String, City> map : cityRef.entrySet())
		{//for every city
			if(findDistance(dock, map.getValue())<amt
					&&!map.getKey().equals(Location)
					&&!noGos.contains(map.getKey()))
			{
				nextCity=map;
			}
		}
		if(nextCity!=null){noGos.add(nextCity.getKey());}
		return nextCity;
	}
}
