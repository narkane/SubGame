package engine;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Graphix
{
	public BufferedImage load(String path)
	{
		try
		{
			return ImageIO.read(new File(path));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
}