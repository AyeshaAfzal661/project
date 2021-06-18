package com.valdioveliu.valdio.audioplayer;


import android.util.Xml;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Valdio Veliu on 16-07-18.
 */
public class Audio implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;
    private transient DbInterface dao = null;
    private boolean fav_flag=false;

    public Audio(String data, String title, String album, String artist) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public String getData() {
        return data;
    }
public void set_flag(boolean f)
{
    this.fav_flag=f;
}
    public boolean get_flag()
    {
        return this.fav_flag;
    }
    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }



    public void set_dao(DbInterface dao)
    {
        this.dao=dao;
    }





    public void save(){

        if (dao != null){

            Hashtable<String,String> d = new Hashtable<String, String>();

            d.put("data", data);
            d.put("title", title);
            d.put("album", album);
            d.put("artist", artist);

            dao.save(d);
        }
    }

    public void load(Hashtable<String, String> d){


        data = d.get("data");
        title = d.get("title");
        album = d.get("album");
        artist = d.get("artist");

    }

    public static ArrayList<Audio> load(DbInterface dao){
        ArrayList<Audio> s_list = new ArrayList<>();
        if(dao != null){

            ArrayList<Hashtable<String,String>> objects = dao.load();
            for(Hashtable<String,String> obj : objects){
                Audio s = new Audio("","","","");
                s.set_dao(dao);
                s.load(obj);
                s_list.add(s);
            }
        }
        return s_list;
    }





}
