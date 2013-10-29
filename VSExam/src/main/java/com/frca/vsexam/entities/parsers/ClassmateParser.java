package com.frca.vsexam.entities.parsers;

import com.frca.vsexam.entities.base.Classmate;
import com.frca.vsexam.entities.base.ParentEntity;

import org.jsoup.nodes.Element;

/**
 * Created by KillerFrca on 29.10.13.
 */
public class ClassmateParser extends BaseParser {

    @Override
    protected ParentEntity doParse() throws EntityParsingException {
        Classmate classmate = new Classmate();

        Element profile = getLinkFromColumn(2);

        classmate.setId(extractParameterFromLink(profile, "id"));
        classmate.setName(profile.text().trim());
        classmate.setRegistered(parseDate(getColumnContent(3, true)));
        classmate.setIdentification(getColumnContent(4, true));

        return classmate;
    }
}
