package burp;
import java.util.*;
import java.util.Map.Entry;

public class CMapSort {
	/**
	 * 使用 Map按key进行排序
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}
	
	/**
	 * 使用 Map按value进行排序
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByValue(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(map.entrySet());
		Collections.sort(entryList, new MapValueComparator());
		Iterator<Map.Entry<String, String>> iter = entryList.iterator();
		Map.Entry<String, String> tmpEntry = null;
		while (iter.hasNext()) {
			tmpEntry = iter.next();
			sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
		}
		return sortedMap;
	}
	
	
	public static String combineMapEntry(Map<String,String> map, String connector){
		String result = "";
		for (Map.Entry<String, String> entry : map.entrySet()){
			if (!result.equals("")){
				result += connector;
			}
			result += entry;
		}
		return result;
	}
	public static void main (String[] args) {
		Map<String, String> map = new TreeMap<String, String>();
		map.put("AKFC", "4kfc");
		map.put("BWNBA", "3wnba");
		map.put("CNBA", "2nba");
		map.put("DCBA", "1cba");
		Map<String, String> resultMap = sortMapByKey(map);	//按Key进行排序
		for (Map.Entry<String, String> entry : resultMap.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		Map<String, String> resultMap1 = sortMapByValue(map);	//按Value进行排序
		for (Map.Entry<String, String> entry : resultMap1.entrySet()) {
			//System.out.println(entry.getKey() + " " + entry.getValue());
			System.out.println(entry);
		}
		System.out.println(combineMapEntry(resultMap1, "&"));
	}
}

	//比较器类
	class MapKeyComparator implements Comparator<String>{
		public int compare(String str1, String str2) {
			return str1.compareTo(str2);
		}
	}

	//比较器类
	class MapValueComparator implements Comparator<Map.Entry<String, String>> {
		public int compare(Entry<String, String> me1, Entry<String, String> me2) {
			return me1.getValue().compareTo(me2.getValue());
		}
	}

