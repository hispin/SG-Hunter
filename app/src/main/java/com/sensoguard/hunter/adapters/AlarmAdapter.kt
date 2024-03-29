package com.sensoguard.hunter.adapters

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.global.getStrDateTimeByMilliSeconds


class AlarmAdapter(
    private var alarms: ArrayList<Alarm>,
    val context: Context,
    val res: Int,
    val isGrid: Boolean,
    var itemClick: (Alarm?, Int) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindReservation((alarms[position]))
        holder.setIsRecyclable(false)
    }

    override fun getItemCount(): Int {
        return this.alarms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                res, parent, false
            )


        return ViewHolder(view, itemClick)
    }

    fun setDetects(_alarm: ArrayList<Alarm>?) {
        _alarm?.let { alarms = it }
        //TODO how to define with this
    }

    inner class ViewHolder(
        private val _itemView: View,
        private val itemClick: (Alarm?, Int) -> Unit
    ) :
        RecyclerView.ViewHolder(_itemView) {
        private var tvName:TextView?=null
        private var tvDate:TextView? = null
        private var tvTime: TextView? = null

        //TODO press twice
        private var tvType: TextView? = null
        private var ivAlarmPic: AppCompatImageView? = null
        private var ivShare: AppCompatImageView? = null
        private var ivIconVideo: AppCompatImageView? = null
        private var pbLoadPhoto: ProgressBar? = null
        private var ivBatteryLevel: AppCompatImageView? = null
        private var ivWifiLevel: AppCompatImageView? = null
        private var tvImageLoadError: TextView? = null
//        private var tvAccept: TextView? = null
//        private var tvZuraSent: TextView? = null
//        private var tvfDate: TextView? = null
//        private var tvsDate: TextView? = null


        init {
            itemView.setOnLongClickListener {

                //toggle the status of ready to delete
                alarms[adapterPosition].isReadyToDelete = !alarms[adapterPosition].isReadyToDelete
                notifyItemChanged(adapterPosition, alarms[adapterPosition])

                return@setOnLongClickListener false

            }

        }

//        init {
//            itemView.setOnClickListener {
//                itemClick.invoke(alarms[adapterPosition])
//            }
//        }


        fun bindReservation(myAlarm: Alarm) {
            //tvId = _itemView.findViewById(R.id.tvId)
            tvName = _itemView.findViewById(R.id.tvName)
            tvDate = _itemView.findViewById(R.id.tvDate)
            tvType = _itemView.findViewById(R.id.tvType)
            tvTime = _itemView.findViewById(R.id.tvTime)


            ivAlarmPic = _itemView.findViewById(R.id.ivAlarmPic)
            ivAlarmPic?.setOnClickListener {
                try {
                    //check if the picture is well
                    myAlarm.myBitmap = (ivAlarmPic?.drawable as BitmapDrawable).bitmap
                    itemClick.invoke(myAlarm, 1)
                } catch (ex: Exception) {
                    itemClick.invoke(null, 1)
                }
                //itemClick.invoke(myAlarm, 1)
                //alarm.imgsPath?.let { it1 -> onAdapterListener.openLargePictureDialog(it1) }
            }

            //because it have onclick listener ,itemView long click does not influence it, it set specify
            ivAlarmPic?.setOnLongClickListener {
                alarms[adapterPosition].isReadyToDelete = !alarms[adapterPosition].isReadyToDelete
                notifyItemChanged(adapterPosition, alarms[adapterPosition])
                return@setOnLongClickListener true
            }

            ivShare = _itemView.findViewById(R.id.ivShare)
            ivShare?.setOnClickListener {
                try {
                    //check if the picture is well
                    myAlarm.myBitmap = (ivAlarmPic?.drawable as BitmapDrawable).bitmap
                    itemClick.invoke(myAlarm, 2)
                } catch (ex: Exception) {
                    itemClick.invoke(null, 2)
                }
            }
            ivIconVideo = _itemView.findViewById(R.id.ivIconVideo)





            pbLoadPhoto = _itemView.findViewById(R.id.pbLoadPhoto)
            if (myAlarm.isLoadPhoto) {
                pbLoadPhoto?.visibility = View.VISIBLE
            } else {
                pbLoadPhoto?.visibility = View.GONE
            }

            ivBatteryLevel = _itemView.findViewById(R.id.ivBatteryLevel)
            ivWifiLevel = _itemView.findViewById(R.id.ivWifiLevel)
            tvImageLoadError = _itemView.findViewById(R.id.tvImageLoadError)
//            tvAccept=_itemView.findViewById(R.id.tvAccept)
//            tvZuraSent=_itemView.findViewById(R.id.tvZuraSent)
//            tvfDate=_itemView.findViewById(R.id.tvfDate)
//            tvsDate = _itemView.findViewById(R.id.tvsDate)

            val batteryLevel = myAlarm.batteryVal
            if (batteryLevel != null) {

                if (batteryLevel >= 0 && batteryLevel < 25) {
                    if (isGrid) {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_1_grid)
                    } else {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_1_list)
                    }
                } else if (batteryLevel >= 25 && batteryLevel < 50) {
                    if (isGrid) {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_2_grid)
                    } else {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_2_list)
                    }
                } else if (batteryLevel >= 50 && batteryLevel < 75) {

                    if (isGrid) {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_3_grid)
                    } else {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_3_list)
                    }

                } else if (batteryLevel in 75.0..100.0) {
                    if (isGrid) {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_4_grid)
                    } else {
                        ivBatteryLevel?.setImageResource(R.drawable.battery_level_4_list)
                    }
                }
            }

            val wifiLevel = myAlarm.wifiVal
            if (wifiLevel != null) {

                if (wifiLevel >= 0 && wifiLevel < 25) {
                    if (isGrid) {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_1_grid)
                    } else {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_1_list)
                    }
                } else if (wifiLevel >= 25 && wifiLevel < 50) {
                    if (isGrid) {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_2_grid)
                    } else {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_2_list)
                    }
                } else if (wifiLevel >= 50 && wifiLevel < 75) {

                    if (isGrid) {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_3_grid)
                    } else {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_3_list)
                    }

                } else if (wifiLevel in 75.0..100.0) {
                    if (isGrid) {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_4_grid)
                    } else {
                        ivWifiLevel?.setImageResource(R.drawable.wifi_level_4_list)
                    }
                }
            }

            if (!isGrid) {
                tvDate?.text = myAlarm.timeInMillis?.let {
                    getStrDateTimeByMilliSeconds(
                        it,
                        "dd/MM/yyyy",
                        context
                    )
                }
            }else{

                tvDate?.text = myAlarm.timeInMillis?.let {
                    getStrDateTimeByMilliSeconds(
                        it,
                        "dd/MM/yy",
                        context
                    )
                }
            }
            tvTime?.text =
                myAlarm.timeInMillis?.let { getStrDateTimeByMilliSeconds(it, "kk:mm", context) }

            /////test
