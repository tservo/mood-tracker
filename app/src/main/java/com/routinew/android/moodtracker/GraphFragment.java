package com.routinew.android.moodtracker;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.routinew.android.moodtracker.POJO.Mood;
import com.routinew.android.moodtracker.ViewModels.MoodViewModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class GraphFragment extends Fragment {

    private MoodViewModel mViewModel;

    // fragments have a special lifestyle, so need this.
    private Unbinder unbinder;
    @BindView(R.id.graph) GraphView mGraphView;


    public static GraphFragment newInstance() {
        return new GraphFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.graph_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(MoodViewModel.class);
        mViewModel.getMoods().observe(this, new Observer<List<Mood>>() {
            @Override
            public void onChanged(@Nullable List<Mood> moods) {
                handleGraph(moods); // the graph monitors this list of moods
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // clean up after butterknife
        unbinder.unbind();
    }

    // private helper methods

    // here we take the mood data and plot it out.
    private void handleGraph(List<Mood> moods) {

        // we need the first and last dates
        // populate the graph view here.
        Collections.sort(moods, Mood.dateComparator()); // make sure they are in order first!

        // populate the series
        DataPoint[] moodData = new DataPoint[moods.size()];
        for (int i=0; i < moods.size(); i++) {
            Mood mood = moods.get(i);
           // moodData[i] = new DataPoint(mood.getDate().getTime(), mood.getMoodScore());
            moodData[i] = new DataPoint(mood.getDate().getTime(), mood.getMoodScore());
        }

        // set static Y axis and date label X-axis formatter
        StaticLabelsFormatter labelsFormatter =
                new StaticLabelsFormatter(mGraphView , new DateAsXAxisLabelFormatter(getActivity()));
        labelsFormatter.setVerticalLabels(
                new String[] {getString(R.string.label_depressed),
                        getString(R.string.label_neutral),
                        getString(R.string.label_manic)});
        mGraphView.getGridLabelRenderer().setLabelFormatter(labelsFormatter);

        mGraphView.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

        // set manual x bounds to have nice steps
        mGraphView.getViewport().setXAxisBoundsManual(true);

        mGraphView.getViewport().setMinX(moodData[0].getX());
        mGraphView.getViewport().setMaxX(moodData[moodData.length-1].getX());
        mGraphView.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        mGraphView.getGridLabelRenderer().setHumanRounding(false);

        // and we can zoom/scroll it.
        mGraphView.getViewport().setScalable(true);

        // add them to the graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(moodData);
        mGraphView.addSeries(series);



    }
}
