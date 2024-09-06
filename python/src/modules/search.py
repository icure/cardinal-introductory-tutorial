from cardinal_sdk import CardinalSdk
from cardinal_sdk.filters import ContactFilters
from cardinal_sdk.filters import PatientFilters
from cardinal_sdk.filters import ServiceFilters
from cardinal_sdk.model import Identifier
from utils import pretty_print_contact, pretty_print_patient, pretty_print_service


def search_patients_contacts_services(sdk: CardinalSdk):
	try:
		name_to_search = input("Enter a name: ")
		patient_iterator = sdk.patient.filter_patients_by_blocking(
			PatientFilters.by_name_for_self(name_to_search)
		)

		patient = None
		while patient_iterator.has_next_blocking() and patient is None:
			p = patient_iterator.next_blocking(1)[0]
			pretty_print_patient(p)
			use = input("Use this patient? [y/N]: ").strip().lower() == "y"
			if use:
				patient = p

		if patient is None:
			print("No matching patient found")
			return

		contact_iterator = sdk.contact.filter_contacts_by_blocking(
			ContactFilters.by_patients_for_self([patient])
		)

		if not contact_iterator.has_next_blocking():
			print("No matching contacts found")

		while contact_iterator.has_next_blocking():
			contact = contact_iterator.next_blocking(1)[0]
			pretty_print_contact(contact)
			input("Press enter for next contact")

		choice = -1
		while choice < 0 or choice >= 3:
			print("0. blood pressure")
			print("1. heart rate")
			print("2. x ray")
			try:
				choice = int(input("Enter your choice: ").strip())
			except ValueError:
				choice = -1

		if choice == 0:
			identifier = Identifier(system="cardinal", value="bloodPressure")
		elif choice == 1:
			identifier = Identifier(system="cardinal", value="ecg")
		else:
			identifier = Identifier(system="cardinal", value="xRay")

		service_iterator = sdk.contact.filter_services_by_blocking(
			ServiceFilters.by_identifiers_for_self([identifier])
		)

		if not service_iterator.has_next_blocking():
			print("No matching services found")

		while service_iterator.has_next_blocking():
			service = service_iterator.next_blocking(1)[0]
			pretty_print_service(service)
			input("Press enter for next service")

	except Exception as e:
		print(f"Something went wrong: {e}")
