package com.cardinal.modules

import com.cardinal.sdk.createSdk
import com.icure.cardinal.sdk.crypto.entities.PatientShareOptions
import com.icure.cardinal.sdk.crypto.entities.ShareMetadataBehaviour
import com.icure.cardinal.sdk.model.DecryptedHealthElement
import com.icure.cardinal.sdk.model.DecryptedPatient
import com.icure.cardinal.sdk.model.User
import com.icure.cardinal.sdk.model.embed.AccessLevel
import com.icure.cardinal.sdk.model.requests.RequestedPermission
import com.cardinal.utils.prettyPrint
import com.icure.cardinal.sdk.CardinalSdk
import java.util.*

suspend fun shareWithPatient(sdk: CardinalSdk) {
	try {
		val newPatient = DecryptedPatient(
			id = UUID.randomUUID().toString(),
			firstName = "Edmond",
			lastName = "Dantes",
		)
		val patientWithMetadata = sdk.patient.withEncryptionMetadata(newPatient)
		val createdPatient = sdk.patient.createPatient(patientWithMetadata)
		val login = "edmond.dantes.${UUID.randomUUID().toString().substring(0, 6)}@icure.com"
		val patientUser = User(
			id = UUID.randomUUID().toString(),
			patientId = createdPatient.id,
			login = login,
			email = login
		)
		val createdUser = sdk.user.createUser(patientUser)
		val loginToken = sdk.user.getToken(createdUser.id, "login")

		createSdk(login, loginToken)

		val patientSecretIds = sdk.patient.getSecretIdsOf(createdPatient)
		val patientShareResult = sdk.patient.shareWith(
			delegateId = createdPatient.id,
			patient = createdPatient,
			options = PatientShareOptions(
				shareSecretIds = patientSecretIds,
				shareEncryptionKey = ShareMetadataBehaviour.IfAvailable,
				requestedPermissions = RequestedPermission.MaxWrite
			)
		)

		if (patientShareResult.isSuccess) {
			println("Successfully shared patient")
		}

		val patient = patientShareResult.updatedEntityOrThrow()

		val patientSdk = createSdk(login, loginToken)

		prettyPrint(patientSdk.patient.getPatient(patient.id))

		val healthElement = DecryptedHealthElement(
			id = UUID.randomUUID().toString(),
			descr = "This is some medical context"
		)
		val healthElementWithMetadata = sdk.healthElement.withEncryptionMetadata(healthElement, patient)
		val createdHealthElement = sdk.healthElement.createHealthElement(healthElementWithMetadata)

		try {
			patientSdk.healthElement.getHealthElement(createdHealthElement.id)
		} catch (e: Exception) {
			println("This means the patient cannot get this health element -> ${e.message}")
		}

		val result = sdk.healthElement.shareWith(
			delegateId = patient.id,
			healthElement = createdHealthElement
		)

		if(result.isSuccess) {
			println("Successfully shared with patient id ${patient.id}")
		}

		prettyPrint(patientSdk.healthElement.getHealthElement(createdHealthElement.id))

		val newHealthElement = DecryptedHealthElement(
			id = UUID.randomUUID().toString(),
			descr = "This is some other medical context"
		)
		val newHealthElementWithMetadata = sdk.healthElement.withEncryptionMetadata(
			newHealthElement,
			patient,
			delegates = mapOf(patient.id to AccessLevel.Write)
		)
		val newCreatedHealthElement = sdk.healthElement.createHealthElement(newHealthElementWithMetadata)

		val retrievedHealthElement = patientSdk.healthElement.getHealthElement(newCreatedHealthElement.id)
		prettyPrint(retrievedHealthElement)

	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
	}
}