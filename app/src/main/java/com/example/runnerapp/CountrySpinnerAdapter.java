package com.example.runnerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CountrySpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] countries;

    public CountrySpinnerAdapter(Context context, int resource, String[] countries) {
        super(context, resource, countries);
        this.context = context;
        this.countries = countries;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item_country, parent, false);

        TextView textView = row.findViewById(R.id.countryName);
        ImageView imageView = row.findViewById(R.id.countryFlag);

        textView.setText(countries[position]);
        imageView.setImageResource(getFlagResource(countries[position]));

        return row;
    }

    private int getFlagResource(String countryName) {
        switch (countryName) {
            case "Costa Rica":
                return R.drawable.flag_costa_rica;
            case "Canada":
                return R.drawable.flag_canada;
            case "Argentina":
                return R.drawable.flag_argentina;
            case "Honduras":
                return R.drawable.flag_honduras;
            // Agrega más casos según necesites
            default:
                return R.drawable.flag_default; // Bandera por defecto
        }
    }
}
