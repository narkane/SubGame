package marketflow;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;

import engine.Game;

public class Ship extends Entity
{
	private enum State
	{
		WAITING,//Just sitting around...
		ASSESSING,//Determines where to go based on global prices of goods compared to local goods until profit threshold is found
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
	
	private int speed = 15;
	//private Map<String, Map<String, Integer>> priceDeltas;
	
	public Ship(String id, int x, int y, String location, String alliance, Map<String, City> c_ref, Map<String, Stock> ref, int pm)
	{
		super(id, ref, x, y);
		PopulationMax=pm;
		cityRef=c_ref;
		home=cityRef.get(alliance);
		dock=cityRef.get(location);
		dest=null;
		img = Game.gfx.load("res/ship.png");
	}

	public City Location()
	{
		return dock;
	}
	public City Destination()
	{
		return dest;
	}
	public City Home()
	{
		return home;
	}

	int stallCount = 0;
	int stuck = 0;
	int stallAmt = 200;
	ArrayList<String> ignoreCargo = new ArrayList<>();
	ArrayList<String> ignoreCity = new ArrayList<>();
	ArrayList<Transaction> ignoreTransactions = new ArrayList<>();
	ArrayList<Transaction> transactions = new ArrayList<>();

	public void update(int count, int tickCount)
	{
		super.update(count, tickCount);
		int mostProfit = 0;
		switch(state)
		{

		case WAITING:
			if(stallCount>=stallAmt)
			{
				stallCount = 0;
				state = state.ASSESSING;
				break;
			}
			stallCount++;
			break;

		case ASSESSING:
			dest=null;
			for (City city : cityRef.values())
			{
				int profit=0;
				if(!dock.equals(city))
				{
					for(String stock : stockRef.keySet())
					{
						int delta = city.Price(stock)-dock.Price(stock);
						if(delta>0){profit+=delta;}
					}
				}
				if(profit>mostProfit)
				{
					mostProfit=profit;
					dest=city;
				}
			}
			if(dest!=null)
			{
				logAction("Heading from "+dock.ID+" to "+dest.ID+" in hopes of profit! ("+transactions.size()+")");
				state=State.LOADING;
			}
			else
			{
				if(stuck>=5)
				{
					dest=home;
					logAction("Fuck this I'm going home to "+home.ID);
					state = State.ENROUTE;
				}
				else
				{
					stuck++;
					logAction("Staying in " + dock.ID + " for a while. (" + transactions.size() + ")");
					state = State.WAITING;
				}
			}
			break;

		case LOADING:
			String Cargo = null;
			for(String stock : stockRef.keySet())
			{
				if(!ignoreCargo.contains(stock))
				{
					int delta = dest.Price(stock) - dock.Price(stock);
					if (delta > mostProfit)
					{
						mostProfit = delta;
						Cargo = stock;
					}
				}
			}
			if(Cargo!=null)
			{
				if(Buy(dock, Cargo, dock.Price(Cargo), 1))
				{
					transactions.add(new Transaction(Cargo, dock.Price(Cargo)));
					break;
				}
				else
				{
					ignoreCargo.add(Cargo);
					break;
				}
			}
			else
			{
				if(transactions.size()>0)
				{
					ignoreCargo.clear();
					ignoreCity.clear();
					state = State.ENROUTE;
				}
				else
				{
					ignoreCity.add(dest.ID);
					logAction("Couldn't afford to go to "+dest.ID+"...");
					state = State.ASSESSING;
				}
			}
			break;
		case ENROUTE:
			boolean doneTraveling= false;
			
			posX += speed * Math.cos(Math.atan2(dest.Y()-posY,dest.X()-posX));
			posY += speed * Math.sin(Math.atan2(dest.Y()-posY,dest.X()-posX));
			
			double a = dest.X()-posX;
			double b = dest.Y()-posY;
			
			if(Math.sqrt(a*a+b*b)<speed){doneTraveling=true;}
			
			if(doneTraveling)
			{
				logAction("Arrived in "+dest.ID+" unscathed!");
				state=State.UNLOADING;
			}
			
			break;
		case UNLOADING:
			boolean doneUnloading=false;
			Transaction sale = null;
			for(Transaction xact : transactions)
			{
				if(dest.Price(xact.Resource())-xact.Price()>mostProfit)
				{
					mostProfit=xact.Price();
					sale=xact;
				}
			}
			if(sale!=null)
			{
				if(Sell(dest, sale.Resource(),dest.Price(sale.Resource()),1))
				{
					transactions.remove(sale);
					break;
				}
				else
				{
					ignoreTransactions.add(sale);
					break;
				}
			}
			else
			{
				doneUnloading=true;
			}
			if(doneUnloading)
			{
				ignoreTransactions.clear();
				dock=dest;
				dest=null;
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
	
	private void logAction(String msg)
	{
		Cell cell = row.createCell(14);
		cell.setCellValue(row.getCell(14).getStringCellValue()+" "+msg);
	}
	
	private double findDistance(City c1, City c2)
	{
		double a = c1.X()-c2.X();
		double b = c1.Y()-c2.Y();
		
		return Math.sqrt(a*a+b*b);
	}
	
	private Entry<String, City> findClosestCity()
	{
		Entry<String, City> nextCity=null;
		double amt = Double.MAX_VALUE;
		for(Entry<String, City> map : cityRef.entrySet())
		{//for every city
			if(findDistance(dock, map.getValue())<amt
					&&!map.getKey().equals(dock.ID))
			{
				nextCity=map;
			}
		}
		return nextCity;
	}
}
