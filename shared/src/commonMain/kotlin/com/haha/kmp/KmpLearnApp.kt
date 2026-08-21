package com.haha.kmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * CMP（Compose Multiplatform）入口：这份 UI 写在 commonMain，
 * Android 用 [com.haha.kmp.KmpLearnActivity] 承载；以后接 iOS 时可直接复用。
 */
@Composable
fun KmpLearnApp() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "KMP / CMP 学习页",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "commonMain 共享逻辑 + Compose UI；androidMain / iosMain 各自 actual。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                ConceptCard(
                    title = "KMP · Kotlin Multiplatform",
                    body = "一份 Kotlin 业务（如 SharedStudy.fibonacci）编译到 Android / iOS。" +
                        "平台差异用 expect/actual：getPlatform() 在 Android 读 Build.VERSION，在 iOS 读 UIDevice。"
                )
                ConceptCard(
                    title = "CMP · Compose Multiplatform",
                    body = "Compose 不仅能写 Android UI。KmpLearnApp() 这份界面在 commonMain，" +
                        "Android Activity 只负责 setContent；以后 iOS 也可以挂同一棵 Compose 树。"
                )

                PlatformCard()
                CounterCard()
                FibonacciCard()
            }
        }
    }
}

@Composable
private fun ConceptCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PlatformCard() {
    val platform = remember { getPlatform() }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("expect / actual 实况", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(SharedStudy.greeting(platform), style = MaterialTheme.typography.bodyLarge)
            Text("details: ${platform.details}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CounterCard() {
    var count by remember { mutableIntStateOf(0) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("CMP 状态示例", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("remember + mutableIntStateOf，和 Android Compose 写法相同。当前：$count")
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { count = (count - 1).coerceAtLeast(0) }) { Text("-1") }
                Button(onClick = { count++ }) { Text("+1") }
            }
        }
    }
}

@Composable
private fun FibonacciCard() {
    var n by remember { mutableIntStateOf(10) }
    val result = remember(n) { SharedStudy.fibonacci(n) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("共享算法示例", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("fibonacci($n) = $result")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { n = (n - 1).coerceAtLeast(0) }) { Text("n-1") }
                Button(onClick = { n = (n + 1).coerceAtMost(30) }) { Text("n+1") }
            }
        }
    }
}
