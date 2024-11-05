import {AccessLevel, CardinalSdk, DecryptedDocument} from "@icure/cardinal-sdk";
import {readLn} from "../utils/read.mjs";
import {createSdk} from "../create_sdk.mjs";
import {v4 as uuid} from 'uuid';
import {prettyPrintDocument} from "../utils/print.mjs";

export async function shareWithHcp(sdk: CardinalSdk) {
	try {
		const username = (await readLn("Login of the other hcp: ")).trim()
		const otherPassword = await readLn("Insert the password for this hcp: ")
		const otherSdk = await createSdk(username, otherPassword)
		const otherHcp = await otherSdk.healthcareParty.getCurrentHealthcareParty()

		const oldDocument = await sdk.document.createDocument(
			await sdk.document.withEncryptionMetadata(new DecryptedDocument({
					id: uuid(),
					name: "An important document"
				}),
			null
			)
		)
		prettyPrintDocument(oldDocument)

		try {
			await otherSdk.document.getDocument(oldDocument.id)
		} catch (e) {
			console.error("This means I am not authorized to read the document -> ", e)
		}

		await sdk.document.shareWith(
			otherHcp.id,
			oldDocument
		)

		const oldDocumentOtherHcp = await otherSdk.document.getDocument(oldDocument.id)
		prettyPrintDocument(oldDocumentOtherHcp)

		const newDocument = new DecryptedDocument({
			id: uuid(),
			name: "Another important document"
		})
		const newDocumentWithMetadata = await sdk.document.withEncryptionMetadata(
			newDocument,
			null,
			{ delegates: { [otherHcp.id]:  AccessLevel.Read } }
		)
		const createdNewDocument = await sdk.document.createDocument(newDocumentWithMetadata)

		const newDocumentOtherHcp = await otherSdk.document.getDocument(createdNewDocument.id)
		prettyPrintDocument(newDocumentOtherHcp)
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
	}
}