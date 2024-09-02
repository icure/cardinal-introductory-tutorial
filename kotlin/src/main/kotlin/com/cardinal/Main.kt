package com.icure

import com.icure.modules.createConsultation
import com.icure.modules.createPatient
import com.icure.modules.manageCodification
import com.icure.modules.searchPatientsContactsServices
import com.icure.modules.shareWithHcp
import com.icure.modules.shareWithPatient
import com.icure.sdk.IcureSdk
import com.icure.sdk.createSdk
import com.icure.sdk.utils.RequestStatusException
import kotlin.system.exitProcess

private suspend fun login(): IcureSdk {
	print("Login: ")
	val username = readln().trim()
	print("Password: ")
	val password = readln().trim()
	return try {
		createSdk(username, password)
	} catch (e: RequestStatusException) {
		if(e.statusCode == 401) {
			println("Invalid username or password, maybe the token expired?")
		} else {
			println("Something went wrong: ${e.message}")
		}
		login()
	} catch (e: Exception) {
		println("Something went wrong: ${e.message}")
		login()
	}
}

suspend fun main() {
	val sdk = login()
	do {
		println("0. Exit")
		println("1. Create a patient")
		println("2. Create medical data")
		println("3. Search")
		println("4. Share data with another hcp")
		println("5. Share data with a patient")
		println("6. Manage codifications")
		print("Make a choice: ")
		val choice = readln().toIntOrNull()
		when(choice) {
			0 -> exitProcess(0)
			1 -> createPatient(sdk)
			2 -> createConsultation(sdk)
			3 -> searchPatientsContactsServices(sdk)
			4 -> shareWithHcp(sdk)
			5 -> shareWithPatient(sdk)
			6 -> manageCodification(sdk)
		}
	} while (choice != 0)
}