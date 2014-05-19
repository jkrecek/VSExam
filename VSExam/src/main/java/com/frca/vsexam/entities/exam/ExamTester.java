package com.frca.vsexam.entities.exam;

import android.content.Context;

import com.frca.vsexam.entities.classmate.Classmate;
import com.frca.vsexam.entities.classmate.ClassmateList;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ExamTester {

    private static final long DAY = 86400000;
    private static final long HOUR = 3600000;

    private static final LinkedHashMap<String, String > coursesMap = new LinkedHashMap<String, String>(){{
        put("PHM", "Státní zkouška ze studijního oboru");
        put("3PA412", "Personální řízení 2");
        put("3TM401", "Management inovací");
        put("3MA413", "Manažerské rozhodování");
        put("3MI403", "Mikroekonomie");
    }};

    private static final String[] locations = {
        "RB 359",
        "RB 211",
        "Likešova aula",
        "SB 132",
        "Vencovského aula"
    };

    private static final String[] types = {
        "zkouška (ústní)",
        "zkouška (písemná)",
        "průběžný test 2 (písemná)"
    };

    private static final float[] occupancy = {
        0.1f, 0.5f, 0.9f, 1.f, 1.f
    };

    private static final int[] totalCapacity = {
        12, 12, 24, 48, 61, 90
    };

    private static final String[] names = {
        "Viktorie Němečková",
        "Svatava Havelková",
        "Anatoliy Soukal",
        "Eduard Žůrek",
        "Zlata Melmuková",
        "Ivana Pexová",
        "Martin Klimek",
        "Marta Matoušková",
        "Michal Lesák",
        "Josef Maršalík",
        "Emilie Drozdová",
        "Věra Šimková",
        "Martina Petřivalská",
        "František Tichý",
        "Marie Vejtrubová",
        "Anna Šlachetková",
        "Pavel Baron",
        "Jana Krejzová",
        "Jiřina Frühaufová",
        "Miloš Štefan",
        "Zdeněk Kinkor",
        "Jan Hloucha",
        "Dominik Popovyč",
        "Kamila Zemanová",
        "Petr Tomáštík",
        "Šárka Marková",
        "Vladimír Šarina",
        "Heřman Michalovský",
        "Vendula Přikrylová",
        "Ondřej Škrob",
        "Vlasta Slezáková",
        "Jaroslav Hamsa",
        "Petra Pekaříková",
        "Tomáš Kubla",
        "Benedikt Peterka",
        "Milan Kraják",
        "Jiří Málek",
        "Zdenka Simonová",
        "Alena Margoliusová",
        "Lucie Ludvíková",
        "Karolína Popelková",
        "Eva Šusterová",
        "Alžběta Mádlová",
        "Pavla Blovská",
        "Blanka Gálová",
        "Miroslav Pavelka",
        "Luboš Med",
        "Magdalena Ochranová",
        "Monika Sluková",
        "Tereza Janíčková",
        "Evžen Klobása",
        "Patrik Trlica",
        "Václav Navrátil",
        "Miroslava Jurková",
        "Aleš Prokeš",
        "Milena Sopuchová",
        "Natálie Wernerová",
        "Irena Košatková",
        "Hana Zárybnická",
        "Helena Holá",
        "Antonín Vlk",
        "Žaneta Hečová",
        "Lenka Skýpalová",
        "Jarmila Valentová",
        "Kateřina Musilová",
        "Jitka Šafaříková",
        "Olga Čechová",
        "Radek Kalousek",
        "Dana Gregorová",
        "Simona Stachová",
        "Miloslava Pfauová",
        "Veronika Bůchová",
        "Marcela Kučerová",
        "Karel Hvězda",
        "Růžena Kysová",
        "Ludmila Fučíková",
        "Kristina Flášarová",
        "David Šmíd",
        "Jakub Špidlen",
        "Ilona Boudná",
        "Barbora Hnudová",
        "Kristýna Čapounová",
        "Ladislav Skotek",
        "Adéla Kamenská",
        "Zdeňka Kozáková",
        "Michaela Křešničková",
        "Ervin Honyš",
        "Božena Dorazilová",
        "Jaroslava Szkanderová",
        "Lukáš Kučera",
        "Oldřich Boor",
        "Roman Lepil",
        "Ivo Bejček",
        "Zora Horvátová",
        "Olena Oščádalová",
        "Melanie Doležalová",
        "Klára Balajková",
        "Yaroslav Suchánek",
        "Adam Matz",
        "Libuše Línková"
    };

    public static void fill(ExamList examList, Context context) {
        long dayStart = System.currentTimeMillis() / DAY * DAY;
        long todaySeconds = System.currentTimeMillis() - dayStart;

        ArrayList<Map.Entry<String, String>> courses = new ArrayList<Map.Entry<String, String>>(coursesMap.entrySet());

        for (int i = 0; i < 20; ++i) {
            Exam exam = examList.createExam(context, 1000 + i);

            exam.setGroup(Exam.Group.values()[((int) (Math.random() * 4))]);

            int randomCourse = (int) (Math.random() * courses.size());
            Map.Entry<String, String> entry = courses.get(randomCourse);
            exam.setCourseName(entry.getValue());
            exam.setCourseCode(entry.getKey());
            exam.setCourseId(100000+randomCourse);
            exam.setLocation(locations[((int) (Math.random() * locations.length))]);
            exam.setType(types[((int) (Math.random() * types.length))]);

            long examDate = dayStart +
                (long)(Math.random() * 60 * DAY) +
                (long)(((Math.random() * 8) + 8) * HOUR);

            exam.setExamDate(new Date(examDate));
            exam.setRegisterStart   (new Date(dayStart));
            exam.setRegisterEnd     (new Date(examDate - 10 * DAY));
            exam.setUnregisterEnd   (new Date(examDate - 10 * DAY));

            exam.setAuthorId(401);
            exam.setAuthorName("I. Vostřelová");
            exam.setMaxCapacity(totalCapacity[((int) (Math.random() * totalCapacity.length))]);
            exam.setCurrentCapacity((int) (exam.getMaxCapacity() * occupancy[((int) (Math.random() * occupancy.length))]));

            exam.setStudyId(123456);
            exam.setPeriodId(825);

            ClassmateList classmateList = new ClassmateList();
            for (int i2 = 0; i2 < exam.getCurrentCapacity(); ++i2) {
                Classmate classmate = new Classmate((int) (Math.random() * 65536));
                classmate.setName(names[i2]);
                classmate.setRegistered(new Date((long) (dayStart - Math.random() * todaySeconds)));
                classmate.setIdentification("FPH B-EM-PE prez [sem 6, E]");
                classmateList.add(classmate);
            }

            exam.setClassmates(classmateList);
            examList.add(exam);
        }

        examList.finalizeInit();
    }
}
