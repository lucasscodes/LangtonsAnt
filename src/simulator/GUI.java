package src.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;        

public class GUI {
	private static List<EventListener> listeners = new ArrayList<>();
	static JPanel drawField;
	static ArrayList<Object[]> draw = new ArrayList<Object[]>();
	static int oldRate = src.simulator.Main.rate;
	static Color[] old;

    public GUI() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
	public static void registerListener(EventListener listener) {
		listeners.add(listener);
	}
	
	public static void notifyListeners(Object event) {
		for (EventListener listener : listeners) {
			listener.update(event);
		}
	}
	
	public static void draw(Color[][] values) {		
		int height = values.length;
		int width = values[0].length;
		
		//System.out.println("x:"+field.getWidth()+" y:"+field.getHeight());
		float boxH = (float) drawField.getHeight() / height;
		float boxW = (float) drawField.getWidth() / width;
		//System.out.println(drawField.getWidth()+"/"+width+"="+boxW);
		
		//for (int[] e:draw) System.out.print(Arrays.toString(e));
		//System.out.println();
		
		//TODO: Only send only new ones!?
		ArrayList<Object[]> draw2 = new ArrayList<Object[]>();
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				Object[] box = {(int)(y*boxH),(int)(x*boxW),(int)boxW+1,(int)boxH+1,values[y][x]};
				draw2.add(box);
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
        		draw = draw2; //only overwrite when ready
        		drawField.repaint();
            }
        });
	}
   
    public static void timedDraws() {
		long start = System.nanoTime();
		while (true) { //window not closed, wait for next time step and evaluate
			//when to execute next time
			start += 1000000000/src.simulator.Main.fps;
			long t = System.nanoTime();
			//need to wait
			if (t<start) {
				long nanos = start-t;
				try {Thread.sleep(nanos/1000000, (int)nanos%1000000);} //calculate delta
				catch (InterruptedException e) {e.printStackTrace();};
			}
			//simulation running and needing a step?
			if (drawField!=null) draw(src.simulator.LangtonsAnt.field); 
		}
	}

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
    	
        //Create and set up the window.
        JFrame frame = new JFrame("Langtons Ant");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        
        //TODO: I need to recieve keystrokes => notify main and close. or close from here?
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed ANY"), "any");
        frame.getRootPane().getActionMap().put("any", new AbstractAction() {
            private static final long serialVersionUID = -4425389178179634179L;

			@Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Any key pressed");
            }
        });
        
        //remember last drawn image and only redraw new fields
        old = new Color[src.simulator.LangtonsAnt.field.length*src.simulator.LangtonsAnt.field[0].length];
        //buffer last image and only change redrawn stuff

        //the field to draw the ants TODO: the buffer seems to be smart but slow?
        drawField = new JPanel() {
        	private static final long serialVersionUID = -2928468841934592107L;
        	private BufferedImage buffer; //remember last draw to only update new things
        	private Graphics2D g2d = null; //the image

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                //new or resized?
                if (buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight()) {
                    // new buffer                	
                    buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    if (g2d!=null) g2d.dispose(); //old left to dispose?
                    g2d = buffer.createGraphics(); //get new image
                    old = new Color[LangtonsAnt.field.length*LangtonsAnt.field[0].length]; //forget all colors setted
                }            	
            	int i = 0;
            	for (Object[] params:GUI.draw) { //for all colors in field
            		if (old[i] != (Color)params[4]) { //if changed
            			old[i] = (Color)params[4]; //save and draw field
            			g2d.setColor((Color)params[4]);
            			g2d.fillRect((int)params[1], 
            					(int)params[0], 
            					(int)params[2], 
            					(int)params[3]);	
            		}
            		i++;
                }

                // Draw the off-screen buffer onto the screen
                g.drawImage(buffer, 0, 0, null);
            }
        };
		//drawField.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		drawField.setBackground(Color.WHITE);
		frame.getContentPane().add(drawField,BorderLayout.CENTER);
        
		//slider to adapt speed
        JSlider slider = new JSlider(src.simulator.Main.minRate,src.simulator.Main.maxRate,src.simulator.Main.rate);
        JLabel text = new JLabel();
        text.setText("Rate(Hz):");
        JTextField hz = new JTextField();
        hz.setText(""+src.simulator.Main.rate);
        //System.out.println(Integer.toString(gui.Main.maxRate)+" yields "+Integer.toString(gui.Main.maxRate).length());
        hz.setColumns(6); //needed for "paused" text
        if (Integer.toString(src.simulator.Main.maxRate).length()>6) { //numbers can become bigger than "paused"  	
        	hz.setColumns(Integer.toString(src.simulator.Main.maxRate).length());
        }
        //user slides rate
        
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (!source.getValueIsAdjusting()) {
                	int rate = slider.getValue();
                    src.simulator.Main.rate = rate;
                    oldRate = rate;
                    hz.setText(""+rate);
                }
            }
        });
        //user writes rate
        hz.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int rate = Integer.valueOf(hz.getText());
					if (Main.minRate<=rate && rate<=Main.maxRate) {	
						src.simulator.Main.rate = rate;
						slider.setValue(rate);
						oldRate = rate;
					}
				}
				catch(java.lang.NumberFormatException err) {
					hz.setText(""+src.simulator.Main.rate);
				}
				
			}        	
        });
        //lets get updates from main with this
		class RateListener implements EventListener {
			RateListener() {}			
			@Override
			public void update(Object rate) {
				hz.setText(""+(int)rate); //get rate change from main
				slider.setValue((int)rate);
				oldRate = (int)rate;
			}
			
		}
        src.simulator.Main.registerListener(new RateListener());
        //Pause and resume
        JButton button = new JButton((src.simulator.Main.run)?"Pause":"Resume");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	notifyListeners("Button pressed!");
                button.setText((src.simulator.Main.run)?"Pause":"Resume");
                src.simulator.Main.rate = src.simulator.Main.run?src.simulator.Main.rate:1;
                slider.setEnabled(src.simulator.Main.run);
                hz.setText(""+(src.simulator.Main.run?src.simulator.Main.rate:"paused"));
            }
        });
        
        //Combine the inputs into panel
        JPanel controlPanel = new JPanel();
        controlPanel.add(slider,BorderLayout.WEST);
        controlPanel.add(text,BorderLayout.CENTER);
        controlPanel.add(hz,BorderLayout.CENTER);
        controlPanel.add(button,BorderLayout.EAST);
        //controlPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
        frame.getContentPane().add(controlPanel, BorderLayout.SOUTH); //TODO: This is bugged after rescaling
        
        //Display the whole frame.
        frame.setVisible(true);
    }
    
}
