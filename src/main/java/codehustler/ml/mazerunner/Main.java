package codehustler.ml.mazerunner;

import codehustler.ml.mazerunner.ui.RunnerGame;

public class Main {
	
	public static void main(String[] args) throws Exception {
		RunnerGame game = new RunnerGame();
		game.setPlayerFactory(new AIPlayerFactory(10));
		game.run();
	}
}
