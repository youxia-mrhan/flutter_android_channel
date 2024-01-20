package com.example.flutter_android_channel.channel

import android.util.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel

/**
 * EventChannel
 *
 * 使用方式：尝试双向通信，但失败
 */
class TestBilateralEventChannel(messenger: BinaryMessenger) : EventChannel.StreamHandler {

    private lateinit var mChannel: EventChannel

    companion object {
        // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
        private const val CHANNEL_NAME = "flutter.mix.android/bilateral_event_channel"
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

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Log.d("TAG","arguments：$arguments")
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

