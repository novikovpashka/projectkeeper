package com.novikovpashka.projectkeeper.data.apicurrency

import java.io.IOException

class NoConnectivityException : IOException() {
    override val message: String
        get() = "No Internet Connection"
}