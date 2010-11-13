package winner;

import Interface.WinnerDisplay;

public class PlayRoundThread implements Runnable{
	public static Boolean running = false;
	
	private WinnerDisplay winner_display;
	
	public PlayRoundThread(WinnerDisplay winner_display){
		this.winner_display = winner_display;
	}
	
	@Override
	public void run() {
		synchronized (running) {
			if(running)
				return;
			running = true;
		}
		try{
			BubbleSpinnerWinner.PlayRound(winner_display);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		synchronized (running) {
			running = false;
		}
	}

}
