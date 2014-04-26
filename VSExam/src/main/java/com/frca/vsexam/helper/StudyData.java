package com.frca.vsexam.helper;

import android.content.Context;
import android.util.Log;

import com.frca.vsexam.fragments.TestFragment;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.TextNetworkTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudyData {

    public static class BaseStudyDataElement {
        private List<String> codes = new ArrayList<String>();
        public String name;

        public void addCode(String code) {
            if (!codes.contains(code))
                codes.add(code);
        }
    }

    public static class Faculty extends BaseStudyDataElement {
        public int id;
        public List<StudyType> types = new ArrayList<StudyType>();
    }

    public static class StudyType extends BaseStudyDataElement {
        public List<Programme> programmes = new ArrayList<Programme>();
        public List<Specialization> specializations = new ArrayList<Specialization>();
    }

    public static class Programme extends BaseStudyDataElement {
        public List<Field> fields = new ArrayList<Field>();
    }

    public static class Specialization extends BaseStudyDataElement { }

    public static class Field extends BaseStudyDataElement { }

    enum Plan {
        OTHERS(5, new int[] {1, 2, 4}),
        DOC(23, new int[] {3});

        public int mId;
        public int[] mPlans;
        private Plan(int id, int[] plans) {
            mId = id;
            mPlans = plans;
        }

        public static Plan getValueForId(int id) {
            for (Plan plan : values()) {
                if (plan.mId == id)
                    return plan;
            }

            return null;
        }


    }

    private Context mContext;

    private OnLoadedCallback mCallback;

    private List<Faculty> mFaculties;

    private List<BaseNetworkTask> mRunningTasks = new ArrayList<BaseNetworkTask>();

    public static interface OnLoadedCallback {
        abstract void loaded(List<Faculty> faculties);
    }

    private StudyData(Context context, OnLoadedCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    public static List<Faculty> loadData(Context context, OnLoadedCallback callback) {
        StudyData instance = new StudyData(context, callback);
        return  instance.loadFaculties();
    }


    public List<Faculty> loadFaculties() {
        mFaculties = new ArrayList<Faculty>();
        for (int facultyId = 1; facultyId < 7; ++facultyId) {
            final int id = facultyId;
            Map<String, String> args = new HashMap<String, String>() {{
                put("fakulta", String.valueOf(id * 10));
            }};

            runTask(args, new OnDocumentLoaded() {
                @Override
                public void loaded(Document document) throws Exception {
                    mFaculties.add(handleBaseFacultyData(id, document));
                }
            });
        }

        return mFaculties;
    }

    private Faculty handleBaseFacultyData(int id, Document doc)  throws Exception  {
        final Faculty faculty = new Faculty();
        faculty.id = id;

        Elements tables = doc.body().select("table");
        faculty.name = tables.get(0).select("tbody td").get(1).text();

        Elements elements = tables.get(1).select("tbody tr");

        for (Element row : elements) {
            Elements cells = row.getElementsByTag("td");

            String link = cells.get(2).getElementsByTag("a").get(0).attr("href");

            Map<String, String> args = HttpRequestBuilder.getGetArguments(link, '=', ';');

            Plan plan = Plan.getValueForId(Integer.parseInt(args.get("typ_ss")));

            if (plan == null)
                continue;

            String period = cells.get(1).text();

            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            if (calendar.get(Calendar.MONTH) <= Calendar.SEPTEMBER)
                --currentYear;

            Pattern pattern = Pattern.compile("^(ZS|WS) (" + String.valueOf(currentYear) + ")/(\\d{4})");
            Matcher matcher = pattern.matcher(period);
            if (!matcher.find())
                continue;

            for (int plans : plan.mPlans) {
                args.put("typ_studia", String.valueOf(plans));
                runTask(args, new OnDocumentLoaded() {
                    @Override
                    public void loaded(Document document) throws Exception {
                        faculty.addCode(getFacultyCode(document));
                        faculty.types.add(handleProgramData(document));
                    }
                });
            }
        }

        return faculty;
    }

    private String getFacultyCode(Document doc) throws Exception {
        Elements tables = doc.body().select("table");
        String facultyString = tables.get(0).select("tbody tr").get(2).select("td").get(1).text();
        return facultyString.substring(facultyString.lastIndexOf(' ') + 1);
    }

    private StudyType handleProgramData(Document doc) throws Exception {
        StudyType studyType = new StudyType();
        Elements tables = doc.body().select("table");
        studyType.name = tables.get(0).select("tbody tr").get(3).select("td").get(1).text();

        Elements programmeRows = tables.get(1).select("tbody tr");

        for (Element row : programmeRows) {
            Elements cells = row.getElementsByTag("td");

            if (cells.get(0).hasAttr("width"))
                break;

            String link = cells.get(5).getElementsByTag("a").get(0).attr("href");
            Map<String, String> args = HttpRequestBuilder.getGetArguments(link, '=', ';');

            String programmeString = cells.get(1).text();

            final Programme programme = new Programme();
            int spaceIdx = programmeString.indexOf(" ");
            String[] codes = programmeString.substring(0, spaceIdx).split("-");
            programme.name = programmeString.substring(spaceIdx + 1);
            programme.addCode(codes[codes.length - 1]);

            studyType.addCode(codes[0]);

            runTask(args, new OnDocumentLoaded() {
                @Override
                public void loaded(Document document) throws Exception {
                    programme.fields = handleFieldsData(document);
                }
            });

            studyType.programmes.add(programme);
        }

        Elements specializationRows = tables.get(2).select("tbody tr");
        for (Element row : specializationRows) {
            Elements cells = row.getElementsByTag("td");

            if (cells.get(0).hasAttr("width"))
                break;

            String specializationString = cells.get(1).text();
            String[] specializationParts = specializationString.split(" ", 2);
            Specialization specialization = new Specialization();
            specialization.addCode(specializationParts[0]);
            specialization.name = specializationParts[1];
            studyType.specializations.add(specialization);
        }
        return studyType;
    }

    private List<Field> handleFieldsData(Document doc) throws Exception {
        Elements tables = doc.body().select("table");
        Elements fieldRows = tables.get(1).select("tbody tr");

        List<Field> fields = new ArrayList<Field>();
        for (Element row : fieldRows) {
            Elements cells = row.getElementsByTag("td");

            Field field = new Field();
            field.name = cells.get(1).text();

            String[] codes = cells.get(0).text().split("-");
            field.addCode(codes[codes.length - 1]);

            fields.add(field);
        }

        return fields;

    }

    private void runTask(Map<String,String> args, final OnDocumentLoaded callback) {
        final TextNetworkTask task = new TextNetworkTask(mContext, HttpRequestBuilder.getPlanRequest(DataHolder.getInstance(mContext), args), new BaseNetworkTask.ResponseCallback() {
            @Override
            public void onSuccess(Response response) {
                if (response.getStatusCode() == 302)
                    return;

                Document doc = Jsoup.parse(response.getText());
                try {
                    callback.loaded(doc);
                } catch (Exception e) {
                    /* TODO */
                }
            }
        });

        task.setFinishCallback(new BaseNetworkTask.FinishCallback() {
            @Override
            public void onFinish(BaseNetworkTask.Result result) {
                mRunningTasks.remove(task);
                TestFragment.postMessage(task.getRequest().getURI().toString(), TestFragment.Type.REMOVE);
                if (mRunningTasks.isEmpty())
                    mCallback.loaded(mFaculties);
            }
        });


        mRunningTasks.add(task);
        TestFragment.postMessage(task.getRequest().getURI().toString(), TestFragment.Type.ADD);
        BaseNetworkTask.run(task);
    }

    private static interface OnDocumentLoaded {
        abstract void loaded(Document document) throws Exception;
    }
}
