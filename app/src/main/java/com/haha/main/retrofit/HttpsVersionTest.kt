package com.haha.main.retrofit

import android.content.Context
import android.util.Log
import com.google.android.gms.net.CronetProviderInstaller
import com.google.android.gms.tasks.Tasks
import com.haha.HahaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: HTTPS 上 HTTP/1.1、HTTP/2、HTTP/3 对照 Demo（口语里常说的 https1/2/3）。
 *              1.1 / 2 用 OkHttp 强制协议；3 用 Cronet（QUIC），因 OkHttp 4.9.1 不原生支持 h3。
 *              协议详解见 docs/https-http-versions.md
 *
 **/
class HttpsVersionTest {
    private val TAG = this.javaClass.simpleName
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cronetExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** 仓库公开 API，多数协商到 h2；对照 [SocketTest] 裸 Socket 只能讲 HTTP/1.1 */
    private val demoUrl = "https://api.github.com/repos/haha-yang/hahalearn"

    /** Cloudflare 专门用于演示 QUIC / HTTP/3 */
    private val http3Url = "https://cloudflare-quic.com/"

    fun test() {
        httpsHttp11(demoUrl)
        httpsHttp2(demoUrl)
        httpsHttp3(http3Url)
    }

    /**
     * 强制只协商 HTTP/1.1：ALPN 只报 `http/1.1`，一条 TCP 连接上明文 HTTP 报文（经 TLS）。
     */
    fun httpsHttp11(url: String) {
        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .eventListener(protocolEventListener("HTTP/1.1"))
            .build()
        enqueue(client, url, expectHint = "http/1.1")
    }

    /**
     * 允许 HTTP/2：TLS ALPN 优先 `h2`，二进制帧 + 多路复用，仍跑在 TCP 上。
     * OkHttp 要求列表里始终包含 HTTP_1_1 作回退。
     */
    fun httpsHttp2(url: String) {
        val client = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .eventListener(protocolEventListener("HTTP/2"))
            .build()
        enqueue(client, url, expectHint = "h2")
    }

    /**
     * HTTP/3：应用层仍是 HTTP 语义，传输改为 **QUIC over UDP**（内建 TLS 1.3）。
     * OkHttp 4.9.1 不能发 h3，这里用 Play Services Cronet；无 GMS / 网络拦 UDP 时会失败并打日志。
     */
    fun httpsHttp3(url: String) {
        val context = HahaApplication.getContext()
        if (context == null) {
            Log.e(TAG, "httpsHttp3 skip: Application Context 为空")
            return
        }
        scope.launch {
            runHttp3WithCronet(context.applicationContext, url)
        }
    }

    private fun enqueue(client: OkHttpClient, url: String, expectHint: String) {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "HahaLearn-HttpsVersionTest")
            .get()
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "[$expectHint] onFailure: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val protocol = it.protocol
                    val body = it.body?.string().orEmpty()
                    val preview = body.take(200)
                    Log.d(
                        TAG,
                        "[$expectHint] code=${it.code} negotiated=$protocol " +
                                "len=${body.length} preview=$preview",
                    )
                    when {
                        expectHint == "http/1.1" && protocol != Protocol.HTTP_1_1 ->
                            Log.w(TAG, "[$expectHint] 期望 HTTP_1_1，实际 $protocol")

                        expectHint == "h2" && protocol != Protocol.HTTP_2 ->
                            Log.w(
                                TAG,
                                "[$expectHint] 期望 HTTP_2，实际 $protocol（服务端可能未开 h2）"
                            )

                        else -> Log.d(TAG, "[$expectHint] 协议符合预期: $protocol")
                    }
                }
            }
        })
    }

    private fun protocolEventListener(label: String): EventListener {
        return object : EventListener() {
            override fun connectEnd(
                call: Call,
                inetSocketAddress: InetSocketAddress,
                proxy: Proxy,
                protocol: Protocol?,
            ) {
                Log.d(
                    TAG,
                    "[$label] TCP/TLS connectEnd addr=$inetSocketAddress alpnProtocol=$protocol",
                )
            }

            override fun connectionAcquired(call: Call, connection: Connection) {
                Log.d(TAG, "[$label] connectionAcquired protocol=${connection.protocol()}")
            }
        }
    }

    private fun runHttp3WithCronet(context: Context, url: String) {
        try {
            val install = CronetProviderInstaller.installProvider(context)
            try {
                Tasks.await(install, 15, TimeUnit.SECONDS)
                Log.d(TAG, "[HTTP/3] CronetProviderInstaller success")
            } catch (e: Exception) {
                Log.w(TAG, "[HTTP/3] CronetProviderInstaller: ${e.message}, 继续尝试本地 Provider")
            }

            val host = java.net.URI(url).host ?: "cloudflare-quic.com"
            val engine = CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .addQuicHint(host, 443, 443)
                .setUserAgent("HahaLearn-HttpsVersionTest-Cronet")
                .build()

            val bodyBuffer = ByteArrayOutputStream()
            val callback = object : UrlRequest.Callback() {
                override fun onRedirectReceived(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    newLocationUrl: String,
                ) {
                    Log.d(
                        TAG,
                        "[HTTP/3] redirect -> $newLocationUrl negotiated=${info.negotiatedProtocol}"
                    )
                    request.followRedirect()
                }

                override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
                    Log.d(
                        TAG,
                        "[HTTP/3] responseStarted code=${info.httpStatusCode} " +
                                "negotiated=${info.negotiatedProtocol} (h3/quic 即 HTTP/3)",
                    )
                    request.read(ByteBuffer.allocateDirect(16 * 1024))
                }

                override fun onReadCompleted(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    byteBuffer: ByteBuffer,
                ) {
                    byteBuffer.flip()
                    val chunk = ByteArray(byteBuffer.remaining())
                    byteBuffer.get(chunk)
                    bodyBuffer.write(chunk)
                    byteBuffer.clear()
                    request.read(byteBuffer)
                }

                override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
                    val text = bodyBuffer.toString(StandardCharsets.UTF_8.name())
                    val negotiated = info.negotiatedProtocol
                    Log.d(
                        TAG,
                        "[HTTP/3] onSucceeded negotiated=$negotiated " +
                                "code=${info.httpStatusCode} len=${text.length} preview=${
                                    text.take(
                                        200
                                    )
                                }",
                    )
                    if (negotiated.contains("h3", ignoreCase = true) ||
                        negotiated.contains("quic", ignoreCase = true)
                    ) {
                        Log.d(TAG, "[HTTP/3] 已走 QUIC/HTTP/3（UDP），与 SocketTest 的 TCP+TLS 不同")
                    } else {
                        Log.w(
                            TAG,
                            "[HTTP/3] 实际落到 $negotiated（可能被网络降级到 h2/http1.1，或 UDP/443 被拦）",
                        )
                    }
                    engine.shutdown()
                }

                override fun onFailed(
                    request: UrlRequest,
                    info: UrlResponseInfo?,
                    error: CronetException,
                ) {
                    Log.e(
                        TAG,
                        "[HTTP/3] onFailed negotiated=${info?.negotiatedProtocol} err=${error.message}",
                        error,
                    )
                    engine.shutdown()
                }
            }

            engine.newUrlRequestBuilder(url, callback, cronetExecutor)
                .addHeader("Accept", "text/html,application/json")
                .build()
                .start()
            Log.d(TAG, "[HTTP/3] UrlRequest started url=$url (QUIC hint host=$host)")
        } catch (e: Exception) {
            Log.e(TAG, "[HTTP/3] Cronet 不可用: ${e.message}", e)
        }
    }
}
