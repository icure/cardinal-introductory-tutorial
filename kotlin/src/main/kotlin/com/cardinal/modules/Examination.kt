package com.cardinal.modules

import com.icure.cardinal.sdk.model.DecryptedContact
import com.icure.cardinal.sdk.model.DecryptedDocument
import com.icure.cardinal.sdk.model.DecryptedHealthElement
import com.icure.cardinal.sdk.model.DecryptedPatient
import com.icure.cardinal.sdk.model.base.Identifier
import com.icure.cardinal.sdk.model.embed.DecryptedContent
import com.icure.cardinal.sdk.model.embed.DecryptedService
import com.icure.cardinal.sdk.model.embed.DecryptedSubContact
import com.icure.cardinal.sdk.model.embed.DocumentType
import com.icure.cardinal.sdk.model.embed.Measure
import com.icure.cardinal.sdk.model.embed.TimeSeries
import com.cardinal.utils.prettyPrint
import com.icure.cardinal.sdk.CardinalSdk
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.random.Random

suspend fun createConsultation(sdk: CardinalSdk) {
	try {
		print("Insert the id of a Patient (blank to create a new one): ")
		val patientId = readlnOrNull()
		val patient = if(patientId.isNullOrBlank()) {
			sdk.patient.createPatient(
				DecryptedPatient(
					id = UUID.randomUUID().toString(),
					firstName = "Annabelle",
					lastName = "Hall",
				).let { sdk.patient.withEncryptionMetadata(it) }
			)
		} else {
			sdk.patient.getPatient(patientId)
		}

		val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
		print("Examination description: ")
		val description = readln()
		val contact = DecryptedContact(
			id = UUID.randomUUID().toString(),
			descr = description,
			openingDate = LocalDateTime.now().format(formatter).toLong()
		)
		val contactWithMetadata = sdk.contact.withEncryptionMetadata(contact, patient)
		val createdContact = sdk.contact.createContact(contactWithMetadata)
		prettyPrint(createdContact)

		print("Register blood pressure? [y/N]: ")
		val hasBloodPressure = readln().trim().lowercase() == "y"
		val contactWithBloodPressure = if (hasBloodPressure) {
			println("Measuring blood pressure...")
			val bloodPressureService = DecryptedService(
				id = UUID.randomUUID().toString(),
				label = "Blood pressure",
				identifier = listOf(Identifier(system = "icure", value = "bloodPressure")),
				content = mapOf(
					"en" to DecryptedContent(
						measureValue = Measure(
							value = Random.nextInt(80, 120).toDouble(),
							unit = "mmHg"
						)
					)
				)
			)
			sdk.contact.modifyContact(
				createdContact.copy(
					services = setOf(bloodPressureService)
				)
			)
		} else createdContact

		print("Register heart rate? [y/N]: ")
		val hasHeartRate = readln().trim().lowercase() == "y"
		val contactWithECG = if(hasHeartRate) {
			println("Measuring heart rate...")
			val ecgSignal = List(10) { Random.nextInt(0, 100) / 100.0 }
			val heartRateService = DecryptedService(
				id = UUID.randomUUID().toString(),
				identifier = listOf(Identifier(system = "icure", value = "ecg")),
				label = "Heart rate",
				content = mapOf(
					"en" to DecryptedContent(
						timeSeries = TimeSeries(
							samples = listOf(ecgSignal)
						)
					)
				)
			)
			sdk.contact.modifyContact(
				contactWithBloodPressure.copy(
					services = contactWithBloodPressure.services + heartRateService
				)
			)
		} else contactWithBloodPressure

		print("Register x-ray? [y/N]: ")
		val hasXRay = readln().trim().lowercase() == "y"
		val contactWithImage = if(hasXRay) {
			println("Generating X Ray image...")
			val document = DecryptedDocument(
				id = UUID.randomUUID().toString(),
				documentType = DocumentType.Labresult
			)
			val createdDocument = sdk.document.createDocument(
				sdk.document.withEncryptionMetadata(document, null)
			)
			val xRayImage = Random.nextBytes(100)
			val documentWithAttachment = sdk.document.encryptAndSetMainAttachment(
				document = createdDocument,
				utis = listOf("public.tiff"),
				attachment = xRayImage
			)
			val xRayService = DecryptedService(
				id = UUID.randomUUID().toString(),
				label = "X-Ray image",
				identifier = listOf(Identifier(system = "icure", value = "xRay")),
				content = mapOf(
					"en" to DecryptedContent(
						documentId = documentWithAttachment.id
					)
				)
			)
			sdk.contact.modifyContact(
				contactWithECG.copy(
					services = contactWithECG.services + xRayService,
				)
			)
		} else contactWithECG

		print("What is the diagnosis?: ")
		val diagnosis = readln().trim()
		val healthElement = DecryptedHealthElement(
			id = UUID.randomUUID().toString(),
			descr = diagnosis
		)
		val createdDiagnosis = sdk.healthElement.createHealthElement(
			sdk.healthElement.withEncryptionMetadata(healthElement, patient)
		)
		val contactWithDiagnosis = sdk.contact.modifyContact(
			contactWithImage.copy(
				subContacts = setOf(DecryptedSubContact(
					descr = "Diagnosis",
					healthElementId = createdDiagnosis.id
				))
			)
		)

		print("Close contact? [y/N]: ")
		val close = readln().trim() == "y"

		val finalContact = if (close) {
			sdk.contact.modifyContact(contactWithDiagnosis.copy(
				closingDate = LocalDateTime.now().format(formatter).toLong()
			))
		} else contactWithImage

		prettyPrint(finalContact)

	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
	}
}