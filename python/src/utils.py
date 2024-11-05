from cardinal_sdk.model import DecryptedPatient, DecryptedDocument, DecryptedHealthElement, Code, \
    DecryptedContact, DecryptedService


def print_line(line: str, max_len: int):
    print(f"| {line}{' ' * (max_len - len(line) + 1)}|")


def print_divider(max_len: int):
    print(f"+{'-' * (max_len + 2)}+")


def pretty_print_patient(patient: DecryptedPatient):
    id_ = f"id: {patient.id}"
    rev = f"rev: {patient.rev or 'rev is missing'}"
    name = f"{patient.first_name} {patient.last_name}"
    date_of_birth = f"Birthday: {patient.date_of_birth}"
    max_len = max(len(id_), len(rev), len(name), len(date_of_birth))
    print_divider(max_len)
    print_line(name, max_len)
    print_divider(max_len)
    print_line(id_, max_len)
    print_line(rev, max_len)
    if patient.date_of_birth is not None:
        print_line(date_of_birth, max_len)
    print_divider(max_len)


def pretty_print_document(document: DecryptedDocument):
    id_ = f"id: {document.id}"
    rev = f"rev: {document.rev or 'rev is missing'}"
    name = f"{document.name}"
    max_len = max(len(id_), len(rev), len(name))
    print_divider(max_len)
    print_line(name, max_len)
    print_divider(max_len)
    print_line(id_, max_len)
    print_line(rev, max_len)
    print_divider(max_len)


def pretty_print_health_element(health_element: DecryptedHealthElement):
    id_ = f"id: {health_element.id}"
    rev = f"rev: {health_element.rev or 'rev is missing'}"
    description = f"{health_element.descr}"
    max_len = max(len(id_), len(rev), len(description))
    print_divider(max_len)
    print_line(description, max_len)
    print_divider(max_len)
    print_line(id_, max_len)
    print_line(rev, max_len)
    print_divider(max_len)


def pretty_print_code(code: Code):
    label = f"{code.label['en']} v{code.version}"
    code_type = f"Type: {code.type}"
    code_code = f"Code: {code.code}"
    max_len = max(len(label), len(code_type), len(code_code))
    print_divider(max_len)
    print_line(label, max_len)
    print_divider(max_len)
    print_line(code_type, max_len)
    print_line(code_code, max_len)
    print_divider(max_len)


def pretty_print_contact(contact: DecryptedContact):
    id_ = f"id: {contact.id}"
    rev = f"rev: {contact.rev or 'rev is missing'}"
    description = f"{contact.descr}"
    opening_date = f"Opened: {contact.opening_date}"
    closing_date = f"Closed: {contact.closing_date}"
    diagnosis = diagnosis_of(contact)
    services = [content_of(service) for service in contact.services if content_of(service) is not None]
    max_len = max(len(id_), len(rev), len(description), len(opening_date), len(closing_date), len(diagnosis or ''), *(len(s) for s in services))
    print_divider(max_len)
    print_line(description, max_len)
    print_divider(max_len)
    if diagnosis is not None:
        print_line(diagnosis, max_len)
        print_divider(max_len)
    print_line(id_, max_len)
    print_line(rev, max_len)
    print_line(opening_date, max_len)
    if contact.closing_date is not None:
        print_line(closing_date, max_len)
    print_divider(max_len)
    for service in services:
        print_line(service, max_len)
    if len(services) > 0:
        print_divider(max_len)


def pretty_print_service(service: DecryptedService):
    id_ = f"id: {service.id}"
    content = content_of(service)
    tags = f"Tags: {', '.join(tag.id or '' for tag in service.tags)}"
    max_len = max(len(id_), len(content or ''), len(tags))
    print_divider(max_len)
    print_line(id_, max_len)
    if content is not None:
        print_line(content, max_len)
    if len(service.tags) > 0:
        print_line(tags, max_len)
    print_divider(max_len)


def diagnosis_of(contact: DecryptedContact):
    sub_contact = next(iter(contact.sub_contacts), None)
    return f"Diagnosis in healthElement: {sub_contact.health_element_id}" if sub_contact is not None else None


def content_of(service: DecryptedService):
    first_content = next(iter(service.content.values()), None)
    if first_content:
        if first_content.measure_value is not None:
            return f"{service.label}: {first_content.measure_value.value} {first_content.measure_value.unit}"
        elif first_content.time_series is not None:
            return f"{service.label}: {' '.join(map(str, first_content.time_series.samples[0]))}"
        elif first_content.document_id is not None:
            return f"{service.label}: in Document with id {first_content.document_id}"
    return None
