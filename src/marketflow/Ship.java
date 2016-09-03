package marketflow;

import java.awt.*;
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
	
	private int speed = 0;
	private int maxspeed;
	
	public Ship(String id, String desc, int x, int y, int spd, String location, String alliance, Map<String, City> c_ref, Map<String, Stock> ref, int pm)
	{
		super(id, desc, ref, x, y);
		PopulationMax=pm;
		cityRef=c_ref;
		home=cityRef.get(alliance);
		dock=cityRef.get(location);
		dest=null;
		maxspeed=spd;
		img = Game.gfx.load("res/ship.png");
		hitbox=new Rectangle(posX-img.getWidth()/2, posY-img.getHeight()/2, img.getWidth(), img.getHeight());
		//Game.clickables.put(this,hitbox);
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

	public int X(){return posX;}
	public int Y(){return posX;}

	int stallCount = 0;
	int stallAmt = 200;

	ArrayList<Integer> ingoreTransaction = new ArrayList<>();
	ArrayList<Transaction> transactions = new ArrayList<>();
	int maxTransactions=100;

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
			dest=null;//The city you will be travelling to
			for (City city : cityRef.values())
			{//check each city
				//Ignore the city you're in
				if(dock.ID.equals(city.ID)){continue;}

				//TODO: prevent all traders going to the same place

				int profit=0;//max amount of money you could make off this city
				if(transactions.size()<maxTransactions)
				{//don't bother checking purchase prices if your cargo is full
				for(String stock : stockRef.keySet())
				{//check each resource
					//ignore resources that your dock city doesn't have
					if(dock.Resource(stock)<=0){continue;}
					//ignore resources you can't afford
					if(dock.Price(stock)>Credit){continue;}

					//find the profit to be made
					int delta = city.Price(stock) - dock.Price(stock);
					//add each resource profit to your max profit
					if (delta > 0) {profit += delta;}
				}}
				for (Transaction xact : transactions)
				{//check your record books! Maybe you can sell stuff you already have.
					//find profit to be made
					int delta = city.Price(xact.Resource()) - xact.Price();
					//add each resource profit to your max profit
					if (delta > 0) {profit += delta;}
				}
				if(profit>mostProfit)
				{//if you've found a better city to go to..
					mostProfit=profit;
					dest=city;
				}
			}
			if(dest==null)
			{//if you haven't found a city to go to...
				if(dock.ID.equals(home.ID))
				{//if you're already home.. just chill.
					state = State.WAITING;
					break;
				}
				//other wise.. head home to kill time for a bit
				dest = home;
				state = State.ENROUTE;
			}
			else
			{//start loading up for the trip!
				state=State.LOADING;
			}
			break;

		case LOADING:
			String Cargo = null;//The cargo you will buy this tick.
			//if you've got too much cargo already... just go!
			if(transactions.size()>=maxTransactions){
				state = State.ENROUTE;
				break;
			}

			for(String stock : stockRef.keySet())
			{//for each resource
				//ignore resources that your dock city doesn't have
				if(dock.Resource(stock)<=0){continue;}
				//ignore resources you can't afford
				if(dock.Price(stock)>Credit){continue;}

				//find the profit to be made
				int delta = dest.Price(stock) - dock.Price(stock);
				if (delta > mostProfit)
				{//If this is the best transaction you can make...
					mostProfit = delta;
					Cargo = stock;
				}
			}
			if(Cargo==null)
			{
				state = State.ENROUTE;
				break;
			}

			if(Buy(dock, Cargo, dock.Price(Cargo), 1))
			{
				transactions.add(new Transaction(tickCount, Cargo, dock.Price(Cargo)));
			}
			else
			{
				System.out.println(ID+": Failed to buy "+Cargo);
			}
			break;
		case ENROUTE:
			//Go go go!
			double angle = Math.atan2(dest.Y() - posY, dest.X() - posX);

			posX += speed * Math.cos(angle);
			posY += speed * Math.sin(angle);

			hitbox.setLocation(posX+Game.mf.mapOffsetX-img.getWidth()/2, posY+Game.mf.mapOffsetY-img.getHeight()/2);

			double a = dest.X()-posX;
			double b = dest.Y()-posY;
			if(Math.sqrt(a*a+b*b)<speed*10)
			{//you made it!
				speed--;
				if(speed==0){state=State.UNLOADING;}
			}
			else
			{
				speed++;
				if(speed>=maxspeed){speed=maxspeed;}
			}
			
			break;
		case UNLOADING:
			Transaction sale = null;//the good you will be selling!
			for(Transaction xact : transactions)
			{//for each transaction in your records
				//ignore transaction if the city can't afford it.
				if(dest.Credit()<dest.Price(xact.Resource())){continue;}

				int delta = dest.Price(xact.Resource())-xact.Price();
				if(delta>mostProfit)
				{
					mostProfit=delta;
					sale=xact;
				}
			}
			if(sale==null)
			{//if you're all out of profitable transactions to be had..
				dock=dest;
				dest=null;
				state=State.ASSESSING;
				break;
			}

			if(Sell(dest, sale.Resource(),dest.Price(sale.Resource()),1))
			{//try to tell the stupid stuff
				transactions.remove(sale);
			}
			else
			{//What? for some reason you couldn't sell that resource.. so don't keep trying.
				System.out.println(ID+": Failed to sell "+sale.Resource());
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
