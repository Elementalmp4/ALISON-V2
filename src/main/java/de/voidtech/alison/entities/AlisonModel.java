package main.java.de.voidtech.alison.entities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class AlisonModel {

	private List<AlisonWord> words = new ArrayList<AlisonWord>(); 
	private AlisonMetadata meta = null;
	private String dataDir;

	public AlisonModel(String pack) {
		dataDir = "models/" + pack;
		File modelFile = new File(dataDir + "/words.alison");
		if (modelFile.exists()) load();
		else {
			try {
				modelFile.getParentFile().mkdirs();
				modelFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		try {
			FileInputStream fileInStream = new FileInputStream(dataDir + "/words.alison");
			ObjectInputStream objectInStream = new ObjectInputStream(fileInStream);
			words = (List<AlisonWord>) objectInStream.readObject();
			objectInStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		File metaFile = new File(dataDir + "/meta.json");
		if (metaFile.exists()) {
			try {
				Gson gson = new Gson();
				this.meta = gson.fromJson(Files.newBufferedReader(Paths.get(metaFile.getPath())), AlisonMetadata.class);
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void save() {
		try {
			FileOutputStream fileOutStream = new FileOutputStream(dataDir + "/words.alison");
			ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream);
			objectOutStream.writeObject(words);
			objectOutStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String createNickname() {
		String name = createSentence();
		if (name == null) return null;
		if (name.length() > 32) {
			Stack<String> words = new Stack<String>();
			Arrays.asList(name.split(" ")).stream().forEach(word -> words.push(word));
			name = "";
			while (name.trim().length() + words.peek().length() < 32) {
				name += words.pop() + " ";
			}
		}
		return name.trim();
	}

	public void learn(String content) {
        List<String> tokens = Arrays.asList(content.split(" "));
        for (int i = 0; i < tokens.size(); ++i) {
            if (i == tokens.size() - 1) words.add(new AlisonWord(tokens.get(i), "StopWord"));
            else words.add(new AlisonWord(tokens.get(i), tokens.get(i + 1)));
        }
        save();
	}

	private AlisonWord getRandomWord(final List<AlisonWord> words) {
        return words.get(new Random().nextInt(words.size()));
    }
	
	public Map<String, Long> getTopFiveWords() {
		Map<String, Long> countedWords = (Map<String, Long>) words.stream()
				.collect(Collectors.groupingBy(word -> word.getWord(), Collectors.counting()));	
		Map<String, Long> topFiveWords = new HashMap<String, Long>();
		String lastWord = "";
		Long lastCount = 0L;
		
		for (int i = 0; i < 5; i++) {
			for(String word : countedWords.keySet()) {
				if (countedWords.get(word) > lastCount && !topFiveWords.containsKey(word)) {
					lastCount = countedWords.get(word);
					lastWord = word;
				}
			}
			topFiveWords.put(lastWord, lastCount);
			lastCount = 0L;
		}
		
		return topFiveWords;
	}
	
	public String createSentence() {
		if (words.isEmpty()) return null;
		List<String> result = new ArrayList<String>();
		AlisonWord next = getRandomStartWord();
		if (next == null) return null;
		while (!next.isStopWord()) {
			result.add(next.getWord());
			List<AlisonWord> potentials = getWordList(next.getNext());
			next = getRandomWord(potentials);
		}
		result.add(next.getWord());
		return String.join(" ", result);
	}
	
	private AlisonWord getRandomStartWord() {
		List<AlisonWord> wordsWithFollows = words.stream().filter(word -> !word.getNext().equals("StopWord")).collect(Collectors.toList());
		if (wordsWithFollows.size() < 2) return wordsWithFollows.get(0);
		else return wordsWithFollows.get(new Random().nextInt(wordsWithFollows.size() - 1));
	}
	
	public List<AlisonWord> getWordList(String wordToFind) {
    	return words.stream().filter(word -> word.getWord().equals(wordToFind)).collect(Collectors.toList());
    }

	public List<String> getAllWords() {
		return words.stream().map(AlisonWord::getWord).collect(Collectors.toList());
	}
	
	public long getWordCount() {
		return words.size();
	}
	
	public boolean hasMeta() {
		return this.meta != null;
	}
	
	public AlisonMetadata getMeta() {
		return this.meta;
	}
}