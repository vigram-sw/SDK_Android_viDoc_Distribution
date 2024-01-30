package com.vigram.test.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vigram.test.R
import com.vigram.test.databinding.ActivityMainBinding
import com.vigram.test.databinding.MountItemBinding

import com.vigram.sdk.NTRIP.NtripMountPoint
import com.vigram.test.MainActivity

class RecyclerViewMountsAdapter(private val context: Context, private  var listMounts: List<NtripMountPoint>?, private val binding: ActivityMainBinding, private val mainActivity: MainActivity) : RecyclerView.Adapter<RecyclerViewMountsAdapter.MountsItemViewHolder>() {

    private val LAST_MOUNT = "LAST_MOUNT"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MountsItemViewHolder {
        val binding = MountItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MountsItemViewHolder(binding)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MountsItemViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listMounts?.let {
                it[position].name.let { mountName ->
                    val sharedPreferences =
                        context.getSharedPreferences(LAST_MOUNT, Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.apply {
                        editor.putString(LAST_MOUNT, mountName)
                        apply()
                    }
                    binding.textViewMount.text = "${context.getString(R.string.mount)} $mountName"
                    mainActivity.currentMount = mountName
                }
            }
        }
        listMounts?.get(position)?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return listMounts?.size ?: 0
    }
    internal fun setData(listMounts: List<NtripMountPoint>){
        this.listMounts = listMounts
        notifyDataSetChanged()
    }


    class MountsItemViewHolder (private val binding: MountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NtripMountPoint){
            binding.textView.text = item.name
        }
    }
}