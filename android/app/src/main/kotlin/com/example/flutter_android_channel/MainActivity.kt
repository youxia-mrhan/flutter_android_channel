package com.example.flutter_android_channel

import com.example.flutter_android_channel.channel.TestBilateralEventChannel
import com.example.flutter_android_channel.channel.TestByteBasicMessageChannel
import com.example.flutter_android_channel.channel.TestJsonBasicMessageChannel
import com.example.flutter_android_channel.channel.TestMethodChannel
import com.example.flutter_android_channel.channel.TestMixUseChannel
import com.example.flutter_android_channel.channel.TestSingleEventChannel
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {

    private lateinit var testJsonBasicMessageChannel: TestJsonBasicMessageChannel
    // private lateinit var testByteBasicMessageChannel: TestByteBasicMessageChannel
    // private lateinit var testMethodChannel: TestMethodChannel
    // private lateinit var testSingleEventChannel : TestSingleEventChannel
    // private lateinit var testBilateralEventChannel: TestBilateralEventChannel
    // private lateinit var testMixUseChannel: TestMixUseChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        testJsonBasicMessageChannel = TestJsonBasicMessageChannel(flutterEngine.dartExecutor.binaryMessenger)
        // testByteBasicMessageChannel = TestByteBasicMessageChannel(flutterEngine.dartExecutor.binaryMessenger,this)
        // testMethodChannel = TestMethodChannel(flutterEngine.dartExecutor.binaryMessenger)
        // testSingleEventChannel = TestSingleEventChannel(flutterEngine.dartExecutor.binaryMessenger)
        // testBilateralEventChannel = TestBilateralEventChannel(flutterEngine.dartExecutor.binaryMessenger)
        // testMixUseChannel = TestMixUseChannel(flutterEngine.dartExecutor.binaryMessenger)
    }

}
