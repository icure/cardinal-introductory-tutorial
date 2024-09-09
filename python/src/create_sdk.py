from cardinal_sdk import CardinalSdk
from cardinal_sdk.authentication.AuthenticationMethod import UsernamePassword
from cardinal_sdk.storage.StorageFacadeOptions import FileSystemStorage

CARDINAL_URL = "https://api.icure.cloud"


def create_cardinal_sdk(username: str, password: str) -> CardinalSdk:
    return CardinalSdk(
        application_id=None,
        baseurl=CARDINAL_URL,
        authentication_method=UsernamePassword(username, password),
        storage_facade=FileSystemStorage("../scratch/storage")
    )
