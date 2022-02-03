package org.csc133.a1;

import static com.codename1.ui.CN.*;

import com.codename1.charts.util.ColorUtil;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.geom.Point;
import com.codename1.ui.layouts.*;
import com.codename1.io.*;
import com.codename1.ui.plaf.*;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose
 * of building native mobile applications using Java.
 */
public class AppMain extends Lifecycle {
    @Override
    public void runApp() {

        new Game();
    }

}

class Game extends Form implements Runnable{
    private GameWorld gw;

    final static int DISP_W = Display.getInstance().getDisplayWidth();
    final static int DISP_H = Display.getInstance().getDisplayHeight();

    public static int getSmallDim() {
        return Math.min(DISP_W, DISP_H);
    }
    public static int getLargeDim(){
        return Math.max(DISP_W, DISP_H);
    }

    public Game(){
        gw = new GameWorld();

        addKeyListener('Q', (evt) -> gw.quit());
        addKeyListener( -91, (evt) -> gw.upArrowPressed());
        addKeyListener(-92, (evt) -> gw.downArrowPressed());
        addKeyListener(-93, (evt) -> gw.leftArrowPressed());
        addKeyListener(-94, (evt) -> gw.rightArrowPressed());
        addKeyListener('f', (evt) -> gw.fightFire());
        addKeyListener('d', (evt) -> gw.drinkWater());

        UITimer timer = new UITimer(this);
        timer.schedule(200, true, this);

        this.getAllStyles().setBgColor(ColorUtil.BLACK);
        this.show();
    }

    public void paint(Graphics g) {
        super.paint(g);
        gw.draw(g);
    }

    @Override
    public void run() {
        gw.tick();
        repaint();
    }
}

class GameWorld {
    private final int NUMBER_OF_FIRES = 3;
    private Helicopter helicopter;
    private ArrayList<Fire> fires;
    private Helipad helipad;
    private River river;

    public GameWorld(){
        init();
    }

    void init() {
        helicopter = new Helicopter();
        helipad = new Helipad();
        river = new River();
        fires = new ArrayList<>();
        for(int i = 1; i <= NUMBER_OF_FIRES;i++) {
            fires.add(new Fire(i));
        }
    }

    public void quit() {

        Display.getInstance().exitApplication();
    }


    public void leftArrowPressed() {
        helicopter.steer(false);
    }

    public void rightArrowPressed(){
        helicopter.steer(true);
    }

    public void upArrowPressed(){
        helicopter.changeSpeed(true);
    }

    public void downArrowPressed(){
        helicopter.changeSpeed(false);
    }

    public void drinkWater(){
        if (helicopter.collidesWithRiver(river) == true) {
            helicopter.drinkWater();
        }
    }

    public void fightFire(){
    }

    public void draw(Graphics g) {

        helicopter.draw(g);
        river.draw(g);
        helipad.draw(g);
        for (Fire fire : fires) {
            fire.draw(g);
        }
        tick();
    }

    public void tick() {

        //if all fires out and speed is 0
        //and resting on helipad you win
        if(allFiresOut(fires) && helicopter.getSpeed() == 0 && landCopter()){
            //victory condition
            //ask to play again or quit

        //if you run out of fuel you lose
        } else if (helicopter.getFuel() <= 0) {
            //defeat
            //ask to play again or quit
        } else {
            //else grow the fires by random amount 1-10
            //and continue the game
            for(Fire fire : fires) {
                fire.grow();
            }
        }

        //move forward
        helicopter.walk();
    }

    public boolean landCopter(){
        Point copter = helicopter.getLocation();
        Point pad = helipad.getLocation();
        int pWidth = Display.getInstance().getDisplayWidth()/10;
        int pLength = Display.getInstance().getDisplayWidth()/10;

        //this conditional makes sure the location of the helicopter is
        //within the helipad
        //if copter.getX is > pad.getX but less than pad.getX + its width
        //and copter.getY is > pad.getY but less than pad.getY + its length
        if((copter.getX() > pad.getX()
            && copter.getX() < (pad.getX() + pWidth))
            && copter.getY() > pad.getY()
            && copter.getY() < pad.getY() + pLength){
            return true;
        }
        return false;
    }

    public boolean allFiresOut(ArrayList<Fire> fires) {
        for(Fire fire: fires) {
            if(fire.getSize() > 0) {
                return false;
            }
        }
        return true;
    }


}

class River {

    private Point location;

    public River(){
        init();
    }

    private void init() {
        location = new Point(0,
                             Display.getInstance().getDisplayHeight() / 3);
    }
    public void draw(Graphics g) {

        g.setColor(ColorUtil.BLUE);
        g.drawRect(location.getX(), location.getY(),
                Display.getInstance().getDisplayWidth(),
                Display.getInstance().getDisplayHeight() / 10);
    }

    public Point getLocation(){
        return location;
    }
}

class Helipad {

    private Point location;

    public Helipad(){
        init();
    }

