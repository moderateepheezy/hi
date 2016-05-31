package com.exolvetechnologies.hidoctor.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.exolvetechnologies.hidoctor.R;
import com.exolvetechnologies.hidoctor.activities.BlogDetailsActivity;
import com.exolvetechnologies.hidoctor.activities.BlogListActivity;
import com.exolvetechnologies.hidoctor.adapters.BlogListAdapter;
import com.exolvetechnologies.hidoctor.data.AppDataManager;
import com.exolvetechnologies.hidoctor.utilities.AppUtils;
import com.exolvetechnologies.hidoctor.utilities.BlogLoadListener;

import java.util.HashMap;
import java.util.List;

public class BlogCategoryFragment extends Fragment implements BlogLoadListener,
        AdapterView.OnItemClickListener{

    private static final String CAT_ID = "catId";

    private String categoryId;

    private AppDataManager dataManager;
    private AbsListView listView;
    private ProgressBar progressBar;
    private BlogListAdapter adapter;
    private List<HashMap<String, String>> blogs;


    public BlogCategoryFragment() {
        // Required empty public constructor
    }

    public static BlogCategoryFragment newInstance(String catId) {
        BlogCategoryFragment fragment = new BlogCategoryFragment();
        Bundle args = new Bundle();
        args.putString(CAT_ID, catId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(CAT_ID);
        }

        dataManager = new AppDataManager(getActivity());
        BlogListActivity.loadListener = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blog_category, container, false);

        listView = (AbsListView) view.findViewById(R.id.blogList);
        progressBar = (ProgressBar) view.findViewById(R.id.getBlogProgress);

        try{
            blogs = dataManager.getCatBlogs(categoryId);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (blogs != null && (blogs.size() > 0)){
            adapter = new BlogListAdapter(blogs, getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

        }else {
            progressBar.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onLoadComplete() {
        progressBar.setVisibility(View.GONE);

        try{
            blogs = dataManager.getCatBlogs(categoryId);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (blogs != null && (blogs.size() > 0)){
            adapter = new BlogListAdapter(blogs, getActivity());
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(getActivity(), BlogDetailsActivity.class)
                .putExtra(BlogDetailsActivity.ID, blogs.get(position).get("id")));
    }
}
