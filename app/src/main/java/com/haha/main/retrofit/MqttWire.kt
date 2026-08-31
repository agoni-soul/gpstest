package com.haha.main.retrofit

import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: MQTT Fixed Header 第一字节 + 16 种控制报文的编解码（对照报文类型表）。
 *
 *  7 6 5 4 |  3  | 2 1 |  0
 *  Type    | DUP | QoS | RETAIN
 *
 **/
/** Fixed Header 高 4 位：0～15 号控制报文类型（对照报文表）。 */
enum class MqttPacketType(val code: Int) {
    /** 0 保留，禁止发送。 */
    RESERVED(0),

    /** 1 客户端请求连接。 */
    CONNECT(1),

    /** 2 连接确认。 */
    CONNACK(2),

    /** 3 发布消息。 */
    PUBLISH(3),

    /** 4 QoS 1 发布确认。 */
    PUBACK(4),

    /** 5 QoS 2 第一步：发布已收到。 */
    PUBREC(5),

    /** 6 QoS 2 第二步：发布释放。 */
    PUBREL(6),

    /** 7 QoS 2 第三步：发布完成。 */
    PUBCOMP(7),

    /** 8 客户端订阅请求。 */
    SUBSCRIBE(8),

    /** 9 订阅确认。 */
    SUBACK(9),

    /** 10 取消订阅。 */
    UNSUBSCRIBE(10),

    /** 11 取消订阅确认。 */
    UNSUBACK(11),

    /** 12 心跳请求。 */
    PINGREQ(12),

    /** 13 心跳响应。 */
    PINGRESP(13),

    /** 14 断开连接。 */
    DISCONNECT(14),

    /** 15 认证交换（MQTT 5.0）。 */
    AUTH(15);

    companion object {
        /** 从第一字节高 4 位还原报文类型。 */
        fun fromCode(code: Int): MqttPacketType {
            return entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("unknown mqtt type $code")
        }
    }
}

/** Fixed Header 第一字节：Type | DUP | QoS | RETAIN。 */
data class MqttFixedHeader(
    val type: MqttPacketType,
    val dup: Boolean = false,
    val qos: Int = 0,
    val retain: Boolean = false,
) {
    /**
     * PUBLISH 才用 DUP / QoS / RETAIN；PUBREL / SUBSCRIBE / UNSUBSCRIBE 标志位必须是 0010。
     */
    fun toFirstByte(): Int {
        val flags = when (type) {
            MqttPacketType.RESERVED -> {
                throw IllegalArgumentException("type 0 Reserved，禁止发送")
            }

            MqttPacketType.PUBLISH -> {
                var f = 0
                // 根据布尔条件 dup 的值，决定是否将变量 f 的第 4 位（从右往左数，索引为 3 的位）设置为 1。
                if (dup) f = f or 0x08
                // 提取变量 qos 的最低 2 位，将其左移 1 位（相当于乘以 2），然后与变量 f 进行按位或运算，最后将结果重新赋值给 f。
                f = f or ((qos and 0x03) shl 1)
                if (retain) f = f or 0x01
                f
            }

            MqttPacketType.PUBREL,
            MqttPacketType.SUBSCRIBE,
            MqttPacketType.UNSUBSCRIBE,
                -> 0x02

            else -> 0x00
        }
        return (type.code shl 4) or flags
    }

    companion object {
        /** 从网上读到的第一字节拆出 type / dup / qos / retain。 */
        fun fromFirstByte(b: Int): MqttFixedHeader {
            val type = MqttPacketType.fromCode((b shr 4) and 0x0F)
            val flags = b and 0x0F
            return if (type == MqttPacketType.PUBLISH) {
                MqttFixedHeader(
                    type = type,
                    dup = flags and 0x08 != 0,
                    qos = (flags shr 1) and 0x03,
                    retain = flags and 0x01 != 0,
                )
            } else {
                MqttFixedHeader(type)
            }
        }
    }
}

/** 线上读到的一包：已解码的头 + 尚未按类型解析的 body。 */
data class MqttRawPacket(val header: MqttFixedHeader, val body: ByteArray)

