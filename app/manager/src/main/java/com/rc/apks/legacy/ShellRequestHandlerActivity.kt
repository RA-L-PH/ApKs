package com.rc.apks.legacy

import android.os.Bundle
import android.widget.Toast
import com.rc.apks.app.AppActivity
import com.rc.apks.shell.ShellBinderRequestHandler

class ShellRequestHandlerActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ShellBinderRequestHandler.handleRequest(this, intent)
        finish()
    }
}
