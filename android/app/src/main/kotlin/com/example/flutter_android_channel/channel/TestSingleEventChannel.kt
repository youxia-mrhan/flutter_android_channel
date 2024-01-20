package com.example.flutter_android_channel.channel

import android.os.CountDownTimer
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel

/**
 * EventChannel
 *
 * 使用方式：单向通信
 */
class TestSingleEventChannel(messenger: BinaryMessenger) : EventChannel.StreamHandler {

    private lateinit var mChannel: EventChannel

    companion object {
        // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
        private const val CHANNEL_NAME = "flutter.mix.android/single_event_channel"
    }

    init {
        initChannel(messenger)
    }

    /**
     * 初始化消息通道
     */
    private fun initChannel(messenger: BinaryMessenger) {
        // 创建 Android端和Flutter端的，相互通信的通道
        // 通道名称，两端必须一致
        mChannel = EventChannel(messenger, CHANNEL_NAME)

        // 监听来自 Flutter端 的消息通道
        // Flutter端调用了函数，这个handler函数就会被触发
        mChannel.setStreamHandler(this)
    }

    private var count: Int = 10

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {

        // 一共10秒，每隔1秒执行一次
        object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 还剩下多少秒，依次为2000、1000、0
                if (millisUntilFinished == 0L) {
                    cancel()
                }
                events?.success("${count--}")
            }

            override fun onFinish() { // 结束后的操作
                events?.success("${count--}")
            }
        }.start()

        // 给Flutter端返回消息
        // events?.endOfStream()
        // events?.success(event: Any?)
        // events?.error(errorCode: String?, errorMessage: String?, errorDetails: Any?)
        // events?.endOfStream() // 流结束
    }

    override fun onCancel(arguments: Any?) {

    }

    /**
     * 解除绑定
     */
    fun closeChannel() {
        mChannel.setStreamHandler(null)
    }

}

