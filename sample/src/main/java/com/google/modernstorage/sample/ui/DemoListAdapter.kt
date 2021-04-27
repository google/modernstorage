package com.google.modernstorage.sample.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.modernstorage.sample.R

data class Demo(@StringRes val nameRes: Int, @IdRes val actionRes: Int)

class DemoListAdapter(private val dataSet: Array<Demo>) :
    RecyclerView.Adapter<DemoListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_row_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.textView.context

        viewHolder.textView.text = context.getString(dataSet[position].nameRes)
        viewHolder.textView.setOnClickListener {
            it.findNavController().navigate(dataSet[position].actionRes)
        }
    }

    override fun getItemCount() = dataSet.size
}
