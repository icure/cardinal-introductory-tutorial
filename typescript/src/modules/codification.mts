import {
	CardinalSdk,
	Code,
	CodeFilters, CodeStub,
	DecryptedContact, DecryptedContent,
	DecryptedPatient,
	DecryptedService, Measure, ServiceFilters
} from "@icure/cardinal-sdk";
import {prettyPrintCode, prettyPrintContact, prettyPrintService} from "../utils/print.mjs";
import {readLn} from "../utils/read.mjs";
import { v4 as uuid } from 'uuid';
import {currentFuzzyDate} from "../utils/date.mjs";
import {random} from "../utils/random.mjs";

export async function manageCodifications(sdk: CardinalSdk) {
	try {
		const existing = await sdk.code.getCodes(
			["INTERNAL|ANALYSIS|1", "SNOMED|45007003|1", "SNOMED|38341003|1", "SNOMED|2004005|1"]
		)

		if(existing.length === 0) {
			const internalCode = await sdk.code.createCode(new Code({
				id: "INTERNAL|ANALYSIS|1",
				type: "INTERNAL",
				code: "ANALYSIS",
				version: "1",
				label: {"en": "Internal analysis code"}
			}))
			prettyPrintCode(internalCode)
			await sdk.code.createCodes([
				new Code({
					id: "SNOMED|45007003|1",
					type: "SNOMED",
					code: "45007003",
					version: "1",
					label: {"en": "Low blood pressure"}
				}),
				new Code({
					id: "SNOMED|38341003|1",
					type: "SNOMED",
					code: "38341003",
					version: "1",
					label: {"en": "High blood pressure"}
				}),
				new Code({
					id: "SNOMED|2004005|1",
					type: "SNOMED",
					code: "2004005",
					version: "1",
					label: {"en": "Normal blood pressure"}
				})
			])
		}

		const codeIterator = await sdk.code.filterCodesBy(
			CodeFilters.byLanguageTypeLabelRegion(
				"en",
				"SNOMED",
				{ label: "blood" }
			)
		)

		let selectedCode: Code | null = null
		while ((await codeIterator.hasNext()) && selectedCode == null) {
			const code = (await codeIterator.next(1))[0]
			prettyPrintCode(code)
			const use = (await readLn("Use this code? [y/N]: ")).trim().toLowerCase() === "y"
			if (use) {
				selectedCode = code
			}
		}

		if (selectedCode == null) {
			console.log("No code was selected")
			return
		}

		const patient = await sdk.patient.createPatient(
			await sdk.patient.withEncryptionMetadata(
				new DecryptedPatient({
					id: uuid(),
					firstName: "Annabelle",
					lastName: "Hall",
				})
			)
	)

		const contact = new DecryptedContact({
			id: uuid(),
			descr: "Blood pressure measurement",
			openingDate: currentFuzzyDate(),
			services: [
				new DecryptedService({
					id: uuid(),
					label: "Blood pressure",
					content: {
						"en": new DecryptedContent({
							measureValue: new Measure({
								value: random(80, 120),
								unit: "mmHg"
							})
						})
					}
				})
			],
			tags: [
				new CodeStub({
					id: selectedCode.id,
					type: selectedCode.type,
					code: selectedCode.code,
					version: selectedCode.version
				})
			]
		})
		const createdContact = await sdk.contact.createContact(
			await sdk.contact.withEncryptionMetadata(contact, patient)
		)
		prettyPrintContact(createdContact)
		prettyPrintService(createdContact.services[0])

		const serviceIterator = await sdk.contact.filterServicesBy(
			ServiceFilters.byTagAndValueDateForSelf(
				selectedCode.type,
				{ tagCode: selectedCode.code }
			)
		)

		console.log(`Result of searching Services by code: ${selectedCode.id}`)
		while (await serviceIterator.hasNext()) {
			const service = (await serviceIterator.next(1))[0]
			prettyPrintService(service)
		}
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
	}
}