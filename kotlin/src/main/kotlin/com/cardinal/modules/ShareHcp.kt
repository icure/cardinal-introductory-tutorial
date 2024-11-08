package com.cardinal.modules

import com.cardinal.sdk.createSdk
import com.icure.cardinal.sdk.model.DecryptedDocument
import com.icure.cardinal.sdk.model.embed.AccessLevel
import com.cardinal.utils.prettyPrint
import com.icure.cardinal.sdk.CardinalSdk
import java.util.*

suspend fun shareWithHcp(sdk: CardinalSdk) {
	try {
		print("Login of the other hcp: ")
		val username = readln().trim()
		print("Insert the password for this hcp: ")
		val otherPassword = readln()
		val otherSdk = createSdk(username, otherPassword)
		val otherHcp = otherSdk.healthcareParty.getCurrentHealthcareParty()

		val oldDocument = sdk.document.createDocument(
			DecryptedDocument(
				id = UUID.randomUUID().toString(),
				name = "An important document"
			).let {
				sdk.document.withEncryptionMetadata(it, null)
			}
		)
		prettyPrint(oldDocument)

		try {
			otherSdk.document.getDocument(oldDocument.id)
		} catch (e: Exception) {
			println("This means I am not authorized to read the document -> ${e.message}")
		}

		sdk.document.shareWith(
			delegateId = otherHcp.id,
			document = oldDocument
		)

		val oldDocumentOtherHcp = otherSdk.document.getDocument(oldDocument.id)
		prettyPrint(oldDocumentOtherHcp)

		val newDocument = DecryptedDocument(
			id = UUID.randomUUID().toString(),
			name = "Another important document"
		)
		val newDocumentWithMetadata = sdk.document.withEncryptionMetadata(
			newDocument,
			null,
			delegates = mapOf(otherHcp.id to AccessLevel.Read)
		)
		val createdNewDocument = sdk.document.createDocument(newDocumentWithMetadata)

		val newDocumentOtherHcp = otherSdk.document.getDocument(createdNewDocument.id)
		prettyPrint(newDocumentOtherHcp)

	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
	}
}