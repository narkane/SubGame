package marketflow;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import engine.Game;

public class Entity
{
	protected int Credit;
	protected String ID;
	protected Map<String, Stock> stockRef;
	protected int Population;
	protected int PopulationMax = 1;
	protected int posX, posY;
	protected String Description;
	protected BufferedImage img;
	protected Rectangle hitbox;
	HSSFSheet reportSheet;
	
	public Entity(String id, String desc, Map<String, Stock> st_ref, int x, int y)
	{
		ID = id;
		Description=desc;
		stockRef = st_ref;
		posX=x;
		posY=y;

		hitbox = new Rectangle(x,y, 0, 0);

		if(this.getClass().getName().equals("marketflow.City"))
		{
			reportSheet = Init.cityBook.createSheet(ID+" Report");
		}
		else if(this.getClass().getName().equals("marketflow.Ship"))
		{
			reportSheet = Init.shipBook.createSheet(ID+" Report");
		}
		else if(this.getClass().getName().equals("marketflow.Housing"))
		{
			reportSheet = Init.housingBook.createSheet(ID+" Report");
		}
		else if(this.getClass().getName().equals("marketflow.Generator"))
		{
			reportSheet = Init.generatorBook.createSheet(ID+" Report");
		}
		else
		{
			System.out.println("WE HAVE A PROBLEM");
			System.out.println(this.getClass().getName());
		}
		
		Row row = reportSheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Time");
		cell = row.createCell(1);
		cell.setCellValue("Credit");
		cell = row.createCell(2);
		cell.setCellValue("Population");
		int colNum=3;
		try{for(String name : st_ref.keySet())
		{
			cell = row.createCell(colNum);
			cell.setCellValue(name);
			colNum++;
		}}catch(Exception e){}
	}
	private int time = 0;
	public void print(){
		String toPrint = "";
		System.out.println("/=================================\\");
		makeLine(ID, ""+time);
		makeLine("Cred: "+Credit(), "Pop: "+Population());
		System.out.println("|--Consumables--------------------|");
		makeLine("Water:    "+Resource("Water"), 		"Fish:     "+Resource("Fish"));
		makeLine("Seaweed:  "+Resource("Seaweed"), 		"Soylent:  "+Resource("Soylent"));
		makeLine("Decapoda: "+Resource("Decapoda"), 	"Jellies:  "+Resource("Jellies"));
		makeLine("Voles:    "+Resource("Water"), ".");
		System.out.println("|--Goods--------------------------|");
		makeLine("Oil:      "+Resource("Oil"), 			"Fuel:     "+Resource("Fuel"));
		makeLine("Metal:    "+Resource("Hard Metals"), 	"Parts:    "+Resource("Parts"));
		makeLine("Energy:   "+Resource("Energy"), ".");
		System.out.println("|--Luxuries-----------------------|");
		makeLine("Shells:   "+Resource("Shells"), 		"Coral:    "+Resource("Coral"));
		makeLine("Pearls:   "+Resource("Pearls"), 		"Spirits:  "+Resource("Spirits"));
		System.out.println("\\=================================/");
	}

	private void makeLine(String first, String second)
	{
		String line = "| "+first;
		int blanks = 17-line.length();
		if(blanks>0)
		{
			for(int i = 0; i < blanks; i++)
			{
				line+=" ";
			}
		}
		line+="| "+second;
		blanks = 34-line.length();
		if(blanks>0)
		{
			for(int i = 0; i < blanks; i++)
			{
				line+=" ";
			}
		}
		line+="|";
		System.out.println(line);
	}


	protected int rowNum = 1;
	protected Row row;
	public void update(int count, int tickCount)
	{
		time=tickCount;
		if(tickCount%10==0)
		{
			row = reportSheet.createRow(rowNum);
			Cell cell = row.createCell(0);
			cell.setCellValue(tickCount);
			cell = row.createCell(1);
			cell.setCellValue(Credit());
			cell = row.createCell(2);
			cell.setCellValue(Population());
			int colNum=3;
			try{
			for(Stock val : stockRef.values())
			{
				cell = row.createCell(colNum);
				cell.setCellValue(val.Resource(ID));
				colNum++;
			}
			}catch(Exception e){}
			rowNum++;
		}
	}
	
	public void render(Graphics g)
	{

		g.drawImage(img, posX+Game.mf.mapOffsetX-img.getWidth()/2, posY+Game.mf.mapOffsetY-img.getHeight()/2, null);
		g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
		g.drawString(ID, posX+Game.mf.mapOffsetX-30, posY+Game.mf.mapOffsetY-20);
	}
	
	public boolean Immigrate(Entity source, int amt)
	{
		if(source.Population()>amt&&Population<(PopulationMax-amt)){
			source.incPopulation(-amt);
			incPopulation(amt);
		}else{
			return false;
		}
		return true;
	}
	public boolean Emigrate(Entity destination, int amt)
	{
		if(Population()>amt&&destination.Population()<(destination.PopulationMax()-amt)){
			destination.incPopulation(amt);
			incPopulation(-amt);
		}else{
			return false;
		}
		return true;
	}
	public boolean Buy(Entity seller, String res, int pricePer, int amt)
	{
		int cost=pricePer*amt;
		if(Credit>=cost&&seller.Resource(res)>=amt){
			seller.incCredit(cost);
			incCredit(-cost);
			seller.incResource(res, -amt);
			incResource(res, amt);
		}else{
			return false;
		}
		return true;
	}
	public boolean Sell(Entity buyer, String res, int pricePer, int amt)
	{
		int cost=pricePer*amt;
		if(buyer.Credit()>=cost&&Resource(res)>=amt){
			incCredit(cost);
			buyer.incCredit(-cost);
			incResource(res, -amt);
			buyer.incResource(res, amt);
		}else{
			return false;
		}
		return true;
	}

	public void Description(String str){Description=str;}
	public String Description(){return Description;}
	public void Credit(int amt){Credit=amt;}
	public int Credit(){return Credit;}
	public void incCredit(int amt){Credit+=amt;}
	
	public void Population(int amt){Population=amt;}
	public int Population(){return Population;}
	public int PopulationMax(){return PopulationMax;}
	public void incPopulation(int amt){Population+=amt;}
	
	public void Resource(String rid, int amt){stockRef.get(rid).Resource(ID, amt);}
	public int Resource(String rid){return stockRef.get(rid).Resource(ID);}
	public void incResource(String rid, int amt){stockRef.get(rid).incResource(ID, amt);}
}
