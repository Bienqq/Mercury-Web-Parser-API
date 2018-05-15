package application;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;

public class HTMLCounter
{
    private Map<String, Long> tagMap = new HashMap<>();
    private Elements elements;

    public HTMLCounter(String htmlString)
    {
        //creating HTML document object
        Document document = Jsoup.parse(htmlString);
        elements = document.body().select("*");
    }

    public void countAndSortTags()
    {
        //counting found tags
        for (Element el : elements)
        {
            String tag = el.tagName();
            long number = tagMap.getOrDefault(tag, 0L) + 1;
            tagMap.put(tag, number);
        }
        //removing unnecessarily counted <body> tag from list
        tagMap.remove("body");
        tagMap = sortTags(tagMap);
    }
    
    private Map<String, Long> sortTags(Map<String, Long> tagMap)
    {
        List<Map.Entry<String, Long>> list = new ArrayList<>(tagMap.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<String, Long> result = new LinkedHashMap<>();

        for (Map.Entry<String, Long> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public Map<String, Long> getSortedTagList()
    {
        return tagMap;
    }
}