//            if(myAlarm.zuraSentDate!=-1L) {
//                tvZuraSent?.text ="sent zura "+
//                        myAlarm.zuraSentDate?.let {
//                        getStrDateTimeByMilliSeconds(
//                            it,
//                            "dd/MM/yyyy kk:mm",
//                            context
//                        )
//                    }
//            }
//            if(myAlarm.acceptDate!=-1L) {
//                tvAccept?.text ="accept "+
//                    myAlarm.acceptDate?.let {
//                        getStrDateTimeByMilliSeconds(
//                            it,
//                            "dd/MM/yyyy kk:mm",
//                            context
//                        )
//                    }
//            }
//            if(myAlarm.fileDateSent!=-1L) {
//                tvfDate?.text ="accept "+
//                        myAlarm.fileDateSent?.let {
//                            getStrDateTimeByMilliSeconds(
//                                it,
//                                "dd/MM/yyyy kk:mm",
//                                context
//                            )
//                        }
//            }
//            if(myAlarm.startPushSent!=-1L) {
//                tvsDate?.text ="accept "+
//                        myAlarm.startPushSent?.let {
//                            getStrDateTimeByMilliSeconds(
//                                it,
//                                "dd/MM/yyyy kk:mm",
//                                context
//                            )
//                        }
//            }

            //test
            tvType?.text = myAlarm.type
            tvName?.text = myAlarm.name



            ivAlarmPic?.let {
                Glide.with(context).load(myAlarm.imgsPath)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            tvImageLoadError?.visibility = View.VISIBLE

                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            //if the the image is video then show the video icon
                            if (myAlarm.imgsPath != null
                                && (myAlarm.imgsPath!!.endsWith("mp4")
                                        || myAlarm.imgsPath!!.endsWith("mov"))
                            ) {
                                ivIconVideo?.visibility = View.VISIBLE
                            } else {
                                ivIconVideo?.visibility = View.GONE
                            }
                            tvImageLoadError?.visibility = View.GONE

                            return false

                        }


                    }).into(it)

                }


            //set selected/unselected
            if (myAlarm.isReadyToDelete) {
                itemView.alpha = 0.5F
            } else {
                itemView.alpha = 1.0F
            }
        }
    }
}