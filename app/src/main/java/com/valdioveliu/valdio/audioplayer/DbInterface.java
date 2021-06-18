package com.valdioveliu.valdio.audioplayer;

import java.util.ArrayList;
import java.util.Hashtable;

public interface DbInterface {
    public void save(Hashtable<String,String> attributes);
    public void save(ArrayList<Hashtable<String,String>> objects);
    public ArrayList<Hashtable<String,String>> load();
    public Hashtable<String,String> load(String id);
}

