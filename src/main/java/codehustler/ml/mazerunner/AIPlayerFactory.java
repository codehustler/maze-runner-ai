package codehustler.ml.mazerunner;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AIPlayerFactory implements PlayerFactory {
	
	private static final Random R = new Random(System.currentTimeMillis()); 

	private final int populationSize;
	
	private Set<Player> previousGeneration = new HashSet<>();


	public Player createAIPlayer(AIPlayer player) {
		return new AIPlayer(player);
	} 

	public Set<Player> createPlayers() {
		Set<Player> players = new HashSet<>();
		
		int playersToKeep = (int) (previousGeneration.size() * 0.2d);
		int playersToBreed = (int) (previousGeneration.size() * 0.8d);
		int newRandomPlayers = (int) (populationSize-(playersToKeep+playersToBreed));

		List<Player> survivors = previousGeneration.stream().sorted()
				.skip(populationSize  - playersToKeep).collect(Collectors.toList());
		
		
		List<Player> newBorns = IntStream.range(0, playersToBreed)
				.mapToObj(n -> createAIPlayer((AIPlayer)survivors.get(R.nextInt(survivors.size()))))
				.collect(Collectors.toList());
		
		 List<Player> newRandoms = IntStream.range(0, newRandomPlayers).mapToObj(n -> new AIPlayer()).collect(Collectors.toList());
		 if ( newRandoms.size() > 0 ) {
			 System.out.println("new random payers created: " + newRandoms.size());
		 }


		survivors.forEach(p -> {
			p.setScore(0);
		});

		players.addAll(survivors);
		players.addAll(newBorns);
		players.addAll(newRandoms);

		previousGeneration.clear();
		previousGeneration.addAll(players);
		return players;
	}
}
