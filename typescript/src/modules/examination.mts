import {
	CardinalSdk,
	DecryptedContact,
	DecryptedContent,
	DecryptedDocument, DecryptedHealthElement,
	DecryptedPatient,
	DecryptedService, DecryptedSubContact,
	DocumentType,
	Identifier,
	Measure,
	TimeSeries
} from "@icure/cardinal-sdk";
import {readLn} from "../utils/read.mjs";
import {v4 as uuid} from 'uuid';
import {currentFuzzyDate} from "../utils/date.mjs";
import {prettyPrintContact} from "../utils/print.mjs";
import {random} from "../utils/random.mjs";

export async function createExamination(sdk: CardinalSdk) {
	try {
		const patientId = await readLn("Insert the id of a Patient (blank to create a new one): ")
		let patient: DecryptedPatient
		if(patientId.length === 0) {
			patient = await sdk.patient.createPatient(
				await sdk.patient.withEncryptionMetadata(
					new DecryptedPatient({
						id: uuid(),
						firstName: "Annabelle",
						lastName: "Hall",
					})
				)
			)
		} else {
			patient = await sdk.patient.getPatient(patientId)
		}

		const description = await readLn("Examination description: ")
		const contact = new DecryptedContact({
			id: uuid(),
			descr: description,
			openingDate: currentFuzzyDate()
		})
		const contactWithMetadata = await sdk.contact.withEncryptionMetadata(contact, patient)
		const createdContact = await sdk.contact.createContact(contactWithMetadata)
		prettyPrintContact(createdContact)

		const hasBloodPressure = (await readLn("Register blood pressure? [y/N]: ")).trim().toLowerCase() === "y"
		let contactWithBloodPressure: DecryptedContact
		if (hasBloodPressure) {
			console.log("Measuring blood pressure...")
			const bloodPressureService = new DecryptedService({
				id: uuid(),
				label: "Blood pressure",
				identifier: [new Identifier({system: "cardinal", value: "bloodPressure"})],
				content: {
					"en": new DecryptedContent({
						measureValue: new Measure({
							value: random(80, 120),
							unit: "mmHg"
						})
					})
				}
			})

			contactWithBloodPressure = await sdk.contact.modifyContact(
				new DecryptedContact({
					...createdContact,
					services: [...createdContact.services, bloodPressureService]
				})
			)
		} else {
			contactWithBloodPressure = createdContact
		}

		const hasHeartRate = (await readLn("Register heart rate? [y/N]: ")).trim().toLowerCase() === "y"
		let contactWithECG: DecryptedContact
		if(hasHeartRate) {
			console.log("Measuring heart rate...")
			const ecgSignal = Array.from({ length: 10 }, () => random(0, 100)/100.0 )
			const heartRateService = new  DecryptedService({
				id: uuid(),
				identifier: [new Identifier({system: "cardinal", value: "ecg"})],
				label: "Heart rate",
				content: {
					"en": new DecryptedContent({
						timeSeries: new TimeSeries({
							samples: [ecgSignal]
						})
					})
				}
			})
			contactWithECG = await sdk.contact.modifyContact(
				new DecryptedContact({
					...contactWithBloodPressure,
					services: [...contactWithBloodPressure.services, heartRateService]
				})
			)
		} else {
			contactWithECG = contactWithBloodPressure
		}

		const hasXRay = (await readLn("Register x-ray? [y/N]: ")).trim().toLowerCase() === "y"
		let contactWithImage: DecryptedContact
		if(hasXRay) {
			console.log("Generating X Ray image...")
			const document = new DecryptedDocument({
				id: uuid(),
				documentType: DocumentType.Labresult
			})
			const createdDocument = await sdk.document.createDocument(
				await sdk.document.withEncryptionMetadata(document, null)
			)
			const xRayImage = new Int8Array(100)
			for (let i = 0; i < 100; i++) {
				xRayImage[i] = random(-127, 128)
			}
			const documentWithAttachment = await sdk.document.encryptAndSetMainAttachment(
				createdDocument,
				["public.tiff"],
				xRayImage
			)
			const xRayService = new DecryptedService({
				id: uuid(),
				label: "X-Ray image",
				identifier: [new Identifier({system: "cardinal", value: "xRay"})],
				content: {
					"en": new DecryptedContent({
						documentId: documentWithAttachment.id
					})
				}
			})
			contactWithImage = await sdk.contact.modifyContact(
				new DecryptedContact({
					...contactWithECG,
					services: [...contactWithECG.services, xRayService]
				})
			)
		} else {
			contactWithImage = contactWithECG
		}

		const diagnosis = await readLn("What is the diagnosis?: ")
		const healthElement = new DecryptedHealthElement({
			id: uuid(),
			descr: diagnosis
		})
		const createdDiagnosis = await sdk.healthElement.createHealthElement(
			await sdk.healthElement.withEncryptionMetadata(healthElement, patient)
		)
		const contactWithDiagnosis = await sdk.contact.modifyContact(
			new DecryptedContact({
				...contactWithImage,
				subContacts: [
					new DecryptedSubContact({
						descr: "Diagnosis",
						healthElementId: createdDiagnosis.id
					})
				]
			})
		)

		const close = (await readLn("Close contact? [y/N]: ")).trim().toLowerCase() === "y"

		let finalContact: DecryptedContact
		if (close) {
			finalContact = await sdk.contact.modifyContact(
				new DecryptedContact({
					...contactWithDiagnosis,
					closingDate: currentFuzzyDate()
				})
			)
		} else {
			finalContact = contactWithImage
		}

		prettyPrintContact(finalContact)
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
	}
}