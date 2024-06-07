package com.example.foodorderingworkplace;

import android.widget.Filter;

import com.example.foodorderingworkplace.adapters.AdapterFoodSeller;
import com.example.foodorderingworkplace.models.ModelFood;

import java.util.ArrayList;

public class FilterFood extends Filter {
    private AdapterFoodSeller adapter;
    private ArrayList<ModelFood> filterList;

    public FilterFood(AdapterFoodSeller adapter, ArrayList<ModelFood> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length() > 0) {
            //search filed not empty, searching something, perform search

            //change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelFood> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check, search by tittle and category
                if (filterList.get(i).getFoodName().toUpperCase().contains(constraint) ||
                        filterList.get(i).getFoodCategory().toUpperCase().contains(constraint)) {
                    //add filtered data to list
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;

        } else {
            //search filed empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.foodList = (ArrayList<ModelFood>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
