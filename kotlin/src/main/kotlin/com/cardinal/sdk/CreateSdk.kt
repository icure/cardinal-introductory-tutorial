package com.cardinal.sdk

import com.icure.cardinal.sdk.CardinalSdk
import com.icure.cardinal.sdk.auth.UsernamePassword
import com.icure.cardinal.sdk.options.AuthenticationMethod
import com.icure.cardinal.sdk.storage.impl.FileStorageFacade

private const val CARDINAL_URL = "https://api.icure.cloud"


suspend fun createSdk(username: String, password: String): CardinalSdk = CardinalSdk.initialize(
	applicationId = null,
	baseUrl = CARDINAL_URL,
	authenticationMethod = AuthenticationMethod.UsingCredentials(
		UsernamePassword(username, password)
	),
	baseStorage = FileStorageFacade("./scratch/storage")
)