    private void init(){

        //set the location of the helipad equal
        //to middle of screen at the bottom (9/10 of the way down)
        location = new Point(Display.getInstance().getDisplayWidth() / 2,
                             Display.getInstance().getDisplayHeight() -
                                Display.getInstance().getDisplayHeight() / 10);
        }
    public void draw(Graphics g) {

        g.setColor(ColorUtil.GRAY);

        //created variables to not repeat
        //"Display.getInstance().get------" several times
        int length = Display.getInstance().getDisplayWidth()/10;
        int width = Display.getInstance().getDisplayWidth()/10;
        int offset = Display.getInstance().getDisplayWidth()/20;
        //Because shapes are drawn from the upperleftmost point
        //I offset it by half of its width to make it look more center
        g.drawRect(location.getX() - offset,location.getY(), width, length);

        g.drawArc(location.getX()- offset,location.getY(),
                    width, length, 0, 360);
    }

    //need this specific getter to make sure
    //the helicopter and helipad spawn with each other
    public Point getLocation(){
        return location;
    }
}

class Fire {
    private Point location;
    private int size;
    int number;
    //using a number parameter to number each fire
    //to know where draw it
    public Fire(int number){

        this.number = number;
        int width = Display.getInstance().getDisplayWidth();
        int height = Display.getInstance().getDisplayHeight();

        //creating conditional statements to draw the fires in random
        //but sectioned coordinates
        //Fire 1 will always be in the upper left
        //Fire 2 will always be in the upper right
        //Fire 3 will always be in the lower bottom
        if(number == 1) {
            location = new Point(
                    new Random().nextInt(width / 10) + width / 10,
                    new Random().nextInt(height / 10) + height /10);
            size = new Random().nextInt(width / 6)+50;

        } else if (number == 2) {
            location = new Point(
                    new Random().nextInt(width / 10) + (6 * width / 10),
                    new Random().nextInt(height / 10) + height / 10);
            size = new Random().nextInt(width / 5)+75;

        } else if (number == 3) {
            location = new Point(
                    new Random().nextInt(width / 2) + width / 10,
                    new Random().nextInt(height / 10) + (7 * height / 10));
            size = new Random().nextInt(width / 4)+100;

        } else {
            size = new Random().nextInt(500)+50;
            location = new Point(new Random().nextInt(500),
                                 new Random().nextInt(500));
        }
    }

    public void draw(Graphics g) {
        g.setColor(ColorUtil.MAGENTA);
        g.fillArc(location.getX(), location.getY(), size, size, 0, 360);
    }

    public void grow(){
        size += new Random().nextInt(3);
    }

    public int getSize(){
        return size;
    }

}

class Helicopter {
    private Point location;
    private int fuel;
    private int water;
    private int speed;
    private static int maxSpeed;
    private static int maxWater;
    private int heading;
    private Helipad helipad;

    public Helicopter(){
        init();
    }

    private void init(){
        int height = Display.getInstance().getDisplayHeight() / 50;
        int width = Display.getInstance().getDisplayWidth() / 30;
        fuel = 25000;
        maxSpeed = 10;
        maxWater = 1000;
        speed = 0;
        water = 0;
        helipad = new Helipad();
        heading = 0;
        location = new Point(
                helipad.getLocation().getX() - width,
                helipad.getLocation().getY() + height);
    }

    public void setHeading(int heading){
        this.heading = heading;
    }
    public Point getLocation() {
        return location;
    }

    public int getFuel() { return fuel;}

    public int getSpeed() {return speed;}

    public void drinkWater(){
        if (speed <= 2 && water < 1000) {
            water += 100;
        }
    }

    //rather than making one method for speedup and
    //another for slowdown, I chose to make one that
    //ued a boolean parameter to either increase or decrease
    public void changeSpeed(boolean speedUp) {

        //check speed first to make sure it's
        //between 0-10 between allowing it to be changed
        if(speedUp == false && speed > 0) {
            speed -= 1;
        } else if (speedUp == true && speed < 10) {
            speed += 1;
        }
    }

    public void fight(){

    }

    public void draw(Graphics g) {
        g.setColor(ColorUtil.YELLOW);

        //drawing a filled circle and line relative to its location
        g.fillArc(location.getX(), location.getY(),50,50,0,360);
        g.drawLine(location.getX()+ location.getX()/25,
                   location.getY()+location.getY()/50,
                   location.getX()+ location.getX()/25,
                   location.getY()- location.getY()/30);
    }

    public void walk(){
        fuel = fuel - ((speed * speed) + 5);
        location.setX(location.getX() + heading );
        location.setY(location.getY() - speed * 2);
        //based on direction pointed it will determine where we steer

    }

    public void steer(boolean direction){
        if (direction) {
            heading += 15;
        } else {
            heading -= 15;
        }
    }

    public boolean collidesWithRiver(River river) {
        return river.getLocation().getY() >= this.getLocation().getY() ;
    }


}
