package com.example.myapplication

// Java-callable function to launch AudioRoom Activity
object AudioRoomLauncher{
    @JvmStatic
    fun launch(context: android.content.Context) {
        val intent = android.content.Intent(context, AudioRoom::class.java)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}