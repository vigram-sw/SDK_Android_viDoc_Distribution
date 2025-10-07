package com.vigram.sdkgh1.domain.entity

data class NtripConnectionData(
    val host: String,
    val port: String,
    val login: String,
    val password: String,
    val mount: String
){
    companion object{
        fun empty(): NtripConnectionData{
            return NtripConnectionData("", "", "", "", "")
        }
    }
}