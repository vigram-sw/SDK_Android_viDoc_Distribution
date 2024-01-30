package com.vigram.test.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.vigram.test.databinding.ActivityMainBinding
import com.vigram.test.databinding.FileItemBinding

import java.io.File
import java.io.IOException


class RecyclerViewFilesAdapter(private val context: Context, private  var listFiles: List<String>?, private val binding: ActivityMainBinding) : RecyclerView.Adapter<RecyclerViewFilesAdapter.FileItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        val binding = FileItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return FileItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listFiles?.let {
                it[position].let { fileName ->
                    openFile(fileName)
                }
            }
        }
        listFiles?.get(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return listFiles?.size ?: 0
    }
    internal fun setData(listFiles: List<String>){
        this.listFiles = listFiles
    }

    class FileItemViewHolder (private val binding: FileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String){
            binding.textView.text = item
        }
    }

    fun openFile(fileName: String) {
        try {
            val file = File("${context.filesDir}/$fileName" )
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            intentShareFile.setType("application/pdf")
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
            context.startActivity(Intent.createChooser(intentShareFile, "Share File"));


        } catch (e: IOException) {
            e.printStackTrace();
        }
    }
}