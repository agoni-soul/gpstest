package com.haha.main.retrofit

import android.util.Log
import com.haha.main.retrofit.mqtt.MqttReceiver
import com.haha.main.retrofit.mqtt.MqttSender
import com.haha.main.retrofit.mqtt.MqttWire
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: MQTT 对照 Demo：发送方 / 接收方分开，按 Fixed Header 表逐项跑一遍。
 *              裸 TCP 编解码，不经过 Paho，才能看到 PUBACK / PUBREC / PUBREL / PUBCOMP。
 *
 **/
/** MQTT Demo 入口：编排发送方 / 接收方，按 Fixed Header 表逐项跑一遍。 */
class MqttTest {
    private val TAG = this.javaClass.simpleName
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** TestLearnUtils 调用的入口，默认跑全部 Broker。 */
    fun test() {
        runAll()
    }

    /** 依次尝试公共 Broker，成功即停，全部失败打 error。 */
    fun runAll(brokers: List<Broker> = DEFAULT_BROKERS) {
        scope.launch {
            var lastError: Exception? = null
            for (broker in brokers) {
                try {
                    runAgainst(broker)
                    return@launch
                } catch (e: Exception) {
                    lastError = e
                    Log.w(
                        TAG,
                        "${broker.name} ${broker.host}:${broker.port} failed: ${e.message}, try next"
                    )
                }
            }
            Log.e(TAG, "MQTT onFailure: all brokers failed", lastError)
        }
    }

    /**
     * 先订后发。发送方按 QoS0 / 1 / 2 / DUP / RETAIN 各发一条；接收方按 QoS 回确认。
     */
    private fun runAgainst(broker: Broker) {
        val suffix = (System.currentTimeMillis() and 0xFFFF).toString(16)
        val topic = "$DEFAULT_TOPIC/$suffix"
        val sender = MqttSender()
        val receiver = MqttReceiver()
        val retainReader = MqttReceiver()
        try {
            reservedSafe()
            sender.auth()

            receiver.connect(broker.host, broker.port, clientId = "hR$suffix")
            receiver.subscribe(topic, qos = 2)
            sender.connect(broker.host, broker.port, clientId = "hS$suffix")

            sender.publishQos0(topic, "qos0 from sender")
            logReceived("QoS0", receiver.receivePublish())

            sender.publishQos1(topic, "qos1 from sender")
            logReceived("QoS1", receiver.receivePublish())

            sender.publishQos2(topic, "qos2 from sender")
            logReceived("QoS2", receiver.receivePublish())

            sender.publishDup(topic, "dup from sender")
            logReceived("DUP", receiver.receivePublish())

            sender.publishRetain(topic, "retain from sender")
            logReceived("RETAIN-live", receiver.receivePublish())

            retainReader.connect(broker.host, broker.port, clientId = "hB$suffix")
            retainReader.subscribe(topic, qos = 1)
            logReceived("RETAIN-new", retainReader.receivePublish())
            retainReader.unsubscribe(topic)
            retainReader.disconnect()

            sender.clearRetain(topic)
            logReceived("RETAIN-clear", receiver.receivePublish())

            sender.ping()
            receiver.ping()
            receiver.unsubscribe(topic)
            sender.disconnect()
            receiver.disconnect()
            Log.d(TAG, "MQTT demo ok broker=${broker.name} topic=$topic")
        } finally {
            sender.close()
            receiver.close()
            retainReader.close()
        }
    }

    /** type 0 Reserved：规范禁止发送，这里只验证会抛错。 */
    private fun reservedSafe() {
        try {
            MqttWire.reserved()
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "RESERVED: ${e.message}")
        }
    }

    /** 打印接收方读到的一条 PUBLISH，对照 qos / dup / retain。 */
    private fun logReceived(label: String, msg: MqttReceiver.Received) {
        Log.d(
            TAG,
            "[5] $label topic=${msg.topic} qos=${msg.qos} dup=${msg.dup} retain=${msg.retain} body=${msg.payload}",
        )
    }

    /** 公共 Broker 地址（明文 1883）。 */
    data class Broker(val name: String, val host: String, val port: Int)

    private companion object {
        const val DEFAULT_TOPIC = "hahalearn/demo"
        val DEFAULT_BROKERS = listOf(
            Broker("EMQX", "broker.emqx.io", 1883),
            Broker("HiveMQ", "broker.hivemq.com", 1883),
            Broker("Mosquitto", "test.mosquitto.org", 1883),
        )
    }
}
