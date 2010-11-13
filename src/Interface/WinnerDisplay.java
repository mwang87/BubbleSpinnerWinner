package Interface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import winner.BubbleSpinnerWinner;
import winner.GameWindowFinder;
import winner.MouseMove;
import winner.PlayRoundThread;

public class WinnerDisplay {
	private Display display;
	
	//probably bad practice, and actually should make this a singleton, but eh
	public static WinnerDisplay active_display;
	
	private static boolean move_ready = false;
	private static MouseMove next_mouse_move;
	
	public WinnerDisplay(){
		display = new Display();
		move_ready = false;
		
		active_display = this;
	}
	
	
	public void DrawDisplay(){
		//org.eclipse.swt.graphics.Image image = display.getSystemImage(SWT.ICON_QUESTION);
		Shell shell = new Shell(display);
		shell.setLayout (new GridLayout());
		
		final Label output_label = new Label (shell, SWT.CHECK);
		
		final Text horizontal_enter = new Text (shell, SWT.BORDER);
		horizontal_enter.setBounds (10, 10, 200, 200);
		
		final Text vertical_enter = new Text (shell, SWT.BORDER);
		vertical_enter.setBounds (10, 10, 200, 200);
		final Button enter_button = new Button(shell, SWT.PUSH);
		enter_button.setText("Enter Top Left Spinner");
		final Button play_button  = new Button(shell, SWT.PUSH);
		
		play_button.setText("Play Round");
		play_button.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event e) {
				if(BubbleSpinnerWinner.bench_x == 0 && BubbleSpinnerWinner.bench_y ==0 && !BubbleSpinnerWinner.DEBUG){
					output_label.setSize(300, 12);
					output_label.setText("Benchmark Location First");
				}
				else{
					(new Thread(new PlayRoundThread(active_display))).start();
				}
			}
		});
		
		final Button shoot_button = new Button(shell, SWT.PUSH);
		shoot_button.setText("Shoot");
		shoot_button.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event e) {
				if(WinnerDisplay.move_ready){
					BubbleSpinnerWinner.MakeMouseMove(next_mouse_move);
					active_display.SetMoveDone();
				}
			}
		});
		
		//Given Starting Location, We Get the Benchmark Location
		enter_button.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event e) {
				String horizontal_pos_string = horizontal_enter.getText();
				String vertical_pos_string = vertical_enter.getText();
				output_label.setSize(300, 12);
				output_label.setText(horizontal_pos_string+ " " + vertical_pos_string);
				
				int horizontal_pos = Integer.parseInt(horizontal_pos_string);
				int vertical_pos = Integer.parseInt(vertical_pos_string);
				
				GameWindowFinder.GetBenchmarkLoc(horizontal_pos, vertical_pos);
			}
			
		});
		
		
		
		shell.setSize(300, 300);
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
	
	public void SetMoveReady(MouseMove mouse_move){
		move_ready = true;
		next_mouse_move = mouse_move;
	}
	
	public void SetMoveDone(){
		move_ready = false;
	}
	
}
