/**
 * Copyright 2023 Viacheslav Barkov
 */

package com.slavabarkov.tidy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText // Changed from TextView for better compatibility
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.slavabarkov.tidy.R
import com.slavabarkov.tidy.adapters.ImageAdapter
import com.slavabarkov.tidy.viewmodels.ORTImageViewModel
import com.slavabarkov.tidy.viewmodels.ORTTextViewModel
import com.slavabarkov.tidy.viewmodels.SearchViewModel


class SearchFragment : Fragment() {
    // We use EditText here as it's the correct type for an input field
    private var searchText: EditText? = null
    private var searchButton: Button? = null
    private var clearButton: Button? = null
    private var indexImagesButton: Button? = null
    private var welcomeTextView: TextView? = null

    private val mORTImageViewModel: ORTImageViewModel by activityViewModels()
    private val mORTTextViewModel: ORTTextViewModel by activityViewModels()
    private val mSearchViewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        // --- Find all our UI elements ---
        searchText = view.findViewById(R.id.searchText)
        searchButton = view.findViewById(R.id.searchButton)
        clearButton = view.findViewById(R.id.clearButton)
        indexImagesButton = view.findViewById(R.id.button_sync) // The ID from our XML
        welcomeTextView = view.findViewById(R.id.text_view_welcome)

        // --- Logic to show/hide the welcome text ---
        // We check if the image list in the ViewModel is empty.
        // If it is, it means indexing has never been run.
        if (mORTImageViewModel.idxList.isEmpty()) {
            welcomeTextView?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE // Hide the empty results area
        } else {
            welcomeTextView?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE // Show the results area
        }

        // --- Setup the RecyclerView Adapter ---
        if (mSearchViewModel.searchResults == null) {
            mSearchViewModel.searchResults = mORTImageViewModel.idxList.reversed()
        }
        recyclerView.adapter = ImageAdapter(requireContext(), mSearchViewModel.searchResults!!)
        recyclerView.scrollToPosition(0)

        mORTTextViewModel.init()

        // --- Setup Click Listeners for all buttons ---
        searchButton?.setOnClickListener {
            val textEmbedding: FloatArray =
                mORTTextViewModel.getTextEmbedding(searchText?.text.toString())
            mSearchViewModel.sortByCosineDistance(textEmbedding, mORTImageViewModel.embeddingsList, mORTImageViewModel.idxList)
            recyclerView.adapter?.notifyDataSetChanged() // More efficient way to update
        }

        clearButton?.setOnClickListener{
            searchText?.text = null
            mSearchViewModel.searchResults = mORTImageViewModel.idxList.reversed()
            recyclerView.adapter?.notifyDataSetChanged() // More efficient way to update
        }

        // --- Our New Button's Logic ---
        indexImagesButton?.setOnClickListener {
            // Use the navigation action we created in navigation.xml
            findNavController().navigate(R.id.action_searchFragment_to_indexFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // This logic is important for when we return from the IndexFragment
        // or from the ImageFragment.
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recycler_view)
        welcomeTextView = view?.findViewById(R.id.text_view_welcome)

        // Re-check if the index is empty and update the UI accordingly
        if (mORTImageViewModel.idxList.isEmpty()) {
            welcomeTextView?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            welcomeTextView?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }

        // This handles the case when returning from image-to-image search
        if (mSearchViewModel.fromImg2ImgFlag) {
            searchText?.text = null
            recyclerView?.scrollToPosition(0)
            mSearchViewModel.fromImg2ImgFlag = false
        }
        // Ensure the adapter is updated with any potential changes
        recyclerView?.adapter?.notifyDataSetChanged()
    }
}