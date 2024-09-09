import {CardinalSdk, ContactFilters, Identifier, Patient, PatientFilters, ServiceFilters} from "@icure/cardinal-sdk";
import {readLn} from "../utils/read.mjs";
import {prettyPrintContact, prettyPrintPatient, prettyPrintService} from "../utils/print.mjs";

export async function searchPatientsContactsServices(sdk: CardinalSdk) {
	try {
		const nameToSearch = await readLn("Enter a name: ")
		const patientIterator = await sdk.patient.filterPatientsBy(
			PatientFilters.byNameForSelf(nameToSearch)
		)

		let patient: Patient | null = null
		while ((await patientIterator.hasNext()) && patient == null) {
			const p = (await patientIterator.next(1))[0]
			prettyPrintPatient(p)
			const use = (await readLn("Use this patient? [y/N]: ")).trim().toLowerCase() === "y"
			if (use) {
				patient = p
			}
		}

		if (patient == null) {
			console.log("No matching patient found")
			return
		}

		const contactIterator = await sdk.contact.filterContactsBy(
			ContactFilters.byPatientsForSelf([patient])
		)

		if (!(await contactIterator.hasNext())) {
			console.log("No matching contacts found")
		}

		while(await contactIterator.hasNext()) {
			const contact = (await contactIterator.next(1))[0]
			prettyPrintContact(contact)
			await readLn("Press enter for next contact")
		}

		let choice = -1
		while (choice < 0 || choice >= 3) {
			console.log("0. blood pressure")
			console.log("1. heart rate")
			console.log("2. x ray")
			choice = parseInt((await readLn("Enter your choice: ")).trim())
		}

		let identifier: Identifier
		switch (choice) {
			case 0:
				identifier = new Identifier({system: "cardinal", value: "bloodPressure"})
				break
			case 1:
				identifier = new Identifier({system: "cardinal", value: "ecg"})
				break
			default:
				identifier = new Identifier({system: "cardinal", value: "xRay"})
				break
		}

		const serviceIterator = await sdk.contact.filterServicesBy(
			ServiceFilters.byIdentifiersForSelf([identifier])
		)

		if (!(await serviceIterator.hasNext())) {
			console.log("No matching services found")
		}

		while (await serviceIterator.hasNext()) {
			const service = (await serviceIterator.next(1))[0]
			prettyPrintService(service)
			await readLn("Press enter for next service")
		}
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
	}
}