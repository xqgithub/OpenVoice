/**
 * MIT License
 * Copyright (c) 2022 Jemshit Iskenderov
 */
package com.shannon.android.lib.web3.impl

import kotlinx.coroutines.Dispatchers
import walletconnect.core.util.DispatcherProvider

class AndroidDispatcherProvider : DispatcherProvider {

    override fun io() = Dispatchers.IO

    override fun computation() = Dispatchers.Default

    override fun ui() = Dispatchers.Main

}