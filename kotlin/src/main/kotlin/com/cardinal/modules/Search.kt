package com.cardinal.modules

import com.icure.sdk.IcureSdk
import com.icure.sdk.filters.ContactFilters
import com.icure.sdk.filters.PatientFilters
import com.icure.sdk.filters.ServiceFilters
import com.icure.sdk.model.Patient
import com.icure.sdk.model.base.Identifier
import com.cardinal.utils.prettyPrint

suspend fun searchPatientsContactsServices(sdk: IcureSdk) {
	try {
		print("Enter a name: ")
		val nameToSearch = readln()
		val patientIterator = sdk.patient.filterPatientsBy(
			PatientFilters.byNameForSelf(nameToSearch)
		)

		var patient: Patient? = null
		while (patientIterator.hasNext() && patient == null) {
			val p = patientIterator.next(1).first()
			prettyPrint(p)
			print("Use this patient? [y/N]: ")
			val use = readln().trim().lowercase() == "y"
			if (use) {
				patient = p
			}
		}

		if (patient == null) {
			println("No matching patient found")
			return
		}

		val contactIterator = sdk.contact.filterContactsBy(
			ContactFilters.byPatientsForSelf(listOf(patient))
		)

		if (!contactIterator.hasNext()) {
			println("No matching contacts found")
		}

		while(contactIterator.hasNext()) {
			val contact = contactIterator.next(1).first()
			prettyPrint(contact)
			print("Press enter for next contact")
			readln()
		}

		var choice = -1
		while (choice < 0 || choice >= 3) {
			println("Make your choice")
			println("0. blood pressure")
			println("1. heart rate")
			println("2. x ray")
			choice = readln().trim().toIntOrNull() ?: 0
		}

		val identifier = when(choice) {
			0 -> Identifier(system = "icure", value = "bloodPressure")
			1 -> Identifier(system = "icure", value = "ecg")
			2 -> Identifier(system = "icure", value = "xRay")
			else -> throw IllegalArgumentException("Invalid choice")
		}

		val serviceIterator = sdk.contact.filterServicesBy(
			ServiceFilters.byIdentifiersForSelf(listOf(identifier))
		)

		if (!serviceIterator.hasNext()) {
			println("No matching services found")
		}

		while (serviceIterator.hasNext()) {
			val service = serviceIterator.next(1).first()
			prettyPrint(service)
			print("Press enter for next service")
			readln()
		}

	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
	}
}
