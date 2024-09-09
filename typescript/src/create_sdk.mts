import {AuthenticationMethod, CardinalSdk, StorageFacade} from "@icure/cardinal-sdk";

const CARDINAL_URL = "https://api.icure.cloud"

export async function createSdk(username: string, password: string): Promise<CardinalSdk> {
	return CardinalSdk.initialize(
		undefined,
		CARDINAL_URL,
		new AuthenticationMethod.UsingCredentials.UsernamePassword(username, password),
		StorageFacade.usingFileSystem("../scratch/storage")
	)
}
