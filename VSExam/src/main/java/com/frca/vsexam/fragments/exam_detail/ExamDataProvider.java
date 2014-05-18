package com.frca.vsexam.fragments.exam_detail;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.frca.vsexam.R;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.helper.Helper;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.tasks.BaseNetworkTask;
import com.frca.vsexam.network.tasks.UserImageNetworkTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

        findViewById(R.id.layout_group).setBackgroundResource(mExam.getGroup().getColorRes());
        setViewText(R.id.text_group, mExam.getGroup().getTitleRes());

        setViewText(R.id.text_capacity, String.valueOf(mExam.getCurrentCapacity()) + "/" + String.valueOf(mExam.getMaxCapacity()));
        setViewText(R.id.text_examDate, Helper.getDateOutput(mExam.getExamDate(), Helper.DateOutputType.FULL_WITH_DAY_ONELINE));
        /*setViewText(R.id.text_registerStart, Helper.getDateOutput(mExam.getRegisterStart(), Helper.DateOutputType.DATE_TIME));
        setViewText(R.id.text_registerEnd, Helper.getDateOutput(mExam.getRegisterEnd(), Helper.DateOutputType.DATE_TIME));
        setViewText(R.id.text_unregisterEnd, Helper.getDateOutput(mExam.getUnregisterEnd(), Helper.DateOutputType.DATE_TIME));*/

        setOnClickListener(R.id.button_author, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authorUrl = HttpRequestBuilder.completeURLString("lide/clovek.pl?id=" + String.valueOf(mExam.getAuthorId()), true);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorUrl));
                getContext().startActivity(browserIntent);
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
        Map<Integer, Long> headerTimeValues = new HashMap<Integer, Long>();
        headerTimeValues.put(R.string.register_start, mExam.getRegisterStart().getTime());
        headerTimeValues.put(R.string.unregister_end, mExam.getUnregisterEnd().getTime());
        if (mExam.getUnregisterEnd().getTime() != mExam.getRegisterEnd().getTime())
            headerTimeValues.put(R.string.register_end, mExam.getRegisterEnd().getTime());
        headerTimeValues.put(R.string.now, System.currentTimeMillis());

        headerTimeValues = Helper.sortByValue(headerTimeValues);

        boolean isStacked = headerTimeValues.size() == 3;
        int requiredResource = isStacked ? R.layout.registration_stacked : R.layout.registration_full;
        FrameLayout inflatedLayout = (FrameLayout) getInflater().inflate(requiredResource, parent, true);

        if (inflatedLayout == null)
            return;

        Set<Map.Entry<Integer, Long>> entrySet = headerTimeValues.entrySet();
        int loopCounter = 0;
        int nowPosition = 0;
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
                textContent.setText(Helper.getDateOutput(entry.getValue(), Helper.DateOutputType.FULL_WITH_DAY_MULTILINE));
            }

            ++loopCounter;
        }

        LinearLayout layoutTimeLine = (LinearLayout) inflatedLayout.findViewById(R.id.layout_time_line);
        View nowView = layoutTimeLine.getChildAt(2 * nowPosition);
        if (nowView != null)
            nowView.setBackgroundResource(R.color.register_now);

        /*LinearLayout layoutTop = (LinearLayout) findViewById(R.id.layout_top);
        LinearLayout layoutTopLast = (LinearLayout) layoutTop.findViewById(R.id.layout_top_last);
        LinearLayout layoutBottom = (LinearLayout) findViewById(R.id.layout_bottom);
        FrameLayout layoutBottomLast = (FrameLayout) layoutBottom.findViewById(R.id.layout_bottom_last);

        View lastSpace = findViewById(R.id.layout_last_space);
        View lastPoint = findViewById(R.id.layout_last_point);

        layoutTop.setWeightSum(4);
        FrameLayout.LayoutParams paramsTopLast = (FrameLayout.LayoutParams)layoutTopLast.getLayoutParams();
        if (paramsTopLast != null)
            paramsTopLast.gravity = Gravity.RIGHT | Gravity.BOTTOM;

        layoutBottom.setWeightSum(2);
        LinearLayout.LayoutParams paramsBottom = (LinearLayout.LayoutParams)layoutBottom.getLayoutParams();
        if (paramsBottom != null)
            paramsBottom.gravity = Gravity.CENTER_HORIZONTAL;

        layoutBottomLast.setVisibility(View.GONE);

        lastSpace.setVisibility(View.GONE);
        lastPoint.setVisibility(View.GONE);*/
    }

}
