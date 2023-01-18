package com.knightboost.turbo.proxy.plugin

import com.knightboost.turbo.proxy.PthreadTimer

class PluginPThreadTimer : PthreadTimer {
    constructor() {}
    constructor(name: String) : super(name) {}
    constructor(name: String, isDaemon: Boolean) : super(name, isDaemon) {}
    constructor(isDaemon: Boolean) : super(isDaemon) {}
}