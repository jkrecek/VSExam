package com.frca.vsexam.fragments.exam_detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Utils;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import java.util.HashMap;
import java.util.Map;

public class ExamDataProvider extends DetailFragment.BaseExamProvider {

    private static final int TEXT_HEADER_IDS[] = {
        R.id.text_header_1,
        R.id.text_header_2,
        R.id.text_header_3,
        R.id.text_header_4
    };

    private static final int TEXT_CONTENT_IDS[] = {
        R.id.text_content_1,
        R.id.text_content_2,
        R.id.text_content_3,
        R.id.text_content_4
    };

    public ExamDataProvider(DetailFragment fragment, ViewGroup parent, LayoutInflater inflater) {
        super(fragment, parent, inflater, R.layout.exam_detail_data);
    }

    @Override
    public Result doLoad() {
        /*loopThoughChildren(new Helper.ViewCallback() {
            @Override
            public void call(View view) {
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    if (textView.getTypeface() != null &&
                        textView.getTypeface().isBold() &&
                        textView.getText() != null) {
                        textView.setText(textView.getText().toString().toUpperCase());
                    }
                }
            }
        });*/

        setViewText(R.id.text_courseCode, mExam.getCourseCode());
        setViewText(R.id.text_courseName, mExam.getCourseName());
        setViewText(R.id.text_authorName, mExam.getAuthorName());
        setViewText(R.id.text_type, mExam.getType());
        setViewText(R.id.text_location, mExam.getLocation());

        findViewById(R.id.layout_group).setBackgroundResource(mExam.getGroup().getColorRes());
        setViewText(R.id.text_group, mExam.getGroup().getTitleRes());

        setViewText(R.id.text_capacity, String.valueOf(mExam.getCurrentCapacity()) + "/" + String.valueOf(mExam.getMaxCapacity()));

        TextView freeCapacityView = (TextView) findViewById(R.id.text_capacity_free);
        freeCapacityView.setText(String.valueOf(mExam.getMaxCapacity() - mExam.getCurrentCapacity()));
        freeCapacityView.setTextColor(getContext().getResources().getColor(
            getColorForCapacity(mExam.getCurrentCapacity(), mExam.getMaxCapacity())
        ));
        setViewText(R.id.text_capacity_occupied, String.valueOf(mExam.getCurrentCapacity()));
        setViewText(R.id.text_capacity_total, String.valueOf(mExam.getMaxCapacity()));

        setViewText(R.id.text_examDate, Utils.getDateOutput(mExam.getExamDate(), Utils.DateOutputType.FULL_WITH_DAY_ONELINE));

        setOnClickListener(R.id.button_author, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.openPartialUrl(getContext(), "lide/clovek.pl?id=" + String.valueOf(mExam.getAuthorId()));
            }
        });

        setOnClickListener(R.id.button_syllabus, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.openPartialUrl(getContext(), "katalog/syllabus.pl?predmet=" + String.valueOf(mExam.getCourseId()));
            }
        });

        setOnClickListener(R.id.button_web, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.openPartialUrl(getContext(), "student/terminy_info.pl?termin=" + mExam.getId() + ";studium=" + mExam.getStudyId() + ";obdobi=" + mExam.getPeriodId());
            }
        });

        BaseNetworkTask.run(new UserImageNetworkTask(getContext(), mExam.getAuthorId(), findViewById(R.id.logo_author)));

        ImageButton button_calendar = (ImageButton) findViewById(R.id.button_calendar);
        if  (DataHolder.getInstance(getContext()).getEventExamSet().has(mExam.getId())) {
            button_calendar.setImageResource(R.drawable.ic_calendar_del);
            button_calendar.setOnClickListener(removeExamFromCalendarListener);
        } else {
            button_calendar.setImageResource(R.drawable.ic_calendar_add);
            button_calendar.setOnClickListener(addExamToCalendarListener);
        }

        setUpRegistrationLayout((FrameLayout) findViewById(R.id.layout_registration));

        return Result.DONE;
    }

    private View.OnClickListener addExamToCalendarListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getExams().putExamToCalendar(mExam, getContext());
            load();
        }
    };

    private View.OnClickListener removeExamFromCalendarListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getExams().removeExamFromCalendar(mExam, getContext());
            load();
        }
    };

    private void setUpRegistrationLayout(FrameLayout parent) {
        long now = System.currentTimeMillis();
        Map<Integer, Long> headerTimeValues = new HashMap<Integer, Long>();
        headerTimeValues.put(R.string.register_start, mExam.getRegisterStart().getTime());
        headerTimeValues.put(R.string.unregister_end, mExam.getUnregisterEnd().getTime());
        if (mExam.getUnregisterEnd().getTime() != mExam.getRegisterEnd().getTime())
            headerTimeValues.put(R.string.register_end, mExam.getRegisterEnd().getTime());
        headerTimeValues.put(R.string.now, now);

        headerTimeValues = Utils.sortByValue(headerTimeValues);

        boolean isStacked = headerTimeValues.size() == 3;
        int requiredResource = isStacked ? R.layout.registration_stacked : R.layout.registration_full;
        FrameLayout inflatedLayout = (FrameLayout) getInflater().inflate(requiredResource, parent, true);

        if (inflatedLayout == null)
            return;

        int loopCounter = 0;
        int nowPosition = -1;
        for (Map.Entry<Integer, Long> entry : headerTimeValues.entrySet()) {
            TextView textHeader = (TextView) inflatedLayout.findViewById(TEXT_HEADER_IDS[loopCounter]);
            TextView textContent = (TextView) inflatedLayout.findViewById(TEXT_CONTENT_IDS[loopCounter]);

            if (entry.getKey() == R.string.unregister_end && isStacked) {
                textHeader.setText(getContext().getString(R.string.unregister_end) + "\n" + getContext().getString(R.string.register_end));
            } else {
                textHeader.setText(entry.getKey());
            }

            if (entry.getKey() == R.string.now) {
                nowPosition = loopCounter;
                textContent.setVisibility(View.GONE);
                textHeader.setTextColor(getContext().getResources().getColor(R.color.register_now));
            } else {
                textContent.setText(Utils.getDateOutput(entry.getValue(), Utils.DateOutputType.FULL_WITH_DAY_MULTILINE));
            }

            ++loopCounter;
        }

        LinearLayout layoutTimeLine = (LinearLayout) inflatedLayout.findViewById(R.id.layout_time_line);
        View nowView = layoutTimeLine.getChildAt(2 * nowPosition);
        if (nowView != null)
            nowView.setBackgroundResource(R.color.register_now);

        TableLayout table = (TableLayout) findViewById(R.id.layout_table);
        long examTimes[] = {
            mExam.getRegisterStart().getTime(),
            mExam.getUnregisterEnd().getTime(),
            mExam.getRegisterEnd().getTime(),
            mExam.getExamDate().getTime()
        };

        for (int i = 0; i < examTimes.length; ++i) {
            TableRow row = (TableRow) table.getChildAt(i);
            if (row != null) {
                long timeDiff = examTimes[i] - now;
                if (timeDiff < 0)
                    row.setVisibility(View.GONE);
                else {
                    TextView textView = (TextView) row.getChildAt(1);
                    if (textView != null)
                        textView.setText(Utils.secondsCountdown(getContext(), timeDiff, false));
                }
            }

        }

    }

    public static int getColorForCapacity(int current, int max) {
        return current == max ? R.color.red :
               current > (int) Math.min(max * 0.8f, max - 5f) ? R.color.orange :
               R.color.green;
    }

}
