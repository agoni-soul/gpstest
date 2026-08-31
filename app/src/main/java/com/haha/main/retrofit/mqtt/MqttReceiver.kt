package com.haha.main.retrofit.mqtt

import android.util.Log
import java.nio.charset.StandardCharsets

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: MQTT 接收方。SUBSCRIBE 后读 PUBLISH，并按 QoS 回 PUBACK / PUBREC / PUBCOMP。
 *
 **/

/** MQTT 接收方：SUBSCRIBE 后读 PUBLISH，按 QoS 回 PUBACK / PUBREC / PUBCOMP。 */
class MqttReceiver {
    private val TAG = this.javaClass.simpleName
    private var connection: MqttConnection? = null

    /** TCP + CONNECT，等 CONNACK。 */
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

    /** 发 SUBSCRIBE，等 SUBACK；默认请求 QoS 2（实际投递取 min(发布, 订阅)）。 */
    fun subscribe(topic: String, qos: Int = 2) {
        val packetId = conn().nextPacketId()
        conn().write(
            MqttFixedHeader(MqttPacketType.SUBSCRIBE),
            MqttWire.encodeSubscribe(packetId, topic, qos),
        )
        val ack = conn().readExpect(MqttPacketType.SUBACK)
        val suback = MqttWire.decodeSuback(ack.body)
        check(suback.packetId == packetId) { "SUBACK PacketId 不匹配" }
        check(suback.returnCodes.none { it == 0x80 }) { "SUBACK 失败 $suback" }
        Log.d(TAG, "[3] SUBACK topic=$topic granted=${suback.returnCodes}")
    }

    /**
     * 读一条 PUBLISH；QoS 1 回 PUBACK，QoS 2 走 PUBREC → PUBREL → PUBCOMP。
     */
    fun receivePublish(): Received {
        val packet = conn().readExpect(MqttPacketType.PUBLISH)
        val publish = MqttWire.decodePublish(packet.header, packet.body)
        val text = String(publish.payload, StandardCharsets.UTF_8)
        when (packet.header.qos) {
            0 -> Log.d(TAG, "[5] QoS0 送达 topic=${publish.topic} body=$text")
            1 -> {
                val id = requireNotNull(publish.packetId)
                conn().write(MqttFixedHeader(MqttPacketType.PUBACK), MqttWire.encodePuback(id))
                Log.d(TAG, "[5] QoS1 回 PUBACK packetId=$id body=$text")
            }

            2 -> {
                val id = requireNotNull(publish.packetId)
                conn().write(MqttFixedHeader(MqttPacketType.PUBREC), MqttWire.encodePubrec(id))
                val rel = conn().readExpect(MqttPacketType.PUBREL)
                check(MqttWire.decodePacketId(rel.body) == id) { "PUBREL PacketId 不匹配" }
                conn().write(MqttFixedHeader(MqttPacketType.PUBCOMP), MqttWire.encodePubcomp(id))
                Log.d(TAG, "[5] QoS2 PUBREC/PUBREL/PUBCOMP packetId=$id body=$text")
            }

            else -> throw IllegalStateException("非法 QoS ${packet.header.qos}")
        }
        return Received(
            topic = publish.topic,
            payload = text,
            qos = packet.header.qos,
            dup = packet.header.dup,
            retain = packet.header.retain,
        )
    }

    /** 发 UNSUBSCRIBE，等 UNSUBACK；之后 Broker 不再推该 topic。 */
    fun unsubscribe(topic: String) {
        val packetId = conn().nextPacketId()
        conn().write(
            MqttFixedHeader(MqttPacketType.UNSUBSCRIBE),
            MqttWire.encodeUnsubscribe(packetId, topic),
        )
        val ack = conn().readExpect(MqttPacketType.UNSUBACK)
        check(MqttWire.decodeUnsuback(ack.body) == packetId) { "UNSUBACK PacketId 不匹配" }
        Log.d(TAG, "[5] UNSUBACK topic=$topic")
    }

    /** 发 PINGREQ，等 PINGRESP。 */
    fun ping() {
        conn().write(MqttFixedHeader(MqttPacketType.PINGREQ), MqttWire.encodePingreq())
        conn().readExpect(MqttPacketType.PINGRESP)
        Log.d(TAG, "[5] PINGRESP")
    }

    /** 发 DISCONNECT 后关 Socket。 */
    fun disconnect() {
        val conn = connection ?: return
        conn.write(MqttFixedHeader(MqttPacketType.DISCONNECT), MqttWire.encodeDisconnect())
        Log.d(TAG, "[6] DISCONNECT")
        conn.close()
        connection = null
    }

    /** 不发 DISCONNECT，直接关 Socket（异常收尾）。 */
    fun close() {
        connection?.close()
        connection = null
    }

    /** 已 CONNECT 的连接，未连上则抛。 */
    private fun conn(): MqttConnection {
        return connection ?: throw IllegalStateException("Receiver 未 CONNECT")
    }

    /** 接收方读到的一条应用消息，带上头里的 qos / dup / retain。 */
    data class Received(
        val topic: String,
        val payload: String,
        val qos: Int,
        val dup: Boolean,
        val retain: Boolean,
    )

    private companion object {
        const val KEEP_ALIVE_SEC = 20
    }
}