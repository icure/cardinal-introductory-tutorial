import uuid
from icure import IcureSdk
from icure.model import DecryptedPatient
from utils import pretty_print_patient


def manage_patient(sdk: IcureSdk):
	try:
		first_name = input("First name: ")
		last_name = input("Last name: ")
		patient = DecryptedPatient(
			id=str(uuid.uuid4()),
			first_name=first_name,
			last_name=last_name,
		)
		patient_with_metadata = sdk.patient.with_encryption_metadata_blocking(patient)
		created_patient = sdk.patient.create_patient_blocking(patient_with_metadata)
		pretty_print_patient(created_patient)

		date_of_birth = int(input("Date of birth (YYYYMMDD): "))
		created_patient.date_of_birth = date_of_birth
		updated_patient = sdk.patient.modify_patient_blocking(created_patient)
		pretty_print_patient(updated_patient)

		print("The retrieved patient is:")
		retrieved_patient = sdk.patient.get_patient_blocking(updated_patient.id)
		pretty_print_patient(retrieved_patient)
	except Exception as e:
		print(f"Something went wrong: {e}")
