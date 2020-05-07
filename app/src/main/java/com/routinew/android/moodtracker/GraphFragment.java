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
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.routinew.android.moodtracker.Data.FirebaseRealtimeDatabase.FirebaseRealtimeDatabaseMoodRepository;
import com.routinew.android.moodtracker.POJO.Mood;
import com.routinew.android.moodtracker.Utilities.CalendarUtilities;
import com.routinew.android.moodtracker.ViewModels.MoodViewModel;
import com.routinew.android.moodtracker.ViewModels.MoodViewModelFactory;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import timber.log.Timber;

public class GraphFragment extends Fragment {



    private MoodViewModel mViewModel;

    // fragments have a special lifestyle, so need this.
    private Unbinder unbinder;

    @BindView(R.id.graph) GraphView mGraphView;
    @BindView(R.id.spinner) Spinner mReportSpinner;
    @BindView(R.id.no_mood_data_label) TextView mNoMoodDataLabel;

    @OnItemSelected(R.id.spinner) void onSpinnerItemSelected(int position) {
        if (null != mViewModel) {
            CalendarUtilities.GraphRange graphRange;
            switch (position) {
                case 0:
                    graphRange = CalendarUtilities.GraphRange.GRAPH_2_WEEKS;
                    break;
                case 1:
                    graphRange = CalendarUtilities.GraphRange.GRAPH_1_MONTH;
                    break;
                case 2:
                    graphRange = CalendarUtilities.GraphRange.GRAPH_3_MONTHS;
                    break;
                case 3:
                    graphRange = CalendarUtilities.GraphRange.GRAPH_6_MONTHS;
                    break;
                case 4:
                    graphRange = CalendarUtilities.GraphRange.GRAPH_1_YEAR;
                    break;
                default:
                    Timber.w("Invalid Position: %d", position);
                    graphRange = CalendarUtilities.GraphRange.GRAPH_2_WEEKS;
            }

            mViewModel.getMoodDateRange().setValue(graphRange);
        }

        Timber.d("Spinner, position %d item %s",position, mReportSpinner.getItemAtPosition(position));
    }

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

        initializeGraph();


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MoodViewModelFactory moodViewModelFactory = new MoodViewModelFactory(FirebaseRealtimeDatabaseMoodRepository.getInstance());
        mViewModel = ViewModelProviders.of(requireActivity(), moodViewModelFactory).get(MoodViewModel.class);

        // initialize and handle changes
        initializeGraph();
        mViewModel.getReportMoods().observe(this, new Observer<List<Mood>>() {
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

    /**
     * handles the initial graph initialization
     */
    private void initializeGraph() {
        // set static Y axis and date label X-axis formatter
        StaticLabelsFormatter labelsFormatter =
                new StaticLabelsFormatter(mGraphView, new DateAsXAxisLabelFormatter(getActivity()));
        labelsFormatter.setVerticalLabels(
                new String[]{"",""});
        mGraphView.getGridLabelRenderer().setLabelFormatter(labelsFormatter);

        // see how many labels we can fit.
        mGraphView.getGridLabelRenderer()
                .setNumHorizontalLabels(
                        //getResources().getInteger(R.integer.num_horizontal_labels)
                        3);

        // set manual x bounds to have nice steps
        mGraphView.getViewport().setXAxisBoundsManual(true);

        // we can go only from minimum mood to maximum mood.
        mGraphView.getViewport().setYAxisBoundsManual(true);
        mGraphView.getViewport().setMinY(Mood.MOOD_MINIMUM);
        mGraphView.getViewport().setMaxY(Mood.MOOD_MAXIMUM);


        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        mGraphView.getGridLabelRenderer().setHumanRounding(false);

        // and we can zoom/scroll it.
        mGraphView.getViewport().setScalable(true);


        // right now we are setting the viewport to go back only 2 weeks initially, prob not the best setting.
        mGraphView.getViewport().setMinX(
                CalendarUtilities.startDateOfGraphRange(
                        CalendarUtilities.GraphRange.GRAPH_2_WEEKS).getTimeInMillis());

        mGraphView.getViewport().setMaxX(Calendar.getInstance().getTimeInMillis());
    }

    // here we take the mood data and plot it out.
    private void handleGraph(List<Mood> moods) {
        if (null == moods || moods.isEmpty()) {
            if (null == moods) {
                Timber.w("called with null moods list"); // this shouldn't happen
            }

            mGraphView.setVisibility(View.GONE);
            mNoMoodDataLabel.setVisibility(View.VISIBLE);
            return;
        }


        // we have a graph to show
        mNoMoodDataLabel.setVisibility(View.GONE);
        mGraphView.setVisibility(View.VISIBLE);

        // clean up.
        mGraphView.removeAllSeries();
        // we need the first and last dates
        // populate the graph view here.
        Collections.sort(moods, Mood.dateComparator()); // make sure they are in order first!

        // populate the series
        DataPoint[] moodData = new DataPoint[moods.size()];
        for (int i=0; i < moods.size(); i++) {
            Mood mood = moods.get(i);
            moodData[i] = new DataPoint(mood.getCalendarDate().getTime(), mood.getMoodScore());
        }

        // add them to the graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(moodData);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);
        mGraphView.addSeries(series);
        mGraphView.getViewport().scrollToEnd(); // make sure to start for the latest day


    }
}