/** CONNACK body：是否已有会话 + 返回码（0 成功）。 */
data class MqttConnack(val sessionPresent: Boolean, val returnCode: Int)

/** PUBLISH body：topic、可选 PacketId、payload。 */
data class MqttPublish(
    val topic: String,
    val packetId: Int?,
    val payload: ByteArray,
)

/** SUBACK body：请求的 PacketId + 每个订阅的授权码（0x80 失败）。 */
data class MqttSuback(val packetId: Int, val returnCodes: List<Int>)

/** 16 种控制报文的 body 编解码（不含第一字节）。 */
object MqttWire {

    /** type 0 Reserved，规范禁止使用。 */
    fun reserved() {
        throw IllegalArgumentException("MQTT type 0 Reserved，规范禁止使用")
    }

    /** CONNECT body：协议名 MQTT、级别 4、cleanSession、keepAlive、clientId。 */
    fun encodeConnect(clientId: String, keepAliveSec: Int, cleanSession: Boolean): ByteArray {
        val flags = if (cleanSession) 0x02 else 0x00
        return buildBytes {
            writeMqttString("MQTT")
            write(0x04)
            write(flags)
            writeU16(keepAliveSec)
            writeMqttString(clientId)
        }
    }

    /** 解析 CONNACK：sessionPresent + returnCode。 */
    fun decodeConnack(body: ByteArray): MqttConnack {
        require(body.size >= 2) { "CONNACK 长度不足" }
        return MqttConnack(
            sessionPresent = body[0].toInt() and 0x01 != 0,
            returnCode = body[1].toInt() and 0xFF,
        )
    }

    /** PUBLISH body：topic +（QoS>0 时）PacketId + payload。 */
    fun encodePublish(topic: String, payload: ByteArray, packetId: Int?): ByteArray {
        return buildBytes {
            writeMqttString(topic)
            if (packetId != null) writeU16(packetId)
            write(payload)
        }
    }

    /** 按 QoS 解析 PUBLISH body；topic 长度用报文里的字节数。 */
    fun decodePublish(header: MqttFixedHeader, body: ByteArray): MqttPublish {
        require(body.size >= 2) { "PUBLISH 缺 Topic" }
        val topicLen = u16(body, 0)
        require(body.size >= 2 + topicLen) { "PUBLISH Topic 越界" }
        val topic = String(body, 2, topicLen, StandardCharsets.UTF_8)
        var offset = 2 + topicLen
        val packetId = if (header.qos > 0) {
            require(offset + 2 <= body.size) { "PUBLISH 缺 PacketId" }
            val id = u16(body, offset)
            offset += 2
            id
        } else {
            null
        }
        val payload = if (offset >= body.size) ByteArray(0) else body.copyOfRange(offset, body.size)
        return MqttPublish(topic, packetId, payload)
    }

    /** PUBACK body：2 字节 PacketId。 */
    fun encodePuback(packetId: Int): ByteArray = encodePacketId(packetId)

    /** PUBREC body：2 字节 PacketId。 */
    fun encodePubrec(packetId: Int): ByteArray = encodePacketId(packetId)

    /** PUBREL body：2 字节 PacketId。 */
    fun encodePubrel(packetId: Int): ByteArray = encodePacketId(packetId)

    /** PUBCOMP body：2 字节 PacketId。 */
    fun encodePubcomp(packetId: Int): ByteArray = encodePacketId(packetId)

    /** 确认类报文共用的 2 字节大端 PacketId。 */
    fun encodePacketId(packetId: Int): ByteArray = buildBytes { writeU16(packetId) }

    /** 从 body 读出 2 字节 PacketId。 */
    fun decodePacketId(body: ByteArray): Int {
        require(body.size >= 2) { "PacketId 长度不足" }
        return u16(body, 0)
    }

    /** SUBSCRIBE body：PacketId + topic + 请求 QoS。 */
    fun encodeSubscribe(packetId: Int, topic: String, qos: Int): ByteArray {
        return buildBytes {
            writeU16(packetId)
            writeMqttString(topic)
            write(qos and 0x03)
        }
    }

