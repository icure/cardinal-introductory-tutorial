package com.cardinal.modules

import com.icure.sdk.IcureSdk
import com.icure.sdk.model.DecryptedPatient
import com.cardinal.utils.prettyPrint
import java.util.*

suspend fun createPatient(sdk: IcureSdk) {
	try {
		print("First name: ")
		val firstName = readln()
		print("Last name: ")
		val lastName = readln()
		val patient = DecryptedPatient(
			id = UUID.randomUUID().toString(),
			firstName = firstName,
			lastName = lastName,
		)
		val patientWithMetadata = sdk.patient.withEncryptionMetadata(patient)
		val createdPatient = sdk.patient.createPatient(patientWithMetadata)
		prettyPrint(createdPatient)

		print("Date of birth (YYYYMMDD): ")
		val dateOfBirth = readln().toInt()
		val patientWithBirth = createdPatient.copy(
			dateOfBirth = dateOfBirth
		)
		val updatedPatient = sdk.patient.modifyPatient(patientWithBirth)
		prettyPrint(updatedPatient)

		print("The retrieved patient is:")
		val retrievedPatient = sdk.patient.getPatient(updatedPatient.id)
		prettyPrint(retrievedPatient)
	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
	}
}