import * as readline from 'readline';
import {CardinalSdk, Enable2faRequest} from "@icure/cardinal-sdk";

export const rl = readline.createInterface({
	input: process.stdin,
	output: process.stdout
});


export const readLn = (query: string): Promise<string> => {
	return new Promise((resolve) => rl.question(query, resolve));
};

Enable2faRequest