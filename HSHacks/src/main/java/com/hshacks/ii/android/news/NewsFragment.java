package com.hshacks.ii.android.news;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hshacks.ii.android.R;
import com.hshacks.ii.android.pong.PongHeaderTransformer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class NewsFragment extends Fragment implements OnRefreshListener {
    private NewsAdapter mAdapter;
    private ArrayList<ParseObject> mAnnouncements;

    private ListView mListView;
    private PullToRefreshLayout mPullToRefreshLayout;

    public NewsFragment() {
        mAnnouncements = new ArrayList<ParseObject>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_news, container, false);

        mListView = (ListView) layout.findViewById(R.id.news_listview);
        mListView.addHeaderView(new View(getActivity()));
        mListView.addFooterView(new View(getActivity()));

        mAdapter = new NewsAdapter(this.getActivity(), mAnnouncements);
        mListView.setAdapter(mAdapter);

        setRetainInstance(true);

        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ViewGroup container = (ViewGroup) view;

        mPullToRefreshLayout = new PullToRefreshLayout(container.getContext());
        ActionBarPullToRefresh.from(getActivity())
                .options(Options.create()
                        .scrollDistance(0.5f)
                        .headerLayout(R.layout.pong_header)
                        .headerTransformer(new PongHeaderTransformer())
                        .build())
                .insertLayoutInto(container)
                .theseChildrenArePullable(R.id.news_listview)
                .listener(this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdapter.getCount() == 0) {
            refresh();
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Announcements");
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    Log.d("News", "Got " + parseObjects.size() + " announcements.");
                    mAnnouncements.clear();
                    mAnnouncements.addAll(parseObjects);
                    mAdapter.notifyDataSetChanged();
                }
                mPullToRefreshLayout.setRefreshComplete();
            }
        });
    }

    private void refresh() {
        mPullToRefreshLayout.setRefreshing(true);
        onRefreshStarted(mListView);
    }
}