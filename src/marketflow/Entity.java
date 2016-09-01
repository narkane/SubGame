package marketflow;

import java.awt.Graphics;
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
	protected BufferedImage img;
	HSSFSheet reportSheet;
	
	public Entity(String id, Map<String, Stock> st_ref, int x, int y)
	{
		ID = id;
		stockRef = st_ref;
		posX=x;
		posY=y;
		
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
	
	protected int rowNum = 1;
	protected Row row;
	public void update(int count, int tickCount)
	{
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
		g.drawImage(img, posX+Game.mf.mapOffsetX, posY+Game.mf.mapOffsetY, null);
		g.drawString(ID, posX+Game.mf.mapOffsetX, posY+Game.mf.mapOffsetY);
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
