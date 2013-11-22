package com.frca.vsexam.entities.classmate;

import com.frca.vsexam.entities.base.BaseParser;
import com.frca.vsexam.entities.base.ParentEntity;

import org.jsoup.nodes.Element;

/**
 * Created by KillerFrca on 29.10.13.
 */
public class ClassmateParser extends BaseParser {

    @Override
    protected ParentEntity doParse() throws EntityParsingException {
        Element profile = getLinkFromColumn(2);
        int id = extractParameterFromLink(profile, "id");

        Classmate classmate = new Classmate(id);
        classmate.setName(profile.text().trim());
        classmate.setRegistered(parseDate(getColumnContent(3, true)));
        classmate.setIdentification(getColumnContent(4, true));

        return classmate;
    }
}
