package com.example.sanesean.csci571_hw9;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sanesean on 2018/4/9.
 */

public class PlacePagination {
    private int index;
    private List<JSONObject> results;
    private int last;
    private Map<Integer,List<JSONObject>> map;
    public PlacePagination(){
        index=0;
        last=0;
        results=new ArrayList<>();
        map=new HashMap<>();
    }
    public void addPage(int index,List<JSONObject> results){
        if(!map.containsKey(index)){
            map.put(index,results);
        }
    }
    public  List<JSONObject> getPage(int index){
        return map.containsKey(index)?map.get(index):new ArrayList<JSONObject>();
    }
    public boolean isNewPage(int index){
        return !map.containsKey(index);
    }
    public void setLastPage(int index){
        last=index;
    }
    public int getLast(){
        return last;
    }

}
