package engine;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Keyboard implements KeyListener
{
	Game instance;
	KeyMap kmap;
	public Keyboard(Game instance, KeyMap km)
	{
		kmap = km;
		instance.addKeyListener(this);
		instance.setFocusable(true);
		instance.setFocusTraversalKeysEnabled(false);
	}
	
	public class Key
	{
		private boolean keyStatus = false;
		
		public void setKeyStatus(boolean input)
		{
			this.keyStatus = input;
		}
		
		public boolean getKeyStatus()
		{
			return keyStatus;
		}
		
	}
	
	public void keyPressed(KeyEvent e) 
	{	
		kmap.press(e.getKeyCode());
	}

	
	public void keyReleased(KeyEvent e) 
	{
		
	}

	public void keyTyped(KeyEvent e)
	{
		
	}
	
	
}