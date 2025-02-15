package com.codebaron.rider.domain.internt_config

interface ConnectivityChecker {
    suspend fun getConnectivityState(): ConnectivityState
}