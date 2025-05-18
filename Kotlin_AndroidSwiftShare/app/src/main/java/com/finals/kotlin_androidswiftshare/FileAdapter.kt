package com.finals.kotlin_androidswiftshare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    private val files: List<File>,              // LIST OF FILE OBJECTS TO BE DISPLAYED
    private val onFileClick: (File) -> Unit     // CALLBACK FUNCTION WHEN A FILE IS CLICKED
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    /**
     * ViewHolder class for displaying a single file item in the RecyclerView
     */
    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(android.R.id.text1) // TEXTVIEW TO DISPLAY FILE NAME
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) // USING BUILT-IN LAYOUT
        return FileViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position
     */
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.fileNameTextView.text = file.name // SET FILE NAME TO TEXTVIEW
        holder.itemView.setOnClickListener { onFileClick(file) } // SET CLICK LISTENER TO HANDLE FILE SELECTION
    }

    /**
     * Returns the total number of items in the data set held by the adapter
     */
    override fun getItemCount(): Int = files.size
}
