package marketflow;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import engine.Game;
import engine.XMLHandler;

public class Init
{
	private Map<String, City> _cityColl;
	private Map<String, Stock> _cityResColl;
	
	private Map<String, Ship> _shipColl;
	private Map<String, Stock> _shipResColl;
	
	private Map<String, Generator> _genColl;
	private Map<String, Stock> _genResColl;
	
	private Map<String, Housing> _houseColl;
	private Map<String, Stock> _houseResColl;
	
	private int scrollSpeed = 25;
	
	public BufferedImage mapImg;
	public int mapWidth;
	public int mapHeight;
	public int mapOffsetX;
	public int mapOffsetY;
	public int mapEdgeX;
	public int mapEdgeY;
	
	public static HSSFWorkbook cityBook;
	public static HSSFWorkbook shipBook;
	public static HSSFWorkbook housingBook;
	public static HSSFWorkbook generatorBook;
	
	public Init(Game i, XMLHandler xmlh)
	{
		cityBook = new HSSFWorkbook();
		shipBook = new HSSFWorkbook();
		housingBook = new HSSFWorkbook();
		generatorBook = new HSSFWorkbook();

		_cityColl = new HashMap<String, City>();
		_cityResColl = new HashMap<String, Stock>();
		
		_shipColl = new HashMap<String, Ship>();
		_shipResColl = new HashMap<String, Stock>();
		
		_genColl = new HashMap<String, Generator>();
		_genResColl = new HashMap<String, Stock>();
		
		_houseColl = new HashMap<String, Housing>();
		_houseResColl = new HashMap<String, Stock>();
		
		mapImg = Game.gfx.load("res/map.jpg");
		mapWidth=mapImg.getWidth();
		mapHeight=mapImg.getHeight();
		mapOffsetX=0;
		mapOffsetY=0;
		mapEdgeX = Game.WIDTH-mapWidth;
		mapEdgeY = Game.HEIGHT-mapHeight;
		
		xmlh.processMarketFlow(_cityColl,_shipColl,_genColl,_cityResColl,_shipResColl,_genResColl,_houseColl,_houseResColl);
		for(City c : _cityColl.values())
		{
			c.update(0, 0);
		}
	}

	public void update(int count, int tickCount)
	{
		for(City c : _cityColl.values())
		{
			c.update(count, tickCount);
		}
		for(Ship s : _shipColl.values())
		{
			s.update(count, tickCount);
		}
		for(Generator g : _genColl.values())
		{
			g.update(count, tickCount);
		}
		for(Housing h : _houseColl.values())
		{
			h.update(count, tickCount);
		}
	}
	
	public void render(Graphics g)
	{
		g.drawImage(mapImg, mapOffsetX, mapOffsetY, null, null);
		
		g.drawString("MarketFlow " + mapOffsetX + ", " + mapOffsetY, 100, 10);
		for(City c : _cityColl.values())
		{
			c.render(g);
		}
		for(Ship s : _shipColl.values())
		{
			s.render(g);
		}
	}

	public void scroll(int x, int y)
	{
		mapOffsetX+=x*scrollSpeed;
		mapOffsetY+=y*scrollSpeed;
		
		if(mapOffsetX<mapEdgeX){mapOffsetX=mapEdgeX;}
		if(mapOffsetY<mapEdgeY){mapOffsetY=mapEdgeY;}
		if(mapOffsetX>0){mapOffsetX=0;}
		if(mapOffsetY>0){mapOffsetY=0;}
	}
	
	public void Report()
	{
		FileOutputStream out;
		String reportPath = "C:\\Users\\Riggy\\IdeaProjects\\SubGame\\data\\marketflow\\reports\\";
		try
		{

			out = new FileOutputStream(new File(reportPath+"CityReport.xls"));
			cityBook.write(out);
			out.close();
			System.out.println("City Report Generated..");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			out = new FileOutputStream(new File(reportPath+"ShipReport.xls"));
			shipBook.write(out);
			out.close();
			System.out.println("Ship Report Generated..");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			out = new FileOutputStream(new File(reportPath+"HousingReport.xls"));
			housingBook.write(out);
			out.close();
			System.out.println("Housing Report Generated..");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			out = new FileOutputStream(new File(reportPath+"GeneratorReport.xls"));
			generatorBook.write(out);
			out.close();
			System.out.println("Generator Report Generated..");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
