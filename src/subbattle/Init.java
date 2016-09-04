package subbattle;

import java.awt.Graphics;

import engine.Game;
import engine.XMLHandler;

public class Init
{
	public Init(Game i, XMLHandler xmlh)
	{
		xmlh.processSubBattle();
	}
	
	public void update(int count)
	{

	}
	
	public void render(Graphics g)
	{
		g.drawString("SubBattle", 100, 10);
	}

	public void tick(int second) {
	}
}
