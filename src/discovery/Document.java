package discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
 * Homework 2 - Daniel D'luyz
 */
public class Document {

	private LinkedHashMap<String,Double> pageRank;
	public File originalFile;
	private LinkedHashMap<String, Double> pageRankNGrams;
	private LinkedHashMap<String, Double> tfIdfNGrams;
	private BufferedReader buf;
	private Hashtable<String, Integer> stopwordsHash;
	public Hashtable<String, Integer> topicsHash;
	private Stemm_es stemmer;
	private ArrayList<Entry<String, Double>> sortedPageRankNGrams;
	private ArrayList<Entry<String, Double>> sortedTfIdfNGrams;
	public LinkedHashMap<String, Integer> vocabulary;
	public LinkedHashMap<String, Double> tfIdfScores;
	private int numberOfDocuments;

	public Document(File file, LinkedHashMap<String,Double> pR, Hashtable<String, Integer> stopwords, Hashtable<String, Integer> topics, LinkedHashMap<String, Integer> vocabulary, int nOfDocuments) {
		this.originalFile = file;
		this.pageRank = pR;
		this.pageRankNGrams = new LinkedHashMap<String, Double>();
		this.tfIdfNGrams = new LinkedHashMap<String, Double>();
		this.tfIdfScores = new LinkedHashMap<String, Double>();
		this.stopwordsHash = stopwords;
		this.sortedPageRankNGrams = new ArrayList<Entry<String, Double>>(pageRankNGrams.entrySet());
		this.sortedTfIdfNGrams = new ArrayList<Entry<String, Double>>(tfIdfNGrams.entrySet());
		this.stemmer = new Stemm_es();
		this.vocabulary = vocabulary;
		this.numberOfDocuments = nOfDocuments;
		this.topicsHash = new Hashtable<String, Integer>();
		ArrayList<Map.Entry<String, Integer>> originalTopics = new ArrayList<Entry<String, Integer>>(topics.entrySet());
		for (int i = 0; i < originalTopics.size(); i++) {
			String topic = originalTopics.get(i).getKey();
			topicsHash.put(topic, 0);
		}
	}

