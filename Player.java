package marketflow;

import engine.Game;
import misc.*;

import java.awt.*;
import java.util.Map;

/**
 * Created by Nark on 9/3/2016.
 */
public class Player extends Entity
{
    private boolean up = false;
    private boolean down = false;
    private boolean left = false;
    private boolean right = false;

    private Vector oldV = new Vector();        // current vector
    private Vector newV = new Vector();        // destination vector

    private Timer timer = new Timer();

    public Player(String id, String desc, Map<String, Stock> st_ref, int x, int y)
    {
        super(id, desc, st_ref, x, y);
        img = Game.gfx.load("res/ship.png");
        //hitbox=new Rectangle(posX-img.getWidth()/2, posY-img.getHeight()/2, img.getWidth(), img.getHeight());
    }

    public void update(int count)
    {
        timer.incTime();

        System.out.println("NEW: "+newV().magnitude());
        System.out.println(oldV().magnitude());
        System.out.println(up+" "+down);
        //temp max speed added
        if(up && newV().magnitude()<15 && (timer.time() % 40 == 0)){newV().incMagnitude(1);}//else{newV().magDrop();}
        if(down && newV().magnitude()>-15 && (timer.time() % 40 == 0)){newV().incMagnitude(-1);}//else{newV().magDrop();}
        if((newV().magnitude() > oldV().magnitude()))
        {
            oldV().incMagnitude(1);
        }else if((newV().magnitude() < oldV().magnitude()))
        {
            oldV().incMagnitude(-1);
        }


        if(!up && !down && (timer.time() % 40 == 0))
        {
            newV().magDrop();
        }

        if(left){oldV().incAngle(-0.01);}
        if(right){oldV().incAngle(0.01);}

        posX = (Game.WIDTH/2)-Game.mf.mapOffsetX;
        posY = (Game.HEIGHT/2)-Game.mf.mapOffsetY;
    }

    public void render(Graphics g)
    {
        super.render(g);
    }

    public void up(boolean u){up=u;}
    public boolean up(){return up;}
    public void down(boolean d){down=d;}
    public boolean down(){return down;}
    public void left(boolean l){left=l;}
    public boolean left(){return left;}
    public void right(boolean r){right=r;}
    public boolean right(){return right;}

    public Vector oldV(){return oldV;}
    public Vector newV(){return newV;}
}
