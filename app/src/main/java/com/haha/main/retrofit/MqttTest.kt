package com.haha.main.retrofit

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: MQTT 3.1.1 对照 Demo（Paho）。HTTP 是请求-响应；MQTT 是长连接 + 发布/订阅。
 *              流程：TCP 连 Broker → CONNECT/CONNACK → SUBSCRIBE → PUBLISH → 回显 → DISCONNECT。
 *
 **/
class MqttTest {
    private val TAG = this.javaClass.simpleName
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun test() {
        // 公共 Broker，无账号；国内优先 EMQX，失败再换
        pubSub()
    }

    /**
     * 自己订自己发：先 SUBSCRIBE，再 PUBLISH 同一 topic，Broker 把消息回推过来。
     * QoS 1：至少一次，Broker 会回 PUBACK。
     */
    fun pubSub(
        topic: String = DEFAULT_TOPIC,
        payload: String = "hello from HahaLearn",
        brokers: List<Broker> = DEFAULT_BROKERS,
    ) {
        scope.launch {
            var lastError: Exception? = null
            for (broker in brokers) {
                try {
                    runSession(broker, topic, payload)
                    return@launch
                } catch (e: Exception) {
                    lastError = e
                    Log.w(TAG, "pubSub ${broker.name} ${broker.uri} failed: ${e.message}, try next")
                }
            }
            Log.e(TAG, "pubSub onFailure: all brokers failed", lastError)
        }
    }

    /**
     * ① TCP 连 Broker → ② CONNECT/CONNACK → ③ SUBSCRIBE → ④ PUBLISH → ⑤ messageArrived → ⑥ DISCONNECT
     */
    private fun runSession(broker: Broker, topic: String, payload: String) {
        val clientId = "HahaLearn-${System.currentTimeMillis() and 0xFFFF}"
        val arrived = CountDownLatch(1)
        val incoming = AtomicReference<String>()
        // 内存持久化：Demo 不落盘；断线重连 / QoS 2 才需要文件 persistence
        val client = MqttClient(broker.uri, clientId, MemoryPersistence())

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.w(TAG, "connectionLost: ${cause?.message}", cause)
            }

            override fun messageArrived(msgTopic: String?, message: MqttMessage?) {
                val text = message?.payload?.toString(Charsets.UTF_8).orEmpty()
                incoming.set(text)
                Log.d(
                    TAG,
                    "[5] messageArrived topic=$msgTopic qos=${message?.qos} retained=${message?.isRetained} body=$text",
                )
                arrived.countDown()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "[4] deliveryComplete (PUBACK) topics=${token?.topics?.joinToString()}")
            }
        })

        try {
            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = CONNECT_TIMEOUT_SEC
                keepAliveInterval = KEEP_ALIVE_SEC
                isAutomaticReconnect = false
            }

            Log.d(TAG, "[1] TCP connect ${broker.name} ${broker.uri} clientId=$clientId")
            // ② MQTT CONNECT：clientId / cleanSession / keepAlive；成功则 CONNACK
            client.connect(options)
            Log.d(TAG, "[2] CONNACK connected=${client.isConnected} keepAlive=${KEEP_ALIVE_SEC}s")

            // ③ SUBSCRIBE：告诉 Broker 关心这个 topic；通配 + / # 见 DEFAULT_TOPIC 注释
            client.subscribe(topic, QOS_AT_LEAST_ONCE)
            Log.d(TAG, "[3] SUBSCRIBE $topic qos=$QOS_AT_LEAST_ONCE")

            val message = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
                qos = QOS_AT_LEAST_ONCE
                isRetained = false
            }
            Log.d(TAG, "[4] PUBLISH $topic qos=$QOS_AT_LEAST_ONCE body=$payload")
            client.publish(topic, message)

            val got = arrived.await(WAIT_ARRIVAL_SEC, TimeUnit.SECONDS)
            if (!got) {
                throw IllegalStateException("timeout waiting messageArrived on $topic")
            }
            Log.d(TAG, "[5] echo ok: ${incoming.get()}")
        } finally {
            if (client.isConnected) {
                client.disconnect()
                Log.d(TAG, "[6] DISCONNECT")
            }
            client.close()
        }
    }

    data class Broker(val name: String, val uri: String)

    private companion object {
        const val CONNECT_TIMEOUT_SEC = 10
        const val KEEP_ALIVE_SEC = 20
        const val WAIT_ARRIVAL_SEC = 8L

        /** QoS 0 最多一次；1 至少一次（本 Demo）；2 恰好一次 */
        const val QOS_AT_LEAST_ONCE = 1

        /**
         * 用独立 topic，避免和公共频道别人的消息搅在一起。
         * MQTT topic 是 `/` 分层；`+` 单层通配，`#` 多层通配。
         */
        const val DEFAULT_TOPIC = "hahalearn/demo"

        val DEFAULT_BROKERS = listOf(
            Broker("EMQX", "tcp://broker.emqx.io:1883"),
            Broker("HiveMQ", "tcp://broker.hivemq.com:1883"),
            Broker("Mosquitto", "tcp://test.mosquitto.org:1883"),
        )
    }
}