    /** 解析 SUBACK：PacketId + 每个订阅的授权码。 */
    fun decodeSuback(body: ByteArray): MqttSuback {
        require(body.size >= 3) { "SUBACK 长度不足" }
        val packetId = u16(body, 0)
        val codes = (2 until body.size).map { body[it].toInt() and 0xFF }
        return MqttSuback(packetId, codes)
    }

    /** UNSUBSCRIBE body：PacketId + topic。 */
    fun encodeUnsubscribe(packetId: Int, topic: String): ByteArray {
        return buildBytes {
            writeU16(packetId)
            writeMqttString(topic)
        }
    }

    /** 解析 UNSUBACK：2 字节 PacketId。 */
    fun decodeUnsuback(body: ByteArray): Int = decodePacketId(body)

    /** PINGREQ body 为空。 */
    fun encodePingreq(): ByteArray = ByteArray(0)

    /** PINGRESP body 为空。 */
    fun encodePingresp(): ByteArray = ByteArray(0)

    /** DISCONNECT body 为空。 */
    fun encodeDisconnect(): ByteArray = ByteArray(0)

    /** MQTT 5.0 才有；3.1.1 Broker 不认，只做编码对照。 */
    fun encodeAuth(reasonCode: Int = 0x00): ByteArray {
        return buildBytes {
            write(reasonCode)
            write(0x00)
        }
    }

    /** 把 body 长度编成 1～4 字节 Remaining Length。 */
    fun encodeRemainingLength(length: Int): ByteArray {
        require(length in 0..268_435_455) { "remaining length 非法: $length" }
        val out = ArrayList<Byte>(4)
        var value = length
        do {
            var encoded = value % 128
            value /= 128
            if (value > 0) encoded = encoded or 0x80
            out.add(encoded.toByte())
        } while (value > 0)
        return out.toByteArray()
    }

    /** 从流中读 Remaining Length，与 encodeRemainingLength 成对。 */
    fun readRemainingLength(input: InputStream): Int {
        var multiplier = 1
        var value = 0
        repeat(4) {
            val encoded = input.read()
            if (encoded < 0) throw java.io.EOFException("remaining length EOF")
            value += (encoded and 0x7F) * multiplier
            if (encoded and 0x80 == 0) return value
            multiplier *= 128
        }
        throw IllegalArgumentException("malformed remaining length")
    }

    /** 拼 body 字节。 */
    private fun buildBytes(block: ByteSink.() -> Unit): ByteArray {
        val sink = ByteSink()
        sink.block()
        return sink.toByteArray()
    }

    /** 大端 16 位无符号整数。 */
    private fun u16(data: ByteArray, offset: Int): Int {
        return ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
    }

    /**
     * 顺序写入 MQTT body：单字节、数组、u16、带长度的 UTF-8 字符串。
     * 大端序：在多字节数据的存储中，大端序将数据的最高有效字节（MSB）存放在低地址，最低有效字节（LSB）存放在高地址。
     * 对于网络传输和许多文件格式，大端序是标准的字节序（也称网络字节序）。
     **/
    private class ByteSink {
        private val out = java.io.ByteArrayOutputStream()

        /** 写 1 字节。 */
        fun write(value: Int) {
            out.write(value)
        }

        /** 写原始字节。 */
        fun write(bytes: ByteArray) {
            out.write(bytes)
        }

        /** 写 2 字节大端整数。 */
        fun writeU16(value: Int) {
            out.write(value ushr 8 and 0xFF)
            out.write(value and 0xFF)
        }

        /** 写 MQTT 字符串：2 字节长度 + UTF-8。 */
        fun writeMqttString(text: String) {
            val bytes = text.toByteArray(StandardCharsets.UTF_8)
            require(bytes.size <= 0xFFFF) { "MQTT 字符串过长" }
            writeU16(bytes.size)
            write(bytes)
        }

        /** 取出已写入的 body。 */
        fun toByteArray(): ByteArray = out.toByteArray()
    }
}
