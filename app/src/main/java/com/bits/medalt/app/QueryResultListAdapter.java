package com.bits.medalt.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bits.medalt.app.db.Medicine;

import java.util.ArrayList;

/**
 * Created by ayush on 21/4/14.
 */
public class QueryResultListAdapter extends BaseAdapter {

    private ArrayList<Medicine> result_medicines;
    private Context context;
    private LayoutInflater layoutInflater;

    public QueryResultListAdapter(ArrayList<Medicine> result_medicines, Context context) {
        this.result_medicines = result_medicines;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return result_medicines.size();
    }

    @Override
    public Object getItem(int position) {
        return result_medicines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.query_result_list_item,null);
        }else {

        }

        TextView trade_name = (TextView) convertView.findViewById(R.id.tv_trade_name);
        TextView api = (TextView) convertView.findViewById(R.id.tv_api);
        TextView dosage = (TextView) convertView.findViewById(R.id.tv_dosage);
        TextView category = (TextView) convertView.findViewById(R.id.tv_category);

        Medicine medicine = result_medicines.get(position);
        trade_name.setText(medicine.getTrade_name());
        api.setText(medicine.getApi());
        dosage.setText(medicine.getDosage());
        category.setText(medicine.getCategory());

        return convertView;
    }
}
