package codehustler.ml.mazerunner;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.util.data.norm.MaxMinNormalizer;
import org.neuroph.util.random.DistortRandomizer;

import codehustler.ml.mazerunner.ui.MazeRunner;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data 
@EqualsAndHashCode(of="uuid")
public class AIPlayer implements Player {
	
	private static Random R = new Random(System.currentTimeMillis());

	private boolean turnLeft;
	private boolean turnRight;

	private MaxMinNormalizer normalizer;
	
	private final String uuid; 

	private double score = 0;
	private NeuralNetwork<BackPropagation> model;	
	private double distortionRate = 10;


	public AIPlayer() {
		this.uuid = UUID.randomUUID().toString();
		createModel();
	}

	public AIPlayer(AIPlayer otherPlayer) {
		this.uuid = UUID.randomUUID().toString();
		
		cloneModel(otherPlayer.getModel());
	}

	public AIPlayer(String weightsFile) {
		this.uuid = UUID.randomUUID().toString();
		createModel();
		restore(weightsFile);
	}
	
	private MultiLayerPerceptron createBasicModel() {
		int[] layerConfig = new int[] { MazeRunner.FIELD_OF_VIEW_ANGLES.length*2+1, 8, 16, 32, 8, 2 };		
		MultiLayerPerceptron model = new MultiLayerPerceptron(TransferFunctionType.LINEAR, layerConfig);
		return model;
	}

	private void cloneModel(NeuralNetwork<BackPropagation> sourceModel) {
		MultiLayerPerceptron model = createBasicModel();
		
		model.setWeights(Arrays.stream(sourceModel.getWeights()).mapToDouble(Double::doubleValue).toArray());
		DistortRandomizer distortRandomizer = new DistortRandomizer(distortionRate);
		distortRandomizer.setRandomGenerator(R);
		model.randomizeWeights(distortRandomizer);
		this.model = model;
	}

	private void createModel() {
		MultiLayerPerceptron model = createBasicModel();
		model.randomizeWeights(R);
		this.model = model;
	}

	public void setInputs(double[] inputs) {
		this.score++;
		inputs = maxNormalize(inputs);
		model.setInput(inputs);
		model.calculate();

		double[] output = model.getOutput();
		double threshold = 0.002;
		double rel = output[0]/output[1];
//		System.out.println(output[0] +" --------------- " + output[1] + " --------------- " + rel);

		turnLeft = rel-threshold > 1;
		turnRight = rel+threshold < 1;
	}

	private double[] maxNormalize(double[] input) {
		Double max = Math.min(Arrays.stream(input).boxed().collect(Collectors.maxBy(Double::compareTo)).get(), 40000);
		return Arrays.stream(input).boxed().map(v -> v / max).mapToDouble(Double::doubleValue).toArray();
	}

	@Override
	public void save() {
		WeightsManager.saveWeights(model.getWeights(), uuid);
	}

	@Override
	public void restore(String id) {
		this.model.setWeights(
				Arrays.stream(WeightsManager.loadWeights(id)).mapToDouble(Double::doubleValue).toArray());
	}
}
