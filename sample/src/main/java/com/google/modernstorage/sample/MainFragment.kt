/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.modernstorage.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.modernstorage.sample.databinding.FragmentDemoListBinding
import com.google.modernstorage.sample.ui.Demo
import com.google.modernstorage.sample.ui.DemoListAdapter

private val apiList = arrayOf(
    Demo(
        R.drawable.ic_baseline_collections_24,
        R.string.demo_mediastore,
        R.id.action_mainFragment_to_mediaStoreFragment
    ),
    Demo(
        R.drawable.ic_baseline_folder_24,
        R.string.demo_filesystem,
        R.id.action_mainFragment_to_fileSystemFragment
    ),
)

class MainFragment : Fragment() {
    private var _binding: FragmentDemoListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoListBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = DemoListAdapter(apiList)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
