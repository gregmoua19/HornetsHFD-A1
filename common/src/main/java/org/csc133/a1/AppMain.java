//Greg Moua 3960
package org.csc133.a1;

import static com.codename1.ui.CN.*;

import com.codename1.charts.util.ColorUtil;
import com.codename1.system.Lifecycle;
import com.codename1.ui.*;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.geom.Point;
import com.codename1.ui.layouts.*;
import com.codename1.io.*;
import com.codename1.ui.plaf.*;
import com.codename1.ui.util.Resources;
import com.codename1.ui.util.UITimer;

import javax.swing.*;
import javax.swing.border.Border;
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
    private int increment;

    public GameWorld(){
        init();
    }

    void init() {
        helicopter = new Helicopter();
        helipad = new Helipad();
        river = new River();
        fires = new ArrayList<>();

        for(int i = 0; i < NUMBER_OF_FIRES;i++) {
            fires.add(new Fire());

            if(i == 0) {
                fires.get(i).setLocation(
                    new Point(
                        new Random().nextInt(
                            Game.DISP_W / 10) + Game.DISP_W / 10,
                        new Random().nextInt(
                            Game.DISP_H / 10) + Game.DISP_H /10));

                fires.get(i).setSize(
                    new Random().nextInt(
                        Game.DISP_W / 10)+50);

            } else if (i == 1) {
                fires.get(i).setLocation(
                    new Point(
                        new Random().nextInt(
                            Game.DISP_W / 10) + (6 * Game.DISP_W / 10),
                        new Random().nextInt(
                            Game.DISP_H / 10) + Game.DISP_H / 10));

                fires.get(i).setSize(
                    new Random().nextInt(
                            Game.DISP_W / 10)+75);
            } else {
                fires.get(i).setLocation(
                    new Point(
                        new Random().nextInt(
                            Game.DISP_W / 2) + Game.DISP_W / 10,
                        new Random().nextInt(
                            Game.DISP_H / 10) + (7 * Game.DISP_H / 10)));

                fires.get(i).setSize(
                    new Random().nextInt(
                        Game.DISP_W / 10)+100);
            }
            }
        increment = 0;
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
        if (helicopter.collidesWithRiver(river)) {
            helicopter.drinkWater();
        }
    }

    public void fightFire(){
        for (Fire fire: fires) {
            helicopter.fight(fire);
        }
    }

    public void draw(Graphics g) {
        g.clearRect(0,0, Game.DISP_W, Game.DISP_H);
        helicopter.draw(g);
        river.draw(g);
        helipad.draw(g);
        for (Fire fire : fires) {
            fire.draw(g);
        }
        tick();
        g.setFont(Font.createSystemFont(CN.FACE_MONOSPACE,
                CN.STYLE_PLAIN,
                CN.SIZE_MEDIUM));
    }

    public void tick() {

        //if all fires out and speed is 0
        //and resting on helipad you win
        if (allFiresOut(fires) && helicopter.getSpeed() == 0 && landCopter()) {
            //victory condition
            //ask to play again or quit

            //create new dialog and create new components
            Dialog victory = new Dialog("You win!");
            victory.setLayout(new BorderLayout());
            Button repeat = new Button("Play again");
            Button quit = new Button("Quit");
            Dialog score = new Dialog("Your score: " + helicopter.getFuel());

            //add the components to the dialog
            victory.add(BorderLayout.EAST, repeat);
            victory.add(BorderLayout.WEST, quit);
            victory.add(BorderLayout.NORTH, score);
            victory.show();

            //action listeners for the events to restart or quit game
            repeat.addActionListener(actionEvent -> init());
            quit.addActionListener(actionEvent -> quit());

            //if you run out of fuel you lose
        } else if (helicopter.getFuel() <= 0) {
            //defeat
            //ask to play again or quit

            //repeat same process as win condition
            //except this time no score printing
            Dialog defeat = new Dialog("You Lose");
            defeat.setLayout(new BorderLayout());
            Button repeat = new Button("Play again");
            Button quit = new Button("Quit");

            defeat.add(BorderLayout.EAST, repeat);
            defeat.add(BorderLayout.WEST, quit);
            defeat.show();
        } else {

            //else grow the fires by random amount 1-10
            //every 5 ticks and continue the game
            for (Fire fire : fires) {
                increment++;
                if (increment >= 5) {
                    increment = 0;
                    fire.grow();
                }
            }
        }

        //move around
        helicopter.walk();
    }

    private void playAgain(boolean replay) {
        if (replay) {
            init();
        } else {
            quit();
        }
    }

    public boolean landCopter(){
        Point copter = helicopter.getLocation();
        Point pad = helipad.getLocation();
        int pWidth = Game.DISP_W/10;
        int pLength = Game.DISP_W/10;

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
                Game.DISP_H / 3);
    }
    public void draw(Graphics g) {

        g.setColor(ColorUtil.BLUE);
        g.drawRect(location.getX(), location.getY(),
                Game.DISP_W,
                Game.DISP_H / 10);
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
        location = new Point(   Game.DISP_W / 2,
                             Game.DISP_H -
                                     Game.DISP_H / 10);
        }
    public void draw(Graphics g) {

        g.setColor(ColorUtil.GRAY);


        int length = Game.DISP_W/20;
        int width = Game.DISP_W/20;
        int offset = Game.DISP_W/20;

        //Because shapes are drawn from the upper   leftmost point
        //I offset it by half of its width to make it look more center
        g.drawRect(location.getX() ,location.getY(), width, length);

        g.drawArc(location.getX(),location.getY(),
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

    public Fire(){
        init();
    }

    private void init(){
        size = new Random().nextInt(500)+50;
        location = new Point(new Random().nextInt(Game.DISP_W),new Random().nextInt(Game.DISP_H));
    }
    public void draw(Graphics g) {
        g.setColor(ColorUtil.MAGENTA);
        if(size > 0) {
            g.fillArc(location.getX(), location.getY(), size, size, 0, 360);
            g.drawString ("" + size,
                    location.getX() + size,
                    location.getY() + size);
        }
    }

    public Point getLocation() {
        return location;
    }
    public void grow(){
        size += new Random().nextInt(3);
    }

    public int getSize(){
        return size;
    }

    public void setSize(int size) { this.size = size;}

    public void setLocation(Point point){

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
    private Point lineLocation;
    private double radianHeading;
    private int x;
    private int y;
    public Helicopter(){
        init();
    }

    private void init(){
        int height = Game.DISP_H / 50;
        int width = Game.DISP_W / 30;
        radianHeading = 0;
        fuel = 25000;
        maxSpeed = 10;
        maxWater = 1000;
        speed = 0;
        water = 0;
        helipad = new Helipad();
        heading = 0;
        x = 0;
        y = 0;
        location = new Point(
                helipad.getLocation().getX() + width/2,
                helipad.getLocation().getY() + height);
        lineLocation = new Point(
                location.getX() + 25,
                location.getY() - 60
        );
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
    //use a boolean parameter to either increase or decrease
    public void changeSpeed(boolean speedUp) {

        //check speed first to make sure it's
        //between 0-10 between allowing it to be changed
        if(speedUp == false && speed > 0) {
            speed -= 1;
        } else if (speedUp == true && speed < 10) {
            speed += 1;
        }
    }

    public void fight(Fire fire){
        if (collidesWithFire(fire)) {
            water -= fire.getSize();
            fire.setSize(-water);
            if (water < 0) {
                water = 0;
            }
        }
    }

    public void draw(Graphics g) {
        g.setColor(ColorUtil.YELLOW);

        //drawing a filled circle and line relative to its location
        g.fillArc(location.getX(), location.getY(),50,50,0,360);
        int x = location.getX() + 25;
        int y = location.getY() - 200;
        g.drawLine(location.getX() + 25,
                   location.getY() + 25,

                //x1 y1 guarantees that the line starts
                //in the center of the circle but the
                //x2 y2 are dictated by the angle of the heading
                lineLocation.getX(),
                lineLocation.getY());
        g.drawString("Water: " + water,
                location.getX(),
                location.getY()  + 100);

        g.drawString("Fuel: " + fuel,
                location.getX(),
                location.getY() + 60);
    }

    public void walk(){
        fuel = fuel - ((speed * speed) + 5);

        //only move left and right if speed is greater than 0
        //this will not change the direction that the line faces
        if(speed > 0) {
            if (heading <= 180) {
                location.setX(location.getX() + speed * 2);
            } else {
                location.setX(location.getX() - speed * 2);
            }
        }

        if (!(heading > 60 && heading < 270)) {
            location.setY(location.getY() - speed * 2);
        } else {
            location.setY(location.getY() + speed * 2);
        }
        lineLocation.setX((int)(location.getX() + 60 * Math.cos(radianHeading)));
        lineLocation.setY((int)(location.getY() + 60 * Math.sin(radianHeading)));

        //based on direction pointed it will determine where we steer

    }

    public void steer(boolean direction){
        //turn right so +15
        if(direction) {

            //conditionals to change heading by 15 degrees
            //once it hits 360 it will jump to 15
            if(heading == 360 || heading == 0) {
                heading = 15;
            } else {
                heading += 15;
            }
        //turn left so -15
        } else {

            //conditionals to change heading by 15 degrees
            //once it hits 0 it will jump to 345
            if (heading == 0 || heading == 360) {
                heading = 345;
            } else {
                heading -= 15;
            }
        }

        radianHeading = Math.toRadians(heading);
        System.out.println("Heading: " + heading);
        System.out.println("Heading post radians: " + radianHeading);
        System.out.println("Cos: " + Math.cos(radianHeading) * 15);
        System.out.println("Sin: " + Math.sin(radianHeading) * 15);
    }

    public boolean collidesWithRiver(River river) {
        int YRiver = river.getLocation().getY();
        int dispHeight = Game.DISP_H / 10;
        return (YRiver <= this.getLocation().getY()) &&
                YRiver + dispHeight >= this.getLocation().getY();
    }

    public boolean collidesWithFire(Fire fire) {
        int fireXLoc = fire.getLocation().getX();
        int fireYLoc = fire.getLocation().getY();
        return (fireYLoc <= location.getY())
                && (fireYLoc + fire.getSize() >= location.getY())
                && (fireXLoc <= location.getX())
                && (fireXLoc + fire.getSize() >= location.getX());
    }
}
