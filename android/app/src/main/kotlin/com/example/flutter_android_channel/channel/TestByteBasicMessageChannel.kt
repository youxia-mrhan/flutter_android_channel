package com.example.flutter_android_channel.channel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.common.io.ByteStreams
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryCodec
import io.flutter.plugin.common.BinaryMessenger
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * BasicMessageChannel
 *
 * 使用 ByteBuffer类型，对应 Flutter端的 ByteData类型
 */
class TestByteBasicMessageChannel(messenger: BinaryMessenger, private val context: Context) :
    BasicMessageChannel.MessageHandler<ByteBuffer> {

    private lateinit var mChannel: BasicMessageChannel<ByteBuffer>

    companion object {
        // Android原生View 在Flutter引擎上注册的唯一标识，在Flutter端使用时必须一样
        private const val CHANNEL_NAME = "flutter.mix.android/byte_basic_message_channel"
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
        mChannel = BasicMessageChannel(messenger, CHANNEL_NAME, BinaryCodec.INSTANCE)

        // 监听来自 Flutter端 的消息通道
        // Flutter端调用了函数，这个handler函数就会被触发
        mChannel.setMessageHandler(this)
    }

    // ========================== PUT 操作 ==========================

    /**
     * 监听来自 Flutter端 的消息通道
     *
     * byteBuffer： Android端 接收到 Flutter端 发来的 数据对象
     * reply：Android端 给 Flutter端 执行回调的接口对象
     */
    override fun onMessage(byteBuffer: ByteBuffer?, reply: BasicMessageChannel.Reply<ByteBuffer>) {
        // 回调结果对象
        // 获取Flutter端传过来的数据
        Log.d("TAG", "byteBuffer：$byteBuffer")

        // 回调接口对象，里面只有一个回调方法
        // reply.reply(@Nullable T reply)
        byteBuffer?.order(ByteOrder.nativeOrder())
        val direct = ByteBuffer.allocateDirect(byteBuffer!!.capacity())
        direct.put(byteBuffer)
        reply.reply(direct) // 返回给Flutter端

        Handler(Looper.getMainLooper()).postDelayed({
            androidSendFlutterData()
        }, 5000)
    }

    /**
     * Android端 向 Flutter端 发送数据
     */
    private fun androidSendFlutterData() {
        // 读取assert目录下的音频文件
        val fileInputStream = context.assets.open("music/di_jia_b.mp3")
        val targetArray = ByteStreams.toByteArray(fileInputStream)
        val byteBuffer = ByteBuffer.wrap(targetArray)

        byteBuffer.order(ByteOrder.nativeOrder())
        val direct = ByteBuffer.allocateDirect(byteBuffer.capacity())
        direct.put(byteBuffer)

        mChannel.send(direct,object : BasicMessageChannel.Reply<ByteBuffer> {

            override fun reply(reply: ByteBuffer?) {
                Log.d("TAG", "reply：$reply")
            }

        })
    }

    // ========================== GET 操作 ==========================

//    /**
//     * 监听来自 Flutter端 的消息通道
//     *
//     * byteBuffer： Android端 接收到 Flutter端 发来的 数据对象
//     * reply：Android端 给 Flutter端 执行回调的接口对象
//     */
//    override fun onMessage(byteBuffer: ByteBuffer?, reply: BasicMessageChannel.Reply<ByteBuffer>) {
//        // 读取assert目录下的音频文件
//        val fileInputStream = context.assets.open("music/di_jia_b.mp3")
//        val targetArray = ByteStreams.toByteArray(fileInputStream)
//        val byteBuffer = ByteBuffer.wrap(targetArray)
//
//        byteBuffer.order(ByteOrder.nativeOrder())
//        val direct = ByteBuffer.allocateDirect(byteBuffer.capacity())
//        direct.put(byteBuffer)
//        reply.reply(direct) // 返回给Flutter端
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            androidGetFlutterData()
//        }, 5000)
//    }
//
//    /**
//     * Android端 获取 Flutter端 数据
//     */
//    private fun androidGetFlutterData() {
//        mChannel.send(null,object : BasicMessageChannel.Reply<ByteBuffer> {
//
//            override fun reply(reply: ByteBuffer?) {
//                // 获取Flutter端传过来的数据
//                Log.d("TAG", "reply：$reply")
//            }
//
//        })
//    }

    /**
     * 解除绑定
     */
    fun closeChannel() {
        mChannel.setMessageHandler(null)
    }

}
