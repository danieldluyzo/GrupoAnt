package discovery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Homework 2 - Daniel D'luyz
 */
public class PageRank {

	private final static double DAMPING_FACTOR = 0.85;
	private final static double MAX_NUMBER_OF_ITERATIONS = 500;
	private final static double MAX_DIFFERENCE = 0.0001;
	private LinkedHashMap<String,WordNode> graph;
	private LinkedHashMap<String,Double> scores;
	private int numberOfIterations;

	public PageRank(WordGraph wg) {
		this.numberOfIterations = 0;
		this.graph = wg.graph;
		this.scores = new LinkedHashMap<String, Double>();
		initialize();
		iterateWithWeightedEdges();
	}

	private void initialize() {
		ArrayList<Map.Entry<String, WordNode>> nodes = new ArrayList<Entry<String, WordNode>>(graph.entrySet());
		for (int i = 0; i < nodes.size(); i++) {
			Map.Entry<String, WordNode> node = nodes.get(i);
			double initialScore = (double) 1/ (double) graph.size();
			scores.put(node.getKey(), initialScore);
		}
	}

	private void iterateWithWeightedEdges() {
		numberOfIterations++;
		
		LinkedHashMap<String,Double> previousScores = createCopy();
		
		ArrayList<Map.Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(scores.entrySet());
		double totalScore = 0;
		for (int i = 0; i < entries.size(); i++) {
			Map.Entry<String, Double> entry = entries.get(i);
			String word = entry.getKey();
			
			double newScore = 1 - DAMPING_FACTOR;
			double n = (double) 1 / (double) graph.size();
			newScore = newScore * n;
			
			LinkedHashMap<String, Integer> neighbors = graph.get(word).getNeighbors();
			ArrayList<Map.Entry<String, Integer>> neighborsEntries = new ArrayList<Entry<String, Integer>>(neighbors.entrySet());
			for (int j = 0; j < neighborsEntries.size(); j++) {
				Map.Entry<String, Integer> neighbor = neighborsEntries.get(j);
				int edgeScore = neighbor.getValue();
				double weight = (double) edgeScore / (double) graph.get(neighbor.getKey()).getTotalOutput();
				weight = weight * previousScores.get(neighbor.getKey());
				newScore += weight;
			}
			
			newScore = newScore * DAMPING_FACTOR;
			totalScore += newScore;
			scores.put(word, newScore);
		}
		
		entries = new ArrayList<Entry<String, Double>>(scores.entrySet());
		for (int i = 0; i < entries.size(); i++) {
			Map.Entry<String, Double> word = entries.get(i);
			double normalizedScore = (double) word.getValue() / totalScore;
			scores.put(word.getKey(), normalizedScore);
		}
		
		boolean hasConverged = hasConverged(previousScores);
		if(!hasConverged) {
			System.out.println("Calculating page rank scores - Iteration number "+numberOfIterations);
			iterateWithWeightedEdges();
		}
		else {
			System.out.println("Page rank scores calculated in "+numberOfIterations+" iterations");
		}
	}
	
	/*
	private void iterateWithBasicEdges() {
		numberOfIterations++;
		
		LinkedHashMap<String,Double> previousScores = createCopy();
		
		ArrayList<Map.Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(scores.entrySet());
		double totalScore = 0;
		for (int i = 0; i < entries.size(); i++) {
			Map.Entry<String, Double> entry = entries.get(i);
			String word = entry.getKey();
			
			double newScore = 1 - DAMPING_FACTOR;
			double n = (double) 1/graph.size();
			newScore = newScore * n;
			
			LinkedHashMap<String, Integer> neighbors = graph.get(word).getNeighbors();
			ArrayList<Map.Entry<String, Integer>> neighborsEntries = new ArrayList<Entry<String, Integer>>(neighbors.entrySet());
			for (int j = 0; j < neighborsEntries.size(); j++) {
				Map.Entry<String, Integer> neighbor = neighborsEntries.get(j);
				double weight = (double) 1 / graph.get(neighbor.getKey()).getNumberOfNeighbors();
				weight = weight * previousScores.get(neighbor.getKey());
				newScore += weight;
			}
			
			newScore = newScore * DAMPING_FACTOR;
			totalScore += newScore;
			scores.put(word, newScore);
		}
		
		entries = new ArrayList<Entry<String, Double>>(scores.entrySet());
		for (int i = 0; i < entries.size(); i++) {
			Map.Entry<String, Double> word = entries.get(i);
			double normalizedScore = (double) word.getValue() / totalScore;
			scores.put(word.getKey(), normalizedScore);
		}
		
		boolean hasConverged = hasConverged(previousScores);
		if(!hasConverged) {
			System.out.println("Calculating page rank scores - Iteration number "+numberOfIterations);
			iterateWithWeightedEdges();
		}
		else {
			System.out.println("Page rank scores calculated in "+numberOfIterations+" iterations");
		}
	}
	*/

	private boolean hasConverged(LinkedHashMap<String,Double> previous) {
		if(numberOfIterations >= MAX_NUMBER_OF_ITERATIONS) return true;
		else {
			boolean converged = true;
			ArrayList<Map.Entry<String, Double>> previousScores = new ArrayList<Entry<String, Double>>(previous.entrySet());
			for (int i = 0; i < previousScores.size() && converged; i++) {
				String word = previousScores.get(i).getKey();
				double previousScore = previousScores.get(i).getValue();
				double newScore = scores.get(word);
				if(Math.abs(newScore - previousScore) > MAX_DIFFERENCE) converged = false;
			}
			return converged;
		}
	}

	private LinkedHashMap<String,Double> createCopy() {
		LinkedHashMap<String, Double> previousScores = new LinkedHashMap<String, Double>(scores.size());
		for(Map.Entry<String, Double> entry : scores.entrySet()) {
			previousScores.put(entry.getKey(), entry.getValue());
		}
		return previousScores;
	}
	
	public void printPageRank() {
		ArrayList<Map.Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(scores.entrySet());
		for (int j = 0; j < entries.size(); j++) {
			Map.Entry<String, Double> neighbor = entries.get(j);
			System.out.println(neighbor.getKey()+" : "+neighbor.getValue());
		}
	}

	public LinkedHashMap<String,Double> getScoresForWords(){
		return scores;
	}

}
