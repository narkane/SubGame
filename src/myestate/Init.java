package myestate;

import java.awt.Graphics;

import engine.Game;
import engine.XMLHandler;

public class Init
{

	public Init(Game i, XMLHandler xmlh)
	{
		xmlh.processMyEstate();
	}

	public void update(int count)
	{

	}
	
	public void render(Graphics g)
	{
		g.drawString("MyEstate", 100, 10);
	}

	public void tick(int count) {
	}
}
