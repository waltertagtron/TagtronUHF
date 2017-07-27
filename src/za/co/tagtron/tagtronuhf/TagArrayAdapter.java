package za.co.tagtron.tagtronuhf;

import java.util.List;

import za.co.tagtron.tagtronuhf.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TagArrayAdapter extends ArrayAdapter<Tag> {
	private final Context context;
	private final List<Tag> values;

	public TagArrayAdapter(Context context, List<Tag> values) {
		super(context, -1, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View row = convertView;
        TagHolder holder = null;
       
        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(R.layout.tag_list_entry, parent, false);
           
            holder = new TagHolder();
            holder.tagId = (TextView)row.findViewById(R.id.tag_id);
           
            row.setTag(holder);
        }
        else {
            holder = (TagHolder)row.getTag();
        }
       
        Tag tag = values.get(position);
        if (tag == null) {
        	return null;
        }
        holder.tagId.setText("ID: " + tag.getTagId());
        return row;
    }
   
    static class TagHolder {
        TextView tagId;
	}
} 


