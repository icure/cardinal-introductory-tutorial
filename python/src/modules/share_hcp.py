import uuid
from create_sdk import create_cardinal_sdk
from cardinal_sdk import CardinalSdk
from cardinal_sdk.model import DecryptedDocument, SimpleShareResultDecryptedDocumentSuccess, AccessLevel
from utils import pretty_print_document


def share_with_hcp(sdk: CardinalSdk):
	try:
		username = input("Login of the other hcp: ").strip()
		other_password = input("Insert the password for this hcp: ")
		other_sdk = create_cardinal_sdk(username, other_password)
		other_hcp = other_sdk.healthcare_party.get_current_healthcare_party_blocking()

		old_document_without_encryption_meta = DecryptedDocument(
			id=str(uuid.uuid4()),
			name="An important document"
		)
		old_document = sdk.document.create_document_blocking(
			sdk.document.with_encryption_metadata_blocking(old_document_without_encryption_meta, None)
		)
		pretty_print_document(old_document)

		try:
			other_sdk.document.get_document_blocking(old_document.id)
		except Exception as e:
			print(f"This means I am not authorized to read the document -> {e}")

		result = sdk.document.share_with_blocking(
			delegate_id=other_hcp.id,
			document=old_document
		)

		if isinstance(result, SimpleShareResultDecryptedDocumentSuccess):
			print("Successfully shared document")

		old_document_other_hcp = other_sdk.document.get_document_blocking(old_document.id)
		pretty_print_document(old_document_other_hcp)

		new_document = DecryptedDocument(
			id=str(uuid.uuid4()),
			name="Another important document"
		)
		new_document_with_metadata = sdk.document.with_encryption_metadata_blocking(
			new_document,
			None,
			delegates={other_hcp.id: AccessLevel.Read}
		)
		created_new_document = sdk.document.create_document_blocking(new_document_with_metadata)

		new_document_other_hcp = other_sdk.document.get_document_blocking(created_new_document.id)
		pretty_print_document(new_document_other_hcp)
	except Exception as e:
		print(f"Something went wrong: {e}")
