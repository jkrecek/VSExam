package com.frca.vsexam.entities;

import android.util.Log;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

public class Classmate extends ParentEntity {

    public final int id;
    public final String name;
    public final Date registered;
    public final String identification;

    public static Classmate get(Elements columns) {
        try {
            return new Classmate(columns);
        } catch (EntityParsingException e) {
            Log.e(Classmate.class.getName(), e.getMessage());
            return null;
        }
    }

    private Classmate(Elements columns) throws EntityParsingException {
        super.init(columns);

        Element profile = getLinkFromColumn(2);
        id = extractParameterFromLink(profile, "id");
        name = profile.text().trim();

        registered = parseDate(getColumnContent(3, true));
        identification = getColumnContent(4, true);

        super.initDone();
    }
}
