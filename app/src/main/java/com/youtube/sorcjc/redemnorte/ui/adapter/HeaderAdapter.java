package com.youtube.sorcjc.redemnorte.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.youtube.sorcjc.redemnorte.R;
import com.youtube.sorcjc.redemnorte.model.Hoja;
import com.youtube.sorcjc.redemnorte.ui.DetailsActivity;
import com.youtube.sorcjc.redemnorte.ui.PanelActivity;
import com.youtube.sorcjc.redemnorte.ui.fragment.HeaderDialogFragment;

import java.util.ArrayList;

public class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.ViewHolder> {
    private ArrayList<Hoja> dataSet;

    // Provide a reference to the views for each dataSet item
    // Complex dataSet items may need more than one view per item, and
    // you provide access to all the views for a dataSet item in a view holder
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // context
        Context context;
        // text views
        TextView headerCode;
        TextView responsibleName;
        TextView headerDate;
        // buttons
        Button btnDetails;
        Button btnEditHeader;
        // params
        String hoja_id, responsable;

        ViewHolder(View v) {
            super(v);
            context = v.getContext();

            headerCode = (TextView) v.findViewById(R.id.headerCode);
            responsibleName = (TextView) v.findViewById(R.id.responsibleName);
            headerDate = (TextView) v.findViewById(R.id.headerDate);

            btnDetails = (Button) v.findViewById(R.id.btnDetails);
            btnEditHeader = (Button) v.findViewById(R.id.btnEditHeader);
        }

        void setOnClickListeners() {
            btnDetails.setOnClickListener(this);
            btnEditHeader.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnDetails:
                    Intent intent = new Intent(context, DetailsActivity.class);
                    intent.putExtra("hoja_id", hoja_id);
                    intent.putExtra("responsable", responsable);
                    context.startActivity(intent);
                    break;
                case R.id.btnEditHeader:
                    showEditHeaderDialog(hoja_id);
                    break;
            }
        }

        private void showEditHeaderDialog(final String hoja_id) {
            FragmentManager fragmentManager = ((PanelActivity) context).getSupportFragmentManager();

            // hoja_id is required to => edit a specific header
            HeaderDialogFragment newFragment = HeaderDialogFragment.newInstance(hoja_id);

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, newFragment)
                    .addToBackStack(null).commit();
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public HeaderAdapter(ArrayList<Hoja> dataSet) {
        this.dataSet = dataSet;
    }

    public void setDataSet(ArrayList<Hoja> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public HeaderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_header, parent, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get element from your dataSet-set at this position
        // replace the contents of the view with that element
        Hoja currentHeader = dataSet.get(position);

        holder.headerCode.setText(currentHeader.getId());
        holder.responsibleName.setText(currentHeader.getResponsable());
        holder.headerDate.setText(currentHeader.getFecha());

        // set events
        holder.setOnClickListeners();

        // params needed to show the details
        holder.hoja_id = currentHeader.getId();
        holder.responsable = currentHeader.getResponsable().trim();
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
