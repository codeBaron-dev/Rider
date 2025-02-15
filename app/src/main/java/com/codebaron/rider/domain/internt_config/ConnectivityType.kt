package com.codebaron.rider.domain.internt_config

sealed class ConnectivityType {
    data object WIFI : ConnectivityType()
    data object CELLULAR : ConnectivityType()
    data object ETHERNET : ConnectivityType()
    data object NONE : ConnectivityType()
}