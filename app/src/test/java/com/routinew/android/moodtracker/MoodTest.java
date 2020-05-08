package com.routinew.android.moodtracker;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.hamcrest.core.CombinableMatcher;
import org.junit.Test;
import com.routinew.android.moodtracker.POJO.Mood;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoodTest {
    private Comparator<Mood> dateComparator = Mood.dateComparator();

    @Test
    public void testInstantiation() {
        Mood mood = new Mood(0,"2020-05-01");
        assertEquals("Did not set mood to 0",mood.getMoodScore(),0);
        assertEquals("Did not set date to 2020-05-01",mood.getDate(),"2020-05-01");
    }

    @Test
    public void testComparator() {
        List<Mood> mood = new ArrayList<>();

    }
}
