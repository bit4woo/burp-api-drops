package custom;
import java.util.*;
import java.util.Map.Entry;

public class MapSort {
	/**
	 * 使用 Map按key进行排序
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByKey(Map<String, String> map, String sortMethod) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		if (sortMethod.equals("ASCENDING")){
			Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparator());
			sortMap.putAll(map);
			return sortMap;
		}else if (sortMethod.equals("DESCENDING")) {
			Map<String, String> sortMap = new TreeMap<String, String>(new MapKeyComparatorDesc());
			sortMap.putAll(map);
			return sortMap;
		}else {
			return null;
		}
	}
	
	/**
	 * 使用 Map按value进行排序
	 * @param map
	 * @return
	 */
	public static Map<String, String> sortMapByValue(Map<String, String> map, String sortMethod) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortedMap = new LinkedHashMap<String, String>();
		List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(map.entrySet());
		if (sortMethod.equals("ASCENDING")){
			Collections.sort(entryList, new MapValueComparator());
		}else if (sortMethod.equals("DESCENDING")) {
			Collections.sort(entryList, new MapValueComparatorDesc());
		}
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
		Map<String, String> resultMap = sortMapByKey(map,"ASCENDING");	//按Key进行升序排序
		System.out.println("key 升序");
		for (Map.Entry<String, String> entry : resultMap.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println("key 降序");
		Map<String, String> resultMap1 = sortMapByKey(map,"DESCENDING");	//按Key进行降序排序
		for (Map.Entry<String, String> entry : resultMap1.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		System.out.println("value 升序");
		Map<String, String> resultMap2 = sortMapByValue(map, "ASCENDING");	//按Value进行排序
		for (Map.Entry<String, String> entry : resultMap2.entrySet()) {
			//System.out.println(entry.getKey() + " " + entry.getValue());
			System.out.println(entry);
		}
		
		System.out.println("vale 降序");
		Map<String, String> resultMap3 = sortMapByValue(map, "DESCENDING");	//按Value进行排序
		for (Map.Entry<String, String> entry : resultMap3.entrySet()) {
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
	
	
	class MapKeyComparatorDesc implements Comparator<String>{
		public int compare(String str1, String str2) {
			return -(str1.compareTo(str2)); //通过控制返回来逆序
		}
	}
	//比较器类
	class MapValueComparator implements Comparator<Map.Entry<String, String>> {
		public int compare(Entry<String, String> me1, Entry<String, String> me2) {
			return me1.getValue().compareTo(me2.getValue());
		}
	}
	
	class MapValueComparatorDesc implements Comparator<Map.Entry<String, String>> {
		public int compare(Entry<String, String> me1, Entry<String, String> me2) {
			return -(me1.getValue().compareTo(me2.getValue()));
		}
	}

