package com.icure.utils

import com.icure.sdk.model.Code
import com.icure.sdk.model.Contact
import com.icure.sdk.model.Document
import com.icure.sdk.model.HealthElement
import com.icure.sdk.model.HealthcareParty
import com.icure.sdk.model.Patient
import com.icure.sdk.model.embed.Service
import kotlin.math.max

fun printLine(line: String, maxLen: Int) {
	println("| $line${" ".repeat(maxLen-line.length+1)}|")
}

fun printDivider(maxLen: Int) {
	println("+${"-".repeat(maxLen+2)}+")
}

fun prettyPrint(patient: Patient) {
	val id = "id: ${patient.id}"
	val rev = "rev: ${patient.rev ?: "rev is missing"}"
	val name = "${patient.firstName} ${patient.lastName}"
	val dateOfBirth = "Birthday: ${patient.dateOfBirth}"
	val maxLen = listOf(id, rev, name, dateOfBirth).maxOf { it.length }
	printDivider(maxLen)
	printLine(name, maxLen)
	printDivider(maxLen)
	printLine(id, maxLen)
	printLine(rev, maxLen)
	patient.dateOfBirth?.also {
		printLine(dateOfBirth, maxLen)
	}
	printDivider(maxLen)
}

fun prettyPrint(hcp: HealthcareParty) {
	val id = "id: ${hcp.id}"
	val rev = "rev: ${hcp.rev ?: "rev is missing"}"
	val name = "${hcp.firstName} ${hcp.lastName}"
	val maxLen = listOf(id, rev, name).maxOf { it.length }
	printDivider(maxLen)
	printLine(name, maxLen)
	printDivider(maxLen)
	printLine(id, maxLen)
	printLine(rev, maxLen)
	printDivider(maxLen)
}

fun prettyPrint(document: Document) {
	val id = "id: ${document.id}"
	val rev = "rev: ${document.rev ?: "rev is missing"}"
	val name = "${document.name}"
	val maxLen = listOf(id, rev, name).maxOf { it.length }
	printDivider(maxLen)
	printLine(name, maxLen)
	printDivider(maxLen)
	printLine(id, maxLen)
	printLine(rev, maxLen)
	printDivider(maxLen)
}

fun prettyPrint(healthElement: HealthElement) {
	val id = "id: ${healthElement.id}"
	val rev = "rev: ${healthElement.rev ?: "rev is missing"}"
	val description = "${healthElement.descr}"
	val maxLen = listOf(id, rev, description).maxOf { it.length }
	printDivider(maxLen)
	printLine(description, maxLen)
	printDivider(maxLen)
	printLine(id, maxLen)
	printLine(rev, maxLen)
	printDivider(maxLen)
}

fun prettyPrint(code: Code) {
	val label = "${code.label?.get("en")} v${code.version}"
	val codeType = "Type: ${code.type}"
	val codeCode = "Code: ${code.code}"
	val maxLen = listOf(label, codeType, codeCode).maxOf { it.length }
	printDivider(maxLen)
	printLine(label, maxLen)
	printDivider(maxLen)
	printLine(codeType, maxLen)
	printLine(codeCode, maxLen)
	printDivider(maxLen)
}

fun prettyPrint(contact: Contact) {
	val id = "id: ${contact.id}"
	val rev = "rev: ${contact.rev ?: "rev is missing"}"
	val description = "${contact.descr}"
	val openingDate = "Opened: ${contact.openingDate}"
	val closingDate = "Closed: ${contact.closingDate}"
	val diagnosis = diagnosisOf(contact)
	val services = contact.services.mapNotNull { contentOf(it) }.toTypedArray()
	val maxLen = listOfNotNull(id, rev, description, openingDate, closingDate, diagnosis, *services).maxOf { it.length }
	printDivider(maxLen)
	printLine(description, maxLen)
	printDivider(maxLen)
	diagnosis?.also {
		printLine(it, maxLen)
		printDivider(maxLen)
	}
	printLine(id, maxLen)
	printLine(rev, maxLen)
	printLine(openingDate, maxLen)
	contact.closingDate?.also {
		printLine(closingDate, maxLen)
	}
	printDivider(maxLen)
	services.forEach {
		printLine(it, maxLen)
	}
	if(services.isNotEmpty()) {
		printDivider(maxLen)
	}
}

fun prettyPrint(service: Service) {
	val id = "id: ${service.id}"
	val content = contentOf(service)
	val tags = "Tags: ${service.tags.joinToString(", ") { it.id ?: "" }}"
	val maxLen = listOfNotNull(id, content, tags).maxOf { it.length }
	printDivider(maxLen)
	printLine(id, maxLen)
	content?.also {
		printLine(it, maxLen)
	}
	if (service.tags.isNotEmpty()) {
		printLine(tags, maxLen)
	}
	printDivider(maxLen)
}

fun diagnosisOf(contact: Contact): String? = contact.subContacts.firstOrNull()?.healthElementId?.let { "Diagnosis in healthElement: $it" }

fun contentOf(service: Service): String? {
	val firstContent = service.content.values.firstOrNull()
	return when {
		firstContent?.measureValue != null -> "${service.label}: ${firstContent.measureValue?.value} ${firstContent.measureValue?.unit}"
		firstContent?.timeSeries != null -> "${service.label}: ${firstContent.timeSeries?.samples?.firstOrNull()?.joinToString(" ")}"
		firstContent?.documentId != null -> "${service.label}: in Document with id ${firstContent.documentId}"
		else -> null
	}
}