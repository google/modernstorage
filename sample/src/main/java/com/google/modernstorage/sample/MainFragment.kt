package com.google.modernstorage.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.modernstorage.sample.databinding.FragmentDemoListBinding
import com.google.modernstorage.sample.ui.DemoListAdapter
import com.google.modernstorage.sample.ui.Demo

private val apiList = arrayOf(
    Demo(R.string.demo_mediastore, R.id.action_mainFragment_to_mediaStoreFragment),
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

        binding.recyclerView

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
