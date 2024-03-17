package src.simulator;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class Main{
	private static LangtonsAnt sim;
	static boolean run = true;
	
	//TODO: Optimize more
	private static final int width = 300;
	private static final int height = 300;
	
	static final int maxRate = 50000;
	static final int minRate = 10;
	static int rate = 100;
	
	private static final int minFPS = 1;
	private static final int maxFPS = 67;
	static int fps = run?maxFPS:minFPS;
	
	private static List<EventListener> listeners = new ArrayList<>();
	
	public static void main(String[] args) {
		sim = new LangtonsAnt(height, width);
		sim.add(height/2, width/2, 0, Color.GREEN); //single ant
		sim.add(height/2, width/2, 1, Color.BLACK); //single ant
		sim.add(height/2, width/2, 2, Color.RED); //single ant
		sim.add(height/2, width/2, 3, Color.BLUE); //single ant
		
		new GUI();
        /*class KeyListener implements EventListener {
        	KeyListener() {}
			@Override
			public void update(String key, int nothing) {
				// React to keys
				if(Integer.valueOf(key)==KeyEvent.VK_ESCAPE) System.exit(0);
			}
        	
        }
		GUI.registerListener(new KeyListener());
		*/
		class PauseButtonListener implements EventListener {
			PauseButtonListener() {}			
			@Override
			public void update(Object none) {	
				run = !run; //flip switch
				fps = run?maxFPS:minFPS; //if paused, use minFPS for rescaling
			}
			
		}
		GUI.registerListener(new PauseButtonListener());
		
		Thread refreshScreen = new Thread(() -> GUI.timedDraws());
		Thread refreshSimulation = new Thread(() -> LangtonsAnt.simulate());
		
		refreshScreen.start();
		refreshSimulation.start();
	}	
	
	public static void registerListener(EventListener listener) {
		listeners.add(listener);
	}
	
	public static void notifyListeners() {
		for (EventListener listener : listeners) {
			listener.update((Object)rate);
		}
	}
	
}
