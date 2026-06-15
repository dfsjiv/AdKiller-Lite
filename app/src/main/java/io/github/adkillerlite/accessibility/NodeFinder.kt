package io.github.adkillerlite.accessibility

import android.view.accessibility.AccessibilityNodeInfo
import io.github.adkillerlite.rules.*
import java.util.ArrayDeque

data class NodeCandidate(val key: CandidateKey, val target: AccessibilityNodeInfo)
class NodeFinder(private val matcher:KeywordMatcher=KeywordMatcher()){
 fun find(root:AccessibilityNodeInfo,packageName:String):NodeCandidate?{
  val queue=ArrayDeque<AccessibilityNodeInfo>();queue.add(root);var seen=0
  while(queue.isNotEmpty()&&seen++<500){val node=queue.removeFirst();val keyword=matcher.match(node.text)?:matcher.match(node.contentDescription)
   if(keyword!=null){var target:AccessibilityNodeInfo?=node;var hops=0;while(target!=null&&!target.isClickable&&hops++<8)target=target.parent;if(target?.isClickable==true)return NodeCandidate(CandidateKey(packageName,node.windowId,keyword),target)}
   for(i in 0 until node.childCount)node.getChild(i)?.let(queue::add)
  };return null
 }
}
