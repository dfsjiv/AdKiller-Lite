package io.github.adkillerlite.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.adkillerlite.AdKillerApplication
import io.github.adkillerlite.data.AppRule
import kotlinx.coroutines.launch

@Composable fun AdKillerApp(app:AdKillerApplication,openAccessibility:()->Unit){MaterialTheme{var page by remember{mutableStateOf("home")};when(page){
 "apps"->AppPicker(app){page="home"};"logs"->Logs(app){page="home"};else->Home(app,openAccessibility,{page="apps"},{page="logs"})
}}}
@Composable private fun Home(app:AdKillerApplication,openAccessibility:()->Unit,apps:()->Unit,logs:()->Unit){val rules by app.settings.rules.collectAsState(emptyList());val stats by app.stats.stats.collectAsState(initial=io.github.adkillerlite.data.DailyStats("",0));Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("广告杀手 Lite",style=MaterialTheme.typography.headlineMedium);Button(onClick=openAccessibility){Text("开启无障碍服务")};Text("今日已关闭：${stats.count} 次");Text("正在保护：${rules.count{it.enabled}} 个 App");Button(onClick=apps){Text("选择保护 App")};Button(onClick=logs){Text("查看日志")}}}
@Composable private fun AppPicker(app:AdKillerApplication,back:()->Unit){var system by remember{mutableStateOf(false)};val installed=remember(system){app.installedApps.load(system)};val rules by app.settings.rules.collectAsState(emptyList());val scope=rememberCoroutineScope();Column(Modifier.fillMaxSize().padding(16.dp)){Row{Button(onClick=back){Text("返回")};Spacer(Modifier.width(12.dp));Text("显示系统应用");Switch(system,{system=it})};LazyColumn{items(installed){item->val selected=rules.any{it.packageName==item.packageName};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(item.label,Modifier.weight(1f));Switch(selected,{on->scope.launch{if(on)app.settings.setRule(AppRule(item.packageName))else app.settings.removeRule(item.packageName)}})}}}}}
@Composable private fun Logs(app:AdKillerApplication,back:()->Unit){val logs by app.stats.logs.collectAsState(emptyList());Column(Modifier.fillMaxSize().padding(16.dp)){Button(onClick=back){Text("返回")};Text("关闭日志",style=MaterialTheme.typography.headlineSmall);LazyColumn{items(logs){Text("${it.packageName} · ${it.keyword} · ${if(it.success)"已关闭" else "已跳过"}")}}}}
