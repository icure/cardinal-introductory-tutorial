import uuid
from create_sdk import create_icure_sdk
from icure import IcureSdk
from icure.model import DecryptedPatient, User, PatientShareOptions, ShareMetadataBehaviour, RequestedPermission, \
	SimpleShareResultDecryptedPatientSuccess, DecryptedHealthElement, SimpleShareResultDecryptedHealthElementSuccess, \
	AccessLevel
from utils import pretty_print_patient, pretty_print_health_element


def share_with_patient(sdk: IcureSdk):
	try:
		new_patient = DecryptedPatient(
			id=str(uuid.uuid4()),
			first_name="Edmond",
			last_name="Dantes",
		)
		patient_with_metadata = sdk.patient.with_encryption_metadata_blocking(new_patient)
		created_patient = sdk.patient.create_patient_blocking(patient_with_metadata)
		login = f"edmond.dantes.{str(uuid.uuid4())[0:6]}@icure.com"
		patient_user = User(
			id=str(uuid.uuid4()),
			patient_id=created_patient.id,
			login=login,
			email=login
		)
		created_user = sdk.user.create_user_blocking(patient_user)
		login_token = sdk.user.get_token_blocking(created_user.id, "login")

		create_icure_sdk(login, login_token)

		patient_secret_ids = sdk.patient.get_secret_ids_of_blocking(created_patient)
		patient_share_result = sdk.patient.share_with_blocking(
			delegate_id=created_patient.id,
			patient=created_patient,
			options=PatientShareOptions(
				share_secret_ids=patient_secret_ids,
				share_encryption_key=ShareMetadataBehaviour.IfAvailable,
				requested_permissions=RequestedPermission.MaxWrite
			)
		)

		if isinstance(patient_share_result, SimpleShareResultDecryptedPatientSuccess):
			print("Successfully shared patient")

		patient = patient_share_result.updated_entity

		patient_sdk = create_icure_sdk(login, login_token)

		pretty_print_patient(patient_sdk.patient.get_patient_blocking(patient.id))

		health_element = DecryptedHealthElement(
			id=str(uuid.uuid4()),
			descr="This is some medical context"
		)
		health_element_with_metadata = sdk.health_element.with_encryption_metadata_blocking(health_element, patient)
		created_health_element = sdk.health_element.create_health_element_blocking(health_element_with_metadata)

		try:
			patient_sdk.health_element.get_health_element_blocking(created_health_element.id)
		except Exception as e:
			print(f"This means the patient cannot get this health element -> {e}")

		result = sdk.health_element.share_with_blocking(
			delegate_id=patient.id,
			health_element=created_health_element
		)

		if isinstance(result, SimpleShareResultDecryptedHealthElementSuccess):
			print(f"Successfully shared with patient id {patient.id}")

		pretty_print_health_element(patient_sdk.health_element.get_health_element_blocking(created_health_element.id))

		new_health_element = DecryptedHealthElement(
			id=str(uuid.uuid4()),
			descr="This is some other medical context"
		)
		new_health_element_with_metadata = sdk.health_element.with_encryption_metadata_blocking(
			new_health_element,
			patient,
			delegates={patient.id: AccessLevel.Write}
		)
		new_created_health_element = sdk.health_element.create_health_element_blocking(new_health_element_with_metadata)

		retrieved_health_element = patient_sdk.health_element.get_health_element_blocking(new_created_health_element.id)
		pretty_print_health_element(retrieved_health_element)
	except Exception as e:
		print(f"Something went wrong: {e}")
