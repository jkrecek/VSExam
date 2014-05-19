package com.frca.vsexam.entities.classmate;

import com.frca.vsexam.entities.base.BaseEntityList;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ClassmateList extends BaseEntityList<Classmate> {

    public void parseAndAdd(Elements elements) {

        for (Element element : elements) {
            Elements columns = element.select("td");
            if (columns.size() <= 1)
                continue;

            ClassmateParser parser = new ClassmateParser();
            add((Classmate)parser.parse(columns));
        }
    }
}
