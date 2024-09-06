import {CardinalSdk, DecryptedPatient} from "@icure/cardinal-sdk";
import {readLn} from "../utils/read.mjs";
import { v4 as uuid } from 'uuid';
import {prettyPrintPatient} from "../utils/print.mjs";

export async function createPatient(sdk: CardinalSdk) {
	try {
		const firstName = await readLn("First name: ")
		const lastName = await readLn("Last name: ")
		const patient = new DecryptedPatient({
			id: uuid(),
			firstName: firstName,
			lastName: lastName,
		})
		const patientWithMetadata = await sdk.patient.withEncryptionMetadata(patient)
		const createdPatient = await sdk.patient.createPatient(patientWithMetadata)
		prettyPrintPatient(createdPatient)


		const dateOfBirth = parseInt((await readLn("Date of birth (YYYYMMDD): ")).trim())
		const patientWithBirth = new DecryptedPatient({
			...createdPatient,
			dateOfBirth: dateOfBirth,
		})
		const updatedPatient = await sdk.patient.modifyPatient(patientWithBirth)
		prettyPrintPatient(updatedPatient)

		console.log("The retrieved patient is:")
		const retrievedPatient = await sdk.patient.getPatient(updatedPatient.id)
		prettyPrintPatient(retrievedPatient)
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
	}
}