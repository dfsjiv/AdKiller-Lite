package io.github.adkillerlite.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import io.github.adkillerlite.AdKillerApplication
import io.github.adkillerlite.data.CloseLog
import io.github.adkillerlite.rules.ClickSafetyPolicy
import kotlinx.coroutines.*

class AdKillerAccessibilityService:AccessibilityService(){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate);private var pending:Job?=null
 private val finder=NodeFinder();private val safety=ClickSafetyPolicy()
 override fun onAccessibilityEvent(event:AccessibilityEvent?){val pkg=event?.packageName?.toString()?:return;val root=rootInActiveWindow?:return
  scope.launch{val app=application as AdKillerApplication;val rule=app.settings.ruleFor(pkg)?:return@launch;if(!rule.enabled)return@launch
   val candidate=finder.find(root,pkg)?:return@launch;val now=System.currentTimeMillis();if(!safety.canSchedule(candidate.key,now))return@launch
   pending?.cancel();pending=launch{delay(rule.delayMs);val current=rootInActiveWindow;val valid=current!=null&&current.packageName?.toString()==pkg&&current.windowId==candidate.key.windowId
    val success=valid&&candidate.target.performAction(AccessibilityNodeInfo.ACTION_CLICK);if(success)safety.recordClick(candidate.key,System.currentTimeMillis())
    app.stats.record(CloseLog(System.currentTimeMillis(),pkg,candidate.key.keyword,success))
   }
  }
 }
 override fun onInterrupt(){pending?.cancel()}
 override fun onDestroy(){scope.cancel();super.onDestroy()}
}
