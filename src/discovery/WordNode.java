package discovery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Homework 2 - Daniel D'luyz
 */
public class WordNode {

	private String word;
	private LinkedHashMap<String, Integer> neighbors;

	public WordNode(String w) {
		this.word = w;
		this.neighbors = new LinkedHashMap<String, Integer>();
	}

	public String getWord() {
		return word;
	}

	public LinkedHashMap<String, Integer> getNeighbors() {
		return neighbors;
	}

	public void incrementNeighbor(String word) {
		if(neighbors.containsKey(word)) neighbors.put(word, neighbors.get(word)+1);
		else neighbors.put(word, 1);
	}
	
	public int getNumberOfNeighbors() {
		ArrayList<Map.Entry<String, Integer>> nodes = new ArrayList<Entry<String, Integer>>(neighbors.entrySet());
		return nodes.size();
	}
	
	public int getTotalOutput() {
		int output = 0;
		ArrayList<Map.Entry<String, Integer>> nodes = new ArrayList<Entry<String, Integer>>(neighbors.entrySet());
		for (int i = 0; i < nodes.size(); i++) {
			output += nodes.get(i).getValue();
		}
		return output;
	}

	@Override
	public String toString() {
		String str = this.word;
		str += " -> ";
		ArrayList<Map.Entry<String, Integer>> nodes = new ArrayList<Entry<String, Integer>>(neighbors.entrySet());
		for (int i = 0; i < nodes.size(); i++) {
			Map.Entry<String, Integer> node = nodes.get(i);
			str += node.getKey() + " : "+node.getValue()+"; ";
		}
		return str;
	}
}
