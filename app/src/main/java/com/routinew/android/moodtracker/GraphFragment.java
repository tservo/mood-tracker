package com.routinew.android.moodtracker;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.routinew.android.moodtracker.databinding.GraphFragmentBinding;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class GraphFragment extends Fragment {



    private MoodViewModel mViewModel;
    private GraphFragmentBinding mBinding;

    GraphView mGraphView;
    Spinner mReportSpinner;
    TextView mNoMoodDataLabel;

    public static GraphFragment newInstance() {
        return new GraphFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = GraphFragmentBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();

        mGraphView = mBinding.graph;
        mReportSpinner = mBinding.spinner;
        mNoMoodDataLabel = mBinding.noMoodDataLabel;

        mReportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        mBinding = null;
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
Timber.w("Mood %s",moods.toString());
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
