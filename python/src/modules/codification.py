import random
import uuid
from datetime import datetime
from cardinal_sdk import CardinalSdk
from cardinal_sdk.filters import CodeFilters
from cardinal_sdk.filters import ServiceFilters
from cardinal_sdk.model import Code, CodeStub, DecryptedContact, DecryptedContent, DecryptedPatient, DecryptedService, \
	Measure
from utils import pretty_print_code, pretty_print_contact, pretty_print_service


def manage_codifications(sdk: CardinalSdk):
	try:
		existing = sdk.code.get_codes_blocking(
			["INTERNAL|ANALYSIS|1", "SNOMED|45007003|1", "SNOMED|38341003|1", "SNOMED|2004005|1"]
		)

		if len(existing) == 0:
			internal_code = sdk.code.create_code_blocking(
				Code(
					id="INTERNAL|ANALYSIS|1",
					type="INTERNAL",
					code="ANALYSIS",
					version="1",
					label={"en": "Internal analysis code"}
				)
			)
			pretty_print_code(internal_code)
			sdk.code.create_codes_blocking(
				Code(
					id="SNOMED|45007003|1",
					type="SNOMED",
					code="45007003",
					version="1",
					label={"en": "Low blood pressure"}
				),
				Code(
					id="SNOMED|38341003|1",
					type="SNOMED",
					code="38341003",
					version="1",
					label={"en": "High blood pressure"}
				),
				Code(
					id="SNOMED|2004005|1",
					type="SNOMED",
					code="2004005",
					version="1",
					label={"en": "Normal blood pressure"}
				)
			)

		code_iterator = sdk.code.filter_codes_by_blocking(
			CodeFilters.by_language_type_label_region(
				language="en",
				label="blood",
				type="SNOMED"
			)
		)

		selected_code = None
		while selected_code is None and code_iterator.has_next_blocking():
			code = code_iterator.next_blocking(1)[0]
			pretty_print_code(code)
			choice = input("Use this code [y/N]: ")
			if choice.lower() == "y":
				selected_code = code
				break

		patient = sdk.patient.create_patient_blocking(
			sdk.patient.with_encryption_metadata_blocking(
				DecryptedPatient(
					id=str(uuid.uuid4()),
					first_name="Annabelle",
					last_name="Hall"
				)
			)
		)

		contact = DecryptedContact(
			id=str(uuid.uuid4()),
			descr="Blood pressure measurement",
			opening_date=int(datetime.now().strftime("%Y%m%d%H%M%S")),
			services=[
				DecryptedService(
					id=str(uuid.uuid4()),
					label="Blood pressure",
					content={
						"en": DecryptedContent(
							measure_value=Measure(
								value=float(random.randint(80, 120)),
								unit="mmHg"
							)
						)
					},
					tags=[
						CodeStub(
							id=selected_code.id,
							type=selected_code.type,
							code=selected_code.code,
							version=selected_code.version
						)
					]
				)
			]
		)
		created_contact = sdk.contact.create_contact_blocking(
			sdk.contact.with_encryption_metadata_blocking(contact, patient)
		)
		pretty_print_contact(created_contact)
		pretty_print_service(created_contact.services[0])

		service_iterator = sdk.contact.filter_services_by_blocking(
			ServiceFilters.by_tag_and_value_date_for_self(
				tag_type=selected_code.type,
				tag_code=selected_code.code
			)
		)
		print(f"Result of searching Services by code: {selected_code.id}")
		while service_iterator.has_next_blocking():
			service = service_iterator.next_blocking(1)[0]
			pretty_print_service(service)

	except Exception as e:
		print(f"Something went wrong: {e}")
