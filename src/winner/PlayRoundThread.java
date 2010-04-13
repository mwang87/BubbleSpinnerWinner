package winner;

public class PlayRoundThread implements Runnable{
	public static Boolean running = false;
	
	@Override
	public void run() {
		synchronized (running) {
			if(running)
				return;
			running = true;
		}
		try{
			BubbleSpinnerWinner.PlayRound();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		synchronized (running) {
			running = false;
		}
	}

}
