package io.github.adkillerlite.apps

import android.content.*
import android.content.pm.ApplicationInfo

 data class InstalledApp(val packageName:String,val label:String,val isSystem:Boolean)
class InstalledAppsRepository(private val context:Context){
 fun load(includeSystem:Boolean):List<InstalledApp>{
  val intent=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
  return context.packageManager.queryIntentActivities(intent,0).map{r->
   val info=r.activityInfo.applicationInfo
   InstalledApp(info.packageName,context.packageManager.getApplicationLabel(info).toString(),info.flags and ApplicationInfo.FLAG_SYSTEM!=0)
  }.distinctBy{it.packageName}.filter{it.packageName!=context.packageName&&(includeSystem||!it.isSystem)}.sortedBy{it.label.lowercase()}
 }
}
