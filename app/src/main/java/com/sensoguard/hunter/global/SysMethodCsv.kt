package com.sensoguard.hunter.global

// inspection fix: removed unused import(s)
import android.content.Context
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.Alarm


const val mySeparator='@'
const val mfolderName="sansorgaurd"
const val mfileName="SG-AlarmLog.csv"


//var mCsvAlarms:ArrayList<String>?=null

private fun alarmTitlesToCsvString(context:Context): String {

     val res = context.resources // inspection fix: val

    //val mySeparator=";"
    val sb=StringBuilder()
    sb.append(res.getString(R.string.csv_date_title)) // inspection fix: redundant toString()
    sb.append(mySeparator)
    sb.append(res.getString(R.string.csv_name_title)) // inspection fix: redundant toString()
    sb.append(mySeparator)
    sb.append(res.getString(R.string.csv_id_title)) // inspection fix: redundant toString()
    sb.append(mySeparator)
    sb.append(res.getString(R.string.csv_alarm_type_title)) // inspection fix: redundant toString()
    sb.append(mySeparator)
    sb.append(res.getString(R.string.csv_latitude_title)) // inspection fix: redundant toString()
    sb.append(mySeparator)
    sb.append(res.getString(R.string.csv_longitude_title)) // inspection fix: redundant toString()


    return sb.toString()
}

private fun alarmToCsvString(alarm: Alarm?, context: Context): String {



    //val mySeparator=";"
    val sb=StringBuilder()
    sb.append(alarm?.getCurrentDate())
    sb.append(mySeparator)
    sb.append(alarm?.name)
    sb.append(mySeparator)
    sb.append(alarm?.id)
    sb.append(mySeparator)
    sb.append(alarm?.type)
    sb.append(mySeparator)
    sb.append(alarm?.latitude)
    sb.append(mySeparator)
    sb.append(alarm?.longitude)


    return sb.toString()
}

// inspection fix: removed unused writeCsvFile()


// inspection fix: removed unused shareCsv()

// inspection fix: removed unused alarmsListToCsvFile()


//fun alarmListToCsvFile(alarms :ArrayList<Alarm>?){
//
//    mCsvAlarms=ArrayList()
//    val collect=
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            alarms?.joinToString(mySeparator.toString())
//        } else {
//            //TODO("VERSION.SDK_INT < N") -COMPLETE
//            val sb=StringBuilder()
//            val iteratorList=alarms?.listIterator()
//
//            while (iteratorList != null && iteratorList.hasNext()) {
//                val item=iteratorList.next()
//                sb.append(item.toString())
//                sb.append(mySeparator)
//            }
//            // last string, no mySeparator
//            if(alarms?.size!=null
//                && alarms.size > 0){
//                sb.append(alarms[alarms.size -1])
//            }
//            sb.toString()
//        }
//
//    collect?.let { mCsvAlarms?.add(it) }
//    //todo-Haggay:-COMPLETE
//    //(a) Iterate through each Uri item in the input arg 'alarms'.
//    //(b) Convert each Uri to String I advise to uri.toString() & add the String to the csvLines ArrayList
//    //(c) When done loop use the set methods to insert into the photoFiles CsvFile
//}
