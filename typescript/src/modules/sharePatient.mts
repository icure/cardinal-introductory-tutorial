import {
	AccessLevel,
	CardinalSdk, DecryptedHealthElement,
	DecryptedPatient,
	PatientShareOptions,
	RequestedPermission,
	ShareMetadataBehaviour, SimpleShareResult,
	User
} from "@icure/cardinal-sdk";
import {v4 as uuid} from 'uuid';
import {createSdk} from "../create_sdk.mjs";
import {prettyPrintHealthElement, prettyPrintPatient} from "../utils/print.mjs";

export async function shareWithPatient(sdk: CardinalSdk) {
	try {
		const newPatient = new DecryptedPatient({
			id: uuid(),
			firstName: "Edmond",
			lastName: "Dantes",
		})
		const patientWithMetadata = await sdk.patient.withEncryptionMetadata(newPatient)
		const createdPatient = await sdk.patient.createPatient(patientWithMetadata)
		const login = `edmond.dantes.${uuid().substring(0, 6)}@icure.com`
		const patientUser = new User({
			id: uuid(),
			patientId: createdPatient.id,
			login: login,
			email: login
		})
		const createdUser = await sdk.user.createUser(patientUser)
		const loginToken = await sdk.user.getToken(createdUser.id, "login")

		await createSdk(login, loginToken)

		const patientSecretIds = await sdk.patient.getSecretIdsOf(createdPatient)
		const patientShareResult = await sdk.patient.shareWith(
			createdPatient.id,
			createdPatient,
			new PatientShareOptions({
				shareSecretIds: patientSecretIds,
				shareEncryptionKey: ShareMetadataBehaviour.IfAvailable,
				requestedPermissions: RequestedPermission.MaxWrite
			})
		)

		if (patientShareResult instanceof SimpleShareResult.Success) {
			console.log("Successfully shared patient")
		}

		const patient = (patientShareResult as SimpleShareResult.Success<DecryptedPatient>).updatedEntity

		const patientSdk = await createSdk(login, loginToken)

		prettyPrintPatient(await patientSdk.patient.getPatient(patient.id))

		const healthElement = new DecryptedHealthElement({
			id: uuid(),
			descr: "This is some medical context"
		})
		const healthElementWithMetadata = await sdk.healthElement.withEncryptionMetadata(healthElement, patient)
		const createdHealthElement = await sdk.healthElement.createHealthElement(healthElementWithMetadata)

		try {
			await patientSdk.healthElement.getHealthElement(createdHealthElement.id)
		} catch (e) {
			console.error("This means the patient cannot get this health element", e)
		}

		const result = await sdk.healthElement.shareWith(
			patient.id,
			createdHealthElement
		)

		if(result instanceof SimpleShareResult.Success) {
			console.log(`Successfully shared with patient id ${patient.id}`)
		}

		prettyPrintHealthElement(await patientSdk.healthElement.getHealthElement(createdHealthElement.id))

		const newHealthElement = new DecryptedHealthElement({
			id: uuid(),
			descr: "This is some other medical context"
		})
		const newHealthElementWithMetadata = await sdk.healthElement.withEncryptionMetadata(
			newHealthElement,
			patient,
			{ delegates: { [patient.id]: AccessLevel.Write } }
		)
		const newCreatedHealthElement = await sdk.healthElement.createHealthElement(newHealthElementWithMetadata)

		const retrievedHealthElement = await patientSdk.healthElement.getHealthElement(newCreatedHealthElement.id)
		prettyPrintHealthElement(retrievedHealthElement)
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
	}
}