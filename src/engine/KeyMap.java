package engine;

import java.util.HashMap;
import java.util.Map;

public class KeyMap
{
	private Map<Integer, String> _keymap;
	private XMLHandler _xmlh;
	public KeyMap(XMLHandler xmlh)
	{
		_xmlh = xmlh;
		_keymap = new HashMap<Integer, String>();
		mapKeys(Game.State.UI);
	}
	
	public void mapKeys(Game.State state)
	{
		switch (state)
		{
		case UI:
			_xmlh.setKeyMap("data/userinterface/KeyMap.xml", _keymap);
			break;
		case MARKETFLOW:
			_xmlh.setKeyMap("data/marketflow/KeyMap.xml", _keymap);
			break;
		case SUBBATTLE:
			_xmlh.setKeyMap("data/subbattle/KeyMap.xml", _keymap);
			break;
		case MYESTATE:
			_xmlh.setKeyMap("data/myestate/KeyMap.xml", _keymap);
			break;
		}
	}
	
	//TODO: Add interface for changing keybindings
	
	public void press(int c)
	{
		if(_keymap.containsKey(c))
		{
			switch(_keymap.get(c))
			{
			case "scrollLeft":
				Game.mf.scroll(1, 0);
				break;
			case "scrollUp":
				Game.mf.scroll(0, 1);
				break;
			case "scrollRight":
				Game.mf.scroll(-1, 0);
				break;
			case "scrollDown":
				Game.mf.scroll(0, -1);
				break;
			
			case "SwitchToUI":
				Game.state(Game.State.UI);
				break;
			case "SwitchToMF":
				Game.state(Game.State.MARKETFLOW);
				break;
			case "SwitchToSB":
				Game.state(Game.State.SUBBATTLE);
				Game.mf.Report();
				break;
			case "SwitchToME":
				Game.state(Game.State.MYESTATE);
				break;
			}
		}
		else
		{
			System.out.println("KeyNotBound: "+c);
		}
	}
}
