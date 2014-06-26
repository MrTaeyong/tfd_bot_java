/**
 * @FileName 	: Blog.java
 * @Project 	: tfd_bot_java
 * @Date 		: 2014. 6. 26.
 * @Author 		: NCri
 */
package analyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.chasen.mecab.Node;
import org.chasen.mecab.Tagger;

/**
 * @Class		: Blog
 * @Date 		: 2014. 6. 26.
 * @Author 		: NCri
 */
public class Blog extends TextMining{

	/* (non-Javadoc)
	 * @see analyzer.TextMining#wordCount(java.lang.String)
	 */
	@Override
	Map<String, Integer> wordCount(String text) {
		// TODO Auto-generated method stub
		Tagger tagger = new Tagger("-d /usr/local/lib/mecab/dic/mecab-ko-dic");
		
		Node root = tagger.parseToNode(text);
		Node nextNode = root;
		
		Map<String, Integer> word = new HashMap<String, Integer>();
		
		String key = null;
		while(nextNode != null){
			key = nextNode.getSurface();
			if(!word.containsKey(key)){
				word.put(key, 0);
			}
			
			int count = word.get(key) + 1;
			word.put(key, count);
			
			nextNode = nextNode.getNext();
		}
		
		ValueComparator comp = new ValueComparator(word);
		TreeMap<String, Integer> sortedWord = new TreeMap<String, Integer>(comp);
		sortedWord.putAll(word);
		
		return sortedWord;
	}

	/* (non-Javadoc)
	 * @see analyzer.TextMining#wordCount(java.util.ArrayList)
	 */
	@Override
	Map<String, Integer> wordCount(ArrayList<String> list) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args){		
		try {
			BufferedReader in = new BufferedReader(new FileReader("/Users/NCri/git/tfd_bot_java/tfd_bot_java/blogtest.txt"));
			String temp = null;
			
			StringBuffer sb = new StringBuffer();
			while( (temp = in.readLine()) !=  null){
				sb.append(temp);
			}			
			in.close();
			
			TextMining tm = TextMining.newInstance(TextMining.Type.BLOG);
			Map<String, Integer> word = tm.wordCount(sb.toString());
			
			for(Entry<String, Integer> ent : word.entrySet()){
				System.out.println(ent.getKey() + "\t" + ent.getValue());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}

}
