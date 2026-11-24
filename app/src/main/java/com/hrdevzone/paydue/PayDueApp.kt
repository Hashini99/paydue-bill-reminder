package com.hrdevzone.paydue

import android.app.Application
import com.hrdevzone.paydue.di.AppContainer
import com.hrdevzone.paydue.notification.NotificationHelper

class PayDueApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
    }
}
