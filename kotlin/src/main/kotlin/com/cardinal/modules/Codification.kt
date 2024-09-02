package com.icure.modules

import com.icure.sdk.IcureSdk
import com.icure.sdk.filters.ServiceFilters
import com.icure.sdk.model.Code
import com.icure.sdk.model.DecryptedContact
import com.icure.sdk.model.DecryptedPatient
import com.icure.sdk.model.base.CodeStub
import com.icure.sdk.model.embed.DecryptedContent
import com.icure.sdk.model.embed.DecryptedService
import com.icure.sdk.model.embed.Measure
import com.icure.utils.prettyPrint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

suspend fun manageCodification(sdk: IcureSdk) {
	try {
		val existing = sdk.code.getCodes(
			listOf("INTERNAL|ANALYSIS|1", "SNOMED|45007003|1", "SNOMED|38341003|1", "SNOMED|2004005|1")
		)

		val snomedCodes = if(existing.isNotEmpty()) {
			val internalCode = existing.first()
			prettyPrint(internalCode)
			val snomedCodes = existing.subList(1, existing.size)
			snomedCodes
		} else {
			val internalCode = sdk.code.createCode(Code(
				id = "INTERNAL|ANALYSIS|1",
				type = "INTERNAL",
				code = "ANALYSIS",
				version = "1",
				label = mapOf("en" to "Internal analysis code")
			))
			prettyPrint(internalCode)
			val snomedCodes = sdk.code.createCodes(listOf(
				Code(
					id = "SNOMED|45007003|1",
					type = "SNOMED",
					code = "45007003",
					version = "1",
					label = mapOf("en" to "Low blood pressure")
				),
				Code(
					id = "SNOMED|38341003|1",
					type = "SNOMED",
					code = "38341003",
					version = "1",
					label = mapOf("en" to "High blood pressure")
				),
				Code(
					id = "SNOMED|2004005|1",
					type = "SNOMED",
					code = "2004005",
					version = "1",
					label = mapOf("en" to "Normal blood pressure")
				)
			))
			snomedCodes
		}

		val selectedCode = snomedCodes.firstNotNullOf { code ->
			prettyPrint(code)
			print("Use this code? [y/N]: ")
			val use = readln().trim().lowercase() == "y"
			if (use) {
				code
			} else null
		}

		val patient = sdk.patient.createPatient(
			DecryptedPatient(
				id = UUID.randomUUID().toString(),
				firstName = "Annabelle",
				lastName = "Hall",
			).let { sdk.patient.withEncryptionMetadata(it) }
		)

		val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
		val contact = DecryptedContact(
			id = UUID.randomUUID().toString(),
			descr = "Blood pressure measurement",
			openingDate = LocalDateTime.now().format(formatter).toLong(),
			services = setOf(
				DecryptedService(
					id = UUID.randomUUID().toString(),
					label = "Blood pressure",
					content = mapOf(
						"en" to DecryptedContent(
							measureValue = Measure(
								value = Random.nextInt(80, 120).toDouble(),
								unit = "mmHg"
							)
						)
					),
					tags = setOf(
						CodeStub(
							id = selectedCode.id,
							type = selectedCode.type,
							code = selectedCode.code,
							version = selectedCode.version
						)
					)
				)
			)
		)
		val createdContact = sdk.contact.createContact(
			sdk.contact.withEncryptionMetadata(contact, patient)
		)
		prettyPrint(createdContact)
		prettyPrint(createdContact.services.first())

		val serviceIterator = sdk.contact.filterServicesBy(
			ServiceFilters.byTagAndValueDateForSelf(
				tagType = selectedCode.type!!,
				tagCode = selectedCode.code
			)
		)

		println("Result of searching by code: ${selectedCode.id}")
		while (serviceIterator.hasNext()) {
			val service = serviceIterator.next(1).first()
			prettyPrint(service)
		}
	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
	}
}