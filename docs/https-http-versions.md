# HTTPS 上的 HTTP/1.1、HTTP/2、HTTP/3

记录时间：2026-08-30  
示例代码：

- `app/src/main/java/com/haha/main/retrofit/SocketTest.kt` — 裸 TCP Socket + TLS，**只能**讲 HTTP/1.1
  明文帧
- `app/src/main/java/com/haha/main/retrofit/HttpsVersionTest.kt` — 对照 Demo：强制 1.1 / 协商 2 /
  Cronet 试 3
-
仓库接口：`https://api.github.com/repos/haha-yang/hahalearn`（对照 [haha-yang/hahalearn](https://github.com/haha-yang/hahalearn)）
- HTTP/3 探测：`https://cloudflare-quic.com/`

口语里说的「HTTPS 1.0 / 2.0 / 3.0」，规范名称是：

| 口语        | 正式名称                             | 传输      | 本工程怎么测                                          |
|-----------|----------------------------------|---------|-------------------------------------------------|
| HTTPS 1.x | **HTTP/1.0 或 HTTP/1.1** + TLS    | TCP     | `SocketTest` / `HttpsVersionTest.httpsHttp11`   |
| HTTPS 2.0 | **HTTP/2** + TLS                 | TCP     | `HttpsVersionTest.httpsHttp2`（OkHttp ALPN `h2`） |
| HTTPS 3.0 | **HTTP/3**（跑在 QUIC 上，TLS 1.3 内建） | **UDP** | `HttpsVersionTest.httpsHttp3`（Cronet）           |

**HTTPS 不是单独第四种协议**，而是「HTTP 语义 + 加密传输」。版本差在 **HTTP 帧怎么切、跑在 TCP 还是 QUIC
**。

项目 OkHttp **4.9.1 原生支持 HTTP/1.1 与 HTTP/2，不原生支持 HTTP/3**；h3 用 Play Services Cronet。

---

## 1. 一张图看清三层差别

```text
HTTP/1.1 + TLS          HTTP/2 + TLS              HTTP/3 (QUIC)
─────────────────       ─────────────────         ─────────────────
应用: 文本请求行/头      应用: 二进制帧 + HPACK     应用: HTTP/3 帧 + QPACK
      请求-响应排队            Stream 多路复用           Stream 多路复用
        │                      │                         │
安全: TLS（常 1.2/1.3）   TLS + ALPN=h2              QUIC 内建 TLS 1.3
        │                      │                         │
传输: TCP 三次握手        TCP 三次握手                UDP（无 TCP 握手）
        │                      │                         │
问题: 队头阻塞(应用层)    TCP 层仍有队头阻塞           按 stream 丢包重传
```

---

## 2. HTTP/1.0（对照基线）

虽日常多用 1.1，对比时仍要知道 1.0：

| 点    | HTTP/1.0                                            |
|------|-----------------------------------------------------|
| 报文   | 纯文本：`GET /path HTTP/1.0` + 头 + 空行 + body            |
| 连接   | **默认短连接**：一请求一关 TCP（除非 `Connection: keep-alive` 扩展） |
| Host | 最初不是强制头（1.1 才强制）                                    |
| 管道   | 无标准 pipeline                                        |

每次页面十几个资源 ≈ 十几次 TCP +（HTTPS 下）多次 TLS，极贵。

---

## 3. HTTP/1.1（`SocketTest` / `httpsHttp11`）

相对 1.0 的关键升级：

| 点    | HTTP/1.1                         |
|------|----------------------------------|
| 默认   | **持久连接** Keep-Alive              |
| Host | **必须**有 `Host`                   |
| 分块   | `Transfer-Encoding: chunked`     |
| 管道   | 可 pipeline，但实现坑多，浏览器/OkHttp 基本不用 |

`SocketTest` 强制 ALPN `http/1.1`，自己拼：

```http
GET /repos/haha-yang/hahalearn HTTP/1.1
Host: api.github.com
...
```

**致命问题：应用层队头阻塞（HOL）**——同一条 TCP 上请求必须排队；前一个响应没完，后面的不能真正并行（除非开多连接，又浪费握手）。

`HttpsVersionTest.httpsHttp11`：`OkHttpClient.protocols(listOf(HTTP_1_1))`，Log 里应看到
`negotiated=http/1.1`。

---

## 4. HTTP/2（`httpsHttp2`）

RFC 7540。仍是 **TCP + TLS**，但应用层换成二进制。

### 4.1 核心能力

| 能力              | 含义                                                 |
|-----------------|----------------------------------------------------|
| **多路复用**        | 一条连接上多个 **Stream** 并行，互不堵在「等上一个 HTTP 响应」           |
| **二进制帧**        | `HEADERS` / `DATA` / `SETTINGS` / `WINDOW_UPDATE`… |
| **HPACK**       | 头部压缩，重复头不反复传                                       |
| **Stream 优先级**  | 可标依赖/权重（实际效果因实现而异）                                 |
| **Server Push** | 服务端可推（现已少用 / 多关闭）                                  |
| **ALPN**        | TLS 握手时协商 `h2`，不再靠明文 Upgrade                       |

### 4.2 和 1.1 的直观差别

```text
HTTP/1.1 一条连接:
  Req1 ──────────────────► resp1
       req2 ─────────────► resp2     （串行）

HTTP/2 一条连接:
  Stream1: HEADERS+DATA ──►
  Stream3: HEADERS+DATA ──►         （帧交错，并行）
  Stream5: HEADERS+DATA ──►
```

### 4.3 仍有的问题

TCP 丢一个包，**整条连接**上所有 Stream 都可能停等重传 → **传输层队头阻塞**。这是 HTTP/3 要解决的。

### 4.4 本工程怎么测

```kotlin
OkHttpClient.Builder()
    .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1)) // 必须保留 1.1 回退
```

对 `api.github.com` 多数会打出：

```text
[HTTP/2] connectEnd ... alpnProtocol=h2
[HTTP/2] code=200 negotiated=h2 ...
```

若服务端不支持 h2，会落到 `http/1.1`，Demo 会 `Log.w`。

---

## 5. HTTP/3（`httpsHttp3`）

RFC 9114。HTTP 语义差不多，**传输换成 QUIC（RFC 9000），跑在 UDP 上**。

### 5.1 为什么不用 TCP

|      | HTTP/2 over TCP            | HTTP/3 over QUIC                 |
|------|----------------------------|----------------------------------|
| 建连   | TCP 握手 + TLS 握手（约 1～2 RTT） | QUIC 常 **1-RTT**，会话恢复可 **0-RTT** |
| 丢包   | 一个丢包卡住整连接                  | **按 Stream 重传**，其它 Stream 继续     |
| 连接迁移 | 四元组变了通常断                   | Connection ID，换 Wi‑Fi/蜂窝可续       |
| 加密   | 独立 TLS 记录层                 | **TLS 1.3 集成进 QUIC**             |
| 中间盒  | TCP 友好                     | 部分网络拦 UDP/443，需回落 h2/1.1         |

### 5.2 和「UDP DNS」的关系

`SocketTest.udpDns` 是裸 UDP 查 DNS；HTTP/3 的 UDP 上跑的是完整 QUIC（拥塞控制、重传、加密、多路复用），*
*不是**「裸 UDP 发 HTTP 文本」。

### 5.3 本工程怎么测

OkHttp 4.9.1 **不能** `protocols(HTTP_3)`。Demo 用 Cronet：

```text
CronetEngine.enableQuic(true)
  → UrlRequest → UrlResponseInfo.negotiatedProtocol
  → 期望含 h3 / quic
```

目标站：`https://cloudflare-quic.com/`。

可能失败原因：

1. 设备无 Google Play Services / Cronet 装不上
2. 运营商或公司网拦 **UDP 443**
3. 首次无 Alt-Svc 缓存时可能先 h2，再升级（Demo 用了 `addQuicHint` 提高首包走 QUIC 概率）

失败时看 `[HTTP/3] onFailed` 或 `实际落到 h2` 警告——这本身也说明网络在降级。

---

## 6. 对照总表

|         | HTTP/1.0 | HTTP/1.1                 | HTTP/2     | HTTP/3             |
|---------|----------|--------------------------|------------|--------------------|
| 报文      | 文本       | 文本                       | 二进制帧       | 二进制帧（QUIC 上）       |
| 传输      | TCP      | TCP                      | TCP        | **UDP + QUIC**     |
| 默认连接    | 短        | Keep-Alive               | 多路复用长连接    | 多路复用               |
| 头部压缩    | 无        | 无                        | HPACK      | QPACK              |
| 应用层 HOL | 有        | 有                        | 基本消除       | 消除                 |
| 传输层 HOL | 有        | 有                        | **仍有**     | **按 stream 消除**    |
| TLS     | 外挂       | 外挂                       | 外挂 + ALPN  | QUIC 内建 TLS 1.3    |
| 本工程     | —        | SocketTest / httpsHttp11 | httpsHttp2 | httpsHttp3(Cronet) |

---

## 7. 和 OkHttp / SocketTest 的关系

```text
SocketTest.httpsRequest
  → TCP connect → TLS ALPN=http/1.1 → 手写文本 HTTP
  = 经典「HTTPS 1.1」教学路径

HttpsVersionTest.httpsHttp11
  → OkHttp 只允许 HTTP_1_1（同语义，库代写）

HttpsVersionTest.httpsHttp2
  → OkHttp ALPN h2 → Http2Connection 多路复用
  ≈ OkHttp RealConnection.connectTls 后的 h2 分支

HttpsVersionTest.httpsHttp3
  → Cronet QUIC，绕过 OkHttp 连接池
```

入口：`TestLearnUtils.test()` → `socketTest.test()` + `httpsVersionTest.test()`。

Logcat 过滤：`HttpsVersionTest` / `SocketTest`。

---

## 8. 建议观察的日志

```text
[HTTP/1.1] connectEnd ... alpnProtocol=http/1.1
[HTTP/1.1] code=200 negotiated=http/1.1 ...

[HTTP/2] connectEnd ... alpnProtocol=h2
[HTTP/2] code=200 negotiated=h2 ...

[HTTP/3] UrlRequest started ...
[HTTP/3] responseStarted ... negotiated=h3-... 或 h3
[HTTP/3] onSucceeded negotiated=h3 ...
```

同一 URL 用 1.1 与 2 各打一次，对比 `negotiated=` 和耗时，最容易建立直觉。
