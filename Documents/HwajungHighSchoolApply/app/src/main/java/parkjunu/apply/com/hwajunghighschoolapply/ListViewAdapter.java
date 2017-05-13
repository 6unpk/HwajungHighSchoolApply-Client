package parkjunu.apply.com.hwajunghighschoolapply;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    LayoutInflater inflater;
    ArrayList<ListItem> items;

    public ListViewAdapter(Context context, ArrayList<ListItem> items){
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.list_view_layout, parent, false);
        }

        ListItem listItem = items.get(position);

        TextView column1 = (TextView)convertView.findViewById(R.id.column1);
        column1.setText(listItem.getColumn1());
        TextView column2 = (TextView)convertView.findViewById(R.id.column2);
        column2.setText(listItem.getColumn2());
        TextView column3 = (TextView)convertView.findViewById(R.id.column3);
        column3.setText(listItem.getColumn3());
        TextView column4 = (TextView)convertView.findViewById(R.id.column4);
        column4.setText(listItem.getColumn4());

        return convertView;
    }
}
