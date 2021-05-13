/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.modernstorage.sample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.modernstorage.sample.databinding.ListRowItemBinding

data class Demo(
    @DrawableRes val iconRes: Int,
    @StringRes val nameRes: Int,
    @IdRes val actionRes: Int
)

class DemoListAdapter(private val dataSet: Array<Demo>) :
    RecyclerView.Adapter<DemoListAdapter.ViewHolder>() {

    class ViewHolder(viewBinding: ListRowItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        val binding = viewBinding
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListRowItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val textView = viewHolder.binding.textView
        val iconView = viewHolder.binding.iconView

        val context = textView.context
        iconView.setImageResource(dataSet[position].iconRes)
        textView.text = context.getString(dataSet[position].nameRes)
        viewHolder.binding.root.setOnClickListener { rowView ->
            rowView.findNavController().navigate(dataSet[position].actionRes)
        }
    }

    override fun getItemCount() = dataSet.size
}
