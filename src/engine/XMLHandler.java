package engine;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import marketflow.City;
import marketflow.Generator;
import marketflow.Housing;
import marketflow.Init;
import marketflow.Ship;
import marketflow.Stock;

public class XMLHandler
{
	private DocumentBuilderFactory factory;
	private static DocumentBuilder builder;
	private static StringBuilder xmlSB;
	
	public XMLHandler()
	{
		factory = DocumentBuilderFactory.newInstance();
		try
		{
			builder  = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
	}
	
	private static Document read(String path)
	{
		xmlSB = new StringBuilder();
		xmlSB.append("<?xml version=\"1.0\"?> <class> </class>");
		try
		{
			File input = new File(path);
			Document doc = builder.parse(input);
			doc.getDocumentElement().normalize();
			return doc;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void setKeyMap(String path, Map<Integer, String> map)
	{
		Document doc = read(path);
		map.clear();
		NodeList nodes = doc.getElementsByTagName("Binding");
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			Element elem = (Element) node;
			map.put(Integer.parseInt(elem.getAttribute("id")), elem.getAttribute("action"));
		}
	}
	
//===___|CUSTOM FUNCTIONS|___===//
	public void processMarketFlow(Map<String, City> cc, Map<String, Ship> sc, Map<String, Generator> gc, Map<String, Stock> crc, Map<String, Stock> src, Map<String, Stock> grc, Map<String, Housing> hc, Map<String, Stock> hrc)
	{//Loads resource holdings for ships, cities, generators, etc for world map mode.
		
		String city;
		String ship;
		String gen;
		String res;
		String house;
		String desc;
		Map<String, Integer> basePrices;

		Document doc = read("data/marketflow/ResInfo.xml");
		NodeList c_nodes = doc.getElementsByTagName("City");
		for(int i = 0; i < c_nodes.getLength(); i++)
		{
			Node c_node = c_nodes.item(i);
			Element c_elem = (Element) c_node;
			
			//sets city name
			city = c_elem.getAttribute("id");
			//get description of city
			desc = c_elem.getElementsByTagName("Description").item(0).getTextContent();

			//initializes city
			cc.put(city, new City(city,							//City Name
					desc,										//Description of City
					Integer.parseInt(c_elem.getAttribute("x")),	//X Coordinate
					Integer.parseInt(c_elem.getAttribute("y")),	//Y Coordinate
					sc,											//Ship Collection
					gc,											//Generator Collection
					hc,											//Housing Collection
					crc											//City Resource Collection
			));
			//initializes city credit amount
			cc.get(city).Credit(Integer.parseInt(c_elem.getAttribute("cred")));

			//gets basePrice map ready to build
			basePrices = new HashMap<String, Integer>();

			NodeList cr_nodes = c_elem.getElementsByTagName("cRes");
			for(int j = 0; j < cr_nodes.getLength(); j++)
			{
				Node cr_node = cr_nodes.item(j);
				Element cr_elem = (Element) cr_node;
				
				//sets city resource product type
				res = cr_elem.getAttribute("id");
				basePrices.put(res, Integer.parseInt(cr_elem.getAttribute("base")));

				//initializes city resource stock (if not already)
				if(crc.get(res)==null){crc.put(res, new Stock(res));}
				//initializes city's resource stock amount
				crc.get(res).initStock(city, Integer.parseInt(cr_elem.getTextContent()));
			}

			//set city base prices
			cc.get(city).BasePrices(basePrices);
		//Ships
			NodeList s_nodes = c_elem.getElementsByTagName("Ship");
			for(int j = 0; j < s_nodes.getLength(); j++)
			{
				Node s_node = s_nodes.item(j);
				Element s_elem = (Element) s_node;
				
				//sets ship name
				ship = s_elem.getAttribute("id");
				
				//Get last known location for loading in
				String docked = s_elem.getAttribute("dock");

				//Get description of ship
				desc = s_elem.getElementsByTagName("Description").item(0).getTextContent();

				//initializes ship
				sc.put(ship, new Ship(ship,								//Ship Name
						desc,
						Integer.parseInt(s_elem.getAttribute("x")),		//X Coordinate
						Integer.parseInt(s_elem.getAttribute("y")),		//Y Coordinate
						Integer.parseInt(s_elem.getAttribute("speed")),	//Max Speed
						docked,											//Starting Location
						city,											//Home City
						cc,												//reference to City Collection
						src,											//Ship Resource Collection
						Integer.parseInt(s_elem.getAttribute("max"))	//Max Population
				));
				//initializes ship credit amount
				sc.get(ship).Credit(Integer.parseInt(s_elem.getAttribute("cred")));
				//initializes ship crew count
				sc.get(ship).Population(Integer.parseInt(s_elem.getAttribute("pop")));
				//adds ship to respective city's ship list
				cc.get(city).ships.add(ship);
				
				NodeList sr_nodes = s_elem.getElementsByTagName("sRes");
				for(int k = 0; k < sr_nodes.getLength(); k++)
				{
					Node sr_node = sr_nodes.item(k);
					Element sr_elem = (Element) sr_node;
					
					//sets ship resource product type
					res = sr_elem.getAttribute("id");
					//initializes ship resource stock (if not already)
					if(src.get(res)==null){src.put(res, new Stock(res));}
					//initializes ship's resource stock amount
					src.get(res).initStock(ship, Integer.parseInt(sr_elem.getTextContent()));
				}
			}
		//Generators
			NodeList g_nodes = c_elem.getElementsByTagName("Generator");
			for(int j = 0; j < g_nodes.getLength(); j++)
			{
				Node g_node = g_nodes.item(j);
				Element g_elem = (Element) g_node;
				
				//sets generator name
				gen = g_elem.getAttribute("id");
				//get generator type
				String gentype = g_elem.getAttribute("type");

				gc.put(gen, makeGenerator(gentype,
						gen,
						Integer.parseInt(c_elem.getAttribute("x")),		//X Coordinate
						Integer.parseInt(c_elem.getAttribute("y")),		//Y Coordinate
						city,											//Name of Home City
						cc,												//reference to City Collection
						grc												//reference to Generator Resource Stock
				));

				//initializes generator credit amount
				gc.get(gen).Credit(Integer.parseInt(g_elem.getAttribute("cred")));
				//initializes generator worker population amount
				gc.get(gen).Population(Integer.parseInt(g_elem.getAttribute("pop")));
				//adds generator to respective city's generator list
				cc.get(city).generators.add(gen);
				
				NodeList gr_nodes = g_elem.getElementsByTagName("gRes");
				for(int k = 0; k < gr_nodes.getLength(); k++)
				{
					Node gr_node = gr_nodes.item(k);
					Element gr_elem = (Element) gr_node;

					//sets generator resource product type
					res = gr_elem.getAttribute("id");
					//initializes generator resource stock (if not already)
					if(grc.get(res)==null){grc.put(res, new Stock(res));}
					//initializes generator's resource stock amount
					grc.get(res).initStock(gen, Integer.parseInt(gr_elem.getTextContent()));
				}
			}
		//Housing
			NodeList h_nodes = c_elem.getElementsByTagName("Housing");
			for(int j = 0; j < h_nodes.getLength(); j++) {
				Node h_node = h_nodes.item(j);
				Element h_elem = (Element) h_node;

				//sets housing unit name
				house = h_elem.getAttribute("id");

				//initializes housing unit
				hc.put(house, new Housing(house,                        //Name of House
						"",
						Integer.parseInt(c_elem.getAttribute("x")),        //X Coordinate
						Integer.parseInt(c_elem.getAttribute("y")),        //Y Coordinate
						city,                                            //Name of Home City
						cc,                                             //reference to City Collection
						hrc,                                            //reference to Housing Resource Stock
						Integer.parseInt(h_elem.getAttribute("max"))    //Max Population of Dwellers
				));
				//initializes housing unit's credit amount
				hc.get(house).Credit(Integer.parseInt(h_elem.getAttribute("cred")));
				//initializes housing resident population
				hc.get(house).Population(Integer.parseInt(h_elem.getAttribute("pop")));
				//initializes housing resident population
				cc.get(city).incPopulationMax(Integer.parseInt(h_elem.getAttribute("max")));
				//adds using unit to respective city's housing list
				cc.get(city).housing.add(house);

				NodeList hr_nodes = h_elem.getElementsByTagName("hRes");
				for (int k = 0; k < hr_nodes.getLength(); k++) {
					Node hr_node = hr_nodes.item(k);
					Element hr_elem = (Element) hr_node;

					//sets housing resource type
					res = hr_elem.getAttribute("id");
					//initializes housing resource stock (if not already)
					if (hrc.get(res) == null) {
						hrc.put(res, new Stock(res));
					}
					//initializes housing unit's resource stock amount
					hrc.get(res).initStock(house, Integer.parseInt(hr_elem.getTextContent()));
				}
			}
		}
	}

	private Generator makeGenerator(
		String type,
		String id,
		int x,
		int y,
		String city,
		Map<String, City> cc,
		Map<String, Stock> grc
	)
	{
		Document doc = read("data/marketflow/GenInfo.xml");
		NodeList nodes = doc.getElementsByTagName("Gen");
		Element elem = null;
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			elem = (Element) node;
			if(elem.getAttribute("id").equals(type)){break;}
		}

		String[] inputs = new String[]{};
		try{NodeList input_nodes = elem.getElementsByTagName("Input");
		inputs = new String[input_nodes.getLength()];
		for(int k = 0; k < input_nodes.getLength(); k++)
		{//find inputs (ingredients) required for creating product...?
			Node input_node = input_nodes.item(k);
			Element input_elem = (Element) input_node;

			inputs[k]=input_elem.getTextContent();
		}}catch(Exception e){}

		return new Generator(id,
				elem.getElementsByTagName("Description").item(0).getTextContent(),
				x,
				y,
				Integer.parseInt(elem.getAttribute("cost")),
				Integer.parseInt(elem.getAttribute("out")),
				inputs,
				elem.getAttribute("product"),
				city,
				cc,
				grc,
				Integer.parseInt(elem.getAttribute("time")),
				Integer.parseInt(elem.getAttribute("max"))
		);
	}
	public void processSubBattle()
	{
		// TODO Auto-generated method stub
	}
	public void processMyEstate()
	{
		// TODO Auto-generated method stub
	}
}
