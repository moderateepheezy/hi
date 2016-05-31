package com.exolvetechnologies.hidoctor.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.activities.ForumThreadActivity;
import com.exolvetechnologies.hidoctor.adapters.ForumAdapter;
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
public class ForumFragment extends Fragment implements AdapterView.OnItemClickListener {

    private AppDataManager dataManager;
    private AbsListView listView;
    private AppUtils utils;
    private ProgressBar progressBar;
    private ForumAdapter adapter;
    private List<HashMap<String, String>> fora;


    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataManager = new AppDataManager(getActivity());
        utils = new AppUtils(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forum, container, false);

        listView = (AbsListView) view.findViewById(R.id.forumList);
        progressBar = (ProgressBar) view.findViewById(R.id.getBlogProgress);

        try{
            fora = dataManager.getFora();
        }catch (Exception e){
            e.printStackTrace();
        }

        if (fora != null && (fora.size() > 0)){
            adapter = new ForumAdapter(fora, getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

            if (utils.isConnectedToInternet()) {
                new GetForaAsync(false).execute();
            }

        }else {
            if (utils.isConnectedToInternet()) {
                new GetForaAsync(true).execute();
            }else {
                utils.showNetworkError(getActivity());
            }
        }

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ForumThreadActivity.class);
        intent.putExtra(ForumThreadActivity.FORUM_ID, fora.get(position).get("id"));
        startActivity(intent);
    }

    private class GetForaAsync extends AsyncTask<Void, Integer, String>{

        boolean showProgress;

        public GetForaAsync(boolean showProgress) {
            this.showProgress = showProgress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (showProgress){
                progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = AppUtils.API_ROOT+"/api/forum";
            try {
                return AppUtils.getRequest(url);
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

                        String id = cat.getString("forum_id");
                        String title = cat.getString("title");
                        String desc = cat.getString("description");
                        String privacy = cat.getString("privacy");

                        JSONArray tArray = cat.getJSONArray("thread");
                        for (int t = 0; t < tArray.length(); t ++){
                            JSONObject tCat = tArray.getJSONObject(t);
                            String tId = tCat.getString("thread_id");
                            String tTitle = tCat.getString("title");
                            String tDesc = tCat.getString("description");
                            /*String url = tCat.getString("image");*/

                            dataManager.insertThread(tId, tTitle, /*url,*/ tDesc, id);

                        }

                        dataManager.insertForum(id, title, privacy, desc);


                        fora = dataManager.getFora();

                        if (fora != null && (fora.size() > 0)){
                            adapter = new ForumAdapter(fora, getActivity());
                            listView.setAdapter(adapter);
                            listView.setOnItemClickListener(ForumFragment.this);
                        }

                        //List<HashMap<String, String>> rCats = dataManager.getUndownloadedCats();
                        //new DownloadFileAsync(rCats).execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
