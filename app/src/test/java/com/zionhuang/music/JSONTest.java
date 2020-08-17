package com.zionhuang.music;

import com.zionhuang.music.extractor.JSON;

import org.junit.Test;

import static org.junit.Assert.*;

public class JSONTest {
    @Test
    public void convertTest() {
        JSON json = JSON.toArray(1, 2, "3", JSON.toArray(100, 200, "3"));
        assertEquals(json.getInt(0), 1);
        assertEquals(json.getInt(1), 2);
        assertEquals(json.getString(2), "3");
        assertEquals(json.get(3).getInt(0), 100);
        assertEquals(json.get(3).getInt(1), 200);
        assertEquals(json.get(3).getString(2), "3");
    }
}
