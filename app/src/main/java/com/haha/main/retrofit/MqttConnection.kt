package com.haha.main.retrofit

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @author:     haha
 * @date:       2026/8/30
 * Description: MQTT 裸 TCP 连接：写 Fixed Header + Remaining Length + body，按类型读回包。
 *
 **/
/** 一条 MQTT TCP：写出/读入「第一字节 + Remaining Length + body」。 */
class MqttConnection(
    private val role: String,
    private val host: String,
    private val port: Int,
) {
    private val tag = role
    private val packetIds = AtomicInteger(1)
    private var socket: Socket? = null
    private var input: BufferedInputStream? = null
    private var output: BufferedOutputStream? = null

    /** TCP 三次握手连 Broker，设置读超时。 */
    fun tcpConnect(timeoutMs: Int = CONNECT_TIMEOUT_MS) {
        val tcp = Socket()
        tcp.soTimeout = READ_TIMEOUT_MS
        Log.d(tag, "[1] TCP connect $host:$port")
        tcp.connect(InetSocketAddress(host, port), timeoutMs)
        socket = tcp
        input = BufferedInputStream(tcp.inputStream)
        output = BufferedOutputStream(tcp.outputStream)
        Log.d(tag, "[1] TCP ESTABLISHED local=${tcp.localSocketAddress}")
    }

    /** 分配 1～65535 的 PacketId，不能为 0；QoS>0 的请求/确认靠它配对。 */
    fun nextPacketId(): Int {
        while (true) {
            val id = packetIds.getAndUpdate { prev ->
                val next = prev + 1
                if (next > 0xFFFF) 1 else next
            }
            if (id != 0) return id
        }
    }

    /** 写出 Fixed Header + Remaining Length + body，并 flush。 */
    fun write(header: MqttFixedHeader, body: ByteArray = ByteArray(0)) {
        val out = output ?: throw IllegalStateException("$role 未连接")
        val first = header.toFirstByte()
        val remaining = MqttWire.encodeRemainingLength(body.size)
        out.write(first)
        out.write(remaining)
        if (body.isNotEmpty()) out.write(body)
        out.flush()
        Log.d(
            tag,
            ">> ${header.type} dup=${header.dup} qos=${header.qos} retain=${header.retain} body=${body.size}",
        )
    }

    /** 读一包：第一字节解类型，再按剩余长度读完 body。 */
    fun read(): MqttRawPacket {
        val inp = input ?: throw IllegalStateException("$role 未连接")
        val first = inp.read()
        if (first < 0) throw java.io.EOFException("$role 连接已关")
        val header = MqttFixedHeader.fromFirstByte(first)
        val remaining = MqttWire.readRemainingLength(inp)
        val body = readFully(inp, remaining)
        Log.d(
            tag,
            "<< ${header.type} dup=${header.dup} qos=${header.qos} retain=${header.retain} body=${body.size}",
        )
        return MqttRawPacket(header, body)
    }

    /** 读一包并断言类型，避免把心跳/其它包当成确认。 */
    fun readExpect(type: MqttPacketType): MqttRawPacket {
        val packet = read()
        check(packet.header.type == type) {
            "$role 期望 $type，实际 ${packet.header.type}"
        }
        return packet
    }

    /** 关闭 Socket，清空流。 */
    fun close() {
        runCatching { socket?.close() }
        socket = null
        input = null
        output = null
    }

    /** 从流中读满 len 字节；对端提前关闭则截断返回。 */
    private fun readFully(input: BufferedInputStream, len: Int): ByteArray {
        if (len == 0) return ByteArray(0)
        val buf = ByteArray(len)
        var off = 0
        while (off < len) {
            val n = input.read(buf, off, len - off)
            if (n < 0) break
            off += n
        }
        return if (off == len) buf else buf.copyOf(off)
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 10_000
        const val READ_TIMEOUT_MS = 10_000
    }
}
