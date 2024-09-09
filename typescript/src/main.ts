import {CardinalSdk} from "@icure/cardinal-sdk";
import {createSdk} from "./create_sdk.mjs";
import {readLn, rl} from "./utils/read.mjs";
import {createPatient} from "./modules/patient.mjs";
import {createExamination} from "./modules/examination.mjs";
import {searchPatientsContactsServices} from "./modules/search.mjs";
import {shareWithHcp} from "./modules/shareHcp.mjs";
import {shareWithPatient} from "./modules/sharePatient.mjs";
import {manageCodifications} from "./modules/codification.mjs";

async function login(): Promise<CardinalSdk> {
	const username = await readLn("Login: ")
	const password = await readLn("Password: ")
	try {
		return createSdk(username, password)
	} catch (e) {
		console.error(`Something went wrong: ${(e as Error).message}`)
		return await login()
	}
}

const main = async () => {
	const sdk = await login()
	let input: number
	do {
		console.log("0. Exit")
		console.log("1. Create a patient")
		console.log("2. Create medical data")
		console.log("3. Search")
		console.log("4. Share data with another hcp")
		console.log("5. Share data with a patient")
		console.log("6. Manage codifications")
		input = parseInt((await readLn("Make a choice: ")).trim())

		switch (input) {
			case 0:
				break
			case 1:
				await createPatient(sdk)
				break
			case 2:
				await createExamination(sdk)
				break
			case 3:
				await searchPatientsContactsServices(sdk)
				break
			case 4:
				await shareWithHcp(sdk)
				break
			case 5:
				await shareWithPatient(sdk)
				break
			case 6:
				await manageCodifications(sdk)
				break
			default:
				console.log(`Unknown option: ${input}`)
		}
	} while (input != 0 )

	rl.close()
};

main()