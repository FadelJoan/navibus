package com.rafid.navibus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rafid.navibus.data.model.HistoryTrip

class HistoryAdapter(private val trips: List<HistoryTrip>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount(): Int = trips.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRoute: TextView = itemView.findViewById(R.id.tvRoute)
        private val tvBusCode: TextView = itemView.findViewById(R.id.tvBusCode)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(trip: HistoryTrip) {
            tvRoute.text = "${trip.startHalte} -> ${trip.destinationHalte}"
            tvBusCode.text = "Bus: ${trip.busCode}"
            tvTimestamp.text = trip.timestamp // Format this later if needed
        }
    }
}