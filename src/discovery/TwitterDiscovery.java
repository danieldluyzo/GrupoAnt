package discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/*
 * Daniel D'luyz
 */
public class TwitterDiscovery {

	private final File tweetsFolder = new File("data/tweets");
	private final File discoveryFolder = new File("data/discovery");
	private Hashtable<String, Integer> hashAbstracts;
	private Hashtable<String, Integer> stopwordsHash;
	private Hashtable<String, Integer> topicsHash;
	private LinkedHashMap<String, Integer> collectionVocabulary;
	private LinkedHashMap<String, LinkedHashMap<String, Integer>> vocabularyByPeople;
	private ArrayList<Entry<String, LinkedHashMap<String, Integer>>> sortedVocabularyByPeople;
	private BufferedReader buf;
	public ArrayList<WordGraph> documentGraphs;
	public ArrayList<Document> documents;

	public TwitterDiscovery() throws Exception {
		this.stopwordsHash = new Hashtable<String, Integer>(); 
		this.documentGraphs = new ArrayList<WordGraph>();
		this.documents = new ArrayList<Document>();
		this.collectionVocabulary = new LinkedHashMap<String, Integer>();
		this.vocabularyByPeople = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
		initialize();
	}

	public void initialize() throws Exception {
		loadStopwords();
		loadTopics();
		createGraphs();
	}

	public void createGraphs() throws Exception {
		for (final File file : tweetsFolder.listFiles()) {
			WordGraph documentGraph = new WordGraph(stopwordsHash);
			documentGraph.createFromFile(file);
			documentGraph.calculateWeightsInGraph();
			documentGraphs.add(documentGraph);
		}
	}

	public void goOverGraph() {
		documentGraphs.get(0).goOverGraph();
	}

	public void calculatePageRank() {
		for (int i = 0; i < documentGraphs.size(); i++) {
			WordGraph documentGraph = documentGraphs.get(i);
			PageRank pageRank = new PageRank(documentGraph);
			Document document = new Document(documentGraph.originalFile, pageRank.getScoresForWords(), stopwordsHash, topicsHash, documentGraph.vocabulary, documentGraphs.size());
			documents.add(document);
		}
	}

