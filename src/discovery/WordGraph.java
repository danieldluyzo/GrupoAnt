package discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Homework 2 - Daniel D'luyz
 */
public class WordGraph {

	private BufferedReader buf;
	private Stemm_es stemmer;
	private Hashtable<String, Integer> stopwordsHash;
	public LinkedHashMap<String, WordNode> graph;
	public LinkedHashMap<String, Integer> vocabulary;
	File originalFile;

	public WordGraph(Hashtable<String, Integer> stopwords) throws Exception {
		stemmer = new Stemm_es();
		stopwordsHash = stopwords; 
		graph = new LinkedHashMap<String, WordNode>(); 
		vocabulary = new LinkedHashMap<String, Integer>();
	}

	public void createFromFile(File file) {
		this.originalFile = file;
		try {
			buf = new BufferedReader(new FileReader(originalFile));
			String line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				tokenizeLineWithStemmer(line);
				line = buf.readLine();
			}
			buf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void tokenizeLineWithStemmer(String line) {
		String[] tokens = line.split("--;--")[1].split("[\\p{Punct}\\s]+");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			token = token.toLowerCase().trim();
			if(!stopwordsHash.containsKey(token) && token.length()>0) {
				token = stemmer.stemm(token);
				if(!token.isEmpty()) {
					addNodeToHash(token);
					addTokenToVocabulary(token);
				}
			}
		}
	}

	public void readLineWithStemmer(String line) {
		String[] tokens = line.split("--;--")[1].split("[\\p{Punct}\\s]+");
		for (int i = 0; i < tokens.length - 1; i++) {
			String firstToken = tokens[i];
			firstToken = firstToken.toLowerCase();
			String secondToken = tokens[i+1];
			secondToken = secondToken.toLowerCase();
			if(!stopwordsHash.containsKey(firstToken) && !stopwordsHash.containsKey(secondToken)) {
				firstToken = stemmer.stemm(firstToken);
				secondToken = stemmer.stemm(secondToken);
				if(!firstToken.isEmpty() && !secondToken.isEmpty()) {
					graph.get(firstToken).incrementNeighbor(secondToken);
					graph.get(secondToken).incrementNeighbor(firstToken);
				}
			}
		}
	}

	public void addNodeToHash(String token) {
		if(!graph.containsKey(token)) {
			WordNode node = new WordNode(token);
			graph.put(token, node);
		}
	}

	public void addTokenToVocabulary(String token) {
		if(!vocabulary.containsKey(token)) vocabulary.put(token, 1);
		else vocabulary.put(token, vocabulary.get(token) + 1);
	}

	public void goOverGraph() {
		ArrayList<Map.Entry<String, WordNode>> nodes = new ArrayList<Entry<String, WordNode>>(graph.entrySet());
		for (int i = 0; i < nodes.size(); i++) {
			Map.Entry<String, WordNode> node = nodes.get(i);
			System.out.println(node.getValue().toString());
		}
	}

	public void calculateWeightsInGraph() {
		try {
			buf = new BufferedReader(new FileReader(originalFile));
			String line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				readLineWithStemmer(line);
				line = buf.readLine();
			}
			buf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
