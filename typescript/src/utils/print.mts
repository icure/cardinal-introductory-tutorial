import {Patient, Document, HealthElement, Code, Contact, Service} from "@icure/cardinal-sdk";

function printLine(line: string, maxLen: number) {
	console.log(`| ${line}${' '.repeat(maxLen - line.length + 1)}|`);
}

function printDivider(maxLen: number) {
	console.log(`+${'-'.repeat(maxLen + 2)}+`);
}

export function prettyPrintPatient(patient: Patient) {
	const id = `id: ${patient.id}`;
	const rev = `rev: ${patient.rev || "rev is missing"}`;
	const name = `${patient.firstName} ${patient.lastName}`;
	const dateOfBirth = `Birthday: ${patient.dateOfBirth}`;
	const maxLen = Math.max(id.length, rev.length, name.length, dateOfBirth.length);

	printDivider(maxLen);
	printLine(name, maxLen);
	printDivider(maxLen);
	printLine(id, maxLen);
	printLine(rev, maxLen);
	if (patient.dateOfBirth) {
		printLine(dateOfBirth, maxLen);
	}
	printDivider(maxLen);
}

export function prettyPrintDocument(document: Document) {
	const id = `id: ${document.id}`;
	const rev = `rev: ${document.rev || "rev is missing"}`;
	const name = document.name;
	const maxLen = Math.max(id.length, rev.length, name.length);

	printDivider(maxLen);
	printLine(name, maxLen);
	printDivider(maxLen);
	printLine(id, maxLen);
	printLine(rev, maxLen);
	printDivider(maxLen);
}

export function prettyPrintHealthElement(healthElement: HealthElement) {
	const id = `id: ${healthElement.id}`;
	const rev = `rev: ${healthElement.rev || "rev is missing"}`;
	const description = healthElement.descr;
	const maxLen = Math.max(id.length, rev.length, description.length);

	printDivider(maxLen);
	printLine(description, maxLen);
	printDivider(maxLen);
	printLine(id, maxLen);
	printLine(rev, maxLen);
	printDivider(maxLen);
}

export function prettyPrintCode(code: Code) {
	const label = `${code.label?.['en']} v${code.version}`;
	const codeType = `Type: ${code.type}`;
	const codeCode = `Code: ${code.code}`;
	const maxLen = Math.max(label.length, codeType.length, codeCode.length);

	printDivider(maxLen);
	printLine(label, maxLen);
	printDivider(maxLen);
	printLine(codeType, maxLen);
	printLine(codeCode, maxLen);
	printDivider(maxLen);
}

export function prettyPrintContact(contact: Contact) {
	const id = `id: ${contact.id}`;
	const rev = `rev: ${contact.rev || "rev is missing"}`;
	const description = contact.descr;
	const openingDate = `Opened: ${contact.openingDate}`;
	const closingDate = `Closed: ${contact.closingDate}`;
	const diagnosis = diagnosisOf(contact);
	const services = contact.services.map(contentOf).filter(Boolean) as string[];

	const maxLen = Math.max(id.length, rev.length, description.length, openingDate.length, closingDate?.length || 0, diagnosis?.length || 0, ...services.map(s => s.length));

	printDivider(maxLen);
	printLine(description, maxLen);
	printDivider(maxLen);
	if (diagnosis) {
		printLine(diagnosis, maxLen);
		printDivider(maxLen);
	}
	printLine(id, maxLen);
	printLine(rev, maxLen);
	printLine(openingDate, maxLen);
	if (contact.closingDate) {
		printLine(closingDate, maxLen);
	}
	printDivider(maxLen);
	services.forEach(service => printLine(service, maxLen));
	if (services.length) {
		printDivider(maxLen);
	}
}

export function prettyPrintService(service: Service) {
	const id = `id: ${service.id}`;
	const content = contentOf(service);
	const tags = `Tags: ${service.tags.map(tag => tag.id || "").join(", ")}`;
	const maxLen = Math.max(id.length, content?.length || 0, tags.length);

	printDivider(maxLen);
	printLine(id, maxLen);
	if (content) {
		printLine(content, maxLen);
	}
	if (service.tags.length) {
		printLine(tags, maxLen);
	}
	printDivider(maxLen);
}

// Helper functions for diagnosis and content
function diagnosisOf(contact: Contact): string | undefined {
	return contact.subContacts[0]?.healthElementId ? `Diagnosis in healthElement: ${contact.subContacts[0]?.healthElementId}` : undefined;
}

function contentOf(service: Service): string | undefined {
	const firstContent = Object.values(service.content)[0];
	if (!firstContent) return undefined;

	if (firstContent.measureValue) {
		return `${service.label}: ${firstContent.measureValue.value} ${firstContent.measureValue.unit}`;
	} else if (firstContent.timeSeries) {
		return `${service.label}: ${firstContent.timeSeries.samples.join(" ")}`;
	} else if (firstContent.documentId) {
		return `${service.label}: in Document with id ${firstContent.documentId}`;
	}
	return undefined;
}
