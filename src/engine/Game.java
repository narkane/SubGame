package engine;
import marketflow.Entity;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

//Version 1.0

public class Game extends Canvas implements Runnable
{
	//TODO: Remove this.. temporary
	public static Map<Entity, Rectangle> clickables = new HashMap<>();

	private static final long serialVersionUID = 1L;
	public static int WIDTH = 1700, HEIGHT = 900;
	public static double SCALE = 1.0;		//zoom value
	public static boolean running = false;	//game running
	public Thread gameThread;
	static final int tickLength = 2;
	
	private BufferedImage spriteSheet;		
	public static enum State
	{
		UI,
		MARKETFLOW,
		SUBBATTLE,
		MYESTATE
	}
	private static State state = State.MARKETFLOW;
	
	
	public static Graphix gfx;			// reads an image in res folder
	XMLHandler xmlh;
	SpriteSheet ss;				// cuts an image out of sprite sheet
	Mouse mouse;				// Everything that you could possibly do with a mouse
	Keyboard keyboard;			// Keyboard stuff.
	static KeyMap keymap;		//Keyboard Bindings
	
	public static ui.Init ui;					//Menu mode.
	public static marketflow.Init mf;			//World map mode.
	public static subbattle.Init sb;			//Combat mode.
	public static myestate.Init me;			//City builder mode.
	
	public void init()			//Initialized (runs ONCE at beginning of program)
	{
		xmlh = new XMLHandler();
		gfx = new Graphix();
		//spriteSheet = gfx.load("SpriteSheet.png");
		ss = new SpriteSheet(spriteSheet);
		mouse = new Mouse(this);
		keymap = new KeyMap(xmlh);
		keyboard = new Keyboard(this, keymap);
		
		ui = new ui.Init(this);
		mf = new marketflow.Init(this, xmlh);
		sb = new subbattle.Init(this, xmlh);
		me = new myestate.Init(this, xmlh);
	}
	
	public static void state(State s)
	{
		state = s;
		count = count%tickLength;
		tickCount = 0;
		keymap.mapKeys(s);
	}
	
	public synchronized void start()			//starts game
	{
		if(running)return;
		
		running = true;
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public synchronized void stop()				//stops game
	{
		if(!running)return;
		
		running = false;
		try
		{
			gameThread.join();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{
		long lastTime = System.nanoTime();
		final double amountOfTicks = 100D;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		
		while(running)
		{
			long now = System.nanoTime();
			delta += (now-lastTime)/ns;
			lastTime = now;
			if(delta >= 1)
			{
				tick();
				delta--;
			}
			render();
		}
		stop();
	}
	
	private static int count;
	private static int tickCount = 0;
	public void tick()
	{
		count++;
		if(count%tickLength==0)
		{
			switch(state)
			{
			case UI:
				ui.update(count, tickCount);
				break;
			case MARKETFLOW:
				mf.update(count, tickCount);
				break;
			case SUBBATTLE:
				sb.update(count, tickCount);
				break;
			case MYESTATE:
				me.update(count, tickCount);
				break;
			}
			tickCount++;
			count=0;
		}	
	}
	
	public void render()
	{
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null)
		{
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		
		//RENDER START
		g.fillRect(0, 0, (int)(WIDTH*SCALE), (int)(WIDTH*SCALE));
		
		//---TEXT ON SCREEN---
		
		
		switch(state)
		{
		case UI:
			ui.render(g);
			break;
		case MARKETFLOW:
			mf.render(g);
			break;
		case SUBBATTLE:
			sb.render(g);
			break;
		case MYESTATE:
			me.render(g);
			break;
		}
		
		g.setColor(Color.ORANGE);
		g.drawString("Time: "+String.format("%02d", count)+" - "+tickCount, 10, 10);
		
		//RENDER END
		
		g.dispose();
		bs.show();
	}
	
	public static void main(String[] args)
	{
		Game game = new Game();
		game.setPreferredSize(new Dimension((int)(WIDTH*SCALE), (int)(HEIGHT*SCALE)));
		game.setMaximumSize(new Dimension((int)(WIDTH*SCALE), (int)(HEIGHT*SCALE)));
		game.setMinimumSize(new Dimension((int)(WIDTH*SCALE), (int)(HEIGHT*SCALE)));
		
		JFrame frame = new JFrame("Sub Salutem");
		frame.setSize((int)(WIDTH*SCALE), (int)(HEIGHT*SCALE));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.add(game);
		
		game.init();
		
		game.start();
	}
	
}