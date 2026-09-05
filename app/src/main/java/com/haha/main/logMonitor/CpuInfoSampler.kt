package com.haha.main.logMonitor

import com.haha.log.DOFLogUtil
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 * @auther: haha
 * @Date:   2025/11/27
 * @Detail:
 */
class CpuInfoSampler: BaseSimpler() {
    private var mPid = -1
    private val mCpuInfoList = ArrayList<CpuInfo>()
    private var mUserPre = 0L
    private var mSystemPre = 0L
    private var mIdlePre = 0L
    private var mIoWaitPre = 0L
    private var mTotalPre = 0L
    private var mAppCpuTimePre = 0L

    override fun doSample() {
        DOFLogUtil.d(TAG, "doSample")
        dumpCpuInfo()
    }

    private fun dumpCpuInfo() {
        var cpuReader: BufferedReader? = null
        var pidReader: BufferedReader? = null
        try {
            cpuReader = BufferedReader(InputStreamReader(FileInputStream("/proc/stat")), 1024)
            val cpuRate = cpuReader.readLine() ?: ""
            if (mPid < 0) {
                mPid = android.os.Process.myPid()
            }

            pidReader = BufferedReader(InputStreamReader(FileInputStream("/proc/${mPid}/stat")), 1024)
            val pidCpuRate = pidReader.readLine() ?: ""
            parseCpuRate(cpuRate, pidCpuRate)
        } catch (ex: Throwable) {
            DOFLogUtil.e(TAG, "doSample: $ex")
        } finally {
            try {
                cpuReader?.close()
                pidReader?.close()
            } catch (e: IOException) {
                DOFLogUtil.e(TAG, "doSample: $e")
            }
        }
    }

    private fun parseCpuRate(cpuRate: String, pidCpuRate: String) {
        val cpuInfoArray = cpuRate.split(" ")
        if (cpuInfoArray.size < 9) return
        val user_time = cpuInfoArray[2].toLong()
        val nice_time = cpuInfoArray[3].toLong()
        val system_time = cpuInfoArray[4].toLong()
        val idle_time = cpuInfoArray[5].toLong()
        val ioWait_time = cpuInfoArray[6].toLong()
        val total_time = user_time + nice_time + system_time + idle_time + ioWait_time +
                cpuInfoArray[7].toLong() + cpuInfoArray[8].toLong()

        val pidCpuInfos = pidCpuRate.split(" ")
        if (pidCpuInfos.size < 17) return
        val appCpu_time = pidCpuInfos[13].toLong() + pidCpuInfos[14].toLong() +
                pidCpuInfos[15].toLong() + pidCpuInfos[16].toLong()
        if (mAppCpuTimePre > 0) {
            val mCi = CpuInfo(System.currentTimeMillis())
            val idleTime = idle_time - mIdlePre
            val totalTime = total_time - mTotalPre
            mCi.mCpuRate = (totalTime - idleTime) * 100L / totalTime
            mCi.mAppRate = (appCpu_time - mAppCpuTimePre) * 100L / totalTime
            mCi.mSystemRate = (system_time - mSystemPre) * 100L / totalTime
            mCi.mUserRate = (user_time - mUserPre) * 100L / totalTime
            mCi.mIoWait = (ioWait_time - mIoWaitPre) * 100L / totalTime
            synchronized(mCpuInfoList) {
                mCpuInfoList.add(mCi)
                DOFLogUtil.d(TAG, "cpu info: $mCi")
            }
        }
        mUserPre = user_time
        mSystemPre = system_time
        mIdlePre = idle_time
        mIoWaitPre = ioWait_time
        mTotalPre = total_time
        mAppCpuTimePre = appCpu_time
    }

    override fun start() {
        super.start()
        mUserPre = 0
        mSystemPre = 0
        mIdlePre = 0
        mIoWaitPre = 0
        mTotalPre = 0
        mAppCpuTimePre = 0
        mCpuInfoList.clear()
    }

    fun getStatCpuInfo(): ArrayList<CpuInfo> = mCpuInfoList
}

data class CpuInfo(
    var mId: Long = 0L, // 一个CPU信息的ID
    var mCpuRate: Long = 0L, // 总的CPU使用率
    var mAppRate: Long = 0L, // 当前app CPU使用率
    var mUserRate: Long = 0L, // 用户进程
    var mSystemRate: Long = 0L, // 系统进程
    var mIoWait: Long = 0L // 等待时间
)

/**
 * 1) 采样两个时间的CPU快照，分别记作c1、c2，其中CPU快照的数据结构是一个九元数组（user、nice、system、idle、
 *    iowait、irq、softirq、strealstolen、guest） 把c1和c2中所有的时间片求和得到t1、t2;
 * 2) 得到总的CPU时间片totalTime = t2 - t1、空闲时间idleTime = c2.idle - c1.idle、用户进程时间c1.user和
 *    c2.user、系统进程时间c1.system和c2.system、IO等待时间c1.ioWait和c2.ioWait；
 * 3) 获取进程的总CPU时间 appCpu_time = utime + stime + cutime + cstime （该值包括其所有线程的CPU时间），
 *    同样采样两个时间的进程快照，进而计算得到 appCpu_time和mAppCpuTimePre。
 */

