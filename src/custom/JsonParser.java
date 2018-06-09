package custom;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class JsonParser {

    public static Map<String, String> parseJson(Object object) {
        Map<String, String> fieldMap = new HashMap<String, String>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                String fieldName = field.getName();
                field.setAccessible(true);
                Object value = field.get(object);
                if (value instanceof String) {
                    fieldMap.put(fieldName, (String) value);
                }
            } catch (Exception e) {
            }
        }
        return fieldMap;
     }
	public static void main(String[] args) {
		String jsonstring = "{\"body\":{\"phone\":\"13750024220\",\"productIds\":\"2200001141\",\"type\":\"2\"},\"header\":{\"channelId\":\"1469763034851\",\"seqNo\":\"SeqNo\",\"sign\":\"486fcd47a0d0b994de85e995ec152bb1\",\"signNonce\":\"kjl;asjifaskl;fjkl;asfjkl;asjiol;'f\",\"signType\":\"MD5\",\"timestamp\":\"1470643726268\",\"version\":\"1.0\"}}";
		Map<String, String> a = parseJson(jsonstring);
		System.out.println(a.values());
	}
}