	public void formNGramsWithPageRank() {
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			document.formNGramsWithPageRank();
		}
	}

	public void formNGramsWitTfIdf() {
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			document.formNGramsWithTfIdf();
		}
	}

	public void calculateTfIdf() {
		for (int i = 0; i < documents.size(); i++) {
			documents.get(i).calculateTfIdf(collectionVocabulary);
		}
	}

	public void buildCollectionVocabulary() {
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			ArrayList<Map.Entry<String, Integer>> vocabulary = new ArrayList<Entry<String, Integer>>(document.vocabulary.entrySet());
			for (int j = 0; j < vocabulary.size(); j++) {
				String token = vocabulary.get(j).getKey();
				int frequency = vocabulary.get(j).getValue();
				if (collectionVocabulary.containsKey(token)) collectionVocabulary.put(token, collectionVocabulary.get(token) + frequency);
				else collectionVocabulary.put(token, 1);
				
				//Who said what
				if(vocabularyByPeople.containsKey(token)) {
					LinkedHashMap<String, Integer> people = vocabularyByPeople.get(token);
					String person = document.originalFile.getName();
					if(people.containsKey(person)) {
						System.out.println("Repeatttttt");
					}
					else people.put(person, frequency);
				} else {
					LinkedHashMap<String, Integer> people = new LinkedHashMap<String, Integer>();
					String person = document.originalFile.getName();
					people.put(person, 1);
					vocabularyByPeople.put(token, people);	
				}

			}
		}
	}

	/*
	 * Support methods
	 */
	public void loadStopwords() {
		File file = new File("./data/spanishStopwords.txt");
		try {
			buf = new BufferedReader(new FileReader(file));
			String stopword = buf.readLine();
			while(stopword!=null) {
				stopword = stopword.toLowerCase();
				stopwordsHash.put(stopword, 1);
				stopword = buf.readLine();
			}
			buf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadTopics() {
		File file = new File("./data/discovery/hashtagsView/topics.txt");
		topicsHash = new Hashtable<String, Integer>();
		try {
			buf = new BufferedReader(new FileReader(file));
			String topic = buf.readLine();
			while(topic!=null) {
				topic = topic.toLowerCase();
				topicsHash.put(topic, 0);
				topic = buf.readLine();
			}
			buf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void findAndDelete() {
		for (final File abstractFile : tweetsFolder.listFiles()) {
			hashAbstracts.put(abstractFile.getName().trim(), 0);
		}
		for (final File goldFile : discoveryFolder.listFiles()) {
			hashAbstracts.put(goldFile.getName().trim(), 1);
		}
		Set<String> keys = hashAbstracts.keySet();
		for(String key: keys){
			if(hashAbstracts.get(key) == 0) {
				File file = new File("./data/www/abstracts/"+key);
				file.delete();
			};
		}
	}

	/*
	 * Print methods
	 */
	public void printVocabulary() {
		System.out.println("**************");
		System.out.println("Vocabulary");
		ArrayList<Map.Entry<String, Integer>> vocabulary = new ArrayList<Entry<String, Integer>>(collectionVocabulary.entrySet());
		for (int i = 0; i < vocabulary.size(); i++) {
			String token = vocabulary.get(i).getKey();
			int frequency = vocabulary.get(i).getValue();
			System.out.println(token+" : "+frequency);
		}
	}

	public void printWhoSaidWhat() throws Exception {
		System.out.println("**************");
		System.out.println("**************");
		System.out.println("WhoSaidWhat");
		PrintWriter actorsPW = new PrintWriter(new FileWriter(new File("./data/discovery/hashtagsView/actors.txt"), false));
		ArrayList<String> actors = new ArrayList<String>();
		
		for (int i = 0; i < 20; i++) {
			String token = sortedVocabularyByPeople.get(i).getKey();
			if(!actors.contains(token)) {
				actors.add(token);
				int id = actors.size() - 1;
				actorsPW.println(id + "," +token+",1");
			}
			LinkedHashMap<String, Integer> people = sortedVocabularyByPeople.get(i).getValue();
			ArrayList<Map.Entry<String, Integer>> count = new ArrayList<Entry<String, Integer>>(people.entrySet());
			for (int j = 0; j < count.size(); j++) {
				String person = count.get(j).getKey();
				if(!actors.contains(person)) {
					actors.add(person);
					String[] personLabel = person.split("\\.");
					int id = actors.size() - 1;
					actorsPW.println(id + "," +personLabel[0]+",0");
				}
			}
		}
		actorsPW.close();
		
		PrintWriter tiesPW = new PrintWriter(new FileWriter(new File("./data/discovery/hashtagsView/ties.txt"), false));
		for (int i = 0; i < 20; i++) {
			String token = sortedVocabularyByPeople.get(i).getKey();
			int tokenIndex = actors.indexOf(token);
			LinkedHashMap<String, Integer> people = sortedVocabularyByPeople.get(i).getValue();
			ArrayList<Map.Entry<String, Integer>> count = new ArrayList<Entry<String, Integer>>(people.entrySet());
			for (int j = 0; j < count.size(); j++) {
				String person = count.get(j).getKey();
				int frequency = count.get(j).getValue();
				int personIndex = actors.indexOf(person);
				tiesPW.println(personIndex + "," + tokenIndex + "," + frequency);
			}
		}
		
		tiesPW.close();
	}
	
	public void printWhoSaidThat() throws Exception {
		System.out.println("**************");
		System.out.println("WhoSaidThat");
		PrintWriter actorsPW = new PrintWriter(new FileWriter(new File("./data/discovery/hashtagsView/actorsThat.txt"), false));
		ArrayList<String> actors = new ArrayList<String>();

		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			String name = document.originalFile.getName().split("\\.")[0];
			if(!actors.contains(name)) {
				actors.add(name);
				int id = actors.size() - 1;
				actorsPW.println(id + "," +name+",0");
			}
			Hashtable<String, Integer> topics = document.getTopicsCount();
			ArrayList<Map.Entry<String, Integer>> actorTopics = new ArrayList<Entry<String, Integer>>(topics.entrySet());
			for (int j = 0; j < actorTopics.size(); j++) {
				String topic = actorTopics.get(j).getKey();
				if(!actors.contains(topic)) {
					actors.add(topic);
					int id = actors.size() - 1;
					actorsPW.println(id + "," +topic+",1");
				}
			}
		}
	
		actorsPW.close();
		
		PrintWriter tiesPW = new PrintWriter(new FileWriter(new File("./data/discovery/hashtagsView/tiesThat.txt"), false));
		
		for (int i = 0; i < documents.size(); i++) {
			Document document = documents.get(i);
			String name = document.originalFile.getName().split("\\.")[0];
			int actorId = actors.indexOf(name);
			Hashtable<String, Integer> topics = document.getTopicsCount();
			System.out.println("----------------> " + name);
			ArrayList<Map.Entry<String, Integer>> actorTopics = new ArrayList<Entry<String, Integer>>(topics.entrySet());
			for (int j = 0; j < actorTopics.size(); j++) {
				String topic = actorTopics.get(j).getKey();
				int frequency = actorTopics.get(j).getValue();
				int topicId = actors.indexOf(topic);
				//System.out.println(topic + " - " + frequency);
				tiesPW.println(actorId + "," + topicId + "," + frequency);
			}
		}
		
		tiesPW.close();
		System.out.println("**************");
	}

	public void printTfIdf() {
		System.out.println("Tf-Idf");
		for (int i = 0; i < documents.size(); i++) {
			documents.get(i).printTfIdf();
		}
	}
	
	/*
	 * Sorting methods
	 */

	public void sortVocabularyByPeople(){
		ArrayList<Map.Entry<String, LinkedHashMap<String, Integer>>> vocabularyByPeopleArray = new ArrayList<Entry<String, LinkedHashMap<String, Integer>>>(vocabularyByPeople.entrySet());
		sortedVocabularyByPeople = new ArrayList<Entry<String, LinkedHashMap<String, Integer>>>();
		for (int i = 0; i < vocabularyByPeopleArray.size(); i++) {
			sortedVocabularyByPeople.add(vocabularyByPeopleArray.get(i));
		}
		
		Collections.sort(sortedVocabularyByPeople, new Comparator<Map.Entry<String, LinkedHashMap<String, Integer>>>(){
			public int compare(Map.Entry<String, LinkedHashMap<String, Integer>> o1, Map.Entry<String, LinkedHashMap<String, Integer>> o2) {
				Integer numberOfPeopleO1 = o1.getValue().size();
				Integer numberOfPeopleO2 = o2.getValue().size();
				return numberOfPeopleO2.compareTo(numberOfPeopleO1);
			}});
	}
	
	/*
	 * Main
	 */
	public static void main(String[] args) {
		try {
			TwitterDiscovery hm2 = new TwitterDiscovery();
			hm2.calculatePageRank();
			hm2.formNGramsWithPageRank();
			hm2.buildCollectionVocabulary();
			hm2.sortVocabularyByPeople();
			hm2.printWhoSaidWhat();
			hm2.printWhoSaidThat();
			hm2.calculateTfIdf();
			hm2.formNGramsWitTfIdf();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
