package com.exolvetechnologies.hidoctor.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.activities.PregnancyCareDetailsActivity;
import com.exolvetechnologies.hidoctor.adapters.PregnancyCareAdapter;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PregnancyCareFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String TAG = "HiDoctor";

    private ProgressBar faqProgress;
    private AppDataManager dataManager;
    private AppUtils utils;
    private AbsListView listView;
    private ProgressBar progressBar;
    private PregnancyCareAdapter adapter;
    private List<HashMap<String, String>> cares;
    private Dialog diag;


    public PregnancyCareFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataManager = new AppDataManager(getActivity());
        utils = new AppUtils(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pregnancy_care, container, false);

        faqProgress = (ProgressBar) view.findViewById(R.id.careProgress);
        progressBar = (ProgressBar) view.findViewById(R.id.getCareProgress);
        listView = (AbsListView) view.findViewById(R.id.careList);

        try{
            cares = dataManager.getPregnancyCares();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (cares != null && (cares.size() > 0)){
            adapter = new PregnancyCareAdapter(cares, getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
            if (utils.isConnectedToInternet()){
                new GetBlogsAsync(false).execute();
            }

        }else {
            if (utils.isConnectedToInternet()){
                new GetBlogsAsync(true).execute();
            }else {
                utils.showNetworkError(getActivity());
            }
        }

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(getActivity(), PregnancyCareDetailsActivity.class)
                .putExtra(PregnancyCareDetailsActivity.ID, cares.get(position).get("id")));
    }

    //region OldAdapter
    /*private void setCareAdapter(List<HashMap<String, String>> faqs){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < 50; i ++) {

            FrameLayout holder = new FrameLayout(getActivity());
            View layout = inflater.inflate(R.layout.model_pregnancy_care, holder, false);

            TextView question = (TextView) layout.findViewById(android.R.id.text1);
            final TextView answer = (TextView) layout.findViewById(android.R.id.text2);
            final LinearLayout detailsLayout = (LinearLayout) layout.findViewById(R.id.detailsLayout);

            //final String answerStr = faqs.get(i).get("answer");

            question.setText(Html.fromHtml("Week "+(i+1)));
            question.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    answer.setText(Html.fromHtml(getString(R.string.token_error_message)));
                    if (detailsLayout.getVisibility() == View.VISIBLE) {
                        detailsLayout.setVisibility(View.GONE);
                    } else {
                        detailsLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            faqsLayout.addView(layout);
        }
    }*/
    //endregion

    private class GetBlogsAsync extends AsyncTask<Void, Integer, String> {
        boolean showProgress;

        public GetBlogsAsync(boolean showProgress) {
            this.showProgress = showProgress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgress) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return AppUtils.getRequest(AppUtils.API_ROOT + "/api/pregnancycare");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.GONE);
            if (s != null){
                try {
                    JSONObject object = new JSONObject("{data:"+s+"}");
                    JSONArray array = object.getJSONArray("data");
                    for (int i = 0; i < array.length(); i ++){
                        JSONObject cat = array.getJSONObject(i);
                        String id = cat.getString("id");
                        String week = cat.getString("week");
                        String excerpt = cat.getString("excerpt");
                        String imageUrl = cat.getString("image");
                        String content = cat.getString("content");

                        dataManager.insertPregnancyCare(id, week, imageUrl, excerpt, content);
                    }

                        cares = dataManager.getPregnancyCares();

                    if (cares != null && (cares.size() > 0)) {
                        adapter = new PregnancyCareAdapter(cares, getActivity());
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(PregnancyCareFragment.this);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showWeeks(){
        ArrayAdapter<String> categoryAdapter, mediaAdapter;
        String[] categories = {"--Please Select--","Secondary School Student",
                "Undergraduate"," Job Seeker","Career Changer","Parent/Guardian","Career Counsellor",
                "Service Provider"};
        //String[] mediaArray = {"--Please Select--","Family/Friend","Social Media","CDNet Website/Newsletter",
        //	"Other Websites","Search Engine","Event","Email/SMS"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogView = inflater.inflate(R.layout.dialog_care_subscription, null);
        final Spinner category = (Spinner) dialogView.findViewById(R.id.spinnerWeek);
    }

}
