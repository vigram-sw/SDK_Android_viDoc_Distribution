package com.vigram.test.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vigram.sdk.Models.DeviceMessage
import com.vigram.sdk.Peripheral.Peripheral
import com.vigram.test.MainActivity
import com.vigram.test.databinding.VersionsItemBinding

class RecyclerViewVersionsAdapter(
    private val context: Context,
    private var listSoftwareVersions: List<DeviceMessage.Software>?
) : RecyclerView.Adapter<RecyclerViewVersionsAdapter.VersionsItemViewHolder>() {
    private var peripheral: Peripheral? = null
    internal fun setPeripheral(peripheral: Peripheral) {
        this.peripheral = peripheral
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VersionsItemViewHolder {
        val binding = VersionsItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return VersionsItemViewHolder(binding)
    }

    private fun setVersionToUpdate(version: DeviceMessage.Software) {
        peripheral?.setUpdateSoftwareToNextStartup(version)
        MainActivity.showAlert(
            context,
            "Update information",
            "The installation of firmware version ${version.toString()} will be started after reconnecting to viDoc"
        )
    }

    override fun onBindViewHolder(holder: VersionsItemViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listSoftwareVersions?.get(position)?.let {
                setVersionToUpdate(it)
            }
        }
        holder.bind(listSoftwareVersions?.get(position))
    }

    override fun getItemCount(): Int {
        return listSoftwareVersions?.size ?: 0
    }

    internal fun setData(listVersions: List<DeviceMessage.Software>) {
        listSoftwareVersions = listVersions
    }


    class VersionsItemViewHolder(private val binding: VersionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeviceMessage.Software?) {
            binding.textView.text = item.toString()
        }
    }
}