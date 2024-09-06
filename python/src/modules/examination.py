import random
import secrets
import uuid
from datetime import datetime
from cardinal_sdk import CardinalSdk
from cardinal_sdk.model import DecryptedContact, DecryptedContent, DecryptedPatient, DecryptedService, Identifier, \
	Measure, TimeSeries, DecryptedDocument, DocumentType, DecryptedHealthElement, DecryptedSubContact
from utils import pretty_print_contact


def manage_examination(sdk: CardinalSdk):
	try:
		patient_id = input("Insert the id of a Patient (blank to create a new one): ")
		if len(patient_id) == 0:
			patient = sdk.patient.create_patient_blocking(
				sdk.patient.with_encryption_metadata_blocking(
					DecryptedPatient(
						id=str(uuid.uuid4()),
						first_name="Annabelle",
						last_name="Hall"
					)
				)
			)
		else:
			patient = sdk.patient.get_patient_blocking(patient_id)

		description = input("Examination description: ")
		contact = DecryptedContact(
			id=str(uuid.uuid4()),
			descr=description,
			opening_date=int(datetime.now().strftime("%Y%m%d%H%M%S"))
		)
		contact_with_metadata = sdk.contact.with_encryption_metadata_blocking(contact, patient)
		created_contact = sdk.contact.create_contact_blocking(contact_with_metadata)
		pretty_print_contact(created_contact)

		has_blood_pressure = input("Register blood pressure? [y/N]: ").strip().lower() == "y"
		if has_blood_pressure:
			print("Measuring blood pressure...")
			blood_pressure_service = DecryptedService(
				id=str(uuid.uuid4()),
				label="Blood pressure",
				identifier=[Identifier(system="cardinal", value="bloodPressure")],
				content={
					"en": DecryptedContent(
						measure_value=Measure(
							value=float(random.randint(80, 120)),
							unit="mmHg"
						)
					)
				}
			)
			created_contact.services = [blood_pressure_service]
			contact_with_blood_pressure = sdk.contact.modify_contact_blocking(
				created_contact
			)
		else:
			contact_with_blood_pressure = created_contact

		has_heart_rate = input("Register heart rate? [y/N]: ").strip().lower() == "y"
		if has_heart_rate:
			print("Measuring heart rate...")
			ecg_signal = [round(float(random.uniform(0, 1)), 2) for _ in range(10)]
			heart_rate_service = DecryptedService(
				id=str(uuid.uuid4()),
				identifier=[Identifier(system="cardinal", value="ecg")],
				label="Heart rate",
				content={
					"en": DecryptedContent(
						time_series=TimeSeries(
							samples=[ecg_signal]
						)
					)
				}
			)
			contact_with_blood_pressure.services = contact_with_blood_pressure.services + [heart_rate_service]
			contact_with_ecg = sdk.contact.modify_contact_blocking(contact_with_blood_pressure)
		else:
			contact_with_ecg = contact_with_blood_pressure

		has_x_ray = input("Register x-ray? [y/N]: ").strip().lower() == "y"
		if has_x_ray:
			print("Generating X Ray image...")
			document = DecryptedDocument(
				id=str(uuid.uuid4()),
				document_type=DocumentType.Labresult
			)
			created_contact = sdk.document.create_document_blocking(
				sdk.document.with_encryption_metadata_blocking(document, None)
			)
			x_ray_image = bytearray(secrets.token_bytes(100))
			document_with_attachment = sdk.document.encrypt_and_set_main_attachment_blocking(
				document=created_contact,
				utis=["public.tiff"],
				attachment=x_ray_image
			)
			x_ray_service = DecryptedService(
				id=str(uuid.uuid4()),
				label="X-Ray image",
				identifier=[Identifier(system="cardinal", value="xRay")],
				content={
					"en": DecryptedContent(
						document_id=document_with_attachment.id
					)
				}
			)
			contact_with_ecg.services = contact_with_ecg.services + [x_ray_service]
			contact_with_image = sdk.contact.modify_contact_blocking(contact_with_ecg)
		else:
			contact_with_image = contact_with_ecg

		diagnosis = input("What is the diagnosis?: ")
		health_element = DecryptedHealthElement(
			id=str(uuid.uuid4()),
			descr=diagnosis
		)
		created_diagnosis = sdk.health_element.create_health_element_blocking(
			sdk.health_element.with_encryption_metadata_blocking(health_element, patient)
		)
		contact_with_image.sub_contacts = [
			DecryptedSubContact(
				descr="Diagnosis",
				health_element_id=created_diagnosis.id
			)
		]
		contact_with_diagnosis = sdk.contact.modify_contact_blocking(contact_with_image)

		close = input("Close contact? [y/N]: ").strip().lower() == "y"
		if close:
			contact_with_diagnosis.closing_date = int(datetime.now().strftime("%Y%m%d%H%M%S"))
			final_contact = sdk.contact.modify_contact_blocking(contact_with_diagnosis)
		else:
			final_contact = contact_with_diagnosis

		pretty_print_contact(final_contact)
	except Exception as e:
		print(f"Something went wrong: {e}")
