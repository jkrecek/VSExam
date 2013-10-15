package com.frca.vsexam.fragments;

/**
 * Created by KillerFrca on 11.10.13.
 */
public class ListFragment extends android.support.v4.app.ListFragment {

    //private ExamList examList;

    /*public ListFragment() {

    }*/

    /*public ListFragment(ExamList examList) {
        this.examList = examList;
    }*/

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        s
        //View rootView = inflater.inflate(R.layout.exam_list, container, false);
        /*if (examList == null) {
            rootView = inflater.inflate(R.layout.layout_loading, container, false);
        } else {
            rootView = inflater.inflate(R.layout.exam_list, container, false);
            ListView lv = (ListView)rootView.findViewById(R.id.list_view);*/
            /*ExamAdapter adapter = new ExamAdapter(getActivity(), examList);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(adapter.new OnExamClickListener());*/
            /*ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.exam_list_item, R.id.text_title, examList.getCourseNames());
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    browserPaneFragment.onClick(i);
                }
            });*/
        //}
        //}

        /*return rootView;
    }*/

    /*@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final BrowserPaneFragment browserPaneFragment = (BrowserPaneFragment) getParentFragment();
        ExamList examList = browserPaneFragment.getExams();

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.exam_list_item, R.id.text_title, examList.getCourseNames());

        setListAdapter(adapter);
    }*/
}