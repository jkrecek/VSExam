package com.frca.vsexam.helper;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class AppSparseArray<E> extends SparseArray<E> {
    public List<E> getValues() {
        List<E> l = new ArrayList<E>();
        for(int i = 0; i < size(); ++i) {
            l.add(i, valueAt(i));
        }
        return l;
    }

    public List<Integer> getKeys() {
        List<Integer> l = new ArrayList<Integer>();
        for(int i = 0; i < size(); ++i) {
            l.add(i, keyAt(i));
        }
        return l;
    }
}