	public void formNGramsWithPageRank() {
		try {
			//Unigrams
			buf = new BufferedReader(new FileReader(originalFile));
			String line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				formUnigramsWithPageRank(line);
				line = buf.readLine();
			}
			buf.close();
			sortPageRankNGrams();
			printPageRankUnigrams();
			//Bigrams
			buf = new BufferedReader(new FileReader(originalFile));
			line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				formUnigramsWithPageRank(line);
				formBigramsWithPageRank(line);
				line = buf.readLine();
			}
			buf.close();
			sortPageRankNGrams();
			printPageRankBigrams();
			printTopicsHash();
//			//Trigrams
//			buf = new BufferedReader(new FileReader(originalFile));
//			line = buf.readLine();
//			while(line!=null) {
//				line = line.toLowerCase();
//				formUnigramsWithPageRank(line);
//				formBigramsWithPageRank(line);
//				formTrigramsWithPageRank(line);
//				line = buf.readLine();
//			}
//			buf.close();
//			sortPageRankNGrams();
//			printPageRankTrigrams();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void formNGramsWithTfIdf() {
		try {
			//Unigrams
			buf = new BufferedReader(new FileReader(originalFile));
			String line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				formUnigramsWithTfIdf(line);
				line = buf.readLine();
			}
			buf.close();
			sortTfIdfNGrams();
			printTfIdfUnigrams();
			//Bigrams
			buf = new BufferedReader(new FileReader(originalFile));
			line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				formUnigramsWithTfIdf(line);
				formBigramsWithTfIdf(line);
				line = buf.readLine();
			}
			buf.close();
			sortTfIdfNGrams();
			printTfIdfBigrams();
			//Trigrams
			buf = new BufferedReader(new FileReader(originalFile));
			line = buf.readLine();
			while(line!=null) {
				line = line.toLowerCase();
				formUnigramsWithTfIdf(line);
				formBigramsWithTfIdf(line);
				formTrigramsWithTfIdf(line);
				line = buf.readLine();
			}
			buf.close();
			sortTfIdfNGrams();
			printTfIdfTrigrams();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void formUnigramsWithPageRank(String line) {
		String[] tokens = line.split("--;--")[1].split("[\\p{Punct}\\s]+");
		for (int i = 0; i < tokens.length - 1; i++) {
			String firstToken = tokens[i];
			firstToken = firstToken.toLowerCase();
			if(!stopwordsHash.containsKey(firstToken)) {
				firstToken = stemmer.stemm(firstToken);
				if(!firstToken.isEmpty()) {
					double score = pageRank.get(firstToken);
					pageRankNGrams.put(firstToken, score);
				}
			}
		}
	}

	private void formBigramsWithPageRank(String line) {
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
					double score = pageRank.get(firstToken);
					score += pageRank.get(secondToken);
					pageRankNGrams.put(firstToken+" "+secondToken, score);
					String bigram = firstToken + " " + secondToken;
					String bigramWithoutAccents = Normalizer.normalize(bigram, Normalizer.Form.NFD);
					bigramWithoutAccents = bigramWithoutAccents.replaceAll("[^\\p{ASCII}]", "");
					if(topicsHash.containsKey(bigramWithoutAccents)) {
						topicsHash.put(bigramWithoutAccents, topicsHash.get(bigramWithoutAccents) + 1);
					}
				}
			}
		}
	}

	private void formTrigramsWithPageRank(String line) {
		String[] tokens = line.split("--;--")[1].split("[\\p{Punct}\\s]+");
		for (int i = 0; i < tokens.length - 2; i++) {
			String firstToken = tokens[i];
			firstToken = firstToken.toLowerCase();
			String secondToken = tokens[i+1];
			secondToken = secondToken.toLowerCase();
			String thirdToken = tokens[i+2];
			thirdToken = thirdToken.toLowerCase();
			if(!stopwordsHash.containsKey(firstToken) && !stopwordsHash.containsKey(secondToken)  && !stopwordsHash.containsKey(thirdToken)) {
				firstToken = stemmer.stemm(firstToken);
				secondToken = stemmer.stemm(secondToken);
				thirdToken = stemmer.stemm(thirdToken);
				if(!firstToken.isEmpty() && !secondToken.isEmpty() && !thirdToken.isEmpty()) {
					double score = pageRank.get(firstToken);
					score += pageRank.get(secondToken);
					score += pageRank.get(thirdToken);
					pageRankNGrams.put(firstToken+" "+secondToken+" "+thirdToken, score);
				}
			}
		}
	}
	
	private void formUnigramsWithTfIdf(String line) {
		String[] tokens = line.split("--;--")[1].split("[\\p{Punct}\\s]+");
		for (int i = 0; i < tokens.length - 1; i++) {
			String firstToken = tokens[i];
			firstToken = firstToken.toLowerCase();
			if(!stopwordsHash.containsKey(firstToken)) {
				firstToken = stemmer.stemm(firstToken);
				if(!firstToken.isEmpty()) {
					double score = tfIdfScores.get(firstToken);
					tfIdfNGrams.put(firstToken, score);
				}
			}
		}
	}

	private void formBigramsWithTfIdf(String line) {
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
					double score = tfIdfScores.get(firstToken);
					score += tfIdfScores.get(secondToken);
					tfIdfNGrams.put(firstToken+" "+secondToken, score);
				}
			}
		}
	}

	private void formTrigramsWithTfIdf(String line) {
		String[] tokens = line.split("--;--")[1].split("[\\p{Punct}\\s]+");
		for (int i = 0; i < tokens.length - 2; i++) {
			String firstToken = tokens[i];
			firstToken = firstToken.toLowerCase();
			String secondToken = tokens[i+1];
			secondToken = secondToken.toLowerCase();
			String thirdToken = tokens[i+2];
			thirdToken = thirdToken.toLowerCase();
			if(!stopwordsHash.containsKey(firstToken) && !stopwordsHash.containsKey(secondToken)  && !stopwordsHash.containsKey(thirdToken)) {
				firstToken = stemmer.stemm(firstToken);
				secondToken = stemmer.stemm(secondToken);
				thirdToken = stemmer.stemm(thirdToken);
				if(!firstToken.isEmpty() && !secondToken.isEmpty() && !thirdToken.isEmpty()) {
					double score = tfIdfScores.get(firstToken);
					score += tfIdfScores.get(secondToken);
					score += tfIdfScores.get(thirdToken);
					tfIdfNGrams.put(firstToken+" "+secondToken+" "+thirdToken, score);
				}
			}
		}
	}
	
	//PRINTING

	public void printPageRankUnigrams() throws Exception {
		System.out.println("**************");
		System.out.println("Sorted N-grams (page rank)");
		System.out.println(this.originalFile.getName());
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/discovery/"+this.originalFile.getName()+"-PageRankUnigrams.txt"), false));
		for (int j = 0; j < 10 ; j++) {
			Map.Entry<String, Double> nGram = sortedPageRankNGrams.get(j);
			pw.println(nGram.getKey());
		}
		pw.close();
		System.out.println("**************");
	}
	
	public void printPageRankBigrams() throws Exception {
		System.out.println("**************");
		System.out.println("Sorted N-grams (page rank)");
		System.out.println(this.originalFile.getName());
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/discovery/"+this.originalFile.getName()+"-PageRankBigrams.txt"), false));
		for (int j = 0; j < 10 ; j++) {
			Map.Entry<String, Double> nGram = sortedPageRankNGrams.get(j);
			pw.println(nGram.getKey());
		}
		pw.close();
		System.out.println("**************");
	}
	
	public void printPageRankTrigrams() throws Exception {
		System.out.println("**************");
		System.out.println("Sorted N-grams (page rank)");
		System.out.println(this.originalFile.getName());
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/discovery/"+this.originalFile.getName()+"-PageRankTrigrams.txt"), false));
		for (int j = 0; j < 10 ; j++) {
			Map.Entry<String, Double> nGram = sortedPageRankNGrams.get(j);
			pw.println(nGram.getKey());
		}
		pw.close();
		System.out.println("**************");
	}
	
	public void printTfIdfUnigrams() throws Exception {
		System.out.println("**************");
		System.out.println("Sorted N-grams (tf-idf)");
		System.out.println(this.originalFile.getName());
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/discovery/"+this.originalFile.getName()+"-TfIdfUnigrams.txt"), false));
		for (int j = 0; j < 10; j++) {
			Map.Entry<String, Double> nGram = sortedTfIdfNGrams.get(j);
			pw.println(nGram.getKey());
		}
		pw.close();
		System.out.println("**************");
	}
	
	public void printTfIdfBigrams() throws Exception {
		System.out.println("**************");
		System.out.println("Sorted N-grams (tf-idf)");
		System.out.println(this.originalFile.getName());
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/discovery/"+this.originalFile.getName()+"-TfIdfBigrams.txt"), false));
		for (int j = 0; j < 10; j++) {
			Map.Entry<String, Double> nGram = sortedTfIdfNGrams.get(j);
			pw.println(nGram.getKey());
		}
		pw.close();
		System.out.println("**************");
	}
	
	public void printTfIdfTrigrams() throws Exception {
		System.out.println("**************");
		System.out.println("Sorted N-grams (tf-idf)");
		System.out.println(this.originalFile.getName());
		PrintWriter pw = new PrintWriter(new FileWriter(new File("./data/discovery/"+this.originalFile.getName()+"-TfIdfTrigrams.txt"), false));
		for (int j = 0; j < 10; j++) {
			Map.Entry<String, Double> nGram = sortedTfIdfNGrams.get(j);
			pw.println(nGram.getKey());
		}
		pw.close();
		System.out.println("**************");
	}


	public void sortPageRankNGrams(){
		ArrayList<Map.Entry<String, Double>> nGramsArray = new ArrayList<Entry<String, Double>>(pageRankNGrams.entrySet());
		sortedPageRankNGrams = new ArrayList<Entry<String, Double>>();
		for (int i = 0; i < nGramsArray.size(); i++) {
			sortedPageRankNGrams.add(nGramsArray.get(i));
		}
		Collections.sort(sortedPageRankNGrams, new Comparator<Map.Entry<String, Double>>(){
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}});
	}
	
	public void sortTfIdfNGrams(){
		ArrayList<Map.Entry<String, Double>> nGramsArray = new ArrayList<Entry<String, Double>>(tfIdfNGrams.entrySet());
		sortedTfIdfNGrams = new ArrayList<Entry<String, Double>>();
		for (int i = 0; i < nGramsArray.size(); i++) {
			sortedTfIdfNGrams.add(nGramsArray.get(i));
		}
		Collections.sort(sortedTfIdfNGrams, new Comparator<Map.Entry<String, Double>>(){
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}});
	}
	
	public ArrayList<Map.Entry<String, Integer>> sortVocabulary() {
		ArrayList<Map.Entry<String, Integer>> tokens = new ArrayList<Entry<String, Integer>>(vocabulary.entrySet());
		Collections.sort(tokens, new Comparator<Map.Entry<?, Integer>>(){
			public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}});
		return tokens;
	}
	
	public void calculateTfIdf(LinkedHashMap<String, Integer> collectionVocabulary) {
		ArrayList<Map.Entry<String, Integer>> wordsInVocabulary = sortVocabulary();
		int max = wordsInVocabulary.get(wordsInVocabulary.size() - 1).getValue();
		for (int i = 0; i < wordsInVocabulary.size(); i++) {
			String token = wordsInVocabulary.get(i).getKey();
			int frequency = wordsInVocabulary.get(i).getValue();
			double tf = (double) frequency / (double) max;
			double denominator = (double) numberOfDocuments / (double) collectionVocabulary.get(token);
			double idf = (double) Math.log(denominator) / (double) Math.log(2);
			double tfIdf = (double) tf * (double) idf;
			tfIdfScores.put(token, tfIdf);
		}
	}
	
	public void printTfIdf() {
		System.out.println("Tf-Idf for document "+originalFile.getName()+" : ");
		ArrayList<Map.Entry<String, Double>> tfIdf = new ArrayList<Entry<String, Double>>(tfIdfScores.entrySet());
		for (int i = 0; i < tfIdf.size(); i++) {
			String token = tfIdf.get(i).getKey();
			double frequency = tfIdf.get(i).getValue();
			System.out.println(token+" : "+frequency);
		}
	}
	
	public Hashtable<String, Integer> getTopicsCount() {
		return this.topicsHash;
	}
	
	public void printTopicsHash() {
		System.out.println("--------------------> " + this.originalFile.getName());
		ArrayList<Map.Entry<String, Integer>> actorTopics = new ArrayList<Entry<String, Integer>>(topicsHash.entrySet());
		for (int j = 0; j < actorTopics.size(); j++) {
			String topic = actorTopics.get(j).getKey();
			int frequency = actorTopics.get(j).getValue();
			System.out.println(topic + " - " + frequency);
		}
	}

}
