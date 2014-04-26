package com.frca.vsexam.entities.classmate;

import com.frca.vsexam.entities.base.BaseParser;
import com.frca.vsexam.entities.base.ParentEntity;

import org.jsoup.nodes.Element;

public class ClassmateParser extends BaseParser {

    @Override
    protected ParentEntity doParse() {
        Element profile = getLinkFromColumn(2);
        int id = extractParameterFromLink(profile, "id");

        Classmate classmate = new Classmate(id);
        classmate.setName(profile.text().trim());
        classmate.setRegistered(parseDate(getColumnContent(3, true)));
        classmate.setIdentification(getColumnContent(4, true));

        return classmate;
    }
}
