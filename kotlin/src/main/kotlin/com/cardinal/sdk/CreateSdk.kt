package com.cardinal.sdk

import com.icure.sdk.IcureSdk
import com.icure.sdk.auth.UsernamePassword
import com.icure.sdk.options.AuthenticationMethod
import com.icure.sdk.storage.impl.FileStorageFacade

private const val ICURE_URL = "https://api.icure.cloud"


suspend fun createSdk(username: String, password: String): IcureSdk = IcureSdk.initialize(
	applicationId = null,
	baseUrl = ICURE_URL,
	authenticationMethod = AuthenticationMethod.UsingCredentials(
		UsernamePassword(username, password)
	),
	baseStorage = FileStorageFacade("./scratch/storage")
)
