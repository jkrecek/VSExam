package com.frca.vsexam.entities.classmate;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ClassmateList extends ArrayList<Classmate> {

    public ClassmateList() {
        super();
    }
    public ClassmateList(Elements elements) {
        super();

        for (Element element : elements) {
            Elements columns = element.select("td");
            if (columns.size() <= 1)
                continue;

            ClassmateParser parser = new ClassmateParser();
            add((Classmate)parser.parse(columns));
        }
    }
}
