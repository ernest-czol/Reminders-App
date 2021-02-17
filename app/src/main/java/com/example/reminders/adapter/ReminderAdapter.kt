package com.example.reminders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.reminders.R
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot


class ReminderAdapter(options: FirestoreRecyclerOptions<Reminder>) :
    FirestoreRecyclerAdapter<Reminder, ReminderAdapter.ReminderHolder>(options) {
    private var listener: OnItemClickListener? = null

    override fun onBindViewHolder(holder: ReminderHolder, position: Int, model: Reminder) {
        holder.textViewTitle.text = model.title
        holder.textViewNotes.text = model.notes
        holder.textViewYear.text = model.year.toString()
        holder.textViewMonth.text = model.month.toString()
        holder.textViewDay.text = model.day.toString()
        holder.textViewHour.text = model.hour.toString()
        holder.textViewMinute.text = model.minute.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(
            R.layout.reminder_item,
            parent, false
        )
        return ReminderHolder(v)
    }

    fun deleteReminder(position: Int): Pair<Int?, ArrayList<PreAlarm>?> {
        snapshots.getSnapshot(position).reference.delete()
        val obj = snapshots.getSnapshot(position).toObject(Reminder::class.java)

        return Pair(obj?.idAlarm, obj?.preAlarms)
    }

    inner class ReminderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView = itemView.findViewById(R.id.item_title)
        var textViewNotes: TextView = itemView.findViewById(R.id.item_notes)
        var textViewYear: TextView = itemView.findViewById(R.id.item_year)
        var textViewMonth: TextView = itemView.findViewById(R.id.item_month)
        var textViewDay: TextView = itemView.findViewById(R.id.item_day)
        var textViewHour: TextView = itemView.findViewById(R.id.item_hour)
        var textViewMinute: TextView = itemView.findViewById(R.id.item_minute)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener!!.onItemClick(snapshots.getSnapshot(position), position)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
