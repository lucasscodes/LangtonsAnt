package src.simulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LangtonsAnt {
	private static int width;
	private static int height;
	public static Color[][] field;
	private static ArrayList<Object[]> ants;
	public static List<Object[]> draw = new LinkedList<Object[]>();
	
	public LangtonsAnt(int x, int y) {
		width = x;
		height = y;
		field = new Color[height][width];
		for (int i = 0; i < field.length; i++) {
		    for (int j = 0; j < field[i].length; j++) {
		    	field[i][j] = Color.WHITE;
		    }
		}
		ants = new ArrayList<Object[]>();
	}
	
	public void add(int y, int x, int dir, Color col) {
		assert (col!=Color.WHITE) : "A ant cannot have the background color!";
		ants.add(new Object[]{y,x,dir,col});
	}
	
	public static void step() throws IllegalArgumentException{
		ArrayList<Object[]> nextAnts = new ArrayList<Object[]>();
		for (Object[] ant : ants) {
			//unpack
			int y = (int) ant[0];
			int x = (int) ant[1];
			int dir = (int) ant[2];
			Color col = (Color) ant[3];
			//turn with field-color
			if (field[y][x]==Color.WHITE) dir += 1; 
			else dir -= 1; 
			//warp around dir
			dir = (dir+4)%4;
			//flip field
			field[y][x] = field[y][x]==Color.WHITE?col:Color.WHITE;
			//go on
			int[] next;
			switch (dir) {
				case 0:
					next = new int[]{y-1,x};
					break;
				case 1:
					next = new int[]{y,x+1};
					break;
				case 2:
					next = new int[]{y+1,x};
					break;
				case 3:
					next = new int[]{y,x-1};
					break;
				default:
					throw new IllegalArgumentException(Integer.toString(dir)+" is not in range [0,3]!");
			}
			//wrap around pos
			if (next[0]<0) next[0] += height;
			if (next[0]>=height) next[0] -= height;
			if (next[1]<0) next[1] += width;
			if (next[1]>=width) next[1] -= width;
			//now add as ant++
			Object[] newAnt = {next[0],next[1],dir,col};
			nextAnts.add(newAnt);
		}
		ants = nextAnts; //use all new ones now
		// for (int[] row:field) System.out.println(Arrays.toString(row));
	}
	
	//hmm to much overhead or not working yet?
	public void parallelStep() {
	    ants = (ArrayList<Object[]>) ants.parallelStream().map(ant -> {
	        //unpack
	        int y = (int) ant[0];
	        int x = (int) ant[1];
	        int dir = (int) ant[2];
	        Color col = (Color) ant[3];
	        //turn with field-color
            if (field[y][x]==Color.WHITE) dir += 1; 
            else dir -= 1; 
            //flip field
            field[y][x] = field[y][x]==Color.WHITE?col:Color.WHITE;
	        //warp around dir
	        if (dir==0) dir += 4;
	        else if (dir==5) dir -= 4;
	        //go on
	        int[] next;
	        switch (dir) {
	            case 1:
	                next = new int[]{y-1,x};
	                break;
	            case 2:
	                next = new int[]{y,x+1};
	                break;
	            case 3:
	                next = new int[]{y+1,x};
	                break;
	            case 4:
	                next = new int[]{y,x-1};
	                break;
	            default:
	                next = new int[]{y,x};
	        }
	        //wrap around pos
	        if (next[0]<0) next[0] += height;
	        if (next[0]>=height) next[0] -= height;
	        if (next[1]<0) next[1] += width;
	        if (next[1]>=width) next[1] -= width;
	        //now add as ant++
	        return new Object[]{next[0],next[1],dir,col};
	    }).collect(Collectors.toList());
	}
	
	public static void simulate() {
		long start = System.nanoTime();
		//int lag = 0;
		while (true) { //window not closed, wait for next time step and evaluate
			//when to execute next time
			start += 1000000000/src.simulator.Main.rate;
			long t = System.nanoTime();
			//need to wait
			if (t<start) {
				long nanos = start-t;
				try {Thread.sleep(nanos/1000000, (int)nanos%1000000);} //calc delta
				catch (InterruptedException e) {e.printStackTrace();};
				//lag = 0; //reset lag growth
			}
			/*
			else { //simulation is lagging behind
				long delta = t-start; //nanoseconds behind
				int decisec = (int)(delta/100000000);
				if (decisec>lag) { //growing lag!
					lag = decisec;
					if (rate>1) { //can reduce simulation rate		
						rate = (int)(rate/1.2);
						notifyListeners(); //update the gui elements
					}
				}
			}*/
			//simulation running and needing a step?
			if (src.simulator.Main.run) step(); 
			//if (run) simulator.parallelStep(); //WARNING: this seems much slower for reasonable ant counts
		}
	}
}
