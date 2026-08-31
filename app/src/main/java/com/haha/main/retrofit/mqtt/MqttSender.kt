package com.haha.main.retrofit.mqtt

import android.util.Log
import java.nio.charset.StandardCharsets

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: MQTT 发送方。只发 CONNECT / PUBLISH / PINGREQ / DISCONNECT，并处理对端确认。
 *
 **/
/** MQTT 发送方：CONNECT / PUBLISH / PINGREQ / DISCONNECT，并等 Broker 确认。 */
class MqttSender {
    private val TAG = this.javaClass.simpleName
    private var connection: MqttConnection? = null

    /** TCP + CONNECT，等 CONNACK；成功后才能 publish。 */
    fun connect(host: String, port: Int, clientId: String, keepAliveSec: Int = KEEP_ALIVE_SEC) {
        val conn = MqttConnection(TAG, host, port)
        conn.tcpConnect()
        conn.write(
            MqttFixedHeader(MqttPacketType.CONNECT),
            MqttWire.encodeConnect(clientId, keepAliveSec, cleanSession = true),
        )
        val ack = conn.readExpect(MqttPacketType.CONNACK)
        val connack = MqttWire.decodeConnack(ack.body)
        check(connack.returnCode == 0) { "CONNACK 拒绝 returnCode=${connack.returnCode}" }
        Log.d(TAG, "[2] CONNACK sessionPresent=${connack.sessionPresent} clientId=$clientId")
        connection = conn
    }

    /** QoS 0：最多一次，不带 PacketId，无确认。 */
    fun publishQos0(topic: String, payload: String) {
        publish(topic, payload, qos = 0, dup = false, retain = false)
    }

    /** QoS 1：至少一次，等 PUBACK。 */
    fun publishQos1(topic: String, payload: String) {
        val packetId = publish(topic, payload, qos = 1, dup = false, retain = false)
        val ack = conn().readExpect(MqttPacketType.PUBACK)
        check(MqttWire.decodePacketId(ack.body) == packetId) { "PUBACK PacketId 不匹配" }
        Log.d(TAG, "[4] PUBACK packetId=$packetId")
    }

    /** QoS 2：恰好一次。PUBLISH → PUBREC → PUBREL → PUBCOMP。 */
    fun publishQos2(topic: String, payload: String) {
        val packetId = publish(topic, payload, qos = 2, dup = false, retain = false)
        val rec = conn().readExpect(MqttPacketType.PUBREC)
        check(MqttWire.decodePacketId(rec.body) == packetId) { "PUBREC PacketId 不匹配" }
        Log.d(TAG, "[4] PUBREC packetId=$packetId")
        conn().write(MqttFixedHeader(MqttPacketType.PUBREL), MqttWire.encodePubrel(packetId))
        val comp = conn().readExpect(MqttPacketType.PUBCOMP)
        check(MqttWire.decodePacketId(comp.body) == packetId) { "PUBCOMP PacketId 不匹配" }
        Log.d(TAG, "[4] PUBCOMP packetId=$packetId")
    }

    /** RETAIN=1：Broker 存最后一条，后订阅者也能收到。 */
    fun publishRetain(topic: String, payload: String) {
        val packetId = publish(topic, payload, qos = 1, dup = false, retain = true)
        conn().readExpect(MqttPacketType.PUBACK)
        Log.d(TAG, "[4] RETAIN PUBACK packetId=$packetId")
    }

    /** DUP=1：QoS>0 的重发标记。这里直接发一条带 DUP 的 QoS1，对照 Fixed Header bit3。 */
    fun publishDup(topic: String, payload: String) {
        val packetId = publish(topic, payload, qos = 1, dup = true, retain = false)
        conn().readExpect(MqttPacketType.PUBACK)
        Log.d(TAG, "[4] DUP PUBACK packetId=$packetId")
    }

    /** RETAIN=1 且 payload 为空：清除该 topic 的保留消息。 */
    fun clearRetain(topic: String) {
        publish(topic, payload = "", qos = 0, dup = false, retain = true)
        Log.d(TAG, "[4] 清除 RETAIN $topic")
    }

    /** 发 PINGREQ，等 PINGRESP，证明连接还活着。 */
    fun ping() {
        conn().write(MqttFixedHeader(MqttPacketType.PINGREQ), MqttWire.encodePingreq())
        conn().readExpect(MqttPacketType.PINGRESP)
        Log.d(TAG, "[4] PINGRESP")
    }

    /** 发 DISCONNECT 后关 Socket，Broker 立刻结束会话。 */
    fun disconnect() {
        val conn = connection ?: return
        conn.write(MqttFixedHeader(MqttPacketType.DISCONNECT), MqttWire.encodeDisconnect())
        Log.d(TAG, "[6] DISCONNECT")
        conn.close()
        connection = null
    }

    /** type 0 Reserved，规范禁止发送，调用即抛。 */
    fun reserved() {
        MqttWire.reserved()
    }

    /** MQTT 5.0 AUTH，3.1.1 会话不发送。 */
    fun auth(): ByteArray {
        val body = MqttWire.encodeAuth()
        val first = MqttFixedHeader(MqttPacketType.AUTH).toFirstByte()
        Log.d(TAG, "AUTH 仅编码 first=0x${first.toString(16)} body=${body.size}，3.1.1 不发送")
        return body
    }

    /** 不发 DISCONNECT，直接关 Socket（异常收尾）。 */
    fun close() {
        connection?.close()
        connection = null
    }

    /** 写出一条 PUBLISH；QoS>0 时分配 PacketId。返回 id 供后续确认核对。 */
    private fun publish(
        topic: String,
        payload: String,
        qos: Int,
        dup: Boolean,
        retain: Boolean,
        packetId: Int? = null,
    ): Int? {
        val id = when {
            qos <= 0 -> null
            packetId != null -> packetId
            else -> conn().nextPacketId()
        }
        val header = MqttFixedHeader(MqttPacketType.PUBLISH, dup = dup, qos = qos, retain = retain)
        val body = MqttWire.encodePublish(topic, payload.toByteArray(StandardCharsets.UTF_8), id)
        conn().write(header, body)
        Log.d(
            TAG,
            "[4] PUBLISH topic=$topic qos=$qos dup=$dup retain=$retain packetId=$id body=$payload"
        )
        return id
    }

    /** 已 CONNECT 的连接，未连上则抛。 */
    private fun conn(): MqttConnection {
        return connection ?: throw IllegalStateException("Sender 未 CONNECT")
    }

    private companion object {
        const val KEEP_ALIVE_SEC = 20
    }
}
