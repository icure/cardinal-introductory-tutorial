from icure import IcureSdk
from icure.authentication.AuthenticationMethod import UsernamePassword
from icure.storage.StorageFacadeOptions import FileSystemStorage

ICURE_URL = "https://api.icure.cloud"


def create_icure_sdk(username: str, password: str) -> IcureSdk:
    return IcureSdk(
        application_id=None,
        baseurl=ICURE_URL,
        authentication_method=UsernamePassword(username, password),
        storage_facade=FileSystemStorage("./scratch/storage")
    )
